# BChemXtract test set & performance benchmarks

Self-contained material for reviewers: the ChemDraw corpus plus the two scripts that
reproduce the BChemXtract-vs-Open-Babel comparison reported in
[`BChemXtract-vs-OpenBabel-performance.pdf`](BChemXtract-vs-OpenBabel-performance.pdf).

## Contents

| Path | What it is |
|---|---|
| `corpus/` | The ChemDraw corpus: 225 binary `.cdx` + 21 `.cdxml` files, grouped by category (`integrationTests/`, `volumeTest/`, `cdx/`, `cheminf/`). Mirrors the layout of `src/test/resources`, so file paths quoted in the PDF resolve here. |
| `run-bchemxtract-performance.sh` | Runs BChemXtract over every `.cdx` and `.cdxml` in the corpus and prints an extraction-performance overview. Builds the fat jar if none is present. |
| `run-openbabel-performance.sh` | Open Babel counterpart, made comparable by deduplicating per file on InChIKey. |
| `BChemXtract-vs-OpenBabel-performance.pdf` | The generated comparison report (BChemXtract v1.0 & v1.1.3 vs Open Babel 3.1.0). |

### Which files each script reads

`run-bchemxtract-performance.sh` reads both formats — `.cdx` through `CDXReader`, `.cdxml` through
`CDXMLReader` — so it reports over all 246 files. `run-openbabel-performance.sh` reads the 225
binary `.cdx` files only, because Open Babel's CDXML support is not equivalent.

The head-to-head figures in the PDF are therefore `.cdx`-only. To reproduce that exact scope, point
the BChemXtract script at a directory holding only the binary files:

```bash
mkdir -p /tmp/cdx-only && (cd testset/corpus && find . -name '*.cdx' -print0 | cpio -pd0m /tmp/cdx-only)
testset/run-bchemxtract-performance.sh /tmp/cdx-only
```

For reference, on the current corpus the two scopes give:

| Scope | Files | With substances | Substances | Real InChIs | Time |
|---|---|---|---|---|---|
| `.cdx` only (comparable to Open Babel) | 225 | 203 | 1360 | 1336 | 5.9 s |
| `.cdx` + `.cdxml` (default) | 246 | 220 | 1415 | 1387 | 6.3 s |

The measurement code is `src/main/java/org/beilstein/chemxtract/samples/BCXTractPerformance.java`.

## Running

Both scripts are run from the repository root and default to `testset/corpus`; pass a
directory to point them elsewhere.

```bash
# BChemXtract — builds the fat jar on first run (needs JDK 21+ and Maven)
testset/run-bchemxtract-performance.sh
FORCE_BUILD=1 testset/run-bchemxtract-performance.sh   # force a rebuild

# Open Babel — needs `obabel` on PATH (report used Open Babel 3.1.0)
testset/run-openbabel-performance.sh
```

## Methodology

- **BChemXtract path:** `CDXReader.readDocument` → `SubstanceXtractor.xtractUnique(doc, info, false)`.
  A "real InChI" is a non-empty `BCXSubstance.getInchi()`; an "unexpanded alias" is a SMILES containing `*`.
- **Open Babel path:** `obabel file.cdx -ocan`, dedup on canonical SMILES, then `obabel -ismi -oinchikey`.
  Unique = distinct InChIKeys + structures with no InChI (deduped on SMILES). Open Babel's InChI
  generation fails on structures containing `*`, hence the SMILES fallback.

Both tools therefore see the same files and the same per-file InChIKey deduplication.

## Note on counts

The PDF was generated on 2026-07-08 against 200 `.cdx` files. The corpus has grown since
(fixtures are added as bugs are reported), so a fresh run reports higher file counts. The
relative picture — near-100 % real InChIs and full alias expansion for BChemXtract versus
Open Babel's ~15 % InChIs and hundreds of leftover `*` atoms, at roughly an order of
magnitude less runtime — is what the report is about.

Two files are expected to fail for every tool: `volumeTest/vol20Test/m31717432-graphical-abstract.cdx`
is not a valid binary CDX (no ChemDraw header). Files reported as "empty" by BChemXtract are
S-group / copolymer / Markush fixtures it deliberately does not turn into discrete substances.
