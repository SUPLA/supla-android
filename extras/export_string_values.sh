#!/usr/bin/env bash
set -euo pipefail

# ---- config / args ----
if [[ -z "${SUPLA_ANDROID:-}" ]]; then
  echo "Environment variable SUPLA_ANDROID is not set." >&2
  echo "Example: export SUPLA_ANDROID=/path/to/your/android/project" >&2
  exit 1
fi

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <lang_code>" >&2
  echo "Example: $0 az" >&2
  exit 1
fi

LANG_CODE="$1"

BASE="$SUPLA_ANDROID/app/src/main/res"
EN_FILE="$BASE/values/strings.xml"
LANG_FILE="$BASE/values-$LANG_CODE/strings.xml"

# ---- checks ----
if [[ ! -f "$EN_FILE" ]]; then
  echo "EN file not found: $EN_FILE" >&2
  exit 1
fi
if [[ ! -f "$LANG_FILE" ]]; then
  echo "Language file not found: $LANG_FILE" >&2
  echo "Looked for values-$LANG_CODE/strings.xml under: $BASE" >&2
  exit 1
fi

command -v xmlstarlet >/dev/null 2>&1 || {
  echo "xmlstarlet not found. Install it first:" >&2
  echo "  macOS:  brew install xmlstarlet" >&2
  echo "  Ubuntu: sudo apt-get install xmlstarlet" >&2
  exit 1
}

# ---- helpers ----
extract_strings() {
  local file="$1"
  # key<TAB>value z <string name="...">...</string>
  xmlstarlet sel -t \
    -m '/resources/string[not(@translatable="false")]' \
    -v '@name' -o $'\t' -v 'normalize-space(.)' -n \
    "$file"
}

# Escaping do bezpiecznego wypisania w cudzysłowie:
# zamienia \ na \\ i " na \"
escape_quotes() {
  sed -e 's/\\/\\\\/g' -e 's/"/\\"/g'
}

# ---- main ----
EN_TMP="$(mktemp)"
LANG_TMP="$(mktemp)"
trap 'rm -f "$EN_TMP" "$LANG_TMP"' EXIT

extract_strings "$EN_FILE"   | sort -k1,1 > "$EN_TMP"
extract_strings "$LANG_FILE" | sort -k1,1 > "$LANG_TMP"

awk -F'\t' '
  NR==FNR { lang[$1]=$2; next }
  {
    key=$1; en=$2
    lv = (key in lang) ? lang[key] : "—MISSING—"
    print en "\t" lv
  }
' "$LANG_TMP" "$EN_TMP" \
| while IFS=$'\t' read -r en_val lang_val; do
    en_esc=$(printf "%s" "$en_val"   | escape_quotes)
    lg_esc=$(printf "%s" "$lang_val" | escape_quotes)
    printf "\"%s\": \"%s\"\n" "$en_esc" "$lg_esc"
  done
