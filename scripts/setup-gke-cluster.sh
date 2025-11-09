#!/bin/bash
set -euo pipefail

# GKE Cluster Setup Script
# This script creates a GKE cluster and all necessary dependencies for the CI/CD pipeline

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    log_error "gcloud CLI is not installed. Please install it first."
    exit 1
fi

# Get GCP project ID
if [ -z "${GCP_PROJECT:-}" ]; then
    CURRENT_PROJECT=$(gcloud config get-value project 2>/dev/null || echo "")
    if [ -z "$CURRENT_PROJECT" ]; then
        log_error "GCP_PROJECT is not set and no default project is configured."
        log_info "Please set GCP_PROJECT environment variable or run: gcloud config set project YOUR_PROJECT_ID"
        exit 1
    else
        GCP_PROJECT=$CURRENT_PROJECT
        log_info "Using current gcloud project: $GCP_PROJECT"
    fi
else
    log_info "Using GCP_PROJECT: $GCP_PROJECT"
    gcloud config set project "$GCP_PROJECT"
fi

# Configuration variables (can be overridden via environment)
GKE_CLUSTER_NAME="${GKE_CLUSTER_NAME:-fully-compliant-cluster}"
GKE_LOCATION="${GKE_LOCATION:-us-central1}"
GKE_REGION="${GKE_LOCATION}"  # Use location as region for regional cluster
NODE_COUNT="${NODE_COUNT:-2}"
MACHINE_TYPE="${MACHINE_TYPE:-e2-standard-2}"
SERVICE_ACCOUNT_NAME="${SERVICE_ACCOUNT_NAME:-github-actions-gke}"
WORKLOAD_IDENTITY_POOL_NAME="${WORKLOAD_IDENTITY_POOL_NAME:-github-actions-pool}"
GITHUB_REPO="${GITHUB_REPO:-}"  # Format: owner/repo-name
GITHUB_ORG="${GITHUB_ORG:-}"    # GitHub organization or username

# Use Artifact Registry (recommended) or GCR
USE_ARTIFACT_REGISTRY="${USE_ARTIFACT_REGISTRY:-true}"
if [ "$USE_ARTIFACT_REGISTRY" = "true" ]; then
    REGISTRY_NAME="${REGISTRY_NAME:-docker-repo}"
    GCR_REGISTRY="${GKE_REGION}-docker.pkg.dev/${GCP_PROJECT}/${REGISTRY_NAME}"
else
    GCR_REGISTRY="gcr.io/${GCP_PROJECT}"
fi

log_info "Configuration:"
log_info "  Project: $GCP_PROJECT"
log_info "  Cluster: $GKE_CLUSTER_NAME"
log_info "  Location: $GKE_LOCATION"
log_info "  Registry: $GCR_REGISTRY"
log_info "  Service Account: $SERVICE_ACCOUNT_NAME"

# Step 1: Enable required APIs
log_info "Step 1: Enabling required GCP APIs..."
gcloud services enable container.googleapis.com \
    artifactregistry.googleapis.com \
    containerregistry.googleapis.com \
    iamcredentials.googleapis.com \
    --project="$GCP_PROJECT"

log_info "Waiting for APIs to be enabled..."
sleep 10

# Step 2: Create Artifact Registry repository (if using Artifact Registry)
if [ "$USE_ARTIFACT_REGISTRY" = "true" ]; then
    log_info "Step 2: Creating Artifact Registry repository..."
    if ! gcloud artifacts repositories describe "$REGISTRY_NAME" \
        --location="$GKE_REGION" \
        --project="$GCP_PROJECT" &>/dev/null; then
        gcloud artifacts repositories create "$REGISTRY_NAME" \
            --repository-format=docker \
            --location="$GKE_REGION" \
            --description="Docker repository for CI/CD pipeline" \
            --project="$GCP_PROJECT"
        log_info "Created Artifact Registry repository: $REGISTRY_NAME"
    else
        log_info "Artifact Registry repository already exists: $REGISTRY_NAME"
    fi
fi

# Step 3: Create GKE cluster
log_info "Step 3: Creating GKE cluster (this may take 5-10 minutes)..."
if gcloud container clusters describe "$GKE_CLUSTER_NAME" \
    --location="$GKE_LOCATION" \
    --project="$GCP_PROJECT" &>/dev/null; then
    log_warn "Cluster $GKE_CLUSTER_NAME already exists in $GKE_LOCATION"
    read -p "Do you want to use the existing cluster? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_error "Please delete the existing cluster or choose a different name."
        exit 1
    fi
else
    gcloud container clusters create "$GKE_CLUSTER_NAME" \
        --location="$GKE_LOCATION" \
        --num-nodes="$NODE_COUNT" \
        --machine-type="$MACHINE_TYPE" \
        --disk-type="pd-standard" \
        --disk-size="20" \
        --enable-autorepair \
        --enable-autoupgrade \
        --workload-pool="${GCP_PROJECT}.svc.id.goog" \
        --enable-ip-alias \
        --network="default" \
        --project="$GCP_PROJECT"
    
    log_info "GKE cluster created successfully!"
fi

# Step 4: Get cluster credentials
log_info "Step 4: Configuring kubectl for GKE cluster..."
gcloud container clusters get-credentials "$GKE_CLUSTER_NAME" \
    --location="$GKE_LOCATION" \
    --project="$GCP_PROJECT"

# Verify kubectl access
if kubectl cluster-info &>/dev/null; then
    log_info "kubectl configured successfully"
else
    log_error "Failed to configure kubectl"
    exit 1
fi

# Step 5: Create dev namespace
log_info "Step 5: Creating dev namespace..."
kubectl create namespace dev --dry-run=client -o yaml | kubectl apply -f -
log_info "dev namespace ready"

# Step 6: Create service account for GitHub Actions
log_info "Step 6: Creating service account for GitHub Actions..."
SERVICE_ACCOUNT_EMAIL="${SERVICE_ACCOUNT_NAME}@${GCP_PROJECT}.iam.gserviceaccount.com"

if ! gcloud iam service-accounts describe "$SERVICE_ACCOUNT_EMAIL" \
    --project="$GCP_PROJECT" &>/dev/null; then
    gcloud iam service-accounts create "$SERVICE_ACCOUNT_NAME" \
        --display-name="GitHub Actions Service Account for GKE" \
        --project="$GCP_PROJECT"
    log_info "Created service account: $SERVICE_ACCOUNT_EMAIL"
else
    log_info "Service account already exists: $SERVICE_ACCOUNT_EMAIL"
fi

# Grant permissions to service account
log_info "Granting permissions to service account..."
gcloud projects add-iam-policy-binding "$GCP_PROJECT" \
    --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
    --role="roles/container.developer" \
    --condition=None

gcloud projects add-iam-policy-binding "$GCP_PROJECT" \
    --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
    --role="roles/storage.admin" \
    --condition=None

if [ "$USE_ARTIFACT_REGISTRY" = "true" ]; then
    gcloud projects add-iam-policy-binding "$GCP_PROJECT" \
        --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
        --role="roles/artifactregistry.writer" \
        --condition=None
fi

log_info "Permissions granted to service account"

# Step 7: Set up Workload Identity
log_info "Step 7: Setting up Workload Identity..."

# Create Workload Identity Pool
if ! gcloud iam workload-identity-pools describe "$WORKLOAD_IDENTITY_POOL_NAME" \
    --location="global" \
    --project="$GCP_PROJECT" &>/dev/null; then
    gcloud iam workload-identity-pools create "$WORKLOAD_IDENTITY_POOL_NAME" \
        --location="global" \
        --project="$GCP_PROJECT"
    log_info "Created Workload Identity Pool: $WORKLOAD_IDENTITY_POOL_NAME"
else
    log_info "Workload Identity Pool already exists: $WORKLOAD_IDENTITY_POOL_NAME"
fi

# Create Workload Identity Provider
PROVIDER_NAME="github-provider"
PROVIDER_RESOURCE_NAME="projects/${GCP_PROJECT}/locations/global/workloadIdentityPools/${WORKLOAD_IDENTITY_POOL_NAME}/providers/${PROVIDER_NAME}"

if ! gcloud iam workload-identity-pools providers describe "$PROVIDER_NAME" \
    --workload-identity-pool="$WORKLOAD_IDENTITY_POOL_NAME" \
    --location="global" \
    --project="$GCP_PROJECT" &>/dev/null; then
    if [ -n "$GITHUB_REPO" ]; then
        # Extract org and repo from GITHUB_REPO
        GITHUB_ORG=$(echo "$GITHUB_REPO" | cut -d'/' -f1)
        REPO_NAME=$(echo "$GITHUB_REPO" | cut -d'/' -f2)
        SUBJECT="repo:${GITHUB_REPO}:ref:refs/heads/main"
        
        gcloud iam workload-identity-pools providers create-oidc "$PROVIDER_NAME" \
            --workload-identity-pool="$WORKLOAD_IDENTITY_POOL_NAME" \
            --location="global" \
            --issuer-uri="https://token.actions.githubusercontent.com" \
            --allowed-audiences="https://github.com/${GITHUB_ORG}" \
            --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.repository=assertion.repository" \
            --attribute-condition="assertion.repository=='${GITHUB_REPO}'" \
            --project="$GCP_PROJECT"
        
        log_info "Created Workload Identity Provider for repository: $GITHUB_REPO"
    else
        log_warn "GITHUB_REPO not set. Creating provider without repository restriction."
        log_warn "You will need to manually configure the provider later."
        gcloud iam workload-identity-pools providers create-oidc "$PROVIDER_NAME" \
            --workload-identity-pool="$WORKLOAD_IDENTITY_POOL_NAME" \
            --location="global" \
            --issuer-uri="https://token.actions.githubusercontent.com" \
            --allowed-audiences="https://github.com" \
            --project="$GCP_PROJECT"
    fi
else
    log_info "Workload Identity Provider already exists: $PROVIDER_NAME"
fi

# Allow the service account to impersonate itself via Workload Identity
gcloud iam service-accounts add-iam-policy-binding "$SERVICE_ACCOUNT_EMAIL" \
    --role="roles/iam.workloadIdentityUser" \
    --member="principalSet://iam.googleapis.com/projects/${GCP_PROJECT}/locations/global/workloadIdentityPools/${WORKLOAD_IDENTITY_POOL_NAME}/*" \
    --project="$GCP_PROJECT"

log_info "Workload Identity configured"

# Step 8: Output summary and GitHub secrets
log_info ""
log_info "=========================================="
log_info "Setup Complete!"
log_info "=========================================="
log_info ""
log_info "GKE Cluster: $GKE_CLUSTER_NAME"
log_info "Location: $GKE_LOCATION"
log_info "Registry: $GCR_REGISTRY"
log_info ""
log_info "GitHub Secrets/Variables to configure:"
log_info ""
echo "GCP_PROJECT=$GCP_PROJECT"
echo "GKE_CLUSTER_NAME=$GKE_CLUSTER_NAME"
echo "GKE_LOCATION=$GKE_LOCATION"
echo "GCR_REGISTRY=$GCR_REGISTRY"
echo "GCP_WORKLOAD_IDENTITY_PROVIDER=$PROVIDER_RESOURCE_NAME"
echo "GCP_SERVICE_ACCOUNT=$SERVICE_ACCOUNT_EMAIL"
echo "SQL_SERVER_PASSWORD=<your-azure-sql-password>"
log_info ""
log_info "Next steps:"
log_info "1. Set the GitHub secrets/variables listed above"
log_info "2. Run the deployment script to deploy services"
log_info ""

