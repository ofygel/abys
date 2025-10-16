#!/usr/bin/env bash
set -euo pipefail

CLI_VERSION="${ANDROID_SDK_CLI_VERSION:-9477386}"
SDK_ROOT="${ANDROID_SDK_ROOT:-$PWD/.android-sdk}"
CMDLINE_TOOLS_DIR="$SDK_ROOT/cmdline-tools"
LATEST_DIR="$CMDLINE_TOOLS_DIR/latest"

mkdir -p "$SDK_ROOT"

if [ ! -x "$LATEST_DIR/bin/sdkmanager" ]; then
  ARCHIVE="commandlinetools-linux-${CLI_VERSION}_latest.zip"
  URL="https://dl.google.com/android/repository/${ARCHIVE}"
  TMP_DIR="$(mktemp -d)"
  cleanup() {
    rm -rf "$TMP_DIR"
  }
  trap cleanup EXIT

  echo "Downloading Android command line tools ${CLI_VERSION}..."
  curl -L "$URL" -o "$TMP_DIR/$ARCHIVE"

  echo "Unpacking command line tools..."
  rm -rf "$LATEST_DIR"
  mkdir -p "$CMDLINE_TOOLS_DIR"
  unzip -q "$TMP_DIR/$ARCHIVE" -d "$TMP_DIR"
  mv "$TMP_DIR/cmdline-tools" "$LATEST_DIR"
fi

SDKMANAGER="$LATEST_DIR/bin/sdkmanager"

if [ ! -x "$SDKMANAGER" ]; then
  echo "sdkmanager not found at $SDKMANAGER" >&2
  exit 1
fi

export ANDROID_SDK_ROOT="$SDK_ROOT"
PACKAGES=(
  "platform-tools"
  "platforms;android-34"
  "build-tools;34.0.0"
)

run_sdkmanager() {
  set +o pipefail
  yes | "$SDKMANAGER" --sdk_root="$SDK_ROOT" "$@"
  local status=$?
  set -o pipefail
  return $status
}

for package in "${PACKAGES[@]}"; do
  echo "Installing $package..."
  run_sdkmanager "$package"

done

set +o pipefail
yes | "$SDKMANAGER" --sdk_root="$SDK_ROOT" --licenses > /dev/null
set -o pipefail

echo "Android SDK installed at $SDK_ROOT"
