#!/usr/bin/env bash
set -euo pipefail

#
# Provision Azure App Service plans for the ecomPOC application.
# - Creates/updates the shared resource group in the specified region.
# - Ensures three Linux App Service plans exist (one per environment: dev, qa, prod).
# - Both backend and UI apps use the same App Service Plan per environment (UI plan).
# - Enables Application Insights for all App Services in the resource group.
# - Intended to be idempotent; rerunning will leave existing resources intact.
#
# Verification:
#   az appservice plan list --resource-group "${RESOURCE_GROUP}" \
#     --query "[].{name:name, sku:sku.name, isLinux:isLinux}" --output table
#   az webapp list --resource-group "${RESOURCE_GROUP}" \
#     --query "[].{name:name, appInsights:siteConfig.appSettings[?name=='APPINSIGHTS_INSTRUMENTATIONKEY'].value}" --output table
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

# Application Insights component name
APP_INSIGHTS_NAME="${APP_INSIGHTS_NAME:-appi-ecompoc-shared-${LOCATION}}"

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
  # Only create UI plans - both backend and UI will use the same plan
  local workloads=("ui")

  for env in "${environments[@]}"; do
    for workload in "${workloads[@]}"; do
      ensure_plan "${env}" "${workload}"
    done
  done
}

ensure_app_insights_component() {
  local app_insights_id
  app_insights_id="$(az monitor app-insights component show \
    --app "${APP_INSIGHTS_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --query id \
    --output tsv 2>/dev/null || echo "")"

  if [[ -n "${app_insights_id}" ]]; then
    info "Application Insights component ${APP_INSIGHTS_NAME} already exists."
    return
  fi

  info "Creating Application Insights component ${APP_INSIGHTS_NAME}."
  az monitor app-insights component create \
    --app "${APP_INSIGHTS_NAME}" \
    --location "${LOCATION}" \
    --resource-group "${RESOURCE_GROUP}" \
    --application-type web \
    --only-show-errors \
    --output none
}

enable_app_insights_for_app() {
  local app_name="$1"
  local app_insights_id
  app_insights_id="$(az monitor app-insights component show \
    --app "${APP_INSIGHTS_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --query id \
    --output tsv)"

  if [[ -z "${app_insights_id}" ]]; then
    error "Application Insights component ${APP_INSIGHTS_NAME} not found."
    return 1
  fi

  # Try using the modern connect-webapp command first
  if az monitor app-insights component connect-webapp \
    --app "${APP_INSIGHTS_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --web-app "${app_name}" \
    --enable-profiler \
    --enable-snapshot-debugger \
    --only-show-errors \
    --output none 2>/dev/null; then
    info "Connected ${app_name} to Application Insights using connect-webapp."
    return 0
  fi

  # Fallback to manual app settings configuration
  info "Using app settings fallback for ${app_name}."
  local connection_string
  connection_string="$(az monitor app-insights component show \
    --app "${APP_INSIGHTS_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --query connectionString \
    --output tsv)"

  if [[ -z "${connection_string}" ]]; then
    error "Failed to retrieve Application Insights connection string."
    return 1
  fi

  # Get instrumentation key as well for compatibility
  local instrumentation_key
  instrumentation_key="$(az monitor app-insights component show \
    --app "${APP_INSIGHTS_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --query instrumentationKey \
    --output tsv)"

  # Set app settings
  az webapp config appsettings set \
    --name "${app_name}" \
    --resource-group "${RESOURCE_GROUP}" \
    --settings \
      "APPLICATIONINSIGHTS_CONNECTION_STRING=${connection_string}" \
      "APPINSIGHTS_INSTRUMENTATIONKEY=${instrumentation_key}" \
      "ApplicationInsightsAgent_EXTENSION_VERSION=~3" \
    --only-show-errors \
    --output none

  # Enable Always On for continuous monitoring
  az webapp config set \
    --name "${app_name}" \
    --resource-group "${RESOURCE_GROUP}" \
    --always-on true \
    --only-show-errors \
    --output none

  info "Configured Application Insights for ${app_name} via app settings."
}

enable_app_insights_for_all_apps() {
  local apps
  apps="$(az webapp list \
    --resource-group "${RESOURCE_GROUP}" \
    --query "[].name" \
    --output tsv 2>/dev/null || echo "")"

  if [[ -z "${apps}" ]]; then
    info "No App Services found in resource group ${RESOURCE_GROUP}."
    return 0
  fi

  local app_count=0
  while IFS= read -r app_name; do
    [[ -z "${app_name}" ]] && continue
    enable_app_insights_for_app "${app_name}"
    ((app_count++)) || true
  done <<< "${apps}"

  if [[ ${app_count} -eq 0 ]]; then
    info "No App Services to configure for Application Insights."
  else
    info "Configured Application Insights for ${app_count} App Service(s)."
  fi
}

verify_app_insights_setup() {
  info "Verifying Application Insights configuration..."

  local apps
  apps="$(az webapp list \
    --resource-group "${RESOURCE_GROUP}" \
    --query "[].name" \
    --output tsv 2>/dev/null || echo "")"

  if [[ -z "${apps}" ]]; then
    info "No App Services found in resource group ${RESOURCE_GROUP}."
    return 0
  fi

  local verified_count=0
  local total_count=0

  echo ""
  echo "Application Insights Status:"
  echo "============================"
  printf "%-40s %-20s\n" "App Service Name" "App Insights Status"
  printf "%-40s %-20s\n" "------------------" "-------------------"

  while IFS= read -r app_name; do
    [[ -z "${app_name}" ]] && continue
    ((total_count++)) || true

    local connection_string
    connection_string="$(az webapp config appsettings list \
      --name "${app_name}" \
      --resource-group "${RESOURCE_GROUP}" \
      --query "[?name=='APPLICATIONINSIGHTS_CONNECTION_STRING' || name=='APPINSIGHTS_INSTRUMENTATIONKEY'].value" \
      --output tsv 2>/dev/null | head -n1)"

    if [[ -n "${connection_string}" ]]; then
      printf "%-40s %-20s\n" "${app_name}" "✓ Enabled"
      ((verified_count++)) || true
    else
      printf "%-40s %-20s\n" "${app_name}" "✗ Not Configured"
    fi
  done <<< "${apps}"

  echo ""
  if [[ ${total_count} -eq 0 ]]; then
    info "No App Services found to verify."
  elif [[ ${verified_count} -eq ${total_count} ]]; then
    info "✓ All ${total_count} App Service(s) have Application Insights enabled."
  else
    info "⚠ ${verified_count} of ${total_count} App Service(s) have Application Insights enabled."
  fi
}

main() {
  require_az_cli

  info "Starting Azure provisioning for ecomPOC App Service plans."
  info "Resource group: ${RESOURCE_GROUP}"
  info "Location: ${LOCATION}"

  ensure_resource_group
  ensure_app_insights_component
  provision_plans
  enable_app_insights_for_all_apps

  info "Provisioning complete."
  verify_app_insights_setup
  info "Run the verification command in the script header to inspect the plans and App Services."
}

main "$@"
