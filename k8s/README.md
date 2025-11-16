# Kubernetes Deployment Structure

**Note:** This directory contains Kubernetes manifests from an earlier local Kubernetes workflow. Production deployments now use Azure App Service, and the recommended local workflow is to run services directly on `localhost` ports.

These manifests are retained for reference only and are not part of the standard development or CI/CD flow.

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
    └── minikube/                  # Legacy overlay for local Kubernetes development
        ├── kustomization.yaml
        ├── ecompoc-deployment-patch.yaml
        └── ui-deployment-patch.yaml
```

## Current Recommendation

For day‑to‑day development and testing:

- Run the backend service locally (e.g., `mvn spring-boot:run` for the ecompoc service)
- Run the UI locally (e.g., `npm run dev -- --port 8084`)
- Use:
  - `scripts/run-local-e2e.sh` for Postman/Newman integration tests against `http://localhost:8080/api`
  - `selenium/run-selenium-tests.sh` for Selenium UI tests against `http://localhost:8084`

Kubernetes manifests in this directory are optional and intended only for advanced or legacy scenarios.


