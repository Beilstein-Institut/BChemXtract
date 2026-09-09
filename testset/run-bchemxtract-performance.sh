#!/usr/bin/env bash
#
# Build (if needed) and run a BChemXtract extraction-performance overview over
# every .cdx file in a directory (default: testset/corpus).
#
# Usage:
#   testset/run-bchemxtract-performance.sh [corpus-dir]
#   FORCE_BUILD=1 testset/run-bchemxtract-performance.sh   # rebuild the fat jar first
#
set -euo pipefail

# Repo root = parent of this script's directory.
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

RESOURCE_DIR="${1:-testset/corpus}"
MAIN_CLASS="org.beilstein.chemxtract.samples.BCXTractPerformance"

# Locate the newest jar-with-dependencies, building one if absent or forced.
jar() { ls -t target/*-jar-with-dependencies.jar 2>/dev/null | head -1; }

if [[ "${FORCE_BUILD:-0}" == "1" || -z "$(jar)" ]]; then
  echo ">> Building fat jar (mvn -q -DskipTests package) ..."
  mvn -q -DskipTests package
fi

FAT_JAR="$(jar)"
if [[ -z "$FAT_JAR" ]]; then
  echo "No fat jar found in target/. Run with FORCE_BUILD=1 or 'mvn package'." >&2
  exit 1
fi

echo ">> Using $FAT_JAR"
exec java -cp "$FAT_JAR" "$MAIN_CLASS" "$RESOURCE_DIR"
