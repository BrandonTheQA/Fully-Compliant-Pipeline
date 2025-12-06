#!/usr/bin/env bash
set -euo pipefail

# Starts the UI production server for Selenium tests
# This uses the built production files instead of Vite dev server
# which works better with headless Chrome

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
UI_DIR="$ROOT_DIR/ui"

UI_PORT="${1:-8084}"

echo "Building UI for production..."
cd "$UI_DIR"
npm run build

echo "Starting UI production server on port $UI_PORT..."
PORT="$UI_PORT" BACKEND_URL="http://localhost:8080" node server.js
