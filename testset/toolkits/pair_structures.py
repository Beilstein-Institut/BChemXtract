"""Pair two tools' structures file by file, as input for ../compare_structures.py.

Usage:
  python pair_structures.py REF.tsv PRED.tsv OUT_PAIRS.tsv

REF and PRED are dumps with columns file, smiles (run-toolkit-performance.py --dump, or the
same layout from another tool). There is no ground truth in the corpus, so REF is simply the
tool the others are compared against; the result measures agreement, not correctness.

Per file, both sides are first deduplicated on InChIKey (canonical SMILES where there is no
key). Structures are then paired greedily, best evidence first:
  1. same InChIKey
  2. same skeleton (InChIKey first block)
  3. highest Morgan-fingerprint Tanimoto similarity, if at least 0.5
Reaction SMILES (Open Babel writes reactions that way) are split into their molecules first.
What is left unpaired is written with an empty pred (ref only) or an empty ref (pred only).
InChIKeys are computed here with RDKit for both sides, so pairing uses one InChI build. All
RDKit work runs in a capped subprocess, because RDKit hangs on a few corpus structures (in
parsing as well as InChI); those are written as ref_hung / pred_hung and must stay out of
compare_structures.py, which would hang on them too.
"""

import csv
import json
import resource
import subprocess
import sys
from collections import defaultdict

from rdkit import DataStructs

MIN_SIMILARITY = 0.5
BATCH_TIMEOUT = 60  # s per file; RDKit hangs on a few corpus structures
SINGLE_TIMEOUT = 30  # s per structure when a file's batch timed out
MEM_LIMIT = 4 << 30
HUNG = "hung"  # key marker: InChI generation did not finish

KEY_SCRIPT = """
import json, sys
from rdkit import Chem, RDLogger
from rdkit.Chem import rdFingerprintGenerator
RDLogger.DisableLog("rdApp.*")
gen = rdFingerprintGenerator.GetMorganGenerator(radius=2, fpSize=2048)
out = []
for smi in json.load(sys.stdin):
    mol = Chem.MolFromSmiles(smi)
    if mol is None:
        out.append(["", smi, ""])
        continue
    try:
        key = Chem.MolToInchiKey(mol) if "*" not in smi else ""
    except Exception:
        key = ""
    out.append([key, Chem.MolToSmiles(mol), gen.GetFingerprint(mol).ToBitString()])
print(json.dumps(out))
"""


def _describe_capped(smiles, timeout):
    def limit():
        resource.setrlimit(resource.RLIMIT_AS, (MEM_LIMIT, MEM_LIMIT))

    try:
        p = subprocess.run([sys.executable, "-I", "-c", KEY_SCRIPT], input=json.dumps(smiles), text=True,
                           capture_output=True, timeout=timeout, preexec_fn=limit)
        return json.loads(p.stdout)
    except (subprocess.TimeoutExpired, json.JSONDecodeError):
        return None


def describe(smiles):
    """[InChIKey, canonical SMILES, fingerprint bits] per SMILES, computed in a capped
    subprocess because RDKit hangs on a few corpus structures (parsing included). A structure
    that does not finish gets the key HUNG."""
    out = _describe_capped(smiles, BATCH_TIMEOUT) if smiles else []
    if out is None:  # retry one by one to find the structure that hangs
        out = [(_describe_capped([s], SINGLE_TIMEOUT) or [[HUNG, s, ""]])[0] for s in smiles]
    return out


def load(path):
    rows = defaultdict(list)
    with open(path, newline="") as fh:
        for row in csv.DictReader(fh, delimiter="\t"):
            smi = row["smiles"].strip()
            if ">" in smi:  # reaction SMILES (Open Babel): pair its molecules one by one
                rows[row["file"]].extend(m for part in smi.split(">") for m in part.split(".") if m)
            elif smi:
                rows[row["file"]].append(smi)
    by_file = {}
    for f, smiles in rows.items():
        entries = {}  # dedup key -> entry
        for smi, (key, canon, bits) in zip(smiles, describe(smiles)):
            fp = DataStructs.CreateFromBitString(bits) if bits else None
            dedup = key if key and key != HUNG else "smi:" + canon
            entries.setdefault(dedup, {"smiles": smi, "key": key, "fp": fp})
        by_file[f] = list(entries.values())
    return by_file


def pair(refs, preds):
    hung = [("ref_hung", r, None) for r in refs if r["key"] == HUNG]
    hung += [("pred_hung", None, p) for p in preds if p["key"] == HUNG]
    pairs = []
    refs = [r for r in refs if r["key"] != HUNG]
    preds = [p for p in preds if p["key"] != HUNG]

    def take(match, step):
        for r in list(refs):
            for p in preds:
                if match(r, p):
                    pairs.append((r, p, step))
                    refs.remove(r)
                    preds.remove(p)
                    break

    take(lambda r, p: r["key"] and r["key"] == p["key"], "inchikey")
    take(lambda r, p: r["key"] and p["key"] and r["key"][:14] == p["key"][:14], "skeleton")
    sims = sorted(
        ((DataStructs.TanimotoSimilarity(r["fp"], p["fp"]), i, j)
         for i, r in enumerate(refs) for j, p in enumerate(preds) if r["fp"] and p["fp"]),
        reverse=True,
    )
    used_r, used_p = set(), set()
    for sim, i, j in sims:
        if sim < MIN_SIMILARITY:
            break
        if i in used_r or j in used_p:
            continue
        used_r.add(i)
        used_p.add(j)
        pairs.append((refs[i], preds[j], "similarity"))
    left_r = [r for i, r in enumerate(refs) if i not in used_r]
    left_p = [p for j, p in enumerate(preds) if j not in used_p]
    return pairs, left_r, left_p, hung


def main():
    ref_path, pred_path, out_path = sys.argv[1:4]
    ref, pred = load(ref_path), load(pred_path)
    with open(out_path, "w", newline="") as fh:
        w = csv.writer(fh, delimiter="\t")
        w.writerow(["id", "file", "pairing", "ref", "pred"])
        n = 0
        for f in sorted(set(ref) | set(pred)):
            pairs, left_r, left_p, hung = pair(ref.get(f, []), pred.get(f, []))
            rows = [(step, r["smiles"], p["smiles"]) for r, p, step in pairs]
            rows += [(step, r["smiles"] if r else "", p["smiles"] if p else "") for step, r, p in hung]
            rows += [("ref_only", r["smiles"], "") for r in left_r]
            rows += [("pred_only", "", p["smiles"]) for p in left_p]
            for step, r, p in rows:
                n += 1
                w.writerow([f"{f}#{n}", f, step, r, p])


if __name__ == "__main__":
    main()
