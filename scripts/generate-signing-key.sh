#!/usr/bin/env bash
set -euo pipefail

KEYSTORE="${1:-flashcard-release.jks}"
ALIAS="${2:-flashcard}"

if [ -e "$KEYSTORE" ]; then
  echo "Refusing to overwrite existing signing key: $KEYSTORE"
  echo "Use the existing permanent key for updates. Replacing it would break update compatibility."
  exit 1
fi

echo "This creates the permanent Android release signing key."
echo "Keep the .jks private and back it up in at least two secure locations."
echo
keytool -genkeypair -v \
  -keystore "$KEYSTORE" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000

echo
echo "Signing certificate fingerprint (record this for future verification):"
keytool -list -v -keystore "$KEYSTORE" -alias "$ALIAS" | grep -E 'SHA256:|SHA-256:' || true

echo
echo "Base64 value for GitHub secret ANDROID_KEYSTORE_BASE64:"
base64 < "$KEYSTORE" | tr -d '\n'
echo
echo
echo "Also create these GitHub Actions secrets:"
echo "ANDROID_KEYSTORE_PASSWORD"
echo "ANDROID_KEY_ALIAS=$ALIAS"
echo "ANDROID_KEY_PASSWORD"
echo
echo "IMPORTANT: Never generate a different key for a later release."
