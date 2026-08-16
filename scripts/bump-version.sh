#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <VERSION_NAME> <VERSION_CODE>"
  echo "Example: $0 2.1.2 20102"
  exit 2
fi

VERSION_NAME="$1"
VERSION_CODE="$2"
FILE="$(cd "$(dirname "$0")/.." && pwd)/version.properties"

if ! [[ "$VERSION_NAME" =~ ^[0-9]+\.[0-9]+\.[0-9]+([.-][0-9A-Za-z.-]+)?$ ]]; then
  echo "Invalid VERSION_NAME: $VERSION_NAME"
  exit 2
fi
if ! [[ "$VERSION_CODE" =~ ^[0-9]+$ ]]; then
  echo "VERSION_CODE must be a positive integer"
  exit 2
fi

OLD_CODE="$(grep '^VERSION_CODE=' "$FILE" | cut -d= -f2-)"
if [ "$VERSION_CODE" -le "$OLD_CODE" ]; then
  echo "VERSION_CODE must increase. Current value: $OLD_CODE"
  exit 2
fi

cat > "$FILE" <<EOV
VERSION_NAME=$VERSION_NAME
VERSION_CODE=$VERSION_CODE
EOV

echo "Updated version.properties -> $VERSION_NAME ($VERSION_CODE)"
