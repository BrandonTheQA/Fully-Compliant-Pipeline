#!/usr/bin/env bash
set -euo pipefail

# Validate that all npm dependencies in ui/package.json are pinned to exact versions
# This prevents security vulnerabilities from automatic dependency updates
# Usage: ./scripts/validate-dependencies.sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PACKAGE_JSON="$REPO_ROOT/ui/package.json"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

if [ ! -f "$PACKAGE_JSON" ]; then
  echo -e "${RED}Error: package.json not found at $PACKAGE_JSON${NC}"
  exit 1
fi

echo -e "${YELLOW}Validating npm dependencies are pinned to exact versions...${NC}"

# Check for version ranges (^ or ~) in package.json
# This regex matches version strings that start with ^ or ~
UNPINNED_DEPS=$(grep -E '":\s*"[~^]' "$PACKAGE_JSON" || true)

if [ -n "$UNPINNED_DEPS" ]; then
  echo -e "${RED}✗ Found unpinned dependencies (using ^ or ~ version ranges):${NC}"
  echo ""
  echo "$UNPINNED_DEPS" | while IFS= read -r line; do
    # Extract package name and version
    if echo "$line" | grep -qE '":\s*"[~^]'; then
      echo -e "  ${RED}${line}${NC}"
    fi
  done
  echo ""
  echo -e "${RED}All dependencies must be pinned to exact versions for security.${NC}"
  echo -e "${YELLOW}Remove ^ and ~ prefixes from version numbers in ui/package.json${NC}"
  echo -e "${YELLOW}Use exact versions matching what's in package-lock.json${NC}"
  echo ""
  echo -e "${YELLOW}Example:${NC}"
  echo -e "  ${RED}❌ \"react\": \"^19.1.1\"${NC}"
  echo -e "  ${GREEN}✓ \"react\": \"19.2.0\"${NC}"
  exit 1
fi

echo -e "${GREEN}✓ All dependencies are pinned to exact versions${NC}"
exit 0
