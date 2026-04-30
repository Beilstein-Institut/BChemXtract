# Releasing BChemXtract

This document describes how a new version of `org.beilstein:bchemxtract` is
cut and published to Maven Central.

The release pipeline is fully automated via GitHub Actions:

```
Conventional Commits on main
        ↓
release-please opens "Release vX.Y.Z" PR
        ↓
maintainer reviews + merges PR
        ↓
release-please pushes tag vX.Y.Z + creates GitHub Release
        ↓
publish.yml builds, signs, deploys to Central Portal
        ↓
artifacts auto-published to https://repo1.maven.org/maven2/org/beilstein/bchemxtract/
```

---

## 1. Day-to-day: keep commits Conventional

Every commit landing on `main` must follow
[Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/):

| Prefix | Effect |
|--------|--------|
| `feat:` | Minor version bump (1.1.0 → 1.2.0) |
| `fix:` / `perf:` | Patch bump (1.1.0 → 1.1.1) |
| `feat!:` or `BREAKING CHANGE:` in body | Major bump (1.1.0 → 2.0.0) |
| `build:` `chore:` `ci:` `docs:` `refactor:` `style:` `test:` | No release; hidden from CHANGELOG |

The PR template enforces this via reviewer checklist.

---

## 2. Cutting a release

You don't cut releases manually. release-please watches `main` and:

1. Maintains an open PR titled `chore(main): release vX.Y.Z` containing:
   - Updated `<version>` in `pom.xml`
   - Updated `CHANGELOG.md` aggregating all conventional commits since the last release
   - Updated `.release-please-manifest.json`
2. When you merge that PR, release-please:
   - Pushes a `vX.Y.Z` git tag
   - Creates a GitHub Release with the changelog as the body

That GitHub Release event triggers `publish.yml`.

> Manually re-running: if `publish.yml` fails (e.g. transient Sonatype outage)
> you can re-run it from the **Actions** tab → **Publish to Maven Central** →
> *Run workflow* → enter the tag (e.g. `v1.2.0`).

---

## 3. Required GitHub Secrets

Configured under **Settings → Secrets and variables → Actions** at the repo
level (or via an environment named `maven-central` for the publish job).

| Secret | What it is | How to create |
|--------|-----------|---------------|
| `MAVEN_GPG_PRIVATE_KEY` | ASCII-armored release private key | `gpg --armor --export-secret-keys <KEY-ID>` |
| `MAVEN_GPG_PASSPHRASE`  | Passphrase for that key | — |
| `CENTRAL_USERNAME`      | Central Portal user-token name | central.sonatype.com → View Account → Generate User Token |
| `CENTRAL_PASSWORD`      | Central Portal user-token value | (same flow) |
| `RELEASE_BCHEMXTRACT`   | PAT used by release-please to open PRs | Fine-grained PAT with `contents:write` + `pull-requests:write` on this repo |
| `NVD_API_KEY` *(optional)* | Speeds up OWASP Dependency-Check | https://nvd.nist.gov/developers/request-an-api-key |

### Generating / preparing the GPG key

If you need to bootstrap a new release key (or rotate the current one):

```bash
# Generate a 4096-bit RSA key
gpg --full-generate-key

# Find its long ID
gpg --list-secret-keys --keyid-format LONG

# Publish the public key to a keyserver (Central requires it)
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY-ID>
gpg --keyserver keys.openpgp.org      --send-keys <KEY-ID>

# Export the private key for GitHub Actions
gpg --armor --export-secret-keys <KEY-ID> | pbcopy   # macOS
# Paste the value into the MAVEN_GPG_PRIVATE_KEY secret
```

The exported value MUST start with `-----BEGIN PGP PRIVATE KEY BLOCK-----`
and end with `-----END PGP PRIVATE KEY BLOCK-----`.

---

## 4. What `publish.yml` does

1. Checks out the release tag.
2. Sets up JDK 17 with a `~/.m2/settings.xml` containing
   `<server id="central">` populated from `CENTRAL_USERNAME` / `CENTRAL_PASSWORD`.
3. Imports the GPG key from `MAVEN_GPG_PRIVATE_KEY`.
4. Refuses to publish if the pom version still ends in `-SNAPSHOT`.
5. Runs `mvn -B -ntp -Prelease -DskipTests deploy` which:
   - Builds the main jar, sources jar, and javadoc jar
   - Signs every artifact with GPG (`maven-gpg-plugin`)
   - Uploads + auto-publishes via `central-publishing-maven-plugin`
     (`autoPublish=true`, `waitUntil=published`)

Within ~15 minutes the artifacts appear on
<https://repo1.maven.org/maven2/org/beilstein/bchemxtract/> and the
[Maven Central search index](https://central.sonatype.com/search?q=g:org.beilstein).

---

## 5. Manual hot-fix release (skipping release-please)

Only if release-please is unavailable:

```bash
# 1. Bump pom.xml version (remove -SNAPSHOT)
mvn versions:set -DnewVersion=1.2.1 -DgenerateBackupPoms=false

# 2. Commit, tag, push
git add pom.xml CHANGELOG.md
git commit -m "chore: release v1.2.1"
git tag v1.2.1
git push origin main v1.2.1

# 3. Create the GitHub Release manually (this triggers publish.yml)
gh release create v1.2.1 --generate-notes

# 4. Restore -SNAPSHOT for ongoing development
mvn versions:set -DnewVersion=1.2.2-SNAPSHOT -DgenerateBackupPoms=false
git commit -am "chore: bump to 1.2.2-SNAPSHOT"
git push origin main
```

---

## 6. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `publish.yml` fails with `401 Unauthorized` | Stale Central user token | Regenerate at central.sonatype.com, update `CENTRAL_USERNAME` / `CENTRAL_PASSWORD` |
| `gpg: signing failed: No secret key` | `MAVEN_GPG_PRIVATE_KEY` empty or malformed | Re-export with `--armor --export-secret-keys`; ensure the BEGIN/END lines are present |
| Artifacts sign but Central rejects with `Validation failed: missing javadoc` | Source/javadoc plugin skipped | Confirm `maven-source-plugin` and `maven-javadoc-plugin` ran (they're configured in default `<build>`) |
| release-please PR isn't appearing | No qualifying commits since last release, or commits don't follow Conventional Commits | Check commit log; add a `feat:` / `fix:` commit |
| publish completed but artifact not on Central | Central indexing lag (up to 30 min) | Wait; check status at central.sonatype.com → Deployments |
