#!/usr/bin/env bash
set -euo pipefail

# Script to run Selenium E2E tests against local services
# This script checks if services are running, starts them if needed, and runs the tests

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SELENIUM_DIR="$SCRIPT_DIR"

# Service ports
API_PORT=8080
UI_PORT=8084

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

check_port() {
    local port="$1"
    nc -z localhost "$port" >/dev/null 2>&1
}

check_services_running() {
    local all_running=true
    
    if ! check_port "$API_PORT"; then
        echo -e "${YELLOW}API service (:$API_PORT) is not running${NC}"
        all_running=false
    fi
    
    if ! check_port "$UI_PORT"; then
        echo -e "${YELLOW}UI service (:$UI_PORT) is not running${NC}"
        all_running=false
    fi
    
    if [ "$all_running" = true ]; then
        return 0
    else
        return 1
    fi
}

wait_for_port() {
    local port="$1"
    local name="$2"
    local tries=40
    
    echo "Waiting for $name to be ready on :$port..."
    for i in $(seq 1 "$tries"); do
        if check_port "$port"; then
            echo -e "${GREEN}$name is ready on :$port${NC}"
            return 0
        fi
        sleep 1
    done
    
    echo -e "${RED}Timed out waiting for $name on :$port${NC}" >&2
    return 1
}

echo "========================================="
echo "Selenium E2E Tests - Local Execution"
echo "========================================="

# Check if services are already running
if ! check_services_running; then
    echo -e "${YELLOW}Some services are not running. Please start them using:${NC}"
    echo -e "${YELLOW}  ./scripts/run-local-e2e.sh${NC}"
    echo -e "${YELLOW}Then run the Selenium tests in another terminal:${NC}"
    echo -e "${YELLOW}  cd selenium && ./run-selenium-tests.sh${NC}"
    exit 1
fi

echo -e "${GREEN}All services are running${NC}"

# Check if Java 17 is available
if ! command -v java >/dev/null 2>&1; then
    echo -e "${RED}Java is not installed. Please install Java 17.${NC}" >&2
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo "Java version: $JAVA_VERSION"

# Check if Maven is available
if ! command -v mvn >/dev/null 2>&1; then
    echo -e "${RED}Maven is not installed. Please install Maven.${NC}" >&2
    exit 1
fi

MVN_VERSION=$(mvn -version | head -n 1)
echo "Maven version: $MVN_VERSION"
echo ""

# Run the Selenium tests
echo "========================================="
echo "Running Selenium E2E Tests"
echo "========================================="
echo "Base URL: http://localhost:$UI_PORT"
echo ""

cd "$SELENIUM_DIR"

# Run Maven test with BASE_URL environment variable
BASE_URL="http://localhost:$UI_PORT" mvn clean test

# Capture exit code
TEST_EXIT_CODE=$?

echo ""
echo "========================================="
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ All tests passed!${NC}"
else
    echo -e "${RED}❌ Tests failed with exit code: $TEST_EXIT_CODE${NC}"
fi
echo "========================================="

exit $TEST_EXIT_CODE
