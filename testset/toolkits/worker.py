"""Read one ChemDraw file with RDKit or Indigo and write a JSON record to OUT.

Usage: python worker.py {rdkit|indigo} FILE OUT
       python worker.py inchikeys RECORD   (fill InChIKeys into a ChemScanner record, in place)

Record: {"ms": int, "status": "ok"|"error", "error": str, "structures": [[smiles, inchikey], ...]}
`ms` covers reading the file and generating SMILES + InChIKey for every structure, as in the
Open Babel benchmark. A structure whose InChIKey cannot be generated carries an empty key.
"""

import json
import sys
import time


def read_rdkit(path):
    from rdkit import Chem, RDLogger

    RDLogger.DisableLog("rdApp.*")
    params = Chem.CDXMLParserParams()
    params.format = Chem.CDXMLFormat.CDXML if path.lower().endswith(".cdxml") else Chem.CDXMLFormat.CDX
    out = []
    for mol in Chem.MolsFromCDXMLFile(path, params):
        if mol is None:  # fragment that failed sanitization
            continue
        smi = Chem.MolToSmiles(mol)
        try:
            key = Chem.MolToInchiKey(mol) if "*" not in smi else ""
        except Exception:
            key = ""
        out.append([smi, key])
    return out


def read_indigo(path):
    from indigo import Indigo, IndigoException
    from indigo.inchi import IndigoInchi

    indigo = Indigo()
    inchi = IndigoInchi(indigo)
    if path.lower().endswith(".cdxml"):
        # No CDXML iterator: the whole page loads as one molecule (or reaction). Split it into
        # connected components so it is counted per structure like the CDX path.
        if _is_reaction(indigo, path):
            objs = [indigo.loadReactionFromFile(path)]
        else:
            objs = [c.clone() for c in indigo.loadMoleculeFromFile(path).iterateComponents()]
    else:
        objs = list(indigo.iterateCDXFile(path))
    mols = []
    for obj in objs:
        if "reaction" in obj.dbgInternalType():
            mols.extend(m.clone() for m in obj.iterateMolecules())
        else:
            mols.append(obj)
    out = []
    for mol in mols:
        try:
            smi = mol.canonicalSmiles()
        except IndigoException:
            continue
        if not smi:
            continue
        try:
            key = inchi.getInchiKey(inchi.getInchi(mol)) if "*" not in smi else ""
        except IndigoException:
            key = ""
        out.append([smi, key])
    return out


def _is_reaction(indigo, path):
    try:
        indigo.loadReactionFromFile(path)
        return True
    except Exception:
        return False


def fill_inchikeys(record):
    from rdkit import Chem, RDLogger

    RDLogger.DisableLog("rdApp.*")
    with open(record) as f:
        rec = json.load(f)
    for s in rec["structures"]:
        mol = Chem.MolFromSmiles(s[0]) if "*" not in s[0] else None
        try:
            s[1] = Chem.MolToInchiKey(mol) if mol is not None else ""
        except Exception:
            s[1] = ""
    with open(record, "w") as f:
        json.dump(rec, f)


def main():
    if sys.argv[1] == "inchikeys":
        fill_inchikeys(sys.argv[2])
        return
    tool, path = sys.argv[1], sys.argv[2]
    reader = {"rdkit": read_rdkit, "indigo": read_indigo}[tool]
    start = time.perf_counter()
    rec = {"status": "ok", "error": "", "structures": []}
    try:
        rec["structures"] = reader(path)
    except Exception as e:  # report, do not crash the run
        rec["status"] = "error"
        rec["error"] = f"{type(e).__name__}: {e}".splitlines()[0][:200]
    rec["ms"] = round((time.perf_counter() - start) * 1000)
    with open(sys.argv[3], "w") as out:
        json.dump(rec, out)


if __name__ == "__main__":
    main()
