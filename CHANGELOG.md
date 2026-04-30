# Changelog

> **Note:** All commits to this repository should follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification (e.g. `feat:`, `fix:`, `build:`, `chore:`). This keeps the changelog accurate and enables automated tooling.

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