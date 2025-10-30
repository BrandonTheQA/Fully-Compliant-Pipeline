# AKS Deployment and GitHub Actions Setup

This repository deploys the `order`, `product`, and `user` Spring Boot services to Azure Kubernetes Service (AKS) using GitHub Actions.

## Azure resources
- AKS: existing cluster in resource group `RG-POC` (cluster name discovered at deploy time)
- ACR: `rgpocacr9021` (`rgpocacr9021.azurecr.io`)

## GitHub configuration
Define the following Repository Variables (Actions → Variables):
- `AZURE_SUBSCRIPTION_ID`: Subscription GUID
- `AZURE_TENANT_ID`: Entra tenant GUID
- `AZURE_CLIENT_ID`: App registration (workload identity) client ID
- `AKS_RESOURCE_GROUP`: `RG-POC` (optional)

No classic Secrets are required if using OIDC with `azure/login`.

## Entra ID and role assignments
Create or use an existing App Registration for GitHub Actions and configure a Federated Credential:
- Entity: GitHub Actions
- Repository: this repo
- Branch: `main`
- Subject: `repo:<owner>/<repo>:ref:refs/heads/main`

Grant the identity permissions:
- On ACR `rgpocacr9021`: `AcrPush`
- On AKS cluster (resource scope): `Azure Kubernetes Service RBAC Writer` (or `Contributor` during bootstrap)

## Workflow
- File: `.github/workflows/ci-cd.yml`
- On push/PR to `main`:
  - Build and push Docker images to ACR with tags `${{ github.sha }}` and `latest`
  - Discover AKS in `RG-POC`, set kube context
  - Ensure namespace `dev` exists; apply manifests in `k8s/*`
  - Update Deployments to the `${{ github.sha }}` image tags

## Kubernetes
- Namespace: `dev`
- Services: `LoadBalancer` type for external access
- Manifests in `k8s/<service>/{deployment,service}.yaml`
- Health checks: `/actuator/health`

## Environments beyond dev (TODOs)
- Add `staging` and `prod` jobs to the workflow:
  - Namespaces: `staging`, `prod`
  - Manual approvals/environments
  - Optional Ingress and TLS per environment
  - Image promotion via retagging (avoid rebuilds)

## Accessing services
After deployment, retrieve external IPs:
```
kubectl get svc -n dev
```
Endpoints will be on port 80 forwarding to container port 8080.
