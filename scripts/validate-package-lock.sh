#!/usr/bin/env bash
set -euo pipefail

# Validate that package-lock.json is in sync with package.json
# This prevents npm ci failures in CI/CD pipelines
# Usage: ./scripts/validate-package-lock.sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PACKAGE_JSON="$REPO_ROOT/ui/package.json"
PACKAGE_LOCK="$REPO_ROOT/ui/package-lock.json"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

if [ ! -f "$PACKAGE_JSON" ]; then
  echo -e "${RED}Error: package.json not found at $PACKAGE_JSON${NC}"
  exit 1
fi

if [ ! -f "$PACKAGE_LOCK" ]; then
  echo -e "${RED}Error: package-lock.json not found at $PACKAGE_LOCK${NC}"
  echo -e "${YELLOW}Run 'npm install' in the ui/ directory to generate package-lock.json${NC}"
  exit 1
fi

echo -e "${YELLOW}Validating package-lock.json is in sync with package.json...${NC}"

# Check if npm is available
if ! command -v npm >/dev/null 2>&1; then
  echo -e "${RED}Error: npm is not installed${NC}"
  exit 1
fi

# Use npm ci with --dry-run to check if package-lock.json is in sync
# This is the same check that CI/CD pipelines will perform
cd "$REPO_ROOT/ui"
if npm ci --dry-run >/dev/null 2>&1; then
  echo -e "${GREEN}✓ package-lock.json is in sync with package.json${NC}"
  exit 0
else
  echo -e "${RED}✗ package-lock.json is out of sync with package.json${NC}"
  echo ""
  echo -e "${YELLOW}To fix this, run:${NC}"
  echo -e "  ${YELLOW}cd ui && npm install${NC}"
  echo ""
  echo -e "${YELLOW}Then commit the updated package-lock.json file${NC}"
  exit 1
fi
