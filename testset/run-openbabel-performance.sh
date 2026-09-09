#!/usr/bin/env bash
#
# Open Babel counterpart to run-bchemxtract-performance.sh, made comparable to BChemXtract's
# `xtractUnique` by deduplicating per file on InChIKey.
#
# For each .cdx file under a directory (default: testset/corpus) it:
#   1. converts to canonical SMILES (one structure per line),
#   2. deduplicates those structures,
#   3. resolves an InChIKey for each (Open Babel's InChI generation fails on
#      structures containing `*` -- e.g. unexpanded ChemDraw aliases or
#      reaction wildcards), and
#   4. counts UNIQUE structures = distinct InChIKeys + structures that have no
#      InChI, deduplicated on canonical SMILES (a SMILES fallback that mirrors
#      BChemXtract dropping to SMILES when InChI is unavailable).
#
# Reported timing covers the canonical-SMILES + InChIKey Open Babel calls.
#
# Usage:
#   testset/run-openbabel-performance.sh [corpus-dir]
#
# Requires Open Babel on PATH (`obabel`).
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

RESOURCE_DIR="${1:-testset/corpus}"

if ! command -v obabel >/dev/null 2>&1; then
  echo "obabel not found on PATH. Install Open Babel first." >&2
  exit 1
fi
if [[ ! -d "$RESOURCE_DIR" ]]; then
  echo "Not a directory: $RESOURCE_DIR" >&2
  exit 1
fi

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
CAN="$TMP/can"     # canonical SMILES output
USMI="$TMP/usmi"   # unique canonical SMILES
IKS="$TMP/iks"     # resolved InChIKeys
ERR="$TMP/err"
# Records: "<millis>\t<unique>\t<rawMols>\t<viaInchi>\t<viaSmiles>\t<wildcards>\t<status>\t<relpath>\t<error>"
REC="$TMP/records"
: >"$REC"

mapfile -d '' FILES < <(find "$RESOURCE_DIR" -type f -iname '*.cdx' -print0 | sort -z)

echo "Scanning ${#FILES[@]} .cdx files under $(cd "$RESOURCE_DIR" && pwd) using $(obabel -V 2>&1)"
echo "Dedup: per-file InChIKey, with canonical-SMILES fallback where InChI is unavailable."
echo

for f in "${FILES[@]}"; do
  rel="${f#"$RESOURCE_DIR"/}"
  start=$(date +%s%3N)

  rc=0
  obabel "$f" -ocan >"$CAN" 2>"$ERR" || rc=$?

  # Raw structures and the unique-by-canonical-SMILES set.
  cut -f1 "$CAN" | grep '[^[:space:]]' >"$TMP/all" || true
  raw=$(grep -c '[^[:space:]]' "$TMP/all")
  sort -u "$TMP/all" >"$USMI"
  u=$(grep -c '[^[:space:]]' "$USMI")

  # Unique structures still carrying a wildcard atom (unexpanded ChemDraw alias
  # or reaction placeholder).
  wildcards=$(grep -c '\*' "$USMI" || true)

  # InChIKeys for the unique structures (failures are silently dropped).
  resolved=0
  distinct=0
  if [[ "$u" -gt 0 ]]; then
    obabel -ismi "$USMI" -oinchikey 2>/dev/null | grep '[^[:space:]]' >"$IKS" || true
    resolved=$(grep -c '[^[:space:]]' "$IKS")
    distinct=$(sort -u "$IKS" | grep -c '[^[:space:]]')
  fi

  end=$(date +%s%3N)
  ms=$((end - start))

  # unique = distinct InChIKeys + structures with no InChI (deduped on SMILES)
  via_inchi=$distinct
  via_smiles=$((u - resolved))
  unique=$((via_inchi + via_smiles))

  status="ok"
  errmsg=""
  if [[ $rc -ne 0 ]] || grep -q "Open Babel Error" "$ERR"; then
    status="error"
    errmsg="$(grep -A1 "Open Babel Error" "$ERR" | tail -1 | sed 's/^ *//;s/ *$//')"
    [[ -z "$errmsg" ]] && errmsg="exit code $rc"
    unique=0
    via_inchi=0
    via_smiles=0
    wildcards=0
  elif [[ "$unique" -eq 0 ]]; then
    status="empty"
  fi

  printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' \
    "$ms" "$unique" "$raw" "$via_inchi" "$via_smiles" "$wildcards" "$status" "$rel" "$errmsg" >>"$REC"
done

awk -F'\t' '
  {
    total++
    ms[NR]=$1; uniq[NR]=$2; status[NR]=$7; file[NR]=$8; err[NR]=$9
    totalMs += $1
    if ($7 == "ok") {
      ok++
      totalUnique  += $2
      totalViaInchi  += $4
      totalViaSmiles += $5
      totalWildcard  += $6
    }
    if ($7 == "empty") { empty++ }
    if ($7 == "error") { errors++ }
    totalRaw += $3
  }
  END {
    printf "=============== EXTRACTION PERFORMANCE (Open Babel, InChIKey dedup) ===============\n"
    printf "Files processed       : %d\n", total
    printf "  with substances     : %d\n", ok
    printf "  empty (0 found)     : %d\n", empty
    printf "  errors              : %d\n", errors
    printf "Unique substances     : %d\n", totalUnique
    printf "  with real InChI     : %d\n", totalViaInchi
    printf "  SMILES fallback     : %d\n", totalViaSmiles
    printf "  unexpanded alias (*): %d\n", totalWildcard
    printf "  fully expanded      : %d\n", totalUnique - totalWildcard
    printf "Raw structures        : %d (before dedup)\n", totalRaw
    printf "Total time            : %d ms (%.2f s)\n", totalMs, totalMs/1000.0
    if (total > 0)   printf "Average per file      : %.1f ms\n", totalMs/total
    if (totalMs > 0) printf "Throughput            : %.1f files/s\n", total*1000.0/totalMs
    printf "\n"

    printf "---- 10 slowest files ----\n"
    for (p = 0; p < 10 && p < total; p++) {
      best = -1
      for (i = 1; i <= total; i++) if (!used[i] && (best == -1 || ms[i] > ms[best])) best = i
      if (best == -1) break
      used[best] = 1
      printf "  %6d ms  subs=%-3d  %s\n", ms[best], uniq[best], file[best]
    }
    printf "\n"

    if (errors > 0) {
      printf "---- errors ----\n"
      for (i = 1; i <= total; i++) if (status[i] == "error") printf "  %s\n      -> %s\n", file[i], err[i]
      printf "\n"
    }
    if (empty > 0) {
      printf "---- files with no substances extracted ----\n"
      for (i = 1; i <= total; i++) if (status[i] == "empty") printf "  %s\n", file[i]
      printf "\n"
    }
    printf "==================================================================================\n"
  }
' "$REC"
