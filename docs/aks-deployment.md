# Deploying Java Services to Azure Kubernetes Service

This guide covers building container images for the Java backend services and deploying them to an existing Azure Kubernetes Service (AKS) cluster.

## 1. Prerequisites

- Docker CLI (with access to your Azure Container Registry (ACR))
- kubectl configured to talk to your AKS cluster (`az aks get-credentials ...`)
- Optional: `az` CLI for logging in and attaching to AKS/ACR

## 2. Build and Push Images

Run the helper script from the repository root, passing your registry name (e.g. `myregistry.azurecr.io`) and an optional image tag.

```bash
./scripts/build-images.sh myregistry.azurecr.io v1.0.0
```

This builds and pushes the following images:

- `myregistry.azurecr.io/user-service:v1.0.0`
- `myregistry.azurecr.io/product-service:v1.0.0`
- `myregistry.azurecr.io/order-service:v1.0.0`

The script defaults the tag to the current Git commit shorthand when the second argument is omitted.

## 3. Configure Secrets (one-time)

If your registry requires authentication, create an image pull secret in the AKS namespace:

```bash
kubectl create namespace retail-platform
kubectl create secret docker-registry acr-credentials \
  --namespace retail-platform \
  --docker-server myregistry.azurecr.io \
  --docker-username <service-principal-or-username> \
  --docker-password <password>
```

Uncomment the `imagePullSecrets` section in `api/functionapp/k8s/*.yaml` if you need the deployments to reference this secret.

## 4. Deploy to AKS

Apply the manifests and point the deployments at the freshly-built images:

```bash
./scripts/deploy-aks.sh myregistry.azurecr.io v1.0.0 retail-platform
```

The script:

1. Applies `namespace.yaml`, `user-service.yaml`, `product-service.yaml`, and `order-service.yaml`
2. Updates the deployments to use images tagged `v1.0.0` from the provided registry

Monitor rollouts with:

```bash
kubectl -n retail-platform rollout status deployment/order-service
```

## 5. Networking

The Kubernetes services are of type `ClusterIP`. Expose them via an ingress controller, API gateway or Azure Application Gateway depending on your topology. Update the frontend (`ui/.env.*`) to target the chosen public endpoints.

## 6. Environment Configuration

The services rely on the following environment variables (defaults shown):

- `SPRING_PROFILES_ACTIVE=prod`
- `SERVER_PORT=8080`
- `JAVA_OPTS=-Xms256m -Xmx512m`
- `USER_SERVICE_HOST=user-service`
- `USER_SERVICE_PORT=8080`
- `PRODUCT_SERVICE_HOST=product-service`
- `PRODUCT_SERVICE_PORT=8080`

Adjust these in the deployment manifests if needed. Local development defaults to ports `8080` (user), `8081` (product), and `8082` (order).

## 7. GitHub Actions (Optional)

For CI/CD, create a workflow that:

1. Logs into Azure (using a service principal stored in `AZURE_CREDENTIALS`)
2. Builds and pushes the three images (reuse `scripts/build-images.sh`)
3. Uses `scripts/deploy-aks.sh` (or inline commands) to roll out the new version

Required secrets:

- `AZURE_CREDENTIALS`: JSON output of `az ad sp create-for-rbac`
- `ACR_NAME`: Container registry (e.g. `myregistry`)
- `AKS_RESOURCE_GROUP` and `AKS_CLUSTER_NAME`
- Optional `AKS_NAMESPACE` (defaults to `retail-platform`)

See `scripts/` and `api/functionapp/k8s/` for concrete commands.
