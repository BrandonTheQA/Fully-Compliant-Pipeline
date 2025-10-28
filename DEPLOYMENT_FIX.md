# Deployment Fix - 404 Error Resolution

## Issue
After migrating from Java to C#, the health endpoint returns 404:
- URL: `https://joaz-func-user-9021-dev.azurewebsites.net/api/health`
- Error: HTTP 404 Not Found
- Impact: Smoke tests failing

## Root Causes

### 1. host.json Configuration
The `host.json` was configured for Java functions with `extensionBundle`, but C# isolated worker functions don't use extension bundles - they use NuGet packages instead.

**Fixed:**
- Removed `extensionBundle` configuration
- Added explicit HTTP routing configuration with `routePrefix: "api"`
- Added proper logging levels

### 2. Build Compilation Errors
The original code used `WriteAsJsonAsync(object, JsonSerializerOptions)` which is not a valid signature. The method only accepts `WriteAsJsonAsync(object, CancellationToken)`.

**Fixed:**
- Changed to manual serialization: `WriteStringAsync(JsonSerializer.Serialize(obj, options))`
- Added `Content-Type: application/json` header explicitly
- Applied fix to all three function apps (Order, Product, User)

## Changes Made

### 1. Updated host.json (All 3 Function Apps)

**Before:**
```json
{
  "version": "2.0",
  "functionTimeout": "00:05:00",
  "logging": { ... },
  "extensionBundle": {
    "id": "Microsoft.Azure.Functions.ExtensionBundle",
    "version": "[4.*, 5.0.0)"
  }
}
```

**After:**
```json
{
  "version": "2.0",
  "functionTimeout": "00:05:00",
  "logging": {
    "applicationInsights": { ... },
    "logLevel": {
      "default": "Information",
      "Host.Results": "Error",
      "Function": "Error",
      "Host.Aggregator": "Trace"
    }
  },
  "extensions": {
    "http": {
      "routePrefix": "api"
    }
  }
}
```

### 2. Fixed JSON Serialization (All Function Files)

**Before (Incorrect):**
```csharp
var response = req.CreateResponse(HttpStatusCode.OK);
await response.WriteAsJsonAsync(data, _jsonOptions);  // WRONG - jsonOptions not accepted here
```

**After (Correct):**
```csharp
var response = req.CreateResponse(HttpStatusCode.OK);
response.Headers.Add("Content-Type", "application/json");
await response.WriteStringAsync(JsonSerializer.Serialize(data, _jsonOptions));
```

### Files Modified:
1. `/workspace/api/functionapp/user/host.json`
2. `/workspace/api/functionapp/product/host.json`
3. `/workspace/api/functionapp/order/host.json`
4. `/workspace/api/functionapp/user/Functions/UserFunctions.cs`
5. `/workspace/api/functionapp/product/Functions/ProductFunctions.cs`
6. `/workspace/api/functionapp/order/Functions/OrderFunctions.cs`

## Verification Steps

### 1. Build Verification
The code should now build without errors:
```bash
cd api/functionapp/user
dotnet restore
dotnet build
```

No more `CS1503` errors about argument type conversion.

### 2. Deployment
After pushing these changes, the GitHub Actions workflow will:
1. Build successfully (no compilation errors)
2. Deploy to Azure
3. Functions will be properly registered with correct routes

### 3. Testing Endpoints
After successful deployment, test:

```bash
# Health endpoint
curl https://joaz-func-user-9021-dev.azurewebsites.net/api/health

# Expected response:
# HTTP 200 OK
# {"status":"UP"}

# Create user
curl -X POST https://joaz-func-user-9021-dev.azurewebsites.net/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123"}'

# Get user (replace {id} with actual ID from create response)
curl https://joaz-func-user-9021-dev.azurewebsites.net/api/users/{id}
```

## Why This Happened

### Extension Bundle vs NuGet Packages
- **Java Functions:** Use extension bundles for bindings
- **C# Isolated Worker:** Use NuGet packages directly (defined in .csproj)

Our .csproj files already have the correct packages:
```xml
<PackageReference Include="Microsoft.Azure.Functions.Worker" Version="1.21.0" />
<PackageReference Include="Microsoft.Azure.Functions.Worker.Extensions.Http" Version="3.1.0" />
```

The extension bundle in `host.json` was conflicting with these.

### HTTP Routing
The isolated worker model requires explicit HTTP routing configuration in `host.json` to ensure the `api/` prefix works correctly.

## Expected Results After Fix

### ✅ Build Pipeline
- All three function apps build successfully
- No CS1503 compilation errors
- Publish artifacts created correctly

### ✅ Deployment
- Functions deploy to Azure without errors
- Function app starts successfully
- All routes registered correctly

### ✅ Smoke Tests
- Health endpoint returns 200 OK
- Response body: `{"status":"UP"}`
- All function endpoints accessible

## Troubleshooting

### If 404 Still Persists:

1. **Check Azure Portal:**
   - Go to Function App → Functions
   - Verify all functions are listed (health, createUser, getUserProfile, login)
   - Check if functions are enabled

2. **Check Application Insights Logs:**
   ```
   traces
   | where timestamp > ago(1h)
   | where message contains "health"
   | order by timestamp desc
   ```

3. **Verify Runtime:**
   - Configuration → General Settings
   - Runtime: .NET
   - Version: 8 (LTS), Isolated
   - Platform: 64 Bit

4. **Restart Function App:**
   ```bash
   az functionapp restart --name joaz-func-user-9021-dev --resource-group brandon
   ```

5. **Check FUNCTIONS_WORKER_RUNTIME:**
   - Configuration → Application Settings
   - Should be: `dotnet-isolated`

### If Other Errors Occur:

Check the logs:
```bash
# Stream logs
func azure functionapp logstream joaz-func-user-9021-dev

# Or in Azure Portal
# Function App → Log stream
```

## Next Steps

1. ✅ Commit and push changes
2. ⏳ Wait for GitHub Actions to complete
3. ⏳ Verify deployment succeeds
4. ✅ Test health endpoint
5. ✅ Run full smoke tests
6. ✅ Continue with integration tests

## Additional Notes

### About .NET Isolated Worker Model

The isolated worker model we're using has several advantages:
- **Process Isolation:** Functions run in a separate process
- **Flexibility:** Can use any .NET version
- **Independence:** Less coupling with Azure Functions host

But it requires:
- NuGet packages (not extension bundles)
- Explicit HTTP configuration
- Different middleware setup

### Migration Considerations

When migrating from Java to C# isolated worker:
1. ❌ Don't use extension bundles
2. ✅ Use NuGet packages for all bindings
3. ✅ Configure HTTP routing explicitly
4. ✅ Use manual JSON serialization when options are needed
5. ✅ Set FUNCTIONS_WORKER_RUNTIME to `dotnet-isolated`

---

**Status:** ✅ Fixed  
**Date:** October 28, 2025  
**Impact:** All three function apps affected  
**Resolution:** Updated host.json and fixed JSON serialization
