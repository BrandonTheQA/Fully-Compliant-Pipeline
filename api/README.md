# Backend Services

This directory contains three Spring Boot services that power the platform's backend APIs:

- `user-service`: user registration and profile APIs (default port 8080)
- `product-service`: product catalogue APIs (default port 8081)
- `order-service`: order workflow APIs (default port 8082)

## Running locally

Each service can be started with:

```bash
cd api/functionapp/<service>
mvn spring-boot:run
```

Override the default ports by setting `SERVER_PORT`. The services automatically discover each other via the environment variables defined in `application.yml` (see `services.*` properties).

## Container builds

Multi-stage Dockerfiles are provided under each service directory. Build locally with:

```bash
docker build -t <registry>/<service>-service:<tag> api/functionapp/<service>
```

To build and push all images at once, use `./scripts/build-images.sh` from the repository root.

## Kubernetes deployment

Manifests for AKS live in `api/functionapp/k8s`. Apply them manually or through the provided deployment script:

```bash
./scripts/deploy-aks.sh <registry> <tag> [namespace]
```

Refer to `docs/aks-deployment.md` for full deployment guidance and CI/CD integration tips.
