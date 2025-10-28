# ⚡ IMMEDIATE ACTION REQUIRED - 404 Fix

## 🔴 Problem
Health endpoints returning **404** because Azure detects function apps as **Java** instead of **.NET**

## ✅ Solution (Takes 2 Minutes)

### **Run This Command Now:**

```bash
# Make the script executable (if not already)
chmod +x fix-azure-runtime.sh

# Run the fix
./fix-azure-runtime.sh
```

### **OR Manually Run These Commands:**

```bash
# 1. Set runtime for User Service
az functionapp config appsettings set \
  --name joaz-func-user-9021-dev \
  --resource-group brandon \
  --settings FUNCTIONS_WORKER_RUNTIME=dotnet-isolated

az functionapp restart \
  --name joaz-func-user-9021-dev \
  --resource-group brandon

# 2. Set runtime for Product Service  
az functionapp config appsettings set \
  --name joaz-func-product-9021-dev \
  --resource-group brandon \
  --settings FUNCTIONS_WORKER_RUNTIME=dotnet-isolated

az functionapp restart \
  --name joaz-func-product-9021-dev \
  --resource-group brandon

# 3. Set runtime for Order Service
az functionapp config appsettings set \
  --name joaz-func-order-9021-dev \
  --resource-group brandon \
  --settings FUNCTIONS_WORKER_RUNTIME=dotnet-isolated

az functionapp restart \
  --name joaz-func-order-9021-dev \
  --resource-group brandon
```

## ⏱️ Wait & Test

**Wait:** 30-60 seconds for apps to restart

**Test:**
```bash
curl https://joaz-func-user-9021-dev.azurewebsites.net/api/health
curl https://joaz-func-product-9021-dev.azurewebsites.net/api/health
curl https://joaz-func-order-9021-dev.azurewebsites.net/api/health
```

**Expected:** `{"status":"UP"}` from all three

## 📊 What Gets Fixed

| Before | After |
|--------|-------|
| ❌ Runtime: Java | ✅ Runtime: dotnet-isolated |
| ❌ 404 on all endpoints | ✅ 200 on all endpoints |
| ❌ Smoke tests fail | ✅ Smoke tests pass |
| ❌ Pipeline blocked | ✅ Pipeline continues |

## 🔄 For Future Deployments

The GitHub Actions workflow has been updated to automatically set this on future deployments. This manual fix is only needed **once** for existing function apps.

## 📚 More Details

See these files for complete information:
- [RUNTIME_FIX.md](./RUNTIME_FIX.md) - Detailed troubleshooting
- [DEPLOYMENT_FIX.md](./DEPLOYMENT_FIX.md) - All fixes applied
- [MIGRATION_SUMMARY.md](./MIGRATION_SUMMARY.md) - Full migration details

---

**TL;DR:** Run `./fix-azure-runtime.sh` → Wait 60 seconds → Test endpoints → ✅ Fixed!
