#!/usr/bin/env bash
set -euo pipefail

# Install git hooks from .githooks directory
# Usage: ./scripts/install-hooks.sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
GITHOOKS_DIR="$ROOT_DIR/.githooks"
GIT_HOOKS_DIR="$ROOT_DIR/.git/hooks"

if [ ! -d "$GITHOOKS_DIR" ]; then
  echo "Error: .githooks directory not found"
  exit 1
fi

echo "Installing git hooks..."

for hook in "$GITHOOKS_DIR"/*; do
  if [ -f "$hook" ] && [ -x "$hook" ]; then
    hook_name=$(basename "$hook")
    target="$GIT_HOOKS_DIR/$hook_name"
    
    # Copy hook and make it executable
    cp "$hook" "$target"
    chmod +x "$target"
    
    echo "  ✓ Installed $hook_name"
  fi
done

echo "Git hooks installed successfully!"
echo ""
