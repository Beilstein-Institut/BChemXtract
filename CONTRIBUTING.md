# Contributing to BChemXtract

Thanks for your interest in improving BChemXtract. This document explains
how to set up a working environment, what we expect from a pull request,
and how the CI / release pipeline behaves.

## Quick start

```bash
git clone https://github.com/Beilstein-Institut/BChemXtract.git
cd BChemXtract
mvn -B verify          # compile + run all tests + spotless check
```

You need **JDK 17** (or 21) and **Maven 3.9+**.

## Workflow

1. **Fork** the repo and create a feature branch off `main`.
2. **Make changes** with tests. New behavior must be covered.
3. **Format** with Spotless before committing:
   ```bash
   mvn spotless:apply
   ```
4. **Run the full check locally**:
   ```bash
   mvn -B verify
   ```
   Optional but encouraged:
   ```bash
   mvn -B -Pquality verify   # adds Checkstyle, PMD, SpotBugs, OWASP Dependency-Check
   ```
5. **Open a PR** against `main`. The PR template will guide you.

## Conventional Commits — required

Every commit landing on `main` (and every PR title) MUST follow the
[Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
specification. release-please reads commit messages to drive versioning
and the CHANGELOG, so noisy or unconventional commits will be sent back.

| Prefix | Use for | Releases |
|--------|---------|----------|
| `feat:` | New feature | minor bump |
| `fix:` | Bug fix | patch bump |
| `perf:` | Performance change without behavior change | patch bump |
| `feat!:` or trailer `BREAKING CHANGE:` | Incompatible change | major bump |
| `build:` | Maven / dependency / packaging | hidden |
| `ci:` | GitHub Actions and other CI configs | hidden |
| `chore:` | Repo housekeeping | hidden |
| `docs:` | Documentation only | hidden |
| `refactor:` | Internal refactor | hidden |
| `style:` | Formatting only | hidden |
| `test:` | Tests only | hidden |

Examples:

```
feat: support ChemDraw 23 abbreviation table
fix: NPE in BCXReactionInfo when no products are present
feat!: drop Java 11 support

BREAKING CHANGE: minimum Java version is now 17.
```

## Continuous integration

Every PR runs the following checks:

| Workflow | What it does |
|----------|--------------|
| `lint.yml` | Spotless, Checkstyle, PMD, SpotBugs, actionlint |
| `test.yml` | `mvn -B verify` on JDK 17 + 21, JaCoCo coverage to Codecov |
| `security.yml` | Gitleaks (secret scan), Trivy (CVE scan), OWASP Dependency-Check |
| `codeql.yml` | GitHub CodeQL static analysis for Java |
| `release-please.yml` | Maintains the open release PR (no-op on contributor PRs) |

Findings from `security.yml` and `codeql.yml` are reported to the
**Security** tab and do not fail the PR. Lint and test failures **do**
block merge.

## Recommended branch protection (for maintainers)

Mark these checks as **required** on `main`:
- `Lint / Spotless`
- `Lint / Checkstyle`
- `Lint / PMD`
- `Lint / SpotBugs`
- `Lint / actionlint`
- `Test / JDK 17`
- `Test / JDK 21`
- `CodeQL / Analyze (Java)`

## Releases

You don't need to do anything special for a release — just merge PRs with
Conventional Commits. A maintainer will merge the release-please PR when
the time comes, which triggers automated publication to Maven Central.

See [RELEASING.md](./RELEASING.md) for the full pipeline.

## Questions?

Open a [discussion](https://github.com/Beilstein-Institut/BChemXtract/discussions)
or email **open-source@beilstein-institut.de**.
