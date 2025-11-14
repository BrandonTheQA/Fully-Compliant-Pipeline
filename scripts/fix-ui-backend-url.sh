#!/usr/bin/env bash
set -euo pipefail

# Quick fix script to update the UI App Service backend URL configuration
RESOURCE_GROUP="ecompoc-appservice-group"
UI_APP_NAME="app-ecompoc-dev-ui"
BACKEND_APP_NAME="app-ecompoc-dev-backend"

BACKEND_URL="https://${BACKEND_APP_NAME}.azurewebsites.net/api"

info() {
  echo "[INFO] $*" >&2
}

if ! command -v az >/dev/null; then
  echo "Azure CLI (az) is required" >&2
  exit 1
fi

info "Setting VITE_API_BASE_URL for ${UI_APP_NAME} to ${BACKEND_URL}"
az webapp config appsettings set \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${UI_APP_NAME}" \
  --settings "VITE_API_BASE_URL=${BACKEND_URL}" \
  --output none

info "Restarting ${UI_APP_NAME} to apply changes"
az webapp restart \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${UI_APP_NAME}" \
  --output none

info "Fix applied successfully!"
info "The UI should now make requests to: ${BACKEND_URL}"

