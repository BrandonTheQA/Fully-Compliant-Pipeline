# Deployment and GitHub Actions Setup

This repository deploys the ecompoc service and UI application to Azure App Service using GitHub Actions.

## Local Development and Testing

### Pre-commit Hook

This repository includes a pre-commit hook that automatically runs security scans and unit tests before each commit.

**Installation:**
```bash
./scripts/install-hooks.sh
```

**What it does:**
1. Runs TruffleHog high‑severity secret scan
2. Runs unit tests for all Java services (user, product, order) using Maven
3. Runs unit tests for UI using Jest
4. Aborts the commit if any of the above checks fail

**Usage:**
- The hook runs automatically on `git commit`

**Requirements:**
- Maven (mvn) must be installed
- npm must be installed

### Local Integration Testing (Postman / Newman)

For local end‑to‑end API testing, you can use the `scripts/run-local-e2e.sh` helper script. It will:

1. Start the ecompoc backend on `http://localhost:8080` using `mvn spring-boot:run` with the `local` profile
2. Start the UI on `http://localhost:8084` using `npm run dev -- --port 8084`
3. Wait for the services to be ready
4. Run the Postman collection via Newman against `http://localhost:8080/api`

Run:

```bash
./scripts/run-local-e2e.sh
```

You can also start services manually (for example, from your IDE) and run Newman yourself as long as you target the same local URLs (`http://localhost:<ports>`).

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
