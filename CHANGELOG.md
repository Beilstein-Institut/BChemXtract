# Changelog

> **Note:** All commits to this repository should follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification (e.g. `feat:`, `fix:`, `build:`, `chore:`). This keeps the changelog accurate and enables automated tooling.

## [1.4.3](https://github.com/Beilstein-Institut/BChemXtract/compare/v1.4.2...v1.4.3) (2026-10-16)


### Bug Fixes

* read spelled-out ortho/meta/para as ring positions in R-group values ([13333ac](https://github.com/Beilstein-Institut/BChemXtract/commit/13333ac4c675ad5ac83c44000139afc49be9ced3))

## [1.4.2](https://github.com/Beilstein-Institut/BChemXtract/compare/v1.4.1...v1.4.2) (2026-10-13)


### ⚠ BREAKING CHANGES

* resolveRGroups extraction now yields fewer substances for legends that vary two or more R-groups at once; set unrestrictedMarkush to keep the previous output.

### Features

* expand R-group legends only where they state each structure ([f5628ec](https://github.com/Beilstein-Institut/BChemXtract/commit/f5628ec480f566c63afa742fcbae822f16e228f1))


### Bug Fixes

* bound position-variation isomer enumeration ([137591d](https://github.com/Beilstein-Institut/BChemXtract/commit/137591ddbb145cabbc80500eed4a1f7f7dbea54e))
* derive stereo from ChemDraw CIP labels by ligand priority ([770d2f2](https://github.com/Beilstein-Institut/BChemXtract/commit/770d2f2f949234faa647af714c9c448d076b9996))
* drop the colon a compound number leaves on a legend value ([54b3abc](https://github.com/Beilstein-Institut/BChemXtract/commit/54b3abceb2210c3bb9e9e3c53f1c3e2752b7ee16))
* fall back to canonical SMILES when canonical numbering fails ([43a467e](https://github.com/Beilstein-Institut/BChemXtract/commit/43a467e31ef2c97063361636b55c053a54b7633f))
* give each scaffold its own legend table when tables sit between scaffolds ([7717598](https://github.com/Beilstein-Institut/BChemXtract/commit/7717598ae42cf0dd4d005527f4467b23b842a635))
* guard label and multiplier parsing against NumberFormatException ([3f31da9](https://github.com/Beilstein-Institut/BChemXtract/commit/3f31da9be158a1d499eccdd98427b351e103056c))
* ignore crossing bonds a position-variation bond never reaches ([087a366](https://github.com/Beilstein-Institut/BChemXtract/commit/087a366b44e27741a5a5df522e0b0d21b1938c6d))
* keep stacked legend tables apart unless a line apart ([0de1bca](https://github.com/Beilstein-Institut/BChemXtract/commit/0de1bca8277f83c6b30034224b23637a5bcf22e4))
* keep the bond attaching an unwanted abbreviation ([b63cb9b](https://github.com/Beilstein-Institut/BChemXtract/commit/b63cb9b7d21ec569b4dd691742d42da20f36e787))
* keep wedges outside sugar rings and read bold-edged Haworth rings ([210ec61](https://github.com/Beilstein-Institut/BChemXtract/commit/210ec61872ec1106022b43930a920171e4037a75))
* lay out abbreviations whose connection atom has no fixed neighbour ([619b81a](https://github.com/Beilstein-Institut/BChemXtract/commit/619b81a640649e8ab37e3e37b2556f02de95099b))
* leave overlapping drawings out of position-variation merging ([3d1a58f](https://github.com/Beilstein-Institut/BChemXtract/commit/3d1a58f9e957efe8bcba8d37a2beda1da2454c75)), closes [#143](https://github.com/Beilstein-Institut/BChemXtract/issues/143)
* leave the parsed document unchanged by extraction ([5c4e157](https://github.com/Beilstein-Institut/BChemXtract/commit/5c4e157734962d1c6877561921d8415de82d6e81))
* let a legend table over all of a scaffold's labels win over one over some ([2c38a33](https://github.com/Beilstein-Institut/BChemXtract/commit/2c38a33dbd32bb97e0ece4f9d22ae5afe1bcbd00))
* merge a positional table split over side-by-side text boxes ([d7043fa](https://github.com/Beilstein-Institut/BChemXtract/commit/d7043fa2d49edf68413173b454fd4ab999b52100))
* move a position-variation substituent to its scaffold only once ([76ce799](https://github.com/Beilstein-Institut/BChemXtract/commit/76ce799f059e9725cbc85f6ae75ce368591d9a66))
* number fused ring systems for positional R-group substituents ([a3ac78f](https://github.com/Beilstein-Institut/BChemXtract/commit/a3ac78fdb92fc20d1308e8a7c7d164989b9ee5d3))
* pick the 3D stereo factory only when every atom has 3D coordinates ([5737cdf](https://github.com/Beilstein-Institut/BChemXtract/commit/5737cdf4e4f4d00f77697f57336534e0ef3a0144)), closes [#146](https://github.com/Beilstein-Institut/BChemXtract/issues/146)
* place ortho/meta substituents on the side of the ring with room ([9d78cde](https://github.com/Beilstein-Institut/BChemXtract/commit/9d78cde277a8b780dba928c8e8572925f6352dfa))
* prefer a legend level with the scaffold over one off its corner ([3dd2c9c](https://github.com/Beilstein-Institut/BChemXtract/commit/3dd2c9c4fcc84ab33c94b6839f0e7590122e42c9))
* read a position-variation stub ending in a ring as any free ring position ([30b6ae1](https://github.com/Beilstein-Institut/BChemXtract/commit/30b6ae106acf72d3822ed11bb17c905d09311d7d))
* read numbered atom labels as atoms, not element formulas ([4561266](https://github.com/Beilstein-Institut/BChemXtract/commit/4561266cd8aa0250b4f84b5257937f526e9b1556))
* read o-Cl-Ph-style R-group values as substituted phenyl groups ([ae37c85](https://github.com/Beilstein-Institut/BChemXtract/commit/ae37c85cbfdf037c1283d8b06ee5bdf20f1bf0c3))
* read R'' and R''' labels and label lists sharing one value ([935f4cf](https://github.com/Beilstein-Institut/BChemXtract/commit/935f4cfda27dff2ed0e4bf9434c357703a28b20c))
* read substituted phenyls written as C6H4 or with the substituent after Ph ([1146f35](https://github.com/Beilstein-Institut/BChemXtract/commit/1146f3539f5d159349b07bd90bbcc19ad14827e0))
* resolve locant-prefixed aryl substituents in either spelling ([c812e25](https://github.com/Beilstein-Institut/BChemXtract/commit/c812e25b467ba036de3a86a410ab99483aeca4a1)), closes [#165](https://github.com/Beilstein-Institut/BChemXtract/issues/165)
* scope position-variation candidates to the merged scaffold ([268c3c4](https://github.com/Beilstein-Institut/BChemXtract/commit/268c3c4fad293498b53a93486a3d2ea17f3da4c0)), closes [#143](https://github.com/Beilstein-Institut/BChemXtract/issues/143)
* skip positional R-group legends on scaffolds whose R is off a ring ([e399ff3](https://github.com/Beilstein-Institut/BChemXtract/commit/e399ff308303ff4ca226ebebdeb15b2b701829a9))
* stop CIP ranking from hanging on polycyclic cages ([81c03c5](https://github.com/Beilstein-Institut/BChemXtract/commit/81c03c59081478b282ce1bf8c0002a244970531a))
* substitute every locant a multi-locant value names ([6f57c74](https://github.com/Beilstein-Institut/BChemXtract/commit/6f57c748826d018769c9d5bd5a15f5c7cd99fcf4))


### Performance Improvements

* build a Markush assignment once per attachment it reaches ([d3f0760](https://github.com/Beilstein-Institut/BChemXtract/commit/d3f07607e37e4baaf1ae336957a63116510c31b1))
* compare atom coordinates as rounded numbers instead of formatted strings ([0cd9068](https://github.com/Beilstein-Institut/BChemXtract/commit/0cd9068ac9b1da8379e76480202caccab7d45f20))
* extract with the silent CDK builder by default ([9d125bf](https://github.com/Beilstein-Institut/BChemXtract/commit/9d125bfcb34118c9241a41539b46bb4ebc43b847))
* prune Markush substituent combinations during enumeration ([1fa34e6](https://github.com/Beilstein-Institut/BChemXtract/commit/1fa34e6a29aa6cc6131aeca736f1eced717b601d)), closes [#142](https://github.com/Beilstein-Institut/BChemXtract/issues/142)
* skip grafting the choices that cannot fail when pruning Markush combinations ([37948ea](https://github.com/Beilstein-Institut/BChemXtract/commit/37948ead145b179a9a24414154ddb7ae0cca23f0))


### Continuous Integration

* run release-please on release/1.4.x ([ecb6b06](https://github.com/Beilstein-Institut/BChemXtract/commit/ecb6b06a0ace110cc5c31bfe65b9be7fded87667))


### Documentation

* drop ChemicalUtils from the untested classes in LIMITATIONS ([d8fe454](https://github.com/Beilstein-Institut/BChemXtract/commit/d8fe45471094baa9bc0f0902a1545900bf51bd86))
* drop StereoHandler from the untested classes in LIMITATIONS ([7be342d](https://github.com/Beilstein-Institut/BChemXtract/commit/7be342d5c8e3bd8c5be8b6d1679cc9aad30f09ae))
* note that the Haworth alignment threshold mirrors CDK ([45a5d1c](https://github.com/Beilstein-Institut/BChemXtract/commit/45a5d1cc1f04036ca88e67ef4dc3a57b3d22043a))
* state that extraction consumes the parsed document ([9b26723](https://github.com/Beilstein-Institut/BChemXtract/commit/9b26723f5f86cde682130f71a335b0c80589e1cb))

## [1.4.1](https://github.com/Beilstein-Institut/BChemXtract/compare/v1.4.0...v1.4.1) (2026-08-21)


### Bug Fixes

* keep both attachments when substituting a bivalent Markush residue ([f14345e](https://github.com/Beilstein-Institut/BChemXtract/commit/f14345e804e261eff4eda53906e6ec7d3669488e))
* keep both attachments when substituting a bivalent Markush residue ([27a0d11](https://github.com/Beilstein-Institut/BChemXtract/commit/27a0d1128570e51e65266aa1a41442556f9f13f0))

## [1.4.0](https://github.com/Beilstein-Institut/BChemXtract/compare/v1.3.1...v1.4.0) (2026-08-18)


### Features

* flag substances originating from Markush R-group expansion ([bba5513](https://github.com/Beilstein-Institut/BChemXtract/commit/bba55136357e7a20e5ce68e79e6175538deb7b99))
* flag substances originating from Markush R-group expansion ([09dd0e1](https://github.com/Beilstein-Institut/BChemXtract/commit/09dd0e107c9be0856de5f16935ef294ea33fb576))
* positional aryl substituents, legend column merging, and a LIMITATIONS doc ([5f8efa8](https://github.com/Beilstein-Institut/BChemXtract/commit/5f8efa8dba7ff27323723f0ae389424e7efaadb2))
* read ortho/meta/para prefixes as ring positions in R-group legends ([c7d6714](https://github.com/Beilstein-Institut/BChemXtract/commit/c7d6714668184123956e9487bfcab3ca722c267d))
* resolve positional aryl-substituent notation in R-group legends ([eae3d60](https://github.com/Beilstein-Institut/BChemXtract/commit/eae3d60ba7aa485c0cd2719b0f422048b2a00547))


### Bug Fixes

* keep scaffold when R-group expansion yields no structures ([7198a0d](https://github.com/Beilstein-Institut/BChemXtract/commit/7198a0d14db322340520798bbd34aca7aa2e5755))
* keep the R label when a position-variation stub is drawn from the residue end ([73699fc](https://github.com/Beilstein-Institut/BChemXtract/commit/73699fcf8f9bb09a30c57e46c7267920c590aa45))
* merge side-by-side legend columns into one R-group list ([d924b36](https://github.com/Beilstein-Institut/BChemXtract/commit/d924b368051d5f91515099d6d972b7efc84493b2))


### Documentation

* add LIMITATIONS.md ([185423f](https://github.com/Beilstein-Institut/BChemXtract/commit/185423f98b395a177b80e4b5335c13e39192b711))
* record the o/m/p prefixes and the residue-end stub fix in LIMITATIONS.md ([a080527](https://github.com/Beilstein-Institut/BChemXtract/commit/a080527abff99f0926110f9c12e1256eb0a71567))

## [1.3.1](https://github.com/Beilstein-Institut/BChemXtract/compare/v1.3.0...v1.3.1) (2026-08-13)


### Bug Fixes

* drop and log partially-resolved R-group combinations ([2d60703](https://github.com/Beilstein-Institut/BChemXtract/commit/2d60703d16a902772c21f226bf9cd3b12fb32438))
* drop leaked reaction-condition annotations from R-group legends ([aa994ad](https://github.com/Beilstein-Institut/BChemXtract/commit/aa994add23d8169d892c6bfeb5baf12b7ef7a2b2))
* lay out grafted R-group atoms in the generator's native scale ([1b1ed6a](https://github.com/Beilstein-Institut/BChemXtract/commit/1b1ed6aa4bc73b7fe890030f8ccacb66c977c733))
* Markush R-group extraction fixes (batch) ([95d81b9](https://github.com/Beilstein-Institut/BChemXtract/commit/95d81b9386b85664359a7af8f00b85f63c84048b))
* merge column legends and skip unresolved-placeholder structures ([49edee7](https://github.com/Beilstein-Institut/BChemXtract/commit/49edee7fe31a3182a3b2fe7bceb8bb0357199ed8))
* recognise primed R-group labels (R') as distinct from R ([dd14b87](https://github.com/Beilstein-Institut/BChemXtract/commit/dd14b87979ddc84064cc4a1874e92e1902b2e282))
* register bond-encoded (CrossingBonds) variable attachments ([b9277cc](https://github.com/Beilstein-Institut/BChemXtract/commit/b9277cc5418474230ee937aee68d4aafd7827fef))
* resolve bare non-organic-subset element R-groups (Se, Te) ([3ca6eed](https://github.com/Beilstein-Institut/BChemXtract/commit/3ca6eedbd439a38cf1b32c6a33d92fa0f81cfc81))
* resolve single-line correlated X/Y Markush tables ([dce27aa](https://github.com/Beilstein-Institut/BChemXtract/commit/dce27aab47b4fd5da1b843860fdd1b954cca166c))
* strip reaction-yield annotations from R-group definitions ([ecc39e0](https://github.com/Beilstein-Institut/BChemXtract/commit/ecc39e0786a606a7b5c9b226792b8cd02fdebf37))

## [1.3.0](https://github.com/Beilstein-Institut/BChemXtract/compare/v1.2.0...v1.3.0) (2026-08-10)


### Features

* improve Markush R-group extraction ([f611d6a](https://github.com/Beilstein-Institut/BChemXtract/commit/f611d6a411579f46c6b035714dc2ad8be802afff))
* improve Markush R-group extraction ([419327a](https://github.com/Beilstein-Institut/BChemXtract/commit/419327a07e7bf838dd8d935692b019ece4364efe))


### Bug Fixes

* preserve tetrahedral stereo in MDL V3000 output ([276d37d](https://github.com/Beilstein-Institut/BChemXtract/commit/276d37df080a10828b43124f662b0c0d49389ab5))
* preserve tetrahedral stereo in MDL V3000 output ([e68d0f2](https://github.com/Beilstein-Institut/BChemXtract/commit/e68d0f2bb47b5bacf1ba7b075afed6f4d87bcfe8))

## [1.2.0](https://github.com/Beilstein-Institut/BChemXtract/compare/v1.1.3...v1.2.0) (2026-07-13)


### Features

* add partial 2D layout for expanded abbreviations ([3da7fef](https://github.com/Beilstein-Institut/BChemXtract/commit/3da7fef7ebb479ccfa0635a4f3a51e6495a1bae6))
* **ci:** make Checkstyle, PMD, SpotBugs and OWASP gates blocking ([7fef2bd](https://github.com/Beilstein-Institut/BChemXtract/commit/7fef2bdd04ec3f28ff11d63526005a2ae03454b8))
* handle multi-center and variable attachment points ([faa288c](https://github.com/Beilstein-Institut/BChemXtract/commit/faa288c6dc7b72d6713f9a5a0e1a60bb8ca305e5))
* handle multi-center and variable attachment points ([827cf4b](https://github.com/Beilstein-Institut/BChemXtract/commit/827cf4bed3cce1cc9559d984b1e278e9eaf0136d))
* lay out expanded abbreviation coordinates ([9a68eaf](https://github.com/Beilstein-Institut/BChemXtract/commit/9a68eaf0a2392347564d2b1d9ee7c51c7825d75c))
* lay out expanded abbreviation coordinates during conversion ([c435dfe](https://github.com/Beilstein-Institut/BChemXtract/commit/c435dfeffb9b7fa3aad242bc40e566262c7106b5))
* **logging:** depend only on slf4j-api; make Log4j backend optional ([ea05991](https://github.com/Beilstein-Institut/BChemXtract/commit/ea05991cff2653c92e1a5d9ec9026295d33d054f))


### Bug Fixes

* **logging:** use log4j-slf4j2-impl so SLF4J 2 finds a provider ([821f821](https://github.com/Beilstein-Institut/BChemXtract/commit/821f8217846210c642b943220cbd708ec6913d6e))
* resolve SpotBugs findings - encodings, resource leaks, map iteration, static exposure ([40530a0](https://github.com/Beilstein-Institut/BChemXtract/commit/40530a0b295b0bf2232b011183c5c8e213e39bd6))

## [1.1.3](https://github.com/Beilstein-Institut/BChemXtract/compare/v1.1.2...v1.1.3) (2026-06-03)


### Bug Fixes

* align InChI generation boundary with SMILES at 500 atoms ([aba0033](https://github.com/Beilstein-Institut/BChemXtract/commit/aba0033f3c9d9f172af215a1cadc523a1fde6182))
* align InChI generation boundary with SMILES at 500 atoms ([5cbf6b7](https://github.com/Beilstein-Institut/BChemXtract/commit/5cbf6b73140ecbc8bad5ac162331693639da34f1))

## [1.1.2](https://github.com/Beilstein-Institut/BChemXtract/compare/v1.1.1...v1.1.2) (2026-05-08)


### Bug Fixes

* set IBond.Display.Solid for double bonds and remove undetermined stereo patches ([0e8cec7](https://github.com/Beilstein-Institut/BChemXtract/commit/0e8cec700a9f65cd5c5c464ea48e3801e794bb7d))
* set IBond.Display.Solid for double bonds and remove undetermined… ([0a4b15e](https://github.com/Beilstein-Institut/BChemXtract/commit/0a4b15ef7fd18870361ac17496399717f8c3bc68))

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
