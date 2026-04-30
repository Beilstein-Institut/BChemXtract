<div align="center">

<img src="doc/BChemXtract_Full_logo.png" alt="BChemXtract" width="640">

### A pure-Java extractor of ChemDraw structures and reactions
*Originally developed for the Beilstein Diamond Open Access Journals.*

<br/>

[![Maven Central](https://img.shields.io/maven-central/v/org.beilstein/bchemxtract?style=flat-square&logo=apachemaven&logoColor=white&color=C71A36&label=Maven%20Central)](https://central.sonatype.com/artifact/org.beilstein/bchemxtract)
[![Latest Release](https://img.shields.io/github/v/release/Beilstein-Institut/BChemXtract?style=flat-square&logo=github&label=Latest%20Release&color=0A2540)](https://github.com/Beilstein-Institut/BChemXtract/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](https://opensource.org/licenses/MIT)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![CDK 2.12](https://img.shields.io/badge/CDK-2.12-008B8B?style=flat-square)](https://cdk.github.io/)

[![Tests](https://img.shields.io/github/actions/workflow/status/Beilstein-Institut/BChemXtract/test.yml?branch=main&style=flat-square&logo=github&label=Tests)](https://github.com/Beilstein-Institut/BChemXtract/actions/workflows/test.yml)
[![Lint](https://img.shields.io/github/actions/workflow/status/Beilstein-Institut/BChemXtract/lint.yml?branch=main&style=flat-square&logo=github&label=Lint)](https://github.com/Beilstein-Institut/BChemXtract/actions/workflows/lint.yml)
[![Security](https://img.shields.io/github/actions/workflow/status/Beilstein-Institut/BChemXtract/security.yml?branch=main&style=flat-square&logo=github&label=Security)](https://github.com/Beilstein-Institut/BChemXtract/actions/workflows/security.yml)
[![CodeQL](https://img.shields.io/github/actions/workflow/status/Beilstein-Institut/BChemXtract/codeql.yml?branch=main&style=flat-square&logo=github&label=CodeQL)](https://github.com/Beilstein-Institut/BChemXtract/actions/workflows/codeql.yml)
[![Coverage](https://img.shields.io/codecov/c/github/Beilstein-Institut/BChemXtract?style=flat-square&logo=codecov&logoColor=white&label=Coverage)](https://app.codecov.io/gh/Beilstein-Institut/BChemXtract)

[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-FE5196?style=flat-square&logo=conventionalcommits&logoColor=white)](https://conventionalcommits.org)
[![release-please](https://img.shields.io/badge/release--please-managed-2EA44F?style=flat-square&logo=googlecloud&logoColor=white)](https://github.com/googleapis/release-please)
[![Dependabot](https://img.shields.io/badge/Dependabot-enabled-025E8C?style=flat-square&logo=dependabot&logoColor=white)](.github/dependabot.yml)
[![FAIR](https://img.shields.io/badge/data-FAIR-3FAE9F?style=flat-square)](https://www.go-fair.org/fair-principles/)

[Documentation](#-documentation) ·
[Quick Start](#-quick-start) ·
[Install](#-installation) ·
[Contributing](CONTRIBUTING.md) ·
[Releasing](RELEASING.md)

</div>

---

## ✨ What is BChemXtract?

**BChemXtract** is an open-source, **pure-Java** parser for **ChemDraw** files
(both binary `.cdx` and XML `.cdxml`) that extracts and validates
chemical structures and reactions, enriching every structure with
**InChI / SMILES** descriptors (and **RInChI / reaction SMILES** for
reactions).

If you have a corpus of ChemDraw documents — published manuscripts,
internal SOPs, supporting information PDFs — BChemXtract turns them into
machine-readable, FAIR-aligned chemistry you can index, search, and
cite.

> **Maturity:** structure extraction is mature and battle-tested in
> Beilstein's Diamond Open Access publishing pipeline. Reaction
> extraction is **experimental** and under active development.

## 🔬 Features

- 🧩 **Pure-Java** — no native dependencies on the parsing path; runs anywhere a JVM does
- 📦 **Both formats** — binary `.cdx` and XML `.cdxml`
- ⚛️ **Structures** → atoms, bonds, stereo, charges, isotopes, rings
- 🔁 **Reactions** → reactants, products, agents, RInChI (experimental)
- 🧪 **Descriptors out of the box** — InChI, InChIKey, canonical SMILES, MDL V3000 mol
- 🅰️ **Markush support** — abbreviations and generic structures
- 🛡️ **Hard safety limits** — refuses to process pathologically large inputs
- 🖼️ **Depiction** — renders extracted structures as PNG via CDK
- 🧰 **Battle-tested CI** — lint, multi-JDK tests, JaCoCo coverage, Gitleaks, Trivy, OWASP Dependency-Check, CodeQL

## 🚀 Quick Start

```bash
# 1. Grab the standalone jar from the latest release
curl -L -o bchemxtract.jar \
  https://repo1.maven.org/maven2/org/beilstein/bchemxtract/1.1/bchemxtract-1.1-jar-with-dependencies.jar

# 2. Extract every structure in a CDX file as PNG into the current directory
java -jar bchemxtract.jar example.cdx
```

Or use the API from your own code:

```java
import org.beilstein.chemxtract.io.IOUtils;
import org.beilstein.chemxtract.cdx.CDXVisitor;

// Read a CDX or CDXML file and walk its object tree
var doc = IOUtils.readCDX("example.cdx");

// Extract structures, enriched with InChI / SMILES / mol coordinates
var structures = new CDXVisitor().extractStructures(doc);

structures.forEach(s -> {
    System.out.println(s.getInChI());
    System.out.println(s.getSMILES());
});
```

For end-to-end snippets see the [HOWTO page](doc/HOWTO.md).

## 📦 Installation

### Maven

```xml
<dependency>
  <groupId>org.beilstein</groupId>
  <artifactId>bchemxtract</artifactId>
  <version>1.1</version>
</dependency>
```

### Gradle

```kotlin
implementation("org.beilstein:bchemxtract:1.1")
```

### Standalone fat jar

Download the `*-jar-with-dependencies.jar` from the
[Releases page](https://github.com/Beilstein-Institut/BChemXtract/releases)
or from
[Maven Central](https://central.sonatype.com/artifact/org.beilstein/bchemxtract).

## 📚 Documentation

| Doc | Purpose |
|---|---|
| [`doc/CONCEPTS.md`](doc/CONCEPTS.md) | The architecture and parsing model behind BChemXtract |
| [`doc/HOWTO.md`](doc/HOWTO.md) | Code recipes — read a CDX, extract structures, render PNGs |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Local dev setup, Conventional Commits policy, CI overview |
| [`RELEASING.md`](RELEASING.md) | End-to-end release pipeline (release-please → Maven Central) |
| [`CHANGELOG.md`](CHANGELOG.md) | Release notes (auto-generated by release-please from v1.2.0+) |

### Reference: ChemDraw file specification

The implementation follows the historical ChemDraw file specification
originally hosted at `cambridgesoft.com`. The site is gone, but the
spec lives on:
- [IUPAC FAIRSpec mirror](https://iupac.github.io/IUPAC-FAIRSpec/cdx_sdk)
- [Wayback Machine snapshot](https://web.archive.org/web/20160225200221/http://www.cambridgesoft.com/services/documentation/sdk/chemdraw/cdx/)

## 🛠️ Building from source

Prerequisites: **JDK 17+** and **Maven 3.9+**.

```bash
git clone https://github.com/Beilstein-Institut/BChemXtract.git
cd BChemXtract
mvn -B clean package
```

Two artifacts land in `target/`:

| Artifact | Use |
|---|---|
| `bchemxtract-X.Y.Z.jar` | Slim jar — bundle with your own application |
| `bchemxtract-X.Y.Z-jar-with-dependencies.jar` | Fat jar — run standalone |

For the full quality sweep (Checkstyle, PMD, SpotBugs, OWASP Dependency-Check):

```bash
mvn -B -Pquality verify
```

## ⚙️ Continuous integration & release pipeline

| Workflow | Triggers | Purpose |
|---|---|---|
| [`lint.yml`](.github/workflows/lint.yml) | PR + push to `main` | Spotless · Checkstyle · PMD · SpotBugs · actionlint |
| [`test.yml`](.github/workflows/test.yml) | PR + push to `main` | `mvn verify` on JDK 17 + 21, JaCoCo → Codecov |
| [`security.yml`](.github/workflows/security.yml) | PR + push + weekly | Gitleaks · Trivy · OWASP Dependency-Check |
| [`codeql.yml`](.github/workflows/codeql.yml) | PR + push + weekly | GitHub CodeQL Java SAST |
| [`release-please.yml`](.github/workflows/release-please.yml) | push to `main` | Drafts release PRs from Conventional Commits |
| [`publish.yml`](.github/workflows/publish.yml) | GitHub Release published | GPG-signs and deploys to Maven Central |
| [`format.yml`](.github/workflows/format.yml) | manual | Applies Spotless and commits the diff |

The release flow is fully automated:

```
Conventional Commits on main
        ↓
release-please opens "Release vX.Y.Z" PR
        ↓
maintainer reviews + merges
        ↓
GitHub Release tagged → publish.yml fires → signs → ships to Maven Central
```

See [`RELEASING.md`](RELEASING.md) for the complete pipeline, including
the active GPG signing key fingerprint.

## 🤝 Contributing

We welcome contributions! Please read [`CONTRIBUTING.md`](CONTRIBUTING.md)
for local setup, coding standards, and the **Conventional Commits**
policy that drives our changelog.

In short:

1. [Fork](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/fork-a-repo) the repo
2. Create a feature branch off `main`
3. Add tests; run `mvn -B verify` locally
4. Open a [pull request](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request) with a Conventional Commit title

## 📖 Citing BChemXtract

If you use BChemXtract in published work, please cite:

```bibtex
@software{bchemxtract,
  author       = {Bänsch, Felix and Nietfeld, Markus and Reschel, Udo and others},
  title        = {{BChemXtract: a pure-Java extractor of ChemDraw structures and reactions}},
  organization = {Beilstein-Institut},
  year         = {2025},
  url          = {https://github.com/Beilstein-Institut/BChemXtract}
}
```

## 📜 License

Released under the **[MIT License](LICENSE)**. Use it freely in research,
software, education, or commercial applications.

## 🙏 Acknowledgments

This project is grounded in the open-science values of the
cheminformatics community and aligns with
[FAIR data principles](https://www.go-fair.org/fair-principles/).

BChemXtract makes extensive use of the
[Chemistry Development Kit (CDK)](https://cdk.github.io/) (LGPL-2.1).
Please cite the CDK papers below if you use BChemXtract:

> Willighagen *et al.* The Chemistry Development Kit (CDK) v2.0: atom
> typing, depiction, molecular formulas, and substructure searching.
> *J. Cheminform.* **2017**, 9(3). [doi:10.1186/s13321-017-0220-4](https://doi.org/10.1186/s13321-017-0220-4)
>
> May & Steinbeck. Efficient ring perception for the Chemistry
> Development Kit. *J. Cheminform.* **2014**.
> [doi:10.1186/1758-2946-6-3](https://doi.org/10.1186/1758-2946-6-3)
>
> Steinbeck *et al.* Recent Developments of the Chemistry Development
> Kit (CDK) — An Open-Source Java Library for Chemo- and Bioinformatics.
> *Curr. Pharm. Des.* **2006**, 12(17), 2111–2120.
> [doi:10.2174/138161206777585274](https://doi.org/10.2174/138161206777585274)
>
> Steinbeck *et al.* The Chemistry Development Kit (CDK): An Open-Source
> Java Library for Chemo- and Bioinformatics. *J. Chem. Inf. Comput.
> Sci.* **2003**, 43(2), 493–500.
> [doi:10.1021/ci025584y](https://doi.org/10.1021/ci025584y)

## 💬 Feedback

Open a [GitHub issue](https://github.com/Beilstein-Institut/BChemXtract/issues/new/choose)
or reach out to the maintainers at
**[open-source@beilstein-institut.de](mailto:open-source@beilstein-institut.de)**.

<div align="center">

---

Made with ⚗️ at the [Beilstein-Institut](https://www.beilstein-institut.de) ·
[GitHub](https://github.com/Beilstein-Institut) ·
[Diamond Open Access Journals](https://www.beilstein-journals.org/)

</div>
