# Kubernetes Deployment Structure

This directory uses Kustomize to manage cloud-specific configurations for AKS (Azure Kubernetes Service) and EKS (Amazon Elastic Kubernetes Service).

## Directory Structure

```
k8s/
├── base/                          # Base manifests (shared across all clouds)
│   ├── kustomization.yaml
│   ├── order-deployment.yaml
│   ├── order-service.yaml
│   ├── product-deployment.yaml
│   ├── product-service.yaml
│   ├── user-deployment.yaml
│   ├── user-service.yaml
│   ├── ui-deployment.yaml
│   └── ui-service.yaml
└── overlays/
    ├── aks/                       # AKS-specific configuration
    │   ├── kustomization.yaml
    │   ├── order-node-affinity.yaml
    │   ├── product-node-affinity.yaml
    │   ├── user-node-affinity.yaml
    │   └── ui-node-affinity.yaml
    └── eks/                       # EKS-specific configuration
        ├── kustomization.yaml
        └── README.md
```

## Deployment Instructions

### For AKS (Azure Kubernetes Service)

Deployments will **require** the `spotnode` node pool:

```bash
kubectl apply -k k8s/overlays/aks/
```

This overlay adds required node affinity to ensure all pods are scheduled on the `spotnode` node pool.

### For EKS (Amazon Elastic Kubernetes Service)

Deployments use AWS ECR for container images and have no node pool constraints:

```bash
# First, update k8s/overlays/eks/kustomization.yaml with your AWS account ID and region
# Then apply the overlay
kubectl apply -k k8s/overlays/eks/
```

This overlay:
- Transforms ACR image references to ECR automatically
- Uses IAM roles for ECR authentication (no secrets required)
- Has no node affinity constraints

**Before deploying**, ensure:
1. Update `k8s/overlays/eks/kustomization.yaml` with your AWS account ID and region
2. ECR repositories exist in your AWS account
3. Images are pushed to ECR
4. IAM permissions are configured for ECR access

See `k8s/overlays/eks/README.md` for detailed setup instructions.

## Features

- **Base Configuration**: Contains common deployment and service manifests with tolerations for both AKS spot nodes and EKS CriticalAddonsOnly taints. Images default to ACR.
- **AKS Overlay**: Adds required node affinity to target the `spotnode` node pool. Uses Azure Container Registry (ACR) for images.
- **EKS Overlay**: Uses base configuration without node pool constraints. Transforms images to AWS ECR. Uses IAM roles for ECR authentication.
- **Cloud-Agnostic Tolerations**: Both overlays include tolerations that work on both cloud providers.

## Building Manifests

To preview the generated manifests:

```bash
# Preview AKS manifests
kubectl kustomize k8s/overlays/aks/

# Preview EKS manifests
kubectl kustomize k8s/overlays/eks/
```

