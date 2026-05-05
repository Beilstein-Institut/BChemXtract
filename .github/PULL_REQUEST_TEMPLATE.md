<!--
Thank you for contributing to BChemXtract!

PR titles MUST follow Conventional Commits — release-please uses them
to drive versioning and the CHANGELOG. Examples:
  feat: add support for ChemDraw 23 abbreviations
  fix: NPE in BCXReactionInfo when no products
  feat!: drop Java 11 support     (the `!` marks a breaking change)

Allowed types: feat, fix, perf, revert, build, chore, ci, docs, refactor,
                style, test
-->

## Summary

<!-- 1–3 bullets describing the change and the motivation. -->

## Type of change

- [ ] feat — new feature (minor bump)
- [ ] fix — bug fix (patch bump)
- [ ] perf — performance improvement (patch bump)
- [ ] BREAKING CHANGE — incompatible change (`!` in title or footer; major bump)
- [ ] refactor / docs / chore / build / ci / test (no release)

## Checklist

- [ ] PR title follows Conventional Commits
- [ ] `mvn -B verify` passes locally
- [ ] `mvn spotless:apply` was run (or `mvn spotless:check` is clean)
- [ ] New behavior is covered by tests
- [ ] Public API changes are documented (Javadoc + README/HOWTO if applicable)

## Related issues

<!-- Closes #123, refs #456, etc. -->
