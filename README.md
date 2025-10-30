# Services on AKS (internal-only)

## Modules
- api/order-service (Spring Boot 3, JDK 17)
- api/product-service (Spring Boot 3, JDK 17)
- api/user-service (Spring Boot 3, JDK 17)
- k8s/* (ClusterIP Services + Deployments in namespace `app-services`)

## Local development
- Prereqs: Java 17, Maven 3.9+
- Run tests:
  - `mvn -B -ntp test` in each service directory
- Run service:
  - `mvn spring-boot:run` (defaults to port 8080)

## Containerization
Each service includes a multistage Dockerfile (Temurin 17):
```
cd api/order-service && docker build -t order-service:dev .
```

## Kubernetes
Internal-only exposure via ClusterIP:
- `k8s/{order,product,user}/deployment.yaml`
- `k8s/{order,product,user}/service.yaml`
Namespace: `app-services` (created by workflows if missing).

## CI/CD (GitHub Actions)
Workflow:
- `.github/workflows/aks-deploy.yml`

Pipeline outline:
1) Build/test with JDK 17 and Maven (per service)
2) Login to ACR, build/push image `${ACR_LOGIN_SERVER}/{service}:${GITHUB_SHA}`
3) Set AKS context; template manifests via envsubst; apply; wait for rollout

Required secrets (OpenID Connect):
- `AZURE_CLIENT_ID`
- `AZURE_TENANT_ID`
- `AZURE_SUBSCRIPTION_ID`

Required repo variables:
- `ACR_NAME` (e.g., myregistry)
- `ACR_LOGIN_SERVER` (e.g., myregistry.azurecr.io)
- `AKS_RESOURCE_GROUP`
- `AKS_CLUSTER_NAME`
- `AKS_NAMESPACE` (e.g., app-services)

Add these in GitHub: Settings → Secrets and variables → Actions.

## Notes
- All Azure Functions and App Service deployment artifacts for backends have been removed.
- Services are internal-only (no Ingress). Consumers should use cluster DNS:
  - `http://order-service.app-services.svc.cluster.local`
  - `http://product-service.app-services.svc.cluster.local`
  - `http://user-service.app-services.svc.cluster.local`
