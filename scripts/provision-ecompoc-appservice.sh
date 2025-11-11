#!/usr/bin/env bash
set -euo pipefail

#
# Provision Azure App Service plans for the ecomPOC application.
# - Creates/updates the shared resource group in the specified region.
# - Ensures six Linux App Service plans exist (backend/ui for dev, qa, prod).
# - Intended to be idempotent; rerunning will leave existing resources intact.
#
# Verification:
#   az appservice plan list --resource-group "${RESOURCE_GROUP}" \
#     --query "[].{name:name, sku:sku.name, isLinux:isLinux}" --output table
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

info() {
  echo "[INFO] $*" >&2
}

error() {
  echo "[ERROR] $*" >&2
}

require_az_cli() {
  if ! command -v az >/dev/null 2>&1; then
    error "Azure CLI (az) is required but not found in PATH."
    exit 1
  fi
}

RESOURCE_GROUP="${RESOURCE_GROUP:-rg-ecompoc-shared-westus3}"
LOCATION="${LOCATION:-westus3}"

# Customize SKUs if needed by exporting e.g. BACKEND_DEV_SKU before running.
DEFAULT_SKU="${DEFAULT_SKU:-B1}"

plan_sku() {
  local env="$1"
  local workload="$2"
  local key="${env}-${workload}"

  case "${key}" in
    "dev-backend") echo "${BACKEND_DEV_SKU:-${DEFAULT_SKU}}" ;;
    "dev-ui") echo "${UI_DEV_SKU:-${DEFAULT_SKU}}" ;;
    "qa-backend") echo "${BACKEND_QA_SKU:-${DEFAULT_SKU}}" ;;
    "qa-ui") echo "${UI_QA_SKU:-${DEFAULT_SKU}}" ;;
    "prod-backend") echo "${BACKEND_PROD_SKU:-${DEFAULT_SKU}}" ;;
    "prod-ui") echo "${UI_PROD_SKU:-${DEFAULT_SKU}}" ;;
    *) echo "${DEFAULT_SKU}" ;;
  esac
}

app_service_plan_name() {
  local env="$1"
  local workload="$2"
  printf "asp-ecompoc-%s-%s-%s" "${env}" "${workload}" "${LOCATION}"
}

ensure_resource_group() {
  if az group show --name "${RESOURCE_GROUP}" --only-show-errors --output none 2>/dev/null; then
    info "Resource group ${RESOURCE_GROUP} already exists in Azure."
    return
  fi

  info "Creating resource group ${RESOURCE_GROUP} in ${LOCATION}."
  az group create \
    --name "${RESOURCE_GROUP}" \
    --location "${LOCATION}" \
    --only-show-errors \
    --output none
}

ensure_plan() {
  local env="$1"
  local workload="$2"
  local plan_name
  plan_name="$(app_service_plan_name "${env}" "${workload}")"
  local sku
  sku="$(plan_sku "${env}" "${workload}")"

  if az appservice plan show \
    --name "${plan_name}" \
    --resource-group "${RESOURCE_GROUP}" \
    --only-show-errors \
    --output none 2>/dev/null; then
    info "App Service plan ${plan_name} already exists (env=${env}, workload=${workload})."
    return
  fi

  info "Creating App Service plan ${plan_name} (SKU=${sku}, Linux)."
  az appservice plan create \
    --name "${plan_name}" \
    --resource-group "${RESOURCE_GROUP}" \
    --location "${LOCATION}" \
    --sku "${sku}" \
    --is-linux \
    --only-show-errors \
    --output none
}

provision_plans() {
  local environments=("dev" "qa" "prod")
  local workloads=("backend" "ui")

  for env in "${environments[@]}"; do
    for workload in "${workloads[@]}"; do
      ensure_plan "${env}" "${workload}"
    done
  done
}

main() {
  require_az_cli

  info "Starting Azure provisioning for ecomPOC App Service plans."
  info "Resource group: ${RESOURCE_GROUP}"
  info "Location: ${LOCATION}"

  ensure_resource_group
  provision_plans

  info "Provisioning complete."
  info "Run the verification command in the script header to inspect the plans."
}

main "$@"
