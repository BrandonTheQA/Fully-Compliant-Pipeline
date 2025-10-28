# Azure Functions - Microservices Architecture

This directory contains three C# Azure Function apps that implement a microservices architecture for an e-commerce platform.

## Services

### 🛒 Order Service
**Directory:** `order/`  
**Port (Local):** 7073  
**Purpose:** Manages customer orders

**Endpoints:**
- `POST /api/orders` - Create a new order
- `GET /api/orders/{id}` - Get order details
- `GET /api/orders/user/{userId}` - Get all orders for a user

**Dependencies:**
- User Service (validates users)
- Product Service (validates products and checks inventory)

---

### 📦 Product Service
**Directory:** `product/`  
**Port (Local):** 7072  
**Purpose:** Manages product catalog

**Endpoints:**
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product details
- `POST /api/products` - Create or update a product

**Dependencies:** None (standalone service)

---

### 👤 User Service
**Directory:** `user/`  
**Port (Local):** 7071  
**Purpose:** Manages user accounts and authentication

**Endpoints:**
- `POST /api/users` - Create a new user account
- `GET /api/users/{id}` - Get user profile
- `POST /api/login` - Authenticate user

**Dependencies:** None (standalone service)

---

## Common Endpoints (All Services)

- `GET /api/health` - Health check endpoint
- `GET /api/v3/api-docs` - OpenAPI specification
- `GET /api/swagger-ui.html` - Interactive API documentation

## Architecture

```
┌─────────────────┐
│                 │
│   Order API     │◄─────┐
│   (Port 7073)   │      │
│                 │      │
└────────┬────────┘      │
         │               │
         │ calls         │
         │               │
    ┌────▼─────┐    ┌────▼─────┐
    │          │    │          │
    │ User API │    │ Product  │
    │(Port 7071)│    │   API    │
    │          │    │(Port 7072)│
    └──────────┘    └──────────┘
```

## Technology Stack

- **Runtime:** .NET 8 (Isolated Worker)
- **Azure Functions:** v4
- **Storage:** In-memory (ConcurrentDictionary)
- **Serialization:** System.Text.Json
- **Dependency Injection:** Microsoft.Extensions.DependencyInjection
- **Monitoring:** Application Insights

## Data Flow Example

### Creating an Order

1. **Client** sends POST request to Order Service:
   ```json
   POST /api/orders
   {
     "userId": "user-123",
     "items": [
       {"productId": "prod-456", "quantity": 2}
     ]
   }
   ```

2. **Order Service** validates the user:
   - Calls `GET /api/users/user-123` on User Service
   - If user doesn't exist, returns 400 Bad Request

3. **Order Service** validates each product:
   - Calls `GET /api/products/prod-456` on Product Service
   - Checks if quantity is available
   - If product unavailable, returns 400 Bad Request

4. **Order Service** creates the order:
   - Calculates total amount
   - Saves order to repository
   - Returns order details with 201 Created

## Security Considerations

⚠️ **Current Implementation (Development Only):**
- Authorization level set to `Anonymous`
- Simple token-based authentication (not JWT)
- Password hashing with SHA-256 (no salt)
- In-memory storage (data lost on restart)

🔒 **Production Recommendations:**
- Use Azure AD authentication
- Implement proper JWT tokens
- Use bcrypt or Argon2 for password hashing
- Use Azure Cosmos DB or Azure SQL for persistence
- Add rate limiting
- Implement API key management
- Enable CORS policies
- Add request validation middleware

## Deployment Environments

Each service is deployed to multiple environments:

| Environment | User Service | Product Service | Order Service |
|-------------|-------------|-----------------|---------------|
| Dev | joaz-func-user-9021-dev | joaz-func-product-9021-dev | joaz-func-order-9021-dev |
| Test | joaz-func-user-9021-test | joaz-func-product-9021-test | joaz-func-order-9021-test |
| Stage | joaz-func-user-9021-stage | joaz-func-product-9021-stage | joaz-func-order-9021-stage |
| Prod | joaz-func-user-9021-prod | joaz-func-product-9021-prod | joaz-func-order-9021-prod |

## Development Workflow

1. **Local Development:**
   ```bash
   # Start all services
   cd user && func start &
   cd ../product && func start --port 7072 &
   cd ../order && func start --port 7073 &
   ```

2. **Testing:**
   - Use Swagger UI for manual testing
   - Run Postman collection for integration tests
   - Unit tests (TODO: add xUnit projects)

3. **Deployment:**
   - Push to `main` branch
   - GitHub Actions handles CI/CD
   - Automatic deployment through environments

## Monitoring & Observability

### Application Insights

All services are configured with Application Insights:
- Request/response telemetry
- Dependency tracking (HTTP calls between services)
- Exception tracking
- Custom metrics

### Health Checks

Each service exposes `/api/health` endpoint:
```json
{
  "status": "UP"
}
```

Monitor health checks in your orchestration/monitoring tools.

### Logging

Structured logging is available via `ILogger<T>`:
```csharp
_logger.LogInformation("Order created: {OrderId}", order.Id);
_logger.LogWarning("Product out of stock: {ProductId}", productId);
_logger.LogError(ex, "Error processing order");
```

## Scaling Considerations

### Horizontal Scaling
- Functions scale automatically based on load
- Consumption plan: automatic scaling
- Premium plan: pre-warmed instances

### State Management
⚠️ **Current:** In-memory storage (not suitable for production)
✅ **Recommended:** 
- Azure Cosmos DB for global distribution
- Azure SQL Database for relational data
- Azure Cache for Redis for caching

### Performance Optimization
- Use async/await throughout
- Implement caching for frequently accessed data
- Use connection pooling for external calls
- Enable HTTP/2 for inter-service communication

## Error Handling

All services implement consistent error handling:

```csharp
// 400 Bad Request - Validation errors
{"error": "User ID is required"}

// 404 Not Found - Resource not found
{"error": "Order not found"}

// 409 Conflict - Resource already exists
{"error": "User with email already exists"}

// 500 Internal Server Error - Unexpected errors
{"error": "Internal server error: <details>"}

// 503 Service Unavailable - Dependent service down
{"error": "Product service is unavailable"}
```

## Testing Strategy

### Unit Tests (Planned)
```bash
# Add xUnit project
dotnet new xunit -n Order.Tests
dotnet add Order.Tests reference order/Order.csproj

# Run tests
dotnet test
```

### Integration Tests
```bash
# Run Postman collection with Newman
newman run postman/IntegrationTest.postman_collection.json
```

### Load Tests
- Use Azure Load Testing
- Test each service independently
- Test full order flow (user → product → order)

## Best Practices

✅ **Follow These Guidelines:**
1. Keep functions small and focused
2. Use dependency injection
3. Handle exceptions gracefully
4. Log appropriately (not too much, not too little)
5. Use async/await consistently
6. Validate input data
7. Return appropriate HTTP status codes
8. Document APIs with OpenAPI/Swagger

❌ **Avoid These:**
1. Storing state in function class members
2. Blocking calls (use async)
3. Long-running operations (>5 minutes)
4. Large payloads (>100MB)
5. Tight coupling between services
6. Hardcoded URLs (use configuration)

## Migration from Java

This codebase was migrated from Java (Spring Boot) to C#. Key differences:

| Aspect | Java (Before) | C# (After) |
|--------|--------------|------------|
| Framework | Spring Boot + Spring Cloud Function | .NET 8 Isolated Worker |
| DI Container | Spring IoC | Microsoft.Extensions.DependencyInjection |
| Config | application.yml | local.settings.json |
| Build | Maven (pom.xml) | .NET SDK (.csproj) |
| Tests | JUnit + Mockito | xUnit + Moq (planned) |
| Serialization | Jackson | System.Text.Json |
| Collections | HashMap | ConcurrentDictionary |

See [MIGRATION_SUMMARY.md](../../MIGRATION_SUMMARY.md) for details.

## Contributing

1. Create a feature branch
2. Make changes
3. Test locally
4. Submit pull request
5. CI/CD pipeline runs automatically
6. Deploy after approval

## Troubleshooting

### "Function not found" error
- Check function name in `[Function("name")]` attribute
- Ensure project builds without errors
- Verify `host.json` is configured correctly

### Service communication fails
- Check service URLs in configuration
- Verify all services are running
- Check network/firewall settings

### Cold start issues
- Use Premium plan for pre-warmed instances
- Implement dependency caching
- Optimize startup code in `Program.cs`

## Resources

- [Quick Start Guide](../../QUICK_START.md)
- [Migration Summary](../../MIGRATION_SUMMARY.md)
- [Postman Collection](../../postman/IntegrationTest.postman_collection.json)
- [Azure Functions Documentation](https://learn.microsoft.com/en-us/azure/azure-functions/)

---

**Last Updated:** October 28, 2025  
**Version:** 1.0.0 (C# Migration)  
**Maintainer:** Development Team
