# GKE Setup Implementation Summary

This document summarizes what has been implemented to set up a GKE cluster and deploy all services.

## Created Scripts

### 1. `scripts/setup-gke-cluster.sh`
**Purpose**: Creates GKE cluster and all necessary dependencies

**What it does**:
- Enables required GCP APIs (Container, Artifact Registry, IAM Credentials)
- Creates Artifact Registry repository for Docker images
- Creates GKE cluster with Workload Identity enabled
- Configures kubectl and creates `dev` namespace
- Creates service account for GitHub Actions
- Sets up Workload Identity Pool and Provider
- Grants necessary IAM permissions

**Usage**:
```bash
export GCP_PROJECT=your-project-id
export GKE_CLUSTER_NAME=fully-compliant-cluster
export GKE_LOCATION=us-central1
export GITHUB_REPO=your-org/your-repo  # Optional

./scripts/setup-gke-cluster.sh
```

### 2. `scripts/deploy-gke-services.sh`
**Purpose**: Deploys all services as green deployment

**What it does**:
- Creates `deployment-state` ConfigMap with `active-color=blue` (so green deploys first)
- Creates `sql-db-secret` with SQL password
- Deploys services in order: user → product → order → ui
- Modifies manifests to:
  - Replace ACR image references with GCR
  - Add `deployment-color=green` labels
  - Add name suffix `-green`
  - Remove Azure-specific node affinity
- Sets environment variables for each service
- Waits for rollouts to complete
- Verifies services and performs health checks

**Usage**:
```bash
export GCP_PROJECT=your-project-id
export GKE_CLUSTER_NAME=fully-compliant-cluster
export GKE_LOCATION=us-central1
export GCR_REGISTRY=us-central1-docker.pkg.dev/your-project/docker-repo
export SQL_SERVER_PASSWORD=your-sql-password

./scripts/deploy-gke-services.sh
```

### 3. `scripts/setup-and-deploy-gke.sh`
**Purpose**: Master script that orchestrates both setup and deployment

**Usage**:
```bash
export GCP_PROJECT=your-project-id
export SQL_SERVER_PASSWORD=your-sql-password
export GITHUB_REPO=your-org/your-repo  # Optional

./scripts/setup-and-deploy-gke.sh
```

## Created Documentation

### 1. `docs/GKE_SETUP.md`
Complete guide covering:
- Prerequisites
- Quick start instructions
- Configuration variables
- What each script does
- Troubleshooting
- Cleanup instructions

### 2. `docs/GITHUB_SECRETS_GKE.md`
Detailed documentation of all GitHub secrets/variables needed:
- Required secrets list
- How to obtain each value
- Example values
- Quick setup commands
- Verification steps

## Implementation Status

✅ **Completed**:
1. GCP Project Setup scripts (API enabling)
2. GKE Cluster creation script with Workload Identity
3. Container Registry setup (Artifact Registry)
4. Workload Identity configuration
5. kubectl configuration and namespace creation
6. Service deployment scripts (green deployment)
7. Service verification and health checks
8. GitHub secrets documentation

## Next Steps for User

### 1. Authenticate with GCP
```bash
gcloud auth login
gcloud auth application-default login
```

### 2. Set GCP Project
```bash
export GCP_PROJECT=your-project-id
gcloud config set project $GCP_PROJECT
```

### 3. Run Setup Script
```bash
cd /Users/brandongarlock/projects/Fully-Compliant-Pipeline

# Set required variables
export GCP_PROJECT=your-project-id
export SQL_SERVER_PASSWORD=your-azure-sql-password
export GITHUB_REPO=your-org/your-repo  # Optional but recommended

# Run master script
./scripts/setup-and-deploy-gke.sh
```

### 4. Configure GitHub Secrets
After the setup script completes, it will output the values needed. Add them as GitHub repository secrets:

1. Go to GitHub repository → Settings → Secrets and variables → Actions
2. Add each secret from the output:
   - `GCP_PROJECT`
   - `GKE_CLUSTER_NAME`
   - `GKE_LOCATION`
   - `GCR_REGISTRY`
   - `GCP_WORKLOAD_IDENTITY_PROVIDER`
   - `GCP_SERVICE_ACCOUNT`
   - `SQL_SERVER_PASSWORD`

See `docs/GITHUB_SECRETS_GKE.md` for detailed instructions.

### 5. Verify Deployment
```bash
# Check deployments
kubectl get deployments -n dev

# Check pods
kubectl get pods -n dev

# Check services
kubectl get svc -n dev

# Check logs
kubectl logs -n dev deployment/user-green
```

## Services Deployed

The deployment script will create the following services in the `dev` namespace:

1. **user-green** - User service (Spring Boot)
2. **product-green** - Product service (Spring Boot)
3. **order-green** - Order service (Spring Boot, depends on user and product)
4. **ui-green** - UI service (React frontend)

All services are deployed with:
- `deployment-color=green` labels
- LoadBalancer services for external access
- Health check endpoints configured
- Environment variables set appropriately

## GitHub Secrets Summary

After running the setup, configure these GitHub secrets:

| Secret | Description | Example |
|--------|-------------|---------|
| `GCP_PROJECT` | GCP project ID | `my-project-123456` |
| `GKE_CLUSTER_NAME` | Cluster name | `fully-compliant-cluster` |
| `GKE_LOCATION` | Cluster location | `us-central1` |
| `GCR_REGISTRY` | Container registry | `us-central1-docker.pkg.dev/my-project/docker-repo` |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | Workload Identity provider | `projects/my-project/locations/global/workloadIdentityPools/github-actions-pool/providers/github-provider` |
| `GCP_SERVICE_ACCOUNT` | Service account email | `github-actions-gke@my-project.iam.gserviceaccount.com` |
| `SQL_SERVER_PASSWORD` | Azure SQL password | `YourPassword123!` |

## Notes

- The scripts use Artifact Registry by default (recommended over GCR)
- Workload Identity is configured for secure GitHub Actions authentication
- Services are deployed as "green" deployment (blue-green pattern)
- All services connect to Azure SQL Database (`sqlserverpoc121212.database.windows.net`)
- LoadBalancer services will provision external IPs (may take 5-10 minutes)

## Troubleshooting

If you encounter issues:

1. **Authentication errors**: Ensure `gcloud auth login` and `gcloud auth application-default login` are completed
2. **Permission errors**: Verify you have necessary GCP permissions (Project Owner or Editor)
3. **Cluster creation fails**: Check billing is enabled and APIs are enabled
4. **Deployment fails**: Check pod logs with `kubectl logs -n dev deployment/SERVICE-green`
5. **No external IPs**: Wait 5-10 minutes for LoadBalancer provisioning

See `docs/GKE_SETUP.md` for detailed troubleshooting.

