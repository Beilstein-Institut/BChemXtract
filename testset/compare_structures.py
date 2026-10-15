#!/usr/bin/env python3
"""Compare predicted structures with reference structures, error type by error type.

A valid InChI only proves that the output is a molecule. This script asks whether it
is the RIGHT molecule, and if not, what kind of wrong it is.

Input: one CSV or TSV, one row per molecule, with an id column, a reference SMILES
column and a predicted SMILES column.

Each row gets exactly one verdict:

    ref_invalid        the reference does not parse (row is left out of the percents)
    pred_invalid       the prediction is empty or does not parse
    exact              canonical isomeric SMILES are equal: same molecule, same drawn form
    same_inchi_form    full standard InChIKeys are equal but the drawn form differs:
                       zwitterion vs neutral, or a tautomer (2-pyridone vs 2-hydroxypyridine).
                       Standard InChI hides these on purpose; decide if your task counts them
    wrong_skeleton     InChIKey block 1 differs: other atoms or bonds
                       (a wrong abbreviation expansion lands here)
    charge_h           same skeleton; charge, protonation or isotope differs, stereo agrees
    stereo             same skeleton; only stereo differs
    charge_h_stereo    same skeleton; both differ
    other_layer        keys differ but no check above finds the cause (InChI-only stereo
                       that CIP does not label, for example); read these rows by hand

Stereo rows are then split by how bad the stereo error is:

    stereo_conflict    at least one centre inverted or one double bond flipped: wrong molecule
    stereo_extra       only stereo the reference does not define: over-specified
    stereo_omission    only stereo the prediction left out: right molecule, under-specified

Per stereocentre (CIP R/S, r/s) and per double bond (CIP E/Z) the script counts
correct / inverted (flipped) / missing / extra over all rows with the same skeleton.

Atom mapping: the two structures are matched atom to atom with stereo, charge and
isotopes ignored. For a symmetric molecule there are many mappings; the script takes
the one with the most stereo agreement, so symmetry never makes a fake error.

Usage:
    python compare_structures.py pairs.tsv --id-col id --ref-col ref --pred-col pred \
        --out rows.tsv --summary summary.json
"""

from __future__ import annotations

import argparse
import csv
import json
import sys
from collections import Counter
from pathlib import Path

from rdkit import Chem, RDLogger
from rdkit.Chem import rdCIPLabeler
from rdkit.Chem import rdMolDescriptors

RDLogger.DisableLog("rdApp.*")

MAX_MAPPINGS = 10_000
STEREO_KEYS = ("correct", "inverted", "missing", "extra")
BOND_KEYS = ("correct", "flipped", "missing", "extra")


def _parse(smiles: str):
    smiles = (smiles or "").strip()
    if not smiles:
        return None
    return Chem.MolFromSmiles(smiles)


def _inchi_layers(mol, options: str = "") -> dict[str, str]:
    """Standard InChI split into layers, keyed by the layer prefix letter."""
    inchi = Chem.MolToInchi(mol, options=options) or ""
    layers = {}
    for part in inchi.split("/")[2:]:  # skip "InChI=1S" and the formula
        if part:
            layers[part[0]] = part
    return layers


def _formula_delta(ref, pred) -> str:
    """'+C1 -O1' style difference of element counts, prediction minus reference."""
    def counts(mol):
        c = Counter()
        for atom in Chem.AddHs(mol).GetAtoms():
            c[atom.GetSymbol()] += 1
        return c

    cr, cp = counts(ref), counts(pred)
    parts = []
    for el in sorted(set(cr) | set(cp)):
        d = cp[el] - cr[el]
        if d:
            parts.append(f"{'+' if d > 0 else '-'}{el}{abs(d)}")
    return " ".join(parts) if parts else "same formula"


def _skeleton_copy(mol):
    """Copy with charges and isotopes cleared, used only to find atom mappings."""
    rw = Chem.RWMol(mol)
    for atom in rw.GetAtoms():
        atom.SetFormalCharge(0)
        atom.SetIsotope(0)
        atom.SetNoImplicit(False)
        atom.SetNumExplicitHs(0)
    m = rw.GetMol()
    m.UpdatePropertyCache(strict=False)
    return m


def _cip(mol):
    """CIP labels for atoms and double bonds; {} if the labeller fails."""
    mol = Chem.Mol(mol)
    try:
        rdCIPLabeler.AssignCIPLabels(mol)
    except Exception:
        return {}, {}
    atoms = {a.GetIdx(): a.GetProp("_CIPCode") for a in mol.GetAtoms() if a.HasProp("_CIPCode")}
    bonds = {}
    for b in mol.GetBonds():
        if b.HasProp("_CIPCode"):
            bonds[frozenset((b.GetBeginAtomIdx(), b.GetEndAtomIdx()))] = b.GetProp("_CIPCode")
    return atoms, bonds


def _score_mapping(amap, ref_atoms, ref_bonds, pred_atoms, pred_bonds):
    atom_out = dict.fromkeys(STEREO_KEYS, 0)
    for i, j in enumerate(amap):
        a, b = ref_atoms.get(i), pred_atoms.get(j)
        if a and b:
            atom_out["correct" if a == b else "inverted"] += 1
        elif a:
            atom_out["missing"] += 1
        elif b:
            atom_out["extra"] += 1
    bond_out = dict.fromkeys(BOND_KEYS, 0)
    mapped_pred_bonds = set()
    for key, a in ref_bonds.items():
        i, j = tuple(key)
        pkey = frozenset((amap[i], amap[j]))
        mapped_pred_bonds.add(pkey)
        b = pred_bonds.get(pkey)
        if b:
            bond_out["correct" if a == b else "flipped"] += 1
        else:
            bond_out["missing"] += 1
    bond_out["extra"] = sum(1 for k in pred_bonds if k not in mapped_pred_bonds)
    return atom_out, bond_out


def compare_stereo(ref, pred):
    """Best-agreement per-centre and per-double-bond comparison, or None if no mapping."""
    matches = _skeleton_copy(pred).GetSubstructMatches(
        _skeleton_copy(ref), useChirality=False, uniquify=False, maxMatches=MAX_MAPPINGS
    )
    if not matches:
        return None
    ref_atoms, ref_bonds = _cip(ref)
    pred_atoms, pred_bonds = _cip(pred)
    best = None
    for amap in matches:
        atom_out, bond_out = _score_mapping(amap, ref_atoms, ref_bonds, pred_atoms, pred_bonds)
        # Most agreement first, then fewest errors; ties keep the first mapping (deterministic).
        score = (
            atom_out["correct"] + bond_out["correct"],
            -(atom_out["inverted"] + bond_out["flipped"]),
        )
        if best is None or score > best[0]:
            best = (score, atom_out, bond_out)
    return best[1], best[2]


def compare(ref_smiles: str, pred_smiles: str) -> dict:
    ref = _parse(ref_smiles)
    if ref is None:
        return {"verdict": "ref_invalid"}
    pred = _parse(pred_smiles)
    if pred is None:
        return {"verdict": "pred_invalid"}

    if Chem.MolToSmiles(ref) == Chem.MolToSmiles(pred):
        return {"verdict": "exact"}
    key_ref, key_pred = Chem.MolToInchiKey(ref), Chem.MolToInchiKey(pred)
    if not key_ref or not key_pred:
        return {"verdict": "pred_invalid" if key_ref else "ref_invalid"}
    if key_ref == key_pred:
        charges = [sorted(a.GetFormalCharge() for a in m.GetAtoms()) for m in (ref, pred)]
        note = "charge separation differs" if charges[0] != charges[1] else "tautomer or H position differs"
        return {"verdict": "same_inchi_form", "note": note}
    if key_ref[:14] != key_pred[:14]:
        return {"verdict": "wrong_skeleton", "formula_delta": _formula_delta(ref, pred)}

    # Same skeleton. Charge / protonation / isotope: compare the non-stereo InChI layers.
    lr, lp = _inchi_layers(ref, "-SNon"), _inchi_layers(pred, "-SNon")
    charge_h = any(lr.get(k) != lp.get(k) for k in "hqpi") or key_ref[-1] != key_pred[-1]

    stereo = compare_stereo(ref, pred)
    if stereo is None:
        return {"verdict": "other_layer", "note": "no atom mapping found"}
    atom_out, bond_out = stereo
    errors = (atom_out["inverted"] + atom_out["missing"] + atom_out["extra"]
              + bond_out["flipped"] + bond_out["missing"] + bond_out["extra"])

    if errors and charge_h:
        verdict = "charge_h_stereo"
    elif errors:
        verdict = "stereo"
    elif charge_h:
        verdict = "charge_h"
    else:
        verdict = "other_layer"

    out = {"verdict": verdict}
    if errors:
        if atom_out["inverted"] or bond_out["flipped"]:
            out["stereo_class"] = "stereo_conflict"
        elif atom_out["extra"] or bond_out["extra"]:
            out["stereo_class"] = "stereo_extra"
        else:
            out["stereo_class"] = "stereo_omission"
    out.update({f"centre_{k}": v for k, v in atom_out.items()})
    out.update({f"bond_{k}": v for k, v in bond_out.items()})
    return out


VERDICTS = ("exact", "same_inchi_form", "wrong_skeleton", "charge_h", "stereo", "charge_h_stereo",
            "other_layer", "pred_invalid")
STEREO_CLASSES = ("stereo_conflict", "stereo_extra", "stereo_omission")


def _pct(n: int, d: int) -> str:
    return f"{100.0 * n / d:.2f}%" if d else "n/a"


def summarise(results: list[dict]) -> dict:
    verdicts = Counter(r["verdict"] for r in results)
    scored = sum(v for k, v in verdicts.items() if k != "ref_invalid")
    classes = Counter(r["stereo_class"] for r in results if "stereo_class" in r)
    centre = {k: sum(r.get(f"centre_{k}", 0) for r in results) for k in STEREO_KEYS}
    bond = {k: sum(r.get(f"bond_{k}", 0) for r in results) for k in BOND_KEYS}
    return {
        "rows_total": len(results),
        "rows_ref_invalid": verdicts.get("ref_invalid", 0),
        "rows_scored": scored,
        "verdicts": {k: {"count": verdicts.get(k, 0), "percent": _pct(verdicts.get(k, 0), scored)}
                     for k in VERDICTS},
        "stereo_rows_by_class": {k: {"count": classes.get(k, 0), "percent": _pct(classes.get(k, 0), scored)}
                                 for k in STEREO_CLASSES},
        "stereocentres_same_skeleton_rows": centre,
        "double_bonds_same_skeleton_rows": bond,
    }


def _sniff_sep(path: Path) -> str:
    if path.suffix.lower() in (".tsv", ".tab"):
        return "\t"
    if path.suffix.lower() == ".csv":
        return ","
    with path.open(newline="", encoding="utf-8") as fh:
        return "\t" if "\t" in fh.readline() else ","


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("input", type=Path, help="CSV or TSV with id, reference and predicted SMILES")
    ap.add_argument("--id-col", default="id")
    ap.add_argument("--ref-col", default="ref")
    ap.add_argument("--pred-col", default="pred")
    ap.add_argument("--sep", default=None, help="column separator (default: from extension)")
    ap.add_argument("--out", type=Path, default=None, help="per-row TSV (default: <input>.compare.tsv)")
    ap.add_argument("--summary", type=Path, default=None, help="summary JSON (default: <input>.summary.json)")
    args = ap.parse_args(argv)

    sep = args.sep or _sniff_sep(args.input)
    out_path = args.out or args.input.with_suffix(".compare.tsv")
    summary_path = args.summary or args.input.with_suffix(".summary.json")

    with args.input.open(newline="", encoding="utf-8") as fh:
        reader = csv.DictReader(fh, delimiter=sep)
        missing = {args.id_col, args.ref_col, args.pred_col} - set(reader.fieldnames or [])
        if missing:
            print(f"missing columns {sorted(missing)}; found {reader.fieldnames}", file=sys.stderr)
            return 2
        rows = list(reader)

    results = []
    for row in rows:
        res = compare(row[args.ref_col], row[args.pred_col])
        res["id"] = row[args.id_col]
        results.append(res)

    fields = ["id", "verdict", "stereo_class", "formula_delta", "note"]
    fields += [f"centre_{k}" for k in STEREO_KEYS] + [f"bond_{k}" for k in BOND_KEYS]
    with out_path.open("w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=fields, delimiter="\t", extrasaction="ignore")
        w.writeheader()
        w.writerows(results)

    summary = summarise(results)
    summary_path.write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")

    print(f"rows {summary['rows_total']}  scored {summary['rows_scored']}  "
          f"ref_invalid {summary['rows_ref_invalid']}")
    for k, v in summary["verdicts"].items():
        print(f"  {k:<18}{v['count']:>8}  {v['percent']:>8}")
    print("  stereo rows by class:")
    for k, v in summary["stereo_rows_by_class"].items():
        print(f"    {k:<16}{v['count']:>8}  {v['percent']:>8}")
    print(f"  stereocentres: {summary['stereocentres_same_skeleton_rows']}")
    print(f"  double bonds:  {summary['double_bonds_same_skeleton_rows']}")
    print(f"wrote {out_path} and {summary_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
