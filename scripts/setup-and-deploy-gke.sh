#!/bin/bash
set -euo pipefail

# Master GKE Setup and Deployment Script
# This script orchestrates the complete setup and deployment process

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    log_error "gcloud CLI is not installed. Please install it first:"
    log_info "  https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Get or set GCP project
CURRENT_PROJECT=$(gcloud config get-value project 2>/dev/null || echo "")
if [ -z "${GCP_PROJECT:-}" ]; then
    if [ -n "$CURRENT_PROJECT" ]; then
        GCP_PROJECT=$CURRENT_PROJECT
        log_info "Using current gcloud project: $GCP_PROJECT"
    else
        log_error "GCP_PROJECT is not set and no default project is configured."
        log_info "Please set GCP_PROJECT environment variable:"
        log_info "  export GCP_PROJECT=your-project-id"
        log_info "Or set it via: gcloud config set project YOUR_PROJECT_ID"
        exit 1
    fi
else
    gcloud config set project "$GCP_PROJECT" >/dev/null 2>&1
    log_info "Using GCP_PROJECT: $GCP_PROJECT"
fi

# Configuration with defaults
GKE_CLUSTER_NAME="${GKE_CLUSTER_NAME:-fully-compliant-cluster}"
GKE_LOCATION="${GKE_LOCATION:-us-central1}"
NODE_COUNT="${NODE_COUNT:-2}"
MACHINE_TYPE="${MACHINE_TYPE:-e2-standard-2}"

# Use Artifact Registry (recommended)
USE_ARTIFACT_REGISTRY="${USE_ARTIFACT_REGISTRY:-true}"
if [ "$USE_ARTIFACT_REGISTRY" = "true" ]; then
    REGISTRY_NAME="${REGISTRY_NAME:-docker-repo}"
    GCR_REGISTRY="${GKE_LOCATION}-docker.pkg.dev/${GCP_PROJECT}/${REGISTRY_NAME}"
else
    GCR_REGISTRY="gcr.io/${GCP_PROJECT}"
fi

SERVICE_ACCOUNT_NAME="${SERVICE_ACCOUNT_NAME:-github-actions-gke}"
WORKLOAD_IDENTITY_POOL_NAME="${WORKLOAD_IDENTITY_POOL_NAME:-github-actions-pool}"

log_info ""
log_info "=========================================="
log_info "GKE Cluster Setup and Deployment"
log_info "=========================================="
log_info ""
log_info "Configuration:"
log_info "  Project: $GCP_PROJECT"
log_info "  Cluster: $GKE_CLUSTER_NAME"
log_info "  Location: $GKE_LOCATION"
log_info "  Registry: $GCR_REGISTRY"
log_info "  Node Count: $NODE_COUNT"
log_info "  Machine Type: $MACHINE_TYPE"
log_info ""

# Prompt for GitHub repository (optional but recommended)
if [ -z "${GITHUB_REPO:-}" ]; then
    log_info "GitHub Repository (optional, format: owner/repo-name):"
    read -p "  Enter GitHub repo (or press Enter to skip): " GITHUB_REPO
fi

# Prompt for SQL password
if [ -z "${SQL_SERVER_PASSWORD:-}" ]; then
    log_warn "SQL_SERVER_PASSWORD is not set"
    read -sp "  Enter Azure SQL Database password: " SQL_SERVER_PASSWORD
    echo
    if [ -z "$SQL_SERVER_PASSWORD" ]; then
        log_error "SQL_SERVER_PASSWORD is required"
        exit 1
    fi
fi

log_info ""
log_step "Step 1: Setting up GKE cluster and dependencies..."
export GCP_PROJECT GKE_CLUSTER_NAME GKE_LOCATION GCR_REGISTRY SERVICE_ACCOUNT_NAME WORKLOAD_IDENTITY_POOL_NAME GITHUB_REPO NODE_COUNT MACHINE_TYPE USE_ARTIFACT_REGISTRY REGISTRY_NAME
"$SCRIPT_DIR/setup-gke-cluster.sh"

if [ $? -ne 0 ]; then
    log_error "Cluster setup failed"
    exit 1
fi

log_info ""
log_step "Step 2: Deploying services to GKE cluster..."
export GCP_PROJECT GKE_CLUSTER_NAME GKE_LOCATION GCR_REGISTRY SQL_SERVER_PASSWORD
"$SCRIPT_DIR/deploy-gke-services.sh"

if [ $? -ne 0 ]; then
    log_error "Service deployment failed"
    exit 1
fi

log_info ""
log_info "=========================================="
log_info "Setup and Deployment Complete!"
log_info "=========================================="
log_info ""
log_info "GitHub Secrets/Variables to configure:"
log_info ""
echo "GCP_PROJECT=$GCP_PROJECT"
echo "GKE_CLUSTER_NAME=$GKE_CLUSTER_NAME"
echo "GKE_LOCATION=$GKE_LOCATION"
echo "GCR_REGISTRY=$GCR_REGISTRY"

# Get Workload Identity Provider resource name
PROVIDER_NAME="github-provider"
PROVIDER_RESOURCE_NAME="projects/${GCP_PROJECT}/locations/global/workloadIdentityPools/${WORKLOAD_IDENTITY_POOL_NAME}/providers/${PROVIDER_NAME}"
SERVICE_ACCOUNT_EMAIL="${SERVICE_ACCOUNT_NAME}@${GCP_PROJECT}.iam.gserviceaccount.com"

echo "GCP_WORKLOAD_IDENTITY_PROVIDER=$PROVIDER_RESOURCE_NAME"
echo "GCP_SERVICE_ACCOUNT=$SERVICE_ACCOUNT_EMAIL"
echo "SQL_SERVER_PASSWORD=<your-azure-sql-password>"
log_info ""
log_info "Add these as GitHub repository secrets/variables:"
log_info "  Settings → Secrets and variables → Actions → New repository secret"
log_info ""

