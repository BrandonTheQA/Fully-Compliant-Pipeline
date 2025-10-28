# Runtime Detection Fix - "Detected function app language: Java"

## 🔴 Critical Issue

Azure is detecting the function apps as **Java** instead of **.NET (C#)**, causing 404 errors on all endpoints.

**Symptom:**
```
GitHub Action logs show: "Detected function app language: Java"
Health endpoints return: HTTP 404 Not Found
```

## 🎯 Root Cause

When migrating from Java to C#, the Azure Function App still has Java runtime settings cached. Azure's Oryx build system is trying to build the apps as Java projects, which fails because:
1. There's no `pom.xml` in the deployment package
2. The runtime setting `FUNCTIONS_WORKER_RUNTIME` is still set to `java` or empty

## ✅ Solution

### Option 1: Quick Fix (Immediate - Run This Now!)

Run the provided script to immediately fix all deployed function apps:

```bash
# From the workspace root
./fix-azure-runtime.sh
```

This script will:
1. ✅ Set `FUNCTIONS_WORKER_RUNTIME=dotnet-isolated` for all function apps
2. ✅ Restart each function app to apply changes
3. ✅ Verify the health endpoints

**OR** manually run these commands:

```bash
# Login to Azure
az login

# Configure User Service
az functionapp config appsettings set \
  --name joaz-func-user-9021-dev \
  --resource-group brandon \
  --settings FUNCTIONS_WORKER_RUNTIME=dotnet-isolated

az functionapp restart \
  --name joaz-func-user-9021-dev \
  --resource-group brandon

# Configure Product Service
az functionapp config appsettings set \
  --name joaz-func-product-9021-dev \
  --resource-group brandon \
  --settings FUNCTIONS_WORKER_RUNTIME=dotnet-isolated

az functionapp restart \
  --name joaz-func-product-9021-dev \
  --resource-group brandon

# Configure Order Service
az functionapp config appsettings set \
  --name joaz-func-order-9021-dev \
  --resource-group brandon \
  --settings FUNCTIONS_WORKER_RUNTIME=dotnet-isolated

az functionapp restart \
  --name joaz-func-order-9021-dev \
  --resource-group brandon
```

### Option 2: Via Azure Portal (Alternative)

For each function app:

1. Go to Azure Portal → Function App
2. Navigate to **Configuration** → **Application Settings**
3. Find or add `FUNCTIONS_WORKER_RUNTIME` setting
4. Set value to: `dotnet-isolated`
5. Click **Save**
6. Click **Restart**

### Option 3: GitHub Actions (Future Deployments)

The workflow has been updated to automatically configure the runtime. On your next deployment:

1. **Added to workflow:** Runtime configuration step before deployment
2. **Added to deployment:** Flags to prevent Oryx from detecting wrong runtime

Changes made to `.github/workflows/workflow.yml`:
- Added `scm-do-build-during-deployment: false`
- Added `enable-oryx-build: false`
- Added runtime configuration step using Azure CLI

## 🔍 Verification

After running the fix, wait 30-60 seconds for the apps to restart, then test:

```bash
# Should return: {"status":"UP"}
curl https://joaz-func-user-9021-dev.azurewebsites.net/api/health

# Should return: {"status":"UP"}
curl https://joaz-func-product-9021-dev.azurewebsites.net/api/health

# Should return: {"status":"UP"}
curl https://joaz-func-order-9021-dev.azurewebsites.net/api/health
```

## 🔧 Troubleshooting

### Still Getting 404?

1. **Wait longer:** Function apps can take up to 2 minutes to fully restart
2. **Check Application Insights:**
   ```bash
   az monitor app-insights events show \
     --app <app-insights-name> \
     --type traces \
     --start-time "30 minutes ago"
   ```

3. **Check Function App logs:**
   ```bash
   az functionapp log tail \
     --name joaz-func-user-9021-dev \
     --resource-group brandon
   ```

4. **Verify runtime setting:**
   ```bash
   az functionapp config appsettings list \
     --name joaz-func-user-9021-dev \
     --resource-group brandon \
     --query "[?name=='FUNCTIONS_WORKER_RUNTIME'].value" \
     --output tsv
   ```
   Should output: `dotnet-isolated`

### Still Showing "Java" in Deployment Logs?

This is a deployment issue. The deployment package needs to be clean:

1. **Ensure publish folder is clean:**
   ```bash
   cd api/functionapp/user
   rm -rf bin obj publish
   dotnet publish --configuration Release --output ./publish
   ```

2. **Check for Java artifacts:**
   ```bash
   # Should return nothing
   find ./publish -name "*.jar" -o -name "pom.xml"
   ```

3. **Verify .NET artifacts exist:**
   ```bash
   # Should show DLLs and User.dll
   ls -la ./publish/*.dll
   ```

### Function App Not Starting?

Check the Application Settings in Azure Portal:

**Required Settings:**
- `FUNCTIONS_WORKER_RUNTIME` = `dotnet-isolated`
- `FUNCTIONS_EXTENSION_VERSION` = `~4`
- `WEBSITE_RUN_FROM_PACKAGE` = `1` (if using package deployment)

**Optional but Recommended:**
- `WEBSITE_ENABLE_SYNC_UPDATE_SITE` = `true`
- `WEBSITE_CONTENTAZUREFILECONNECTIONSTRING` = `<storage connection string>`

## 📝 What Changed in Workflow

### Before (Incorrect):
```yaml
- name: Deploy to Azure Functions
  uses: Azure/functions-action@v1
  with:
    app-name: ${{ matrix.function-app-name-dev }}
    package: ${{ matrix.working-dir }}/publish
```

### After (Correct):
```yaml
- name: Configure Function App Runtime
  run: |
    az functionapp config appsettings set \
      --name ${{ matrix.function-app-name-dev }} \
      --resource-group brandon \
      --settings FUNCTIONS_WORKER_RUNTIME=dotnet-isolated

- name: Deploy to Azure Functions
  uses: Azure/functions-action@v1
  with:
    app-name: ${{ matrix.function-app-name-dev }}
    package: ${{ matrix.working-dir }}/publish
    respect-funcignore: true
    scm-do-build-during-deployment: false
    enable-oryx-build: false
```

## 🎓 Why This Happens

### Migration Context
When migrating from Java to C#:
1. Azure Function App retains old configuration
2. Kudu/Oryx build system has cached detection
3. `FUNCTIONS_WORKER_RUNTIME` may be empty or set to `java`
4. Without explicit runtime, Azure tries to auto-detect (finds old Java artifacts)

### .NET Isolated Worker Requirements
- Needs explicit `FUNCTIONS_WORKER_RUNTIME=dotnet-isolated`
- Does NOT use extension bundles (unlike in-process)
- Requires pre-built binaries (not built in Azure)

## 📋 Checklist

Run through this checklist to ensure everything is configured:

- [ ] `FUNCTIONS_WORKER_RUNTIME=dotnet-isolated` set for all function apps
- [ ] All function apps restarted
- [ ] Health endpoints return 200 OK
- [ ] No `.jar` or `pom.xml` files in deployment package
- [ ] Workflow includes runtime configuration step
- [ ] Workflow disables Oryx build (`scm-do-build-during-deployment: false`)

## 🚀 Next Steps

1. **Immediate Action:**
   ```bash
   ./fix-azure-runtime.sh
   ```

2. **Verify Fix:**
   ```bash
   curl https://joaz-func-user-9021-dev.azurewebsites.net/api/health
   ```

3. **Future Deployments:**
   The updated workflow will handle this automatically

4. **Repeat for Other Environments:**
   Update the script for test/stage/prod as needed

---

**Status:** 🔴 Critical - Blocking Deployment  
**Priority:** P0 - Immediate Fix Required  
**Impact:** All endpoints returning 404  
**Resolution Time:** 2-3 minutes  
**Prevention:** Workflow updated to auto-configure
