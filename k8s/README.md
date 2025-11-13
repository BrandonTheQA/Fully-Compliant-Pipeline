# Kubernetes Deployment Structure

**Note:** This directory is for **local development only** using Minikube. Production deployments use Azure App Service, not Kubernetes.

## Directory Structure

```
k8s/
├── base/                          # Base manifests (shared configurations)
│   ├── kustomization.yaml
│   ├── ecompoc-deployment.yaml
│   ├── ecompoc-service.yaml
│   ├── ui-deployment.yaml
│   └── ui-service.yaml
└── overlays/
    └── minikube/                  # Minikube-specific configuration
        ├── kustomization.yaml
        ├── ecompoc-deployment-patch.yaml
        ├── ui-deployment-patch.yaml
        └── ui-nginx-configmap.yaml
```

## Local Development with Minikube

This Kubernetes configuration is used for local development and testing with Minikube. It is not used for production deployments.

### Deployment Instructions

To deploy to Minikube for local testing:

```bash
kubectl apply -k k8s/overlays/minikube/
```

See `scripts/deploy-minikube.sh` for a complete deployment script that handles building, pushing images, and deploying to Minikube.

## Features

- **Base Configuration**: Contains common deployment and service manifests for local development
- **Minikube Overlay**: Adds Minikube-specific patches and configurations for local testing

## Building Manifests

To preview the generated manifests:

```bash
# Preview Minikube manifests
kubectl kustomize k8s/overlays/minikube/
```

