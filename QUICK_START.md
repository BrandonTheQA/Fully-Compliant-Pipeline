# Quick Start Guide - C# Azure Functions

## Prerequisites

- [.NET 8 SDK](https://dotnet.microsoft.com/download/dotnet/8.0)
- [Azure Functions Core Tools](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local)
- [Visual Studio Code](https://code.visualstudio.com/) (recommended) or Visual Studio 2022

## Local Development

### Running a Function App

```bash
# Navigate to the function app directory
cd api/functionapp/user  # or order, product

# Restore dependencies
dotnet restore

# Build the project
dotnet build

# Run the function locally
func start
```

The function will be available at `http://localhost:7071`

### Running All Three Function Apps

Open three terminal windows:

**Terminal 1 - User Service (Port 7071):**
```bash
cd api/functionapp/user
func start
```

**Terminal 2 - Product Service (Port 7072):**
```bash
cd api/functionapp/product
func start --port 7072
```

**Terminal 3 - Order Service (Port 7073):**
```bash
cd api/functionapp/order
func start --port 7073
```

### Testing Endpoints

**User Service:**
```bash
# Create a user
curl -X POST http://localhost:7071/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","password":"password123"}'

# Get user
curl http://localhost:7071/api/users/{userId}

# Login
curl -X POST http://localhost:7071/api/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'

# Health check
curl http://localhost:7071/api/health

# Swagger UI
open http://localhost:7071/api/swagger-ui.html
```

**Product Service:**
```bash
# Create a product
curl -X POST http://localhost:7072/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"Gaming laptop","price":999.99,"quantity":10,"category":"Electronics"}'

# Get all products
curl http://localhost:7072/api/products

# Get product by ID
curl http://localhost:7072/api/products/{productId}

# Health check
curl http://localhost:7072/api/health

# Swagger UI
open http://localhost:7072/api/swagger-ui.html
```

**Order Service:**
```bash
# Create an order
curl -X POST http://localhost:7073/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-id","items":[{"productId":"product-id","quantity":2}]}'

# Get order
curl http://localhost:7073/api/orders/{orderId}

# Get user orders
curl http://localhost:7073/api/orders/user/{userId}

# Health check
curl http://localhost:7073/api/health

# Swagger UI
open http://localhost:7073/api/swagger-ui.html
```

## Project Structure

Each function app follows this structure:

```
{service}/
├── {Service}.csproj           # Project configuration
├── Program.cs                 # Entry point & DI setup
├── host.json                  # Azure Functions config
├── local.settings.json        # Local settings
├── DTOs/                      # Request/Response models
├── Models/                    # Domain models
├── Repositories/              # Data access
├── Services/                  # Business logic
├── Exceptions/                # Custom exceptions
└── Functions/                 # HTTP-triggered functions
```

## Common Commands

### Build & Test
```bash
# Restore NuGet packages
dotnet restore

# Build in Release mode
dotnet build --configuration Release

# Run tests (when available)
dotnet test

# Publish for deployment
dotnet publish --configuration Release --output ./publish
```

### Clean Build
```bash
# Clean build artifacts
dotnet clean

# Remove bin and obj folders
rm -rf bin obj
```

## Configuration

### Environment Variables

Edit `local.settings.json` in each function app:

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "dotnet-isolated",
    "USER_SERVICE_URL": "http://localhost:7071/api",
    "PRODUCT_SERVICE_URL": "http://localhost:7072/api",
    "ENVIRONMENT": "local"
  }
}
```

## Debugging

### Visual Studio Code

1. Install C# extension for VS Code
2. Open function app folder
3. Press F5 to start debugging
4. Set breakpoints in your code

### Visual Studio 2022

1. Open the `.csproj` file
2. Press F5 to start debugging
3. Set breakpoints in your code

## Adding New Functions

To add a new HTTP-triggered function:

```csharp
[Function("myNewFunction")]
public async Task<HttpResponseData> MyNewFunction(
    [HttpTrigger(AuthorizationLevel.Anonymous, "get", "post", Route = "my-route")] 
    HttpRequestData req)
{
    var response = req.CreateResponse(HttpStatusCode.OK);
    await response.WriteAsJsonAsync(new { message = "Hello World" });
    return response;
}
```

## Troubleshooting

### Port Already in Use
```bash
# Find process using port
lsof -i :7071  # on macOS/Linux
netstat -ano | findstr :7071  # on Windows

# Kill the process
kill -9 <PID>  # on macOS/Linux
taskkill /PID <PID> /F  # on Windows
```

### Function Not Found
- Make sure the function name in `[Function("name")]` attribute is unique
- Ensure the project builds successfully
- Check `host.json` configuration

### Dependencies Not Resolving
```bash
# Clear NuGet cache
dotnet nuget locals all --clear

# Restore packages
dotnet restore
```

### Azure Storage Connection
For local development, you can use:
- Azurite storage emulator (recommended)
- Or set `AzureWebJobsStorage` to `UseDevelopmentStorage=true`

## Deployment

### Using GitHub Actions (Automatic)

Push to `main` branch:
```bash
git add .
git commit -m "Your changes"
git push origin main
```

The workflow will automatically:
1. Build all function apps
2. Run tests and code analysis
3. Deploy to Dev
4. Run smoke tests
5. Deploy to Test
6. Run integration tests
7. Continue through Stage to Production

### Manual Deployment

```bash
# Login to Azure
az login

# Deploy function app
func azure functionapp publish joaz-func-{service}-9021-dev
```

## Integration Testing

Run Postman collection:
```bash
# Install Newman
npm install -g newman

# Run tests
newman run postman/IntegrationTest.postman_collection.json \
  --env-var "userBaseUrl=http://localhost:7071/api" \
  --env-var "productBaseUrl=http://localhost:7072/api" \
  --env-var "orderBaseUrl=http://localhost:7073/api"
```

## Resources

- [Azure Functions C# Guide](https://learn.microsoft.com/en-us/azure/azure-functions/dotnet-isolated-process-guide)
- [.NET 8 Documentation](https://learn.microsoft.com/en-us/dotnet/core/whats-new/dotnet-8)
- [Azure Functions Best Practices](https://learn.microsoft.com/en-us/azure/azure-functions/functions-best-practices)

## Support

For issues or questions:
1. Check the [MIGRATION_SUMMARY.md](./MIGRATION_SUMMARY.md) for detailed migration notes
2. Review Azure Functions logs in Application Insights
3. Check GitHub Actions workflow runs for CI/CD issues
