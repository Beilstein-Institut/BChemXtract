# Test set and performance benchmarks

Everything needed to reproduce the comparison in
[`BChemXtract-vs-OpenBabel-performance.pdf`](BChemXtract-vs-OpenBabel-performance.pdf).

| Path | Contents |
|---|---|
| `corpus/` | 225 binary `.cdx` + 21 `.cdxml` ChemDraw files, laid out as in `src/test/resources` so paths quoted in the report resolve here |
| `run-bchemxtract-performance.sh` | BChemXtract benchmark; reads `.cdx` and `.cdxml` |
| `run-openbabel-performance.sh` | Open Babel benchmark; reads `.cdx` only |
| `BChemXtract-vs-OpenBabel-performance.pdf` | The generated report |

## Running

From the repository root. Both scripts default to `testset/corpus`; pass a directory to override.

```bash
testset/run-bchemxtract-performance.sh    # needs JDK 21+ and Maven; builds the fat jar on first run
testset/run-openbabel-performance.sh      # needs `obabel` on PATH (report used Open Babel 3.1.0)
```

## Method

- **BChemXtract:** `CDXReader`/`CDXMLReader.readDocument` → `SubstanceXtractor.xtractUnique(doc, info, false)`.
  Real InChI = non-empty `BCXSubstance.getInchi()`; unexpanded alias = SMILES containing `*`.
  Source: `src/main/java/org/beilstein/chemxtract/samples/BCXTractPerformance.java`.
- **Open Babel:** `obabel file.cdx -ocan`, dedup on canonical SMILES, then `obabel -ismi -oinchikey`.
  Unique = distinct InChIKeys + structures with no InChI (Open Babel cannot generate InChI for
  structures containing `*`), deduplicated on SMILES.

Both deduplicate per file on InChIKey.

## Scope and expected results

The report's figures are `.cdx`-only. To match that scope exactly:

```bash
mkdir -p /tmp/cdx-only && (cd testset/corpus && find . -name '*.cdx' -print0 | cpio -pd0m /tmp/cdx-only)
testset/run-bchemxtract-performance.sh /tmp/cdx-only
```

BChemXtract on the current corpus:

| Scope | Files | Substances | Real InChIs | Time |
|---|---|---|---|---|
| `.cdx` only | 225 | 1360 | 1336 | 5.9 s |
| `.cdx` + `.cdxml` | 246 | 1415 | 1387 | 6.3 s |

The report was generated 2026-07-08 against 200 `.cdx` files; the corpus has grown since, so counts
are higher than printed there.

Expected non-results: `volumeTest/vol20Test/m31717432-graphical-abstract.cdx` fails for every tool —
it is not a valid binary CDX (no ChemDraw header). Files reported as empty are S-group, copolymer
and Markush fixtures that BChemXtract deliberately does not resolve into discrete substances.
