# Changelog

> **Note:** All commits to this repository should follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification (e.g. `feat:`, `fix:`, `build:`, `chore:`). This keeps the changelog accurate and enables automated tooling.

## [1.1.1](https://github.com/Beilstein-Institut/BChemXtract/compare/v1.1.0...v1.1.1) (2026-05-06)


### Bug Fixes

* **ci:** pin aquasecurity/trivy-action to v0.36.0 (missing v prefix) ([248ff40](https://github.com/Beilstein-Institut/BChemXtract/commit/248ff400fa51bacb0cda6a6dc3a7147e62fe575d))
* **ci:** repair workflow failures surfaced by first PR run ([def3ae7](https://github.com/Beilstein-Institut/BChemXtract/commit/def3ae7fe7cd9212d509917439bff65fcec8ea7f))
* **codeowners:** use individual maintainers instead of nonexistent team ([2a66831](https://github.com/Beilstein-Institut/BChemXtract/commit/2a6683162e115639fedd429eb21fea2ba9b44513))
* **javadoc:** correct stale [@param](https://github.com/param) tag in SubstanceXtractor.xtractSubstances ([c6704bb](https://github.com/Beilstein-Institut/BChemXtract/commit/c6704bb220e258193eefe235dd0013fc45d76856))
* map Bold and Hash bond displays to WedgeBegin and WedgedHashBegin ([99c043c](https://github.com/Beilstein-Institut/BChemXtract/commit/99c043cc8f0eab8023f572d355f52570db1e4bd6))
* suppress double bond stereochemistry for single-atom label abbreviations ([8cbed9e](https://github.com/Beilstein-Institut/BChemXtract/commit/8cbed9e3df1e594ea83dbbcf6d12094b0a1fd32f))


### Documentation

* add CITATION.cff and align README citation with author ORCIDs ([9037c31](https://github.com/Beilstein-Institut/BChemXtract/commit/9037c31598e7d2681c2b4f9c40f8def17a6e70ca))
* add RELEASING.md and CONTRIBUTING.md ([f490ba4](https://github.com/Beilstein-Institut/BChemXtract/commit/f490ba443a3dd9aff1e35367ac6ff2830e52c23e))
* **releasing:** record current release signing key fingerprint ([230f9e3](https://github.com/Beilstein-Institut/BChemXtract/commit/230f9e33c8e2ad1e9c81108397a5bef3a4c0ab5c))
* revamp README with logo, badges, and structured layout ([4ce72c2](https://github.com/Beilstein-Institut/BChemXtract/commit/4ce72c2382afaf8a806044357435399c79714aab))
* update README example to reflect current API ([87899c4](https://github.com/Beilstein-Institut/BChemXtract/commit/87899c49b1b6d2658b618d53d8a56e0377889c80))
* update README example to reflect current API ([adb8dc0](https://github.com/Beilstein-Institut/BChemXtract/commit/adb8dc05f684f204e0375c7006e7bbbdd71f6000))

## [v1.1] – 2026-04-29

### New Features

- feat: **Markush support** – added helper methods, adapted regex, and repositioned handler initialisation for correct Markush structure processing ([#29](https://github.com/Beilstein-Institut/BChemXtract/pull/29))
- feat: **BCXReactionInfo** – introduced `BCXReactionInfo` object and exposed it as a parameter in `ReactionXtractor` ([#28](https://github.com/Beilstein-Institut/BChemXtract/pull/28))
- feat: **Unwanted abbreviations** – added functionality to filter/exclude unwanted abbreviations during extraction ([#3](https://github.com/Beilstein-Institut/BChemXtract/pull/3), [#27](https://github.com/Beilstein-Institut/BChemXtract/pull/27))
- feat: **Reaction sanitizing** – reactions that contain no reactants or no products are now skipped instead of producing malformed output ([#22](https://github.com/Beilstein-Institut/BChemXtract/pull/22))
- feat: **Safety limit for large structures** – added a hard safety block that prevents processing of excessively large structures ([#31](https://github.com/Beilstein-Institut/BChemXtract/pull/31))

### Bug Fixes

- fix: undetermined double bonds in ChemDraw abbreviation structures ([#30](https://github.com/Beilstein-Institut/BChemXtract/pull/30))
- fix: NPE in reaction extraction path ([#10](https://github.com/Beilstein-Institut/BChemXtract/pull/10))
- fix: missing attachment points; added MDL V3000 mol file support ([#4](https://github.com/Beilstein-Institut/BChemXtract/pull/4))
- fix: missing implicit hydrogens on extracted structures
- fix: line break handling bug in ChemDraw abbreviations ([#21](https://github.com/Beilstein-Institut/BChemXtract/pull/21))
- fix: bounding-box bounds for internal fragments when bounds were `null` ([#9](https://github.com/Beilstein-Institut/BChemXtract/pull/9))
- fix: serialisation of coloured mol areas to XML (issue #12, [#16](https://github.com/Beilstein-Institut/BChemXtract/pull/16))
- fix: issue #13 ([#15](https://github.com/Beilstein-Institut/BChemXtract/pull/15))
- fix: issue #8 ([#9](https://github.com/Beilstein-Institut/BChemXtract/pull/9))
- fix: issues introduced by CDK 2.12 update ([#20](https://github.com/Beilstein-Institut/BChemXtract/pull/20))
- fix: added catch for `ArrayIndexOutOfBoundsException` in abbreviation processing

### Improvements

- chore: added new abbreviation mappings for ChemDraw agent structures ([#21](https://github.com/Beilstein-Institut/BChemXtract/pull/21))
- chore: made `CDXChunk.color` mutable (added setter)
- chore: added private constructor to `IOUtils` to prevent instantiation

### Dependency Updates

- build: Updated CDK to **2.12** ([#20](https://github.com/Beilstein-Institut/BChemXtract/pull/20))
- build: Bumped `log4j-core` from 2.25.2 → 2.25.3 ([#5](https://github.com/Beilstein-Institut/BChemXtract/pull/5))
- build: Bumped `log4j-web` and `log4j-slf4j-impl` to latest ([#19](https://github.com/Beilstein-Institut/BChemXtract/pull/19))
- build: Bumped `assertj-core` from 3.11.1 → 3.27.7 ([#6](https://github.com/Beilstein-Institut/BChemXtract/pull/6))

---

## [v1.0] – 2025-10-31

Initial public release.
