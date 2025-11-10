# GKE Setup and Deployment Guide

This guide walks you through setting up a GKE cluster and deploying all services using the provided scripts.

## Prerequisites

1. **gcloud CLI installed and authenticated**
   ```bash
   gcloud auth login
   gcloud auth application-default login
   ```

2. **GCP Project with billing enabled**
   - Create a project at https://console.cloud.google.com
   - Enable billing for the project

3. **Azure SQL Database password**
   - You'll need the password for the Azure SQL Database (`sqlserverpoc121212.database.windows.net`)

4. **GitHub Repository information** (optional but recommended for Workload Identity)
   - Format: `owner/repo-name` (e.g., `myorg/my-repo`)

## Quick Start

### Option 1: Run the master script (recommended)

```bash
cd /Users/brandongarlock/projects/Fully-Compliant-Pipeline

# Set your GCP project
export GCP_PROJECT=your-project-id
gcloud config set project $GCP_PROJECT

# Optionally set other variables
export GKE_CLUSTER_NAME=fully-compliant-cluster
export GKE_LOCATION=us-central1
export GITHUB_REPO=your-org/your-repo  # Optional
export SQL_SERVER_PASSWORD=your-sql-password

# Run the master script
./scripts/setup-and-deploy-gke.sh
```

### Option 2: Run scripts separately

#### Step 1: Setup GKE Cluster

```bash
export GCP_PROJECT=your-project-id
export GKE_CLUSTER_NAME=fully-compliant-cluster
export GKE_LOCATION=us-central1
export GITHUB_REPO=your-org/your-repo  # Optional

./scripts/setup-gke-cluster.sh
```

#### Step 2: Deploy Services

```bash
export GCP_PROJECT=your-project-id
export GKE_CLUSTER_NAME=fully-compliant-cluster
export GKE_LOCATION=us-central1
export GCR_REGISTRY=us-central1-docker.pkg.dev/your-project-id/docker-repo
export SQL_SERVER_PASSWORD=your-sql-password

./scripts/deploy-gke-services.sh
```

## Configuration Variables

You can customize the setup by setting these environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `GCP_PROJECT` | (required) | GCP project ID |
| `GKE_CLUSTER_NAME` | `fully-compliant-cluster` | Name of the GKE cluster |
| `GKE_LOCATION` | `us-central1` | GKE cluster location (region) |
| `NODE_COUNT` | `2` | Number of nodes in the cluster |
| `MACHINE_TYPE` | `e2-standard-2` | Machine type for nodes |
| `GITHUB_REPO` | (optional) | GitHub repository (format: `owner/repo`) |
| `SQL_SERVER_PASSWORD` | (required for deployment) | Azure SQL Database password |
| `USE_ARTIFACT_REGISTRY` | `true` | Use Artifact Registry (recommended) |
| `REGISTRY_NAME` | `docker-repo` | Artifact Registry repository name |

## What the Scripts Do

### setup-gke-cluster.sh

1. **Enable GCP APIs**
   - Container API (GKE)
   - Artifact Registry API
   - Container Registry API
   - IAM Credentials API (Workload Identity)

2. **Create Artifact Registry repository**
   - Creates a Docker repository for storing container images

3. **Create GKE Cluster**
   - Standard GKE cluster (not Autopilot)
   - Enables Workload Identity
   - Configures node pool with specified machine type and node count

4. **Configure kubectl**
   - Gets cluster credentials
   - Creates `dev` namespace

5. **Set up Workload Identity**
   - Creates service account for GitHub Actions
   - Grants necessary permissions:
     - `roles/container.developer` (GKE access)
     - `roles/storage.admin` (GCR/Artifact Registry access)
     - `roles/artifactregistry.writer` (Artifact Registry write access)
   - Creates Workload Identity Pool and Provider
   - Binds service account to GitHub repository (if provided)

### deploy-gke-services.sh

1. **Create Kubernetes resources**
   - `deployment-state` ConfigMap with `active-color=blue` (so green deploys first)
   - `sql-db-secret` with SQL password

2. **Deploy services as green deployment**
   - Deploys monolith (API) followed by ui
   - Modifies manifests:
     - Replaces ACR image references with GCR
     - Adds `deployment-color=green` labels
     - Adds name suffix `-green`
     - Removes Azure-specific node affinity
   - Sets environment variables on each deployment:
     - `DEPLOYMENT_COLOR`
     - `NAMESPACE`

3. **Wait for rollouts**
   - Waits for all deployments to be ready (5 minute timeout)

4. **Verify services**
   - Checks deployments, pods, and services
   - Waits for LoadBalancer external IPs
   - Performs health checks on all services

## GitHub Secrets/Variables

After running the setup script, you'll need to configure these GitHub secrets/variables:

### Required Secrets (Repository Secrets)

1. **GCP_PROJECT**
   - Your GCP project ID
   - Example: `my-gcp-project-123456`

2. **GKE_CLUSTER_NAME**
   - Name of your GKE cluster
   - Example: `fully-compliant-cluster`

3. **GKE_LOCATION**
   - GKE cluster location (region)
   - Example: `us-central1`

4. **GCR_REGISTRY**
   - Container registry URL
   - Artifact Registry: `us-central1-docker.pkg.dev/PROJECT_ID/docker-repo`
   - GCR: `gcr.io/PROJECT_ID`

5. **GCP_WORKLOAD_IDENTITY_PROVIDER**
   - Workload Identity provider resource name
   - Format: `projects/PROJECT_ID/locations/global/workloadIdentityPools/POOL_NAME/providers/PROVIDER_NAME`
   - Example: `projects/my-project/locations/global/workloadIdentityPools/github-actions-pool/providers/github-provider`

6. **GCP_SERVICE_ACCOUNT**
   - Service account email
   - Format: `SERVICE_ACCOUNT_NAME@PROJECT_ID.iam.gserviceaccount.com`
   - Example: `github-actions-gke@my-project.iam.gserviceaccount.com`

7. **SQL_SERVER_PASSWORD**
   - Azure SQL Database password
   - The password for the `bgarlock` user

### How to Add GitHub Secrets

1. Go to your GitHub repository
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each secret with the name and value from above

## Verifying the Deployment

After deployment, verify services are running:

```bash
# Check deployments
kubectl get deployments -n dev

# Check pods
kubectl get pods -n dev

# Check services and external IPs
kubectl get svc -n dev

# Check logs
kubectl logs -n dev deployment/user-green
kubectl logs -n dev deployment/product-green
kubectl logs -n dev deployment/order-green
kubectl logs -n dev deployment/ui-green
```

## Health Check Endpoints

Once LoadBalancer IPs are assigned, you can access:

- **API Services**: `http://<EXTERNAL-IP>/actuator/health`
  - user-green
  - product-green
  - order-green

- **UI Service**: `http://<EXTERNAL-IP>/health`
  - ui-green

## Troubleshooting

### Cluster creation fails

- Ensure billing is enabled for your GCP project
- Check that required APIs are enabled
- Verify you have necessary permissions

### Deployment fails

- Check pod logs: `kubectl logs -n dev deployment/SERVICE-green`
- Check pod status: `kubectl describe pod -n dev -l app=SERVICE`
- Verify SQL password is correct
- Ensure images are pushed to the registry (CI/CD will handle this)

### Services not getting external IPs

- LoadBalancer provisioning can take 5-10 minutes
- Check service status: `kubectl describe svc -n dev SERVICE-green`
- Verify cluster has sufficient resources

### Workload Identity not working

- Verify the Workload Identity provider is configured correctly
- Check service account permissions
- Ensure GitHub repository matches the configured repository in Workload Identity

## Cleanup

To delete the cluster and resources:

```bash
# Delete cluster
gcloud container clusters delete $GKE_CLUSTER_NAME \
    --location=$GKE_LOCATION \
    --project=$GCP_PROJECT

# Delete Artifact Registry repository (optional)
gcloud artifacts repositories delete docker-repo \
    --location=$GKE_LOCATION \
    --project=$GCP_PROJECT

# Delete service account (optional)
gcloud iam service-accounts delete github-actions-gke@$GCP_PROJECT.iam.gserviceaccount.com \
    --project=$GCP_PROJECT
```

## Next Steps

1. **Configure GitHub Secrets** as listed above
2. **Push code to trigger CI/CD pipeline** - the pipeline will build and push images, then deploy
3. **Monitor deployments** via GitHub Actions and kubectl

