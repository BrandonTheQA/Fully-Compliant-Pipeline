#!/usr/bin/env bash
set -euo pipefail

# Quick script to run specific Selenium tests before committing
# This runs a quick smoke test to verify basic functionality
# Usage: ./scripts/run-selenium-tests-quick.sh [BASE_URL]
#   BASE_URL defaults to http://localhost:8084

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SELENIUM_DIR="$ROOT_DIR/selenium"

# Default to localhost UI port
BASE_URL="${1:-http://localhost:8084}"
UI_PORT="${BASE_URL##*:}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================="
echo "Quick Selenium Test Check"
echo "========================================="
echo "Running: E2EWorkflowTest (smoke test)"
echo "Base URL: $BASE_URL"
echo ""

# Check if services are running
check_port() {
    local port="$1"
    if command -v nc >/dev/null 2>&1; then
        nc -z localhost "$port" >/dev/null 2>&1
    elif command -v lsof >/dev/null 2>&1; then
        lsof -ti tcp:"$port" >/dev/null 2>&1
    else
        # Try curl as fallback
        curl -sf "http://localhost:$port" >/dev/null 2>&1
    fi
}

if ! check_port "$UI_PORT"; then
    echo -e "${YELLOW}⚠ Warning: UI service (:$UI_PORT) does not appear to be running${NC}"
    echo -e "${YELLOW}  Tests may fail. Start services with:${NC}"
    echo -e "${YELLOW}    ./scripts/run-local-e2e.sh${NC}"
    echo ""
    read -p "Continue anyway? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Check if Java and Maven are available
if ! command -v java >/dev/null 2>&1; then
    echo -e "${RED}✗ Java is not installed. Please install Java 17.${NC}" >&2
    exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
    echo -e "${RED}✗ Maven is not installed. Please install Maven.${NC}" >&2
    exit 1
fi

cd "$SELENIUM_DIR"

# Run the E2E workflow test as a smoke test
echo "Running tests..."
BASE_URL="$BASE_URL" mvn clean test -Dtest=E2EWorkflowTest

# Capture exit code
TEST_EXIT_CODE=$?

echo ""
echo "========================================="
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ E2E Workflow Test passed!${NC}"
else
    echo -e "${RED}❌ E2E Workflow Test failed${NC}"
    echo -e "${YELLOW}Fix the issues before committing.${NC}"
fi
echo "========================================="

exit $TEST_EXIT_CODE




