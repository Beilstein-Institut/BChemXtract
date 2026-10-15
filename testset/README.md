# Test set and performance benchmarks

Everything needed to reproduce the comparisons in
[`BChemXtract-1.4.1-vs-OpenBabel-3.1.1-performance.pdf`](BChemXtract-1.4.1-vs-OpenBabel-3.1.1-performance.pdf) and
[`BChemXtract-1.4.1-vs-ChemScanner-RDKit-Indigo-performance.pdf`](BChemXtract-1.4.1-vs-ChemScanner-RDKit-Indigo-performance.pdf).

| Path | Contents |
|---|---|
| `corpus/` | 225 binary `.cdx` + 21 `.cdxml` ChemDraw files, laid out as in `src/test/resources` so paths quoted in the reports resolve here |
| `run-bchemxtract-performance.sh` | BChemXtract benchmark; reads `.cdx` and `.cdxml` |
| `run-openbabel-performance.sh` | Open Babel benchmark; reads `.cdx` only |
| `toolkits/` | RDKit, Indigo and ChemScanner benchmark (`run-toolkit-performance.py`) and its per-file workers |
| `BChemXtract-1.4.1-vs-ChemScanner-RDKit-Indigo-performance.pdf` | 1.4.1 vs ChemScanner 0.1.3, RDKit 2026.09.1, Indigo 1.46.0 (+ Open Babel 3.1.1), `.cdx` and `.cdxml`, 2026-10-15 |
| `BChemXtract-1.4.1-vs-OpenBabel-3.1.1-performance.pdf` | Current report — 1.4.1 vs Open Babel 3.1.1, 225 files, 2026-09-09 |
| `BChemXtract-vs-OpenBabel-performance.pdf` | Previous report — v1.0 & v1.1.3 vs Open Babel 3.1.0, 200 files, 2026-07-08 |

## Running

From the repository root. Both scripts default to `testset/corpus`; pass a directory to override.

```bash
testset/run-bchemxtract-performance.sh    # needs JDK 25 and Maven; builds the fat jar on first run
testset/run-openbabel-performance.sh      # needs `obabel` on PATH
```

RDKit, Indigo and ChemScanner (one worker process per file, capped at 120 s and 4 GB):

```bash
python3 -m venv /tmp/tk && /tmp/tk/bin/pip install rdkit epam.indigo
GEM_HOME=/tmp/gems gem install chem_scanner        # pulls a prebuilt rdkit_chem
GEM_HOME=/tmp/gems /tmp/tk/bin/python -I testset/toolkits/run-toolkit-performance.py \
    {rdkit|indigo|chemscanner} [corpus-dir] --ext {cdx|cdxml|all} [--tsv per-file.tsv]
```

ChemScanner 0.1.3 needs one compatibility patch against current `rdkit_chem`. The worker applies it at load
time and leaves the gem untouched; see `toolkits/chemscanner_worker.rb`.

`pom.xml` sets `<java.version>25</java.version>`, so the build and the fat jar both require JDK 25 —
an older JDK fails with `release version 25 not supported`, and a JDK 25-built jar will not run on one
(`UnsupportedClassVersionError`, class file version 69).

## Method

- **BChemXtract:** `CDXReader`/`CDXMLReader.readDocument` → `SubstanceXtractor.xtractUnique(doc, info, false)`.
  Real InChI = non-empty `BCXSubstance.getInchi()`; unexpanded alias = SMILES containing `*`.
  Source: `src/main/java/org/beilstein/chemxtract/samples/BCXTractPerformance.java`.
- **Open Babel:** `obabel file.cdx -ocan`, dedup on canonical SMILES, then `obabel -ismi -oinchikey`.
  Unique = distinct InChIKeys + structures with no InChI (Open Babel cannot generate InChI for
  structures containing `*`), deduplicated on SMILES.

Both deduplicate per file on InChIKey.

## Scope and expected results

The reports' figures are `.cdx`-only, matching Open Babel's reach. To use that scope:

```bash
mkdir -p /tmp/cdx-only && (cd testset/corpus && find . -name '*.cdx' -print0 | cpio -pd0m /tmp/cdx-only)
testset/run-bchemxtract-performance.sh /tmp/cdx-only
testset/run-openbabel-performance.sh /tmp/cdx-only
```

BChemXtract 1.4.1 on the current corpus:

| Scope | Files | Substances | Real InChIs | Unexpanded alias (`*`) |
|---|---|---|---|---|
| `.cdx` only | 225 | 1360 | 1336 | 23 |
| `.cdx` + `.cdxml` | 246 | 1415 | 1387 | 27 |

Head to head on the 225 `.cdx` files (2026-09-09, BChemXtract 1.4.1 on JDK 25 vs Open Babel 3.1.1):

| Metric | BChemXtract 1.4.1 | Open Babel 3.1.1 |
|---|---|---|
| Files yielding structures | 203 | 224 |
| Empty (0 found) | 21 | 0 |
| Failures | 1 | 1 |
| Unique substances | 1360 | 1317 |
| Real InChIs | 1336 (98.2%) | 187 (14.2%) |
| SMILES fallback | 24 | 1130 |
| Unexpanded alias (`*`) | 23 | 617 |
| Fully expanded | 1337 | 700 |

Counts are hardware-independent and should reproduce exactly on the same corpus. **Times are not** —
treat any absolute figure below as a property of the machine that produced it:

| Run | BChemXtract | Open Babel |
|---|---|---|
| 2026-07-08 report (200 files) | 34 ms/file | 401 ms/file |
| 2026-09-09 rerun (225 files) | 51 ms/file | 252 ms/file |

Open Babel's 56.7 s total on the 2026-09-09 rerun is dominated by one file:
`volumeTest/bug/m17121576-9.cdx` takes 44.3 s of it. Excluding that file, Open Babel runs the remaining
224 in 12.4 s (55 ms/file), against BChemXtract's 51 ms/file — so on this corpus the two are comparable
per file and the difference that holds up is chemical resolution, not throughput. The same file is the
one BChemXtract v1.0 hung on indefinitely; 1.4.1 handles it in milliseconds.

Expected non-results: `volumeTest/vol20Test/m31717432-graphical-abstract.cdx` fails for every tool —
it is not a valid binary CDX (no ChemDraw header). Files reported as empty are S-group, copolymer
and Markush fixtures that BChemXtract deliberately does not resolve into discrete substances.

The 23 unexpanded aliases BChemXtract reports are not yet attributed. The 2026-07-08 report measured 0
over 200 files; the corpus has since grown to 225, so these may be new fixtures rather than a
regression — check before reusing the "no unexpanded aliases" claim.
