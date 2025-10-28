#!/bin/bash

# Script to fix Azure Function App runtime settings for C# migration
# This sets the correct runtime for all deployed function apps

set -e

echo "======================================"
echo "Azure Function Runtime Configuration"
echo "======================================"
echo ""

# Check if Azure CLI is installed
if ! command -v az &> /dev/null; then
    echo "ERROR: Azure CLI is not installed."
    echo "Install it from: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
    exit 1
fi

echo "Azure CLI found ✓"
echo ""

# Login check
echo "Checking Azure login status..."
az account show &> /dev/null || {
    echo "Not logged in. Please run: az login"
    exit 1
}

echo "Logged in to Azure ✓"
echo ""

# Function to configure runtime
configure_runtime() {
    local app_name=$1
    local resource_group=$2
    local env=$3
    
    echo "Configuring $app_name ($env)..."
    
    # Set FUNCTIONS_WORKER_RUNTIME to dotnet-isolated
    az functionapp config appsettings set \
        --name "$app_name" \
        --resource-group "$resource_group" \
        --settings FUNCTIONS_WORKER_RUNTIME=dotnet-isolated \
        --output none
    
    # Restart the function app to apply changes
    echo "  Restarting function app..."
    az functionapp restart \
        --name "$app_name" \
        --resource-group "$resource_group" \
        --output none
    
    echo "  ✓ $app_name configured and restarted"
    echo ""
}

echo "======================================"
echo "Configuring DEV Environment"
echo "======================================"
echo ""

configure_runtime "joaz-func-user-9021-dev" "brandon" "dev"
configure_runtime "joaz-func-product-9021-dev" "brandon" "dev"
configure_runtime "joaz-func-order-9021-dev" "brandon" "dev"

echo "======================================"
echo "Verification"
echo "======================================"
echo ""

echo "Checking User Service..."
curl -s https://joaz-func-user-9021-dev.azurewebsites.net/api/health || echo "  Not ready yet (may need a few moments)"

echo ""
echo "Checking Product Service..."
curl -s https://joaz-func-product-9021-dev.azurewebsites.net/api/health || echo "  Not ready yet (may need a few moments)"

echo ""
echo "Checking Order Service..."
curl -s https://joaz-func-order-9021-dev.azurewebsites.net/api/health || echo "  Not ready yet (may need a few moments)"

echo ""
echo "======================================"
echo "Configuration Complete!"
echo "======================================"
echo ""
echo "IMPORTANT: The function apps may take 30-60 seconds to fully restart."
echo "If you still get 404 errors, wait a minute and try again."
echo ""
echo "Test commands:"
echo "  curl https://joaz-func-user-9021-dev.azurewebsites.net/api/health"
echo "  curl https://joaz-func-product-9021-dev.azurewebsites.net/api/health"
echo "  curl https://joaz-func-order-9021-dev.azurewebsites.net/api/health"
echo ""
