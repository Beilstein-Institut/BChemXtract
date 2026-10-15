"""Run RDKit, Indigo or ChemScanner over a ChemDraw corpus and report it like
run-openbabel-performance.sh, deduplicating per file on InChIKey.

Usage:
  python run-toolkit-performance.py {rdkit|indigo|chemscanner} [corpus-dir] [--ext cdx|cdxml|all]
                                    [--timeout SECONDS] [--tsv OUT]

Each file runs in its own worker process, capped at 120 s and 4 GB, so a hang or crash
costs one file, not the run;
reported times are measured inside the worker and exclude interpreter start-up.
The Python interpreter running this script must have rdkit and epam.indigo installed.
ChemScanner needs the chem_scanner gem (GEM_HOME is passed through).

Per file: UNIQUE = distinct InChIKeys + structures without an InChIKey, deduplicated on
canonical SMILES. Unexpanded alias (*) = unique SMILES containing `*`.
For ChemScanner, InChIKeys are computed afterwards with RDKit from its SMILES in a second
capped worker (its Ruby RDKit binding has no InChI); that step is not timed.
"""

import argparse
import json
import resource
import subprocess
import sys
import tempfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
MEM_LIMIT = 4 << 30  # per worker; a runaway file fails alone instead of taking the run down


def worker_cmd(tool, path, out):
    if tool == "chemscanner":
        return ["ruby", "-rfileutils", str(HERE / "chemscanner_worker.rb"), str(path), str(out)]
    return [sys.executable, "-I", str(HERE / "worker.py"), tool, str(path), str(out)]


def limit_memory():
    resource.setrlimit(resource.RLIMIT_AS, (MEM_LIMIT, MEM_LIMIT))


def run_capped(cmd, timeout, err):
    return subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=err, timeout=timeout, preexec_fn=limit_memory)


def run_one(tool, path, timeout):
    # The worker writes its record to a file; its stdout goes nowhere (chem_scanner 0.1.3
    # leaves debug `puts` in, which ran to gigabytes on one file).
    with tempfile.NamedTemporaryFile(suffix=".json") as out, tempfile.TemporaryFile() as err:
        try:
            p = run_capped(worker_cmd(tool, path, out.name), timeout, err)
        except subprocess.TimeoutExpired:
            return {"ms": timeout * 1000, "status": "error", "error": f"timeout after {timeout} s", "structures": []}
        try:
            rec = json.load(open(out.name))
        except json.JSONDecodeError:
            err.seek(-min(2000, err.tell()), 2)
            tail = err.read().decode(errors="replace").strip().splitlines()
            return {"ms": 0, "status": "error", "error": f"crash (exit {p.returncode}): {(tail or [''])[-1][:200]}", "structures": []}
        if tool == "chemscanner":
            # Untimed, but capped like the read: RDKit's InChI can run away on odd structures.
            # If it does, the file keeps its structures without keys (SMILES fallback).
            try:
                run_capped([sys.executable, "-I", str(HERE / "worker.py"), "inchikeys", out.name], timeout, err)
                rec = json.load(open(out.name))
            except (subprocess.TimeoutExpired, json.JSONDecodeError):
                rec["inchikey_error"] = True
    return rec


def summarise(rec):
    structs = rec["structures"]
    unique_smiles = {s for s, _ in structs}
    keys = {k for _, k in structs if k}
    no_key_smiles = {s for s, k in structs if not k}
    via_inchi, via_smiles = len(keys), len(no_key_smiles)
    status = rec["status"]
    if status == "ok" and via_inchi + via_smiles == 0:
        status = "empty"
    if status == "error":
        via_inchi = via_smiles = 0
    wildcards = sum("*" in s for s in unique_smiles) if status == "ok" else 0
    return {
        "ms": rec["ms"],
        "unique": via_inchi + via_smiles,
        "raw": len(structs),
        "via_inchi": via_inchi,
        "via_smiles": via_smiles,
        "wildcards": wildcards,
        "status": status,
        "error": rec.get("error", ""),
        "inchikey_error": rec.get("inchikey_error", False),
    }


def tool_version(tool):
    if tool == "rdkit":
        import rdkit

        return f"RDKit {rdkit.__version__}"
    if tool == "indigo":
        from indigo import Indigo

        return f"Indigo {Indigo().version()}"
    out = subprocess.run(
        ["ruby", "-e", 'v = ->(n) { Gem::Specification.find_by_name(n).version }; puts "#{v["chem_scanner"]} (rdkit_chem #{v["rdkit_chem"]})"'],
        capture_output=True, text=True,
    )
    return f"ChemScanner {out.stdout.strip()}"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("tool", choices=["rdkit", "indigo", "chemscanner"])
    ap.add_argument("corpus", nargs="?", default=str(HERE.parent / "corpus"))
    ap.add_argument("--ext", choices=["cdx", "cdxml", "all"], default="cdx")
    ap.add_argument("--timeout", type=int, default=120)
    ap.add_argument("--tsv", help="write per-file results here")
    a = ap.parse_args()

    root = Path(a.corpus)
    exts = {"cdx": {".cdx"}, "cdxml": {".cdxml"}, "all": {".cdx", ".cdxml"}}[a.ext]
    files = sorted(p for p in root.rglob("*") if p.is_file() and p.suffix.lower() in exts)
    print(f"Scanning {len(files)} files ({a.ext}) under {root.resolve()} using {tool_version(a.tool)}")
    print("Dedup: per-file InChIKey, with canonical-SMILES fallback where InChI is unavailable.\n")

    rows = []
    for f in files:
        s = summarise(run_one(a.tool, f, a.timeout))
        s["file"] = str(f.relative_to(root))
        rows.append(s)

    if a.tsv:
        with open(a.tsv, "w") as out:
            cols = ["file", "status", "ms", "unique", "raw", "via_inchi", "via_smiles", "wildcards", "error"]
            out.write("\t".join(cols) + "\n")
            for r in rows:
                out.write("\t".join(str(r[c]) for c in cols) + "\n")

    ok = [r for r in rows if r["status"] == "ok"]
    total_ms = sum(r["ms"] for r in rows)
    uniq = sum(r["unique"] for r in ok)
    wild = sum(r["wildcards"] for r in ok)
    print(f"=============== EXTRACTION PERFORMANCE ({a.tool}, InChIKey dedup) ===============")
    print(f"Files processed       : {len(rows)}")
    print(f"  with substances     : {len(ok)}")
    print(f"  empty (0 found)     : {sum(r['status'] == 'empty' for r in rows)}")
    print(f"  errors              : {sum(r['status'] == 'error' for r in rows)}")
    print(f"Unique substances     : {uniq}")
    print(f"  with real InChI     : {sum(r['via_inchi'] for r in ok)}")
    print(f"  SMILES fallback     : {sum(r['via_smiles'] for r in ok)}")
    print(f"  unexpanded alias (*): {wild}")
    print(f"  fully expanded      : {uniq - wild}")
    print(f"Raw structures        : {sum(r['raw'] for r in rows)} (before dedup)")
    print(f"Total time            : {total_ms} ms ({total_ms / 1000:.2f} s)")
    if rows:
        print(f"Average per file      : {total_ms / len(rows):.1f} ms")
    print("\n---- 10 slowest files ----")
    for r in sorted(rows, key=lambda r: -r["ms"])[:10]:
        print(f"  {r['ms']:6d} ms  subs={r['unique']:<3d}  {r['file']}")
    errs = [r for r in rows if r["status"] == "error"]
    if errs:
        print("\n---- errors ----")
        for r in errs:
            print(f"  {r['file']}\n      -> {r['error']}")
    no_keys = [r for r in rows if r["inchikey_error"]]
    if no_keys:
        print("\n---- InChIKey step failed (counted as SMILES fallback) ----")
        for r in no_keys:
            print(f"  {r['file']}")
    empty = [r for r in rows if r["status"] == "empty"]
    if empty:
        print("\n---- files with no substances extracted ----")
        for r in empty:
            print(f"  {r['file']}")
    print("=" * 82)


if __name__ == "__main__":
    main()
