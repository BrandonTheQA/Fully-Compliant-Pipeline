# Deployment and GitHub Actions Setup

This repository deploys the ecompoc service and UI application to Azure App Service using GitHub Actions.

## Local Development with Minikube

### Pre-commit Hook

This repository includes a pre-commit hook that automatically runs unit tests and minikube deployment tests before each commit. This ensures that all changes work correctly and maintain code quality.

**Installation:**
```bash
./scripts/install-hooks.sh
```

**What it does:**
1. Runs unit tests for all Java services (user, product, order) using Maven
2. Runs unit tests for UI using Jest
3. If unit tests pass, deploys all services to minikube and runs Postman integration tests
4. If any tests fail, the commit is aborted

**Usage:**
- The hook runs automatically on `git commit`
- To skip tests: `SKIP_MINIKUBE_TESTS=1 git commit`

**Requirements:**
- Maven (mvn) must be installed
- npm must be installed
- Minikube must be running (`minikube start`)
- All prerequisites from `scripts/deploy-minikube.sh` must be installed

**Manual Testing:**
```bash
# Deploy and test
./scripts/deploy-minikube.sh

# Cleanup
./scripts/deploy-minikube.sh --cleanup
```

## Azure App Service Deployment

### Workflow
- File: `.github/workflows/ci-cd-appservice.yml`
- On push/PR to `main`:
  - Builds the ecompoc service JAR artifact
  - Builds the UI application bundle
  - Deploys both to Azure App Service

### Build Optimization
- Maven dependencies are cached in GitHub Actions to speed up build times
- Cache is automatically invalidated when pom.xml files change
