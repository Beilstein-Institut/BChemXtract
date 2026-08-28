# Limitations & Known Gaps

This document is an honest account of what BChemXtract can and cannot do
today, where it fails, and what is missing. It complements the
[README](README.md) feature list — read it before relying on
BChemXtract for anything beyond discrete small-molecule extraction.

> **TL;DR** — Extracting individual small-to-medium chemical structures
> from ChemDraw into InChI / SMILES / mol is **mature and battle-tested**.
> Reactions, very large molecules, Markush alternative-groups, wavy-bond
> E/Z, and non-chemistry drawing constructs are **weaker, experimental,
> or silently dropped**. When fidelity matters, validate against the
> source file.

## What works well

Single-structure extraction is the mature core and runs Beilstein's
Diamond Open Access publishing pipeline in production. For a well-drawn
structure in either `.cdx` (binary) or `.cdxml` (XML) you reliably get:

- Atoms, bonds, stereo (from wedge/dash geometry), charges, isotopes, rings
- InChI + InChIKey, canonical / isomeric SMILES, extended SMILES (CXSMILES),
  MDL V3000 mol block, molecular formula
- Abbreviation expansion (`Ph`, S-groups, …) and sugar-projection detection
  (Chair / Haworth)
- PNG depiction via CDK

Both parsers converge on one format-agnostic object model, so downstream
behavior is consistent across the two file formats.

## Limitations at a glance

| # | Area | Limitation | Severity |
|---|------|------------|----------|
| 1 | Reactions | Experimental; arrow-alignment heuristics, `sanitize` off by default | High |
| 2 | CDXML fidelity | `Arrow`, `BioShape`, `LinkNode`, `ColoredMolecularAreas` silently dropped | Medium-High |
| 3 | Large structures | > 500 atoms → **no InChI/InChIKey/AuxInfo**, SMILES degrades | Medium |
| 4 | Stereo | Wavy-bond E/Z: SMILES vs InChI/MDL descriptors can disagree | Medium |
| 5 | Markush | Alt-group path thinly validated (1 synthetic file in the whole corpus); multi-atom alt-group substituents can fail; `Me` shorthand missing | Medium |
| 6 | Security | XXE not explicitly disabled for untrusted CDXML input | Medium |
| 7 | Configurability | Safety limits and reader strictness are hardcoded | Low-Medium |
| 8 | Test coverage | No unit tests for several correctness-critical chemistry handlers | Medium |
| 9 | Coordinates | Contracted-nickname atoms keep ChemDraw's hidden geometry, which can overlap the visible structure | Low |

## Details

### 1. Reaction extraction is experimental

Reaction extraction is under active development and should be treated as
a preview, not a supported feature. `ReactionXtractor` defaults to
`sanitize = false`, and correct grouping of reactants / products / agents
depends on **arrow-alignment heuristics**. RInChI and reaction SMILES are
produced, but reaction output should not be trusted unsupervised.

### 2. Silent data loss on several CDXML constructs

Several CDX/CDXML constructs are dropped during read or write **without
surfacing to the caller** — the riskiest kind of gap, because it is
invisible unless you diff against the source:

- `Arrow` and `BioShape` elements are discarded on read
  (`CDXMLReader.java` — `// TODO arrow, bioshape`)
- `LinkNode` maps to `null` (`CDXMLConstants.java`)
- `ColoredMolecularAreas` are not written by `CDXMLWriter` (round-trip loss)
- `CDArrow.dipole` is unimplemented
- Arrowhead-type constants are marked unverified
  (`// TODO: check if this are the correct values`)

BChemXtract is a **chemistry extractor, not a faithful CDX round-trip
tool** — drawing-level (non-chemistry) content is not a first-class
citizen.

### 3. Hard safety limits on large structures

Safety limits in `SubstanceXtractor` are hardcoded via
`Definitions.MAX_ATOM_COUNT = 500` and are **not configurable without
recompiling**:

- **> 500 atoms → no InChI, InChIKey, or AuxInfo is generated at all**, and
  SMILES degrades from absolute to plain isomeric. Large peptides, polymers,
  and big natural products therefore come back InChI-less.
- **AuxInfo longer than 4000 characters is silently dropped.**

### 4. Stereochemistry edge cases

Tetrahedral stereo loss on the MDL V3000 round-trip has been fixed
(`SubstanceXtractor.withWedgeBonds`). One open, deliberately **undecided**
case remains:

- **Double bonds drawn with a wavy substituent** mean "E/Z deliberately
  unspecified" in ChemDraw. SMILES honors the wavy and omits E/Z, but the
  InChI and MDL→InChI paths assign E/Z from the 2D coordinates. As a result,
  the three descriptors for the *same molecule* can legitimately disagree.
  This is a pending chemistry-intent decision (tracked in the disabled
  `StereoInconsistencyTest`), not yet a code fix.

Related: when a stereocentre is left undetermined, InChI falls back to raw
2D coordinates for that centre — so **coordinates are not always cosmetic**
and can change the InChIKey.

### 5. Markush / R-group support has soft edges

R-group enumeration is opt-in (`SubstanceXtractor.xtract(doc, info, true)`)
and combinatorial. Known gaps:

- The structural `NamedAlternativeGroup` path is validated only against a
  **hand-authored fixture**. A census of the entire corpus — 245 `.cdx` /
  `.cdxml` files under `src/test/resources`, parsed with the real readers —
  finds alternative groups in exactly **one** file:
  `cheminf/bugs/markush/altgroup_R_methyl_chloro.cdxml`, with two
  one-heavy-atom alternatives (methyl, chloro). Real-world
  alternative-group connection conventions are therefore unverified, and
  because both alternatives take the single-atom graft branch, the
  multi-atom path (`MarkushHandler.replaceMultiAtom`, which requires
  exactly one `*` connection point) is exercised by no ChemDraw file in the
  repo at all.
- **Multi-atom alternative-group substituents** can error (the
  external-connection-point `*` is dropped).
- Common shorthands such as `Me` are absent from the SMILES lookup tables
  and are not valid SMILES, so `R = Me` alone does not resolve.
- Positional aryl-substituent notation — `<position>-<group>`, written
  either with a number (`R = 3-OMe, 4-CF3`) or with an ortho/meta/para
  prefix (`R = o-Cl, m-F, p-OMe`) — **is resolved** (`MarkushHandler`): the
  position index is mapped to the ring atom that many bonds from the ring's
  attachment (ipso) atom, the residue is moved there, and the group is
  grafted. `o`/`m`/`p` are read as positions 2/3/4. It therefore also
  reaches positions the drawn (possibly position-variation) attachment never
  covered. Remaining edges:
  - the abbreviation lookup is tried **first**, so names that merely look
    positional (`2-py`, `4-ClPh`, `4-MeO-Ph`, `p-Tol`) keep their dictionary
    meaning;
  - the substituent half must itself resolve, which is what a value like
    `p-Cl-Ph-` or `o-Cl-Ph-` fails: there the prefix belongs to a
    substituted-aryl *name* standing in for the whole residue, and no
    positional reading applies. Such values are dropped, not misplaced;
  - the group is resolved **before** the ring is inspected, so a dictionary
    gap in the substituent half masks a working positional path: `4-CH3`
    resolves while `2-Me`, `4-OCH3`, `4-t-Bu` and `4-CO2CH3` do not, for the
    same reason `R = Me` alone does not (see above);
  - a ring with no unique ipso atom (several exocyclic substituents) is
    rejected rather than guessed at, and the value is dropped;
  - where both directions round the ring reach the named position, the atom
    nearer the drawn residue wins — equivalent for a ring symmetric about
    the ipso atom, a heuristic otherwise;
  - a drawn position-variation attachment still expands to one scaffold per
    candidate atom, so a positional value yields duplicate (identical)
    structures. `xtractUnique` collapses them; plain `xtract` does not;
  - a page that also carries a generic scheme where the same label stands for
    a whole group (`R–C(=O)NH–Ar`, residue on no ring) logs one dropped
    combination per positional value there. Those warnings are expected: the
    value still resolves on the scaffold its legend describes.
- Bond-encoded position variation (`AttachmentHandler`) is normalised from
  either drawing direction: when the residue label itself is the stub end
  sitting on the crossed bond, the *other* end becomes the synthetic junction,
  so the R identity survives the expansion. Before that, such drawings lost
  the label and silently grafted a methyl instead.
- Two-column legends **are** merged (`MarkushHandler.sideBySideLegend`): a
  substrate scope split into two adjacent text columns is recognised as one
  list when the columns span the same rows and the gutter is narrow relative
  to the columns, so all values reach the scaffold. The threshold is
  geometric, not scaffold-aware — two *different* scaffolds whose legends sit
  that close would merge wrongly, which would need scaffold-to-legend
  association to resolve properly.

### 6. Security posture on untrusted input

XML parsing uses the deprecated `XMLReaderFactory` and **does not
explicitly disable external entities / DTDs**, leaving it potentially
exposed to XXE if untrusted CDXML is parsed. This is acceptable for a
trusted, in-house corpus but is a concern for any public-facing service
that accepts arbitrary CDXML.

### 7. Limited configurability

- Atom-count and AuxInfo limits are compile-time constants.
- Reader strictness is governed by a compile-time constant (`RIGID`); there
  is no option to toggle strict vs. best-effort parsing per call.

### 8. Test-coverage gaps in chemistry-critical code

No dedicated unit tests exist for several non-trivial, correctness-critical
classes — they are exercised only indirectly through integration tests:

- `StereoHandler`, `SugarProjectionDetector`, `ChemicalUtils`,
  `SgroupHandler`, `TextVisitor`, `BracketVisitor`, `ReactionStepVisitor`,
  and the lookup classes.

Additionally, JaCoCo has no minimum-coverage threshold, and the
static-analysis and CVE gates are advisory (`failOnViolation=false`, CVE
threshold set unreachably high), so quality can regress without a signal.

### 9. Contracted nicknames can carry overlapping coordinates

A contracted ChemDraw nickname (`Mes`, `Bn`, … shown as text, not as a
dictionary abbreviation) stores its member atoms in the file with whatever
geometry the template had. ChemDraw never draws them, so an orientation
pointing back over the visible structure is harmless there — but BChemXtract
expands the nickname and keeps those coordinates, so the group can land on
top of the parent structure. Atoms then share coordinates in the depiction
**and in the MDL V3000 output**; connectivity, formula, SMILES and InChI are
unaffected. Seen in 3 of 62 nickname-bearing fragments in the `mantis11158`
corpus (all in `20-2-i3.cdx`, where the mesityl ring's atoms fall onto the
drawn N and O). This is inherited from the source file and is not corrected
on read.

## Missing outright

- **Non-ChemDraw inputs** — no MOL/SDF or other ingest; the entry point is
  strictly ChemDraw `.cdx` / `.cdxml`.
- **Full CDX(ML) fidelity** — arrows, bio-shapes, colored areas, and link
  nodes are not faithfully preserved.
- **Robust reaction semantics** — atom-atom mapping, reaction roles, and
  conditions beyond the experimental heuristics.
- **Runtime configuration** — of safety limits and parser strictness.

## Reporting

Found a limitation not listed here, or a case that should work but does
not? Please open a
[GitHub issue](https://github.com/Beilstein-Institut/BChemXtract/issues/new/choose)
or email **[open-source@beilstein-institut.de](mailto:open-source@beilstein-institut.de)**.
