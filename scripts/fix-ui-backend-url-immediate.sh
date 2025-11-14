#!/usr/bin/env bash
set -euo pipefail

# Immediate fix script that rebuilds and redeploys the UI with correct backend URL
# This fixes the backend URL issue by injecting it into index.html during build
RESOURCE_GROUP="rg-ecompoc-shared-westus3"
UI_APP_NAME="app-ecompoc-dev-ui"
BACKEND_APP_NAME="app-ecompoc-dev-backend"

BACKEND_URL="https://${BACKEND_APP_NAME}.azurewebsites.net/api"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
UI_DIR="${ROOT_DIR}/ui"
UI_DIST_DIR="${UI_DIR}/dist"

info() {
  echo "[INFO] $*" >&2
}

error() {
  echo "[ERROR] $*" >&2
}

if ! command -v az >/dev/null; then
  error "Azure CLI (az) is required"
  exit 1
fi

if ! command -v npm >/dev/null; then
  error "npm is required"
  exit 1
fi

info "Building UI with backend URL injection..."
cd "${UI_DIR}"

# Install dependencies if needed
if [[ ! -d "node_modules" ]]; then
  info "Installing UI dependencies..."
  npm ci
fi

# Build UI
info "Building UI bundle..."
npm run build

# Inject backend URL into index.html
info "Injecting backend URL (${BACKEND_URL}) into UI build..."
INDEX_HTML="${UI_DIST_DIR}/index.html"
if [[ -f "${INDEX_HTML}" ]]; then
  # Remove any existing __APP_CONFIG__ script
  if [[ "$OSTYPE" == "darwin"* ]]; then
    sed -i '' '/<script>.*window\.__APP_CONFIG__.*<\/script>/d' "${INDEX_HTML}"
    sed -i '' "s|</head>|<script>window.__APP_CONFIG__={VITE_API_BASE_URL:'${BACKEND_URL}'};</script></head>|" "${INDEX_HTML}"
  else
    sed -i '/<script>.*window\.__APP_CONFIG__.*<\/script>/d' "${INDEX_HTML}"
    sed -i "s|</head>|<script>window.__APP_CONFIG__={VITE_API_BASE_URL:'${BACKEND_URL}'};</script></head>|" "${INDEX_HTML}"
  fi
  info "Backend URL injected successfully"
else
  error "index.html not found in build output"
  exit 1
fi

# Package UI
info "Packaging UI for deployment..."
TEMP_ZIP=$(mktemp).zip
cd "${UI_DIST_DIR}"
zip -qr "${TEMP_ZIP}" .

# Deploy UI
info "Deploying updated UI to ${UI_APP_NAME}..."
az webapp deploy \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${UI_APP_NAME}" \
  --type zip \
  --src-path "${TEMP_ZIP}" \
  --clean true \
  --output none

rm -f "${TEMP_ZIP}"

# Set environment variable (for Docker deployments)
info "Setting VITE_API_BASE_URL environment variable..."
az webapp config appsettings set \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${UI_APP_NAME}" \
  --settings "VITE_API_BASE_URL=${BACKEND_URL}" \
  --output none

# Restart app
info "Restarting ${UI_APP_NAME}..."
az webapp restart \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${UI_APP_NAME}" \
  --output none

info ""
info "✓ Fix applied successfully!"
info "The UI should now make requests to: ${BACKEND_URL}"
info "Please wait 1-2 minutes for the app to restart, then hard refresh your browser (Ctrl+Shift+R or Cmd+Shift+R)"

