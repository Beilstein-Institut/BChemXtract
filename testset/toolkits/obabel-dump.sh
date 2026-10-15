#!/usr/bin/env bash
# Dump Open Babel's canonical SMILES for every .cdx file, in the layout pair_structures.py
# reads (file, smiles, inchikey; the key is left empty and computed there).
# Usage: testset/toolkits/obabel-dump.sh CORPUS_DIR OUT.tsv
set -uo pipefail
corpus="$1"; out="$2"
printf 'file\tsmiles\tinchikey\n' >"$out"
(cd "$corpus" && find . -name '*.cdx' | sed 's#^\./##' | sort) | while read -r f; do
  timeout 120 obabel "$corpus/$f" -ocan 2>/dev/null | cut -f1 | grep . | sed "s#^#$f\t#; s#\$#\t#" >>"$out"
done
