"""Run ../compare_structures.py over a pairs file from pair_structures.py, robustly.

Usage:
  python run_compare.py PAIRS.tsv OUT_PREFIX

compare_structures.compare() is called unchanged, but in capped worker processes (chunks of
rows, 120 s and 4 GB per chunk, then 30 s per row when a chunk fails), because RDKit hangs or
raises on a few corpus structures and one such row would otherwise end the whole run. A row
that raises gets the verdict "error", a row that does not finish gets "timeout".
Only paired rows are compared (pairing inchikey / skeleton / similarity); the other rows are
tallied as ref_only / pred_only / ref_hung / pred_hung.

Writes OUT_PREFIX.compare.tsv (per row) and OUT_PREFIX.summary.json.
"""

import csv
import importlib.util
import json
import resource
import subprocess
import sys
from collections import Counter
from pathlib import Path

HERE = Path(__file__).resolve().parent
COMPARE = HERE.parent / "compare_structures.py"
CHUNK, CHUNK_TIMEOUT, ROW_TIMEOUT = 25, 120, 30
MEM_LIMIT = 4 << 30
PAIRED = {"inchikey", "skeleton", "similarity"}


def load_compare():
    spec = importlib.util.spec_from_file_location("compare_structures", COMPARE)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def worker():
    cs = load_compare()
    out = []
    for ref, pred in json.load(sys.stdin):
        try:
            out.append(cs.compare(ref, pred))
        except Exception as e:
            out.append({"verdict": "error", "note": f"{type(e).__name__}: {e}".splitlines()[0][:120]})
    print(json.dumps(out))


def run_capped(rows, timeout):
    def limit():
        resource.setrlimit(resource.RLIMIT_AS, (MEM_LIMIT, MEM_LIMIT))

    try:
        p = subprocess.run([sys.executable, "-I", __file__, "--worker"], input=json.dumps(rows), text=True,
                           capture_output=True, timeout=timeout, preexec_fn=limit)
        return json.loads(p.stdout)
    except (subprocess.TimeoutExpired, json.JSONDecodeError):
        return None


def main():
    pairs_path, prefix = sys.argv[1], sys.argv[2]
    with open(pairs_path, newline="") as fh:
        rows = list(csv.DictReader(fh, delimiter="\t"))
    pairing = Counter(r["pairing"] for r in rows)
    todo = [r for r in rows if r["pairing"] in PAIRED]

    results = []
    for i in range(0, len(todo), CHUNK):
        chunk = todo[i:i + CHUNK]
        out = run_capped([[r["ref"], r["pred"]] for r in chunk], CHUNK_TIMEOUT)
        if out is None:
            out = [(run_capped([[r["ref"], r["pred"]]], ROW_TIMEOUT) or [{"verdict": "timeout"}])[0] for r in chunk]
        for r, res in zip(chunk, out):
            res.update(id=r["id"], pairing=r["pairing"], ref=r["ref"], pred=r["pred"])
            results.append(res)

    cs = load_compare()
    fields = ["id", "pairing", "verdict", "stereo_class", "formula_delta", "note"]
    fields += [f"centre_{k}" for k in cs.STEREO_KEYS] + [f"bond_{k}" for k in cs.BOND_KEYS] + ["ref", "pred"]
    with open(f"{prefix}.compare.tsv", "w", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=fields, delimiter="\t", extrasaction="ignore")
        w.writeheader()
        w.writerows(results)

    summary = cs.summarise(results)
    summary["pairing"] = dict(pairing)
    summary["errors"] = sum(r["verdict"] == "error" for r in results)
    summary["timeouts"] = sum(r["verdict"] == "timeout" for r in results)
    summary["verdict_by_pairing"] = {
        step: dict(Counter(r["verdict"] for r in results if r["pairing"] == step)) for step in sorted(PAIRED)
    }
    Path(f"{prefix}.summary.json").write_text(json.dumps(summary, indent=2) + "\n")
    print(json.dumps({k: summary[k] for k in ("pairing", "errors", "timeouts")}))
    for k, v in summary["verdicts"].items():
        print(f"  {k:<18}{v['count']:>6}  {v['percent']:>8}")
    for k, v in summary["stereo_rows_by_class"].items():
        print(f"    {k:<16}{v['count']:>6}  {v['percent']:>8}")


if __name__ == "__main__":
    worker() if "--worker" in sys.argv else main()
