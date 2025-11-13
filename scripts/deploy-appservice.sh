#!/usr/bin/env bash
set -euo pipefail

# Configuration
RESOURCE_GROUP="AKS-POC_group"
APP_SERVICE_PLAN="ASP-AKSPOCgroup-8f3c"
ECOMPOC_APP_NAME="aks-poc-ecompoc"
UI_APP_NAME="aks-poc-ui"
ECOMPOC_RUNTIME="JAVA:17-java17"
UI_RUNTIME="NODE:20-lts"
ECOMPOC_STARTUP="startup.sh"
UI_STARTUP="pm2 serve /home/site/wwwroot --no-daemon --spa"
UI_PORT="8080"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

ECOMPOC_PROJECT_DIR="${ROOT_DIR}/api/services/ecompoc"
ECOMPOC_JAR="${ECOMPOC_PROJECT_DIR}/target/ecompoc-service-1.0.0.jar"
UI_DIR="${ROOT_DIR}/ui"
UI_DIST_DIR="${UI_DIR}/dist"

info() {
  echo "[INFO] $*" >&2
}

ensure_ecompoc_artifact() {
  if [[ -f "${ECOMPOC_JAR}" ]]; then
    info "Reusing existing ecompoc artifact at ${ECOMPOC_JAR}"
    return
  fi

  info "Building ecompoc service (skip tests)"
  (cd "${ECOMPOC_PROJECT_DIR}" && mvn package -DskipTests)
}

package_ecompoc() {
  ensure_ecompoc_artifact
  local ecompoc_zip
  ecompoc_zip="$(mktemp)"
  local startup_script="${SCRIPT_DIR}/startup.sh"
  info "Packaging ecompoc jar and startup script into ${ecompoc_zip}"
  # Create a temporary directory for packaging
  local temp_dir
  temp_dir="$(mktemp -d)"
  cp "${ECOMPOC_JAR}" "${temp_dir}/ecompoc-service-1.0.0.jar"
  if [[ -f "${startup_script}" ]]; then
    cp "${startup_script}" "${temp_dir}/startup.sh"
    chmod +x "${temp_dir}/startup.sh"
  fi
  (cd "${temp_dir}" && zip -r "${ecompoc_zip}" . >/dev/null)
  rm -rf "${temp_dir}"
  printf '%s' "${ecompoc_zip}"
}

ensure_ui_build() {
  if [[ -d "${UI_DIST_DIR}" && -n "$(ls -A "${UI_DIST_DIR}" 2>/dev/null)" ]]; then
    info "Reusing existing UI build at ${UI_DIST_DIR}"
  else
    info "Installing UI dependencies"
    (cd "${UI_DIR}" && npm ci)
    info "Building UI bundle"
    (cd "${UI_DIR}" && npm run build)
  fi
}

package_ui() {
  ensure_ui_build
  local ui_zip
  ui_zip="$(mktemp)"
  info "Packaging UI assets into ${ui_zip}"
  (cd "${UI_DIST_DIR}" && zip -r "${ui_zip}" . >/dev/null)
  printf '%s' "${ui_zip}"
}

ensure_webapp() {
  local app_name="$1"
  local runtime="$2"

  if az webapp show --resource-group "${RESOURCE_GROUP}" --name "${app_name}" &>/dev/null; then
    info "Web app ${app_name} already exists"
  else
    info "Creating web app ${app_name}"
    az webapp create \
      --resource-group "${RESOURCE_GROUP}" \
      --plan "${APP_SERVICE_PLAN}" \
      --name "${app_name}" \
      --runtime "${runtime}" \
      --os-type Linux >/dev/null
  fi

  info "Ensuring ${app_name} runtime stack is ${runtime}"
  az webapp config set \
    --resource-group "${RESOURCE_GROUP}" \
    --name "${app_name}" \
    --linux-fx-version "${runtime}" >/dev/null
}

deploy_ecompoc() {
  ensure_webapp "${ECOMPOC_APP_NAME}" "${ECOMPOC_RUNTIME}"
  local ecompoc_zip
  ecompoc_zip="$(package_ecompoc)"

  info "Deploying ecompoc package to ${ECOMPOC_APP_NAME}"
  az webapp deploy \
    --resource-group "${RESOURCE_GROUP}" \
    --name "${ECOMPOC_APP_NAME}" \
    --type zip \
    --src-path "${ecompoc_zip}" >/dev/null

  rm -f "${ecompoc_zip}"

  info "Setting startup command for ${ECOMPOC_APP_NAME}"
  az webapp config set \
    --resource-group "${RESOURCE_GROUP}" \
    --name "${ECOMPOC_APP_NAME}" \
    --startup-file "${ECOMPOC_STARTUP}" >/dev/null

  info "Restarting ${ECOMPOC_APP_NAME}"
  az webapp restart \
    --resource-group "${RESOURCE_GROUP}" \
    --name "${ECOMPOC_APP_NAME}" >/dev/null
}

deploy_ui() {
  ensure_webapp "${UI_APP_NAME}" "${UI_RUNTIME}"
  local ui_zip
  ui_zip="$(package_ui)"

  info "Deploying UI assets to ${UI_APP_NAME}"
  az webapp deploy \
    --resource-group "${RESOURCE_GROUP}" \
    --name "${UI_APP_NAME}" \
    --type zip \
    --src-path "${ui_zip}" >/dev/null

  rm -f "${ui_zip}"

  info "Configuring startup command for ${UI_APP_NAME}"
  az webapp config set \
    --resource-group "${RESOURCE_GROUP}" \
    --name "${UI_APP_NAME}" \
    --startup-file "${UI_STARTUP}" >/dev/null

  info "Setting port configuration for ${UI_APP_NAME}"
  az webapp config appsettings set \
    --resource-group "${RESOURCE_GROUP}" \
    --name "${UI_APP_NAME}" \
    --settings "WEBSITES_PORT=${UI_PORT}" >/dev/null

  info "Restarting ${UI_APP_NAME}"
  az webapp restart \
    --resource-group "${RESOURCE_GROUP}" \
    --name "${UI_APP_NAME}" >/dev/null
}

main() {
  if ! command -v az >/dev/null; then
    echo "Azure CLI (az) is required" >&2
    exit 1
  fi

  info "Deploying ecompoc service"
  deploy_ecompoc

  info "Deploying UI application"
  deploy_ui

  info "Deployment complete"
  info "eComPOC URL: https://${ECOMPOC_APP_NAME}.azurewebsites.net"
  info "UI URL: https://${UI_APP_NAME}.azurewebsites.net"
}

main "$@"
