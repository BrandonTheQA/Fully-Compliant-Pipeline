# GitHub Secrets and Variables Required for GKE CI/CD Pipeline

After running the GKE setup scripts (`scripts/setup-gke-cluster.sh`), you will need to configure the following GitHub repository secrets and variables.

## Required GitHub Secrets

Add these as **Repository Secrets** in GitHub:
**Settings → Secrets and variables → Actions → New repository secret**

### 1. GCP_PROJECT
- **Type**: Secret
- **Description**: Your Google Cloud Platform project ID
- **Example**: `my-gcp-project-123456`
- **How to get**: 
  - Run: `gcloud config get-value project`
  - Or check in GCP Console: https://console.cloud.google.com

### 2. GKE_CLUSTER_NAME
- **Type**: Secret (can be variable)
- **Description**: Name of your GKE cluster
- **Default**: `fully-compliant-cluster`
- **Example**: `fully-compliant-cluster`
- **How to get**: Set during cluster creation or check with: `gcloud container clusters list`

### 3. GKE_LOCATION
- **Type**: Secret (can be variable)
- **Description**: GKE cluster location (region or zone)
- **Default**: `us-central1`
- **Example**: `us-central1` or `us-central1-a`
- **How to get**: Set during cluster creation or check with: `gcloud container clusters describe CLUSTER_NAME --location=LOCATION`

### 4. GCR_REGISTRY
- **Type**: Secret (can be variable)
- **Description**: Container registry URL for Docker images
- **Format (Artifact Registry)**: `REGION-docker.pkg.dev/PROJECT_ID/REPOSITORY_NAME`
- **Format (GCR)**: `gcr.io/PROJECT_ID`
- **Example**: `us-central1-docker.pkg.dev/my-project/docker-repo`
- **How to get**: 
  - Artifact Registry: `gcloud artifacts repositories describe REPO_NAME --location=REGION --format="value(name)"`
  - Or construct manually: `{REGION}-docker.pkg.dev/{PROJECT_ID}/{REPO_NAME}`

### 5. GCP_WORKLOAD_IDENTITY_PROVIDER
- **Type**: Secret
- **Description**: Full resource name of the Workload Identity provider
- **Format**: `projects/{PROJECT_ID}/locations/global/workloadIdentityPools/{POOL_NAME}/providers/{PROVIDER_NAME}`
- **Example**: `projects/my-project/locations/global/workloadIdentityPools/github-actions-pool/providers/github-provider`
- **How to get**: 
  - Output from `setup-gke-cluster.sh` script
  - Or run: `gcloud iam workload-identity-pools providers describe github-provider --workload-identity-pool=github-actions-pool --location=global --format="value(name)"`

### 6. GCP_SERVICE_ACCOUNT
- **Type**: Secret
- **Description**: Service account email for GitHub Actions
- **Format**: `{SERVICE_ACCOUNT_NAME}@{PROJECT_ID}.iam.gserviceaccount.com`
- **Example**: `github-actions-gke@my-project.iam.gserviceaccount.com`
- **Default**: `github-actions-gke@{PROJECT_ID}.iam.gserviceaccount.com`
- **How to get**: 
  - Output from `setup-gke-cluster.sh` script
  - Or run: `gcloud iam service-accounts list --filter="displayName:GitHub Actions" --format="value(email)"`

### 7. SQL_SERVER_PASSWORD
- **Type**: Secret
- **Description**: Password for Azure SQL Database user `bgarlock`
- **Database**: `sqlserverpoc121212.database.windows.net`
- **Example**: `YourSecurePassword123!`
- **How to get**: 
  - Azure Portal → SQL Database → Reset password
  - Or contact database administrator

## Summary Table

| Secret Name | Type | Required | Example Value |
|------------|------|----------|---------------|
| `GCP_PROJECT` | Secret | Yes | `my-gcp-project-123456` |
| `GKE_CLUSTER_NAME` | Secret/Variable | Yes | `fully-compliant-cluster` |
| `GKE_LOCATION` | Secret/Variable | Yes | `us-central1` |
| `GCR_REGISTRY` | Secret/Variable | Yes | `us-central1-docker.pkg.dev/my-project/docker-repo` |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | Secret | Yes | `projects/my-project/locations/global/workloadIdentityPools/github-actions-pool/providers/github-provider` |
| `GCP_SERVICE_ACCOUNT` | Secret | Yes | `github-actions-gke@my-project.iam.gserviceaccount.com` |
| `SQL_SERVER_PASSWORD` | Secret | Yes | `YourSecurePassword123!` |

## Quick Setup Commands

After running the setup script, you can extract the values with these commands:

```bash
# Set your project
export GCP_PROJECT=your-project-id

# Get cluster name (if using default)
export GKE_CLUSTER_NAME=fully-compliant-cluster

# Get location (if using default)
export GKE_LOCATION=us-central1

# Get GCR registry (Artifact Registry)
export GCR_REGISTRY="${GKE_LOCATION}-docker.pkg.dev/${GCP_PROJECT}/docker-repo"

# Get Workload Identity Provider
export GCP_WORKLOAD_IDENTITY_PROVIDER="projects/${GCP_PROJECT}/locations/global/workloadIdentityPools/github-actions-pool/providers/github-provider"

# Get Service Account
export GCP_SERVICE_ACCOUNT="github-actions-gke@${GCP_PROJECT}.iam.gserviceaccount.com"

# Print all values
echo "GCP_PROJECT=$GCP_PROJECT"
echo "GKE_CLUSTER_NAME=$GKE_CLUSTER_NAME"
echo "GKE_LOCATION=$GKE_LOCATION"
echo "GCR_REGISTRY=$GCR_REGISTRY"
echo "GCP_WORKLOAD_IDENTITY_PROVIDER=$GCP_WORKLOAD_IDENTITY_PROVIDER"
echo "GCP_SERVICE_ACCOUNT=$GCP_SERVICE_ACCOUNT"
echo "SQL_SERVER_PASSWORD=<your-password>"
```

## Verification

After adding the secrets, verify the CI/CD pipeline can access them by:

1. Pushing a commit to trigger the workflow
2. Checking the workflow logs for authentication success
3. Verifying the "Authenticate to Google Cloud" step succeeds

## Notes

- All secrets are sensitive and should never be committed to the repository
- The `setup-gke-cluster.sh` script will output these values at the end
- Workload Identity eliminates the need for service account keys (more secure)
- The `SQL_SERVER_PASSWORD` is shared across all environments (dev, staging, prod)

