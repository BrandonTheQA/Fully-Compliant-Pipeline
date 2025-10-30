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
Workflows:
- `.github/workflows/order-service.yml`
- `.github/workflows/product-service.yml`
- `.github/workflows/user-service.yml`

Pipeline outline:
1) Build/test with JDK 17 and Maven
2) Discover AKS and ACR in resource group `RG-POC`
3) Login to ACR, build/push image `<acr>.azurecr.io/{service}:${GIT_SHA}`
4) Set AKS context; apply manifests; wait for rollout

Required secrets/vars:
- `AZURE_CREDENTIALS` (OIDC or SP JSON)

Optional org/repo vars (if you want to pin):
- `AZ_SUBSCRIPTION_ID`, `AKS_NAME`, `ACR_NAME`, `AZURE_RESOURCE_GROUP=RG-POC`

## Notes
- All Azure Functions/App Service deployment artifacts have been removed.
- Services are internal-only (no Ingress). Consumers should use cluster DNS:
  - `http://order-service.app-services.svc.cluster.local`
  - `http://product-service.app-services.svc.cluster.local`
  - `http://user-service.app-services.svc.cluster.local`
