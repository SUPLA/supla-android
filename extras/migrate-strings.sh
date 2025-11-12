#!/usr/bin/env bash
# migrate_strings.sh
# Moves selected translations from Supla Android to Supla iOS App.
# Moves only existing languages. New translations are added to the end
# of iOS translations file

if [ $# -ne 1 ]; then
  echo "Usage: $0 <list_of_keys_comma_separated>"
  exit 1
fi

if [ -z "$SUPLA_ANDROID" ]; then
  echo "❌ The Android application path is unset. Use export 'SUPLA_ANDROID=\"/your/path\"' to set it"
  exit 1
fi
if [ -z "$SUPLA_IOS" ]; then
  echo "❌ The iOS application path is unset. 'Use export SUPLA_IOS=\"/your/path\"' to set it"
  exit 1
fi

KEYS_STRING=$1
IFS=',' read -r -a KEYS_ARRAY <<< "$KEYS_STRING"

ANDROID_RES="$SUPLA_ANDROID/app/src/main/res"
IOS_RES="$SUPLA_IOS/SUPLA/Resources"

if [ ! -d "$ANDROID_RES" ]; then
  echo "❌ Android resources folder not found: $ANDROID_RES"
  exit 1
fi

echo "📋 Keys to copy: ${#KEYS_ARRAY[@]}"
echo ""

parse_strings_xml() {
  local file="$1"
  grep '<string name=' "$file" | sed -E 's/.*name="([^"]+)".*>(.*)<\/string>/\1=\2/' | sed 's/\\/\\\\/g; s/"/\\"/g'
}

find "$ANDROID_RES" -type f -name "strings.xml" | while read -r XML_FILE; do
  DIR=$(dirname "$XML_FILE")
  BASE=$(basename "$DIR")
  
  LANG="en"
  if [ "$BASE" != "values" ]; then
    SUFFIX=${BASE#values-}
    LANG=${SUFFIX%%-*}
  fi

  if [ "$LANG" = "en" ]; then
    IOS_FILE="$IOS_RES/Default.strings"
  elif [ "$LANG" = "pt" ]; then
    IOS_FILE="$IOS_RES/pt-PT.lproj/Localizable.strings"
  elif [ "$LANG" = "sk" ]; then
    IOS_FILE="$IOS_RES/sk-SK.lproj/Localizable.strings"
  else
    IOS_FILE="$IOS_RES/$LANG.lproj/Localizable.strings"
  fi

  if [ ! -f "$IOS_FILE" ]; then
    echo "⤷ SKIPPED $LANG: destination file does not exist -> $IOS_FILE"
    continue
  fi

  parse_strings_xml "$XML_FILE" | while IFS='=' read -r key value; do
    for K in "${KEYS_ARRAY[@]}"; do
      if [ "$key" = "$K" ]; then
        echo "\"$key\" = \"$value\";" >> "$IOS_FILE"
        echo "✅ Added $key to $IOS_FILE"
      fi
    done
  done
done

echo "📌 Migration finished."

