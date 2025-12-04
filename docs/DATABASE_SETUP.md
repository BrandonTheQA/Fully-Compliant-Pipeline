# Database Setup Guide

This guide explains how to configure the Azure SQL Database connection for the services and securely set up the password via GitHub Secrets.

## Overview

All services (user, product, and order) now use a shared Azure SQL Database instead of in-memory storage. Each service uses Liquibase for database schema management.

## Database Connection

**Note:** The server, database name, and username shown below are **examples** for demonstration purposes. In production, these should be configured via environment variables and not hardcoded in the repository.

The services connect to:
- **Server**: `sqlserverpoc121212.database.windows.net` (example - use environment variable in production)
- **Database**: `pocdb` (example)
- **Username**: `bgarlock` (example - use environment variable in production)
- **Password**: Set via GitHub Secret (see below)

## Setting Up GitHub Secret

The database password must be configured as a GitHub Secret to securely deploy services to Kubernetes.

### Steps to Add GitHub Secret

1. **Navigate to GitHub Repository Settings**
   - Go to your repository on GitHub
   - Click on **Settings** (in the repository, not your profile settings)
   - In the left sidebar, click on **Secrets and variables** → **Actions**

2. **Add New Secret**
   - Click **New repository secret**
   - Name: `SQL_SERVER_PASSWORD`
   - Value: Enter your Azure SQL Database password (the password for the database user)
   - Click **Add secret**
   
   **Note:** The username `bgarlock` shown in examples is for demonstration purposes only. Use your actual database credentials.

3. **Verify Secret**
   - The secret should now appear in your secrets list
   - Note: Once saved, you cannot view the secret value again (for security)
   - To update it, delete and recreate the secret

## How It Works

### Development Environment

When deploying to the `dev` environment via GitHub Actions:

1. The workflow reads `SQL_SERVER_PASSWORD` from GitHub Secrets
2. Creates a Kubernetes secret named `sql-db-secret` in the `dev` namespace
3. Each service deployment references this secret via environment variable
4. The application reads `SQL_SERVER_PASSWORD` environment variable and uses it in the JDBC connection string

### Local Development

For local development, set the environment variable:

```bash
export SQL_SERVER_PASSWORD=your_password_here
```

Or create a `.env` file (not committed to git) with:
```
SQL_SERVER_PASSWORD=your_password_here
```

## Kubernetes Secret

The Kubernetes secret is automatically created by the GitHub Actions workflow in the `deploy-dev` job. The secret is created with:

```yaml
name: sql-db-secret
namespace: dev
data:
  password: <base64-encoded-password>
```

Each service deployment references this secret:
```yaml
env:
  - name: SQL_SERVER_PASSWORD
    valueFrom:
      secretKeyRef:
        name: sql-db-secret
        key: password
```

## Manual Secret Creation (Optional)

If you need to manually create the Kubernetes secret, you can run:

```bash
kubectl create secret generic sql-db-secret \
  --from-literal=password="your_password_here" \
  --namespace=dev
```

## Security Best Practices

1. **Never commit passwords to the repository**
2. **Use GitHub Secrets for CI/CD pipelines**
3. **Rotate passwords periodically**
4. **Use different passwords for different environments (dev, staging, prod)**
5. **Limit access to GitHub Secrets to authorized personnel only**

## Troubleshooting

### Service fails to start with database connection error

1. Verify the GitHub secret `SQL_SERVER_PASSWORD` is set correctly
2. Check that the Kubernetes secret exists:
   ```bash
   kubectl get secret sql-db-secret -n dev
   ```
3. Verify the secret contains the password:
   ```bash
   kubectl get secret sql-db-secret -n dev -o jsonpath='{.data.password}' | base64 -d
   ```
4. Check service logs:
   ```bash
   kubectl logs -n dev deployment/user
   kubectl logs -n dev deployment/product
   kubectl logs -n dev deployment/order
   ```

### Database connection string issues

The connection string format is:
```
jdbc:sqlserver://sqlserverpoc121212.database.windows.net:1433;database=pocdb;encrypt=true;trustServerCertificate=false;loginTimeout=30
```

With username and password:
- **Username**: Set via environment variable (example shown: `bgarlock` - replace with your actual database username)
- **Password**: Set via `SQL_SERVER_PASSWORD` environment variable

Make sure:
- The server name is correct
- The database name is correct
- The username is correct
- The password is set correctly in GitHub Secrets or environment variables
- Network access to Azure SQL is allowed (check firewall rules)

