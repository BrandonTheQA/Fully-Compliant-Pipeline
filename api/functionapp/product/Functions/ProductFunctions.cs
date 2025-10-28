using System.Net;
using System.Text.Json;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.Extensions.Logging;
using ProductFunction.DTOs;
using ProductFunction.Exceptions;
using ProductFunction.Services;

namespace ProductFunction.Functions;

public class ProductFunctions
{
    private readonly ILogger<ProductFunctions> _logger;
    private readonly IProductService _productService;
    private readonly JsonSerializerOptions _jsonOptions;

    public ProductFunctions(ILogger<ProductFunctions> logger, IProductService productService)
    {
        _logger = logger;
        _productService = productService;
        _jsonOptions = new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
            WriteIndented = false
        };
    }

    [Function("getAllProducts")]
    public async Task<HttpResponseData> GetAllProducts(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "products")] HttpRequestData req)
    {
        try
        {
            var products = _productService.GetAllProducts();
            var response = req.CreateResponse(HttpStatusCode.OK);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(products, _jsonOptions));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving all products");
            var response = req.CreateResponse(HttpStatusCode.InternalServerError);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { error = $"Internal server error: {ex.Message}" }, _jsonOptions));
            return response;
        }
    }

    [Function("getProduct")]
    public async Task<HttpResponseData> GetProduct(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "products/{id}")] HttpRequestData req,
        string id)
    {
        try
        {
            var product = _productService.GetProduct(id);
            var response = req.CreateResponse(HttpStatusCode.OK);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(product, _jsonOptions));
            return response;
        }
        catch (ProductNotFoundException ex)
        {
            _logger.LogWarning(ex, "Product not found: {ProductId}", id);
            var response = req.CreateResponse(HttpStatusCode.NotFound);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { error = ex.Message }, _jsonOptions));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving product: {ProductId}", id);
            var response = req.CreateResponse(HttpStatusCode.InternalServerError);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { error = $"Internal server error: {ex.Message}" }, _jsonOptions));
            return response;
        }
    }

    [Function("createOrUpdateProduct")]
    public async Task<HttpResponseData> CreateOrUpdateProduct(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "products")] HttpRequestData req)
    {
        try
        {
            var requestBody = await req.ReadAsStringAsync();
            if (string.IsNullOrEmpty(requestBody))
            {
                var badRequestResponse = req.CreateResponse(HttpStatusCode.BadRequest);
                badRequestResponse.Headers.Add("Content-Type", "application/json");
                await badRequestResponse.WriteStringAsync(JsonSerializer.Serialize(new { error = "Request body is required" }, _jsonOptions));
                return badRequestResponse;
            }

            var createRequest = JsonSerializer.Deserialize<CreateProductRequest>(requestBody, _jsonOptions);
            if (createRequest == null)
            {
                var badRequestResponse = req.CreateResponse(HttpStatusCode.BadRequest);
                badRequestResponse.Headers.Add("Content-Type", "application/json");
                await badRequestResponse.WriteStringAsync(JsonSerializer.Serialize(new { error = "Invalid request body" }, _jsonOptions));
                return badRequestResponse;
            }

            var product = _productService.CreateOrUpdateProduct(createRequest);
            var response = req.CreateResponse(HttpStatusCode.Created);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(product, _jsonOptions));
            return response;
        }
        catch (ProductNotFoundException ex)
        {
            _logger.LogWarning(ex, "Product not found for update");
            var response = req.CreateResponse(HttpStatusCode.NotFound);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { error = ex.Message }, _jsonOptions));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error creating/updating product");
            var response = req.CreateResponse(HttpStatusCode.InternalServerError);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { error = $"Internal server error: {ex.Message}" }, _jsonOptions));
            return response;
        }
    }

    [Function("health")]
    public async Task<HttpResponseData> Health(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "health")] HttpRequestData req)
    {
        try
        {
            var response = req.CreateResponse(HttpStatusCode.OK);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { status = "UP" }, _jsonOptions));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Health check failed");
            var response = req.CreateResponse(HttpStatusCode.InternalServerError);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { status = "DOWN", error = $"Health check failed: {ex.Message}" }, _jsonOptions));
            return response;
        }
    }

    [Function("swaggerApiDocs")]
    public async Task<HttpResponseData> SwaggerApiDocs(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "v3/api-docs")] HttpRequestData req)
    {
        try
        {
            var openApiSpec = new
            {
                openapi = "3.0.1",
                info = new
                {
                    title = "Product Management API",
                    version = "v1.0.0",
                    description = "A Product Management Azure Function with endpoints for product listing, retrieval, and management"
                },
                paths = new
                {
                    __api_products = new
                    {
                        get = new
                        {
                            summary = "Get all products",
                            description = "Retrieves a list of all available products",
                            operationId = "getAllProducts",
                            responses = new
                            {
                                __200 = new { description = "Products retrieved successfully" },
                                __500 = new { description = "Internal server error" }
                            }
                        },
                        post = new
                        {
                            summary = "Create or update a product",
                            description = "Creates a new product or updates an existing one with the provided product details",
                            operationId = "createOrUpdateProduct",
                            responses = new
                            {
                                __201 = new { description = "Product created/updated successfully" },
                                __400 = new { description = "Invalid request data" },
                                __500 = new { description = "Internal server error" }
                            }
                        }
                    },
                    __api_products_id = new
                    {
                        get = new
                        {
                            summary = "Get product by ID",
                            description = "Retrieves product details for the specified product ID",
                            operationId = "getProduct",
                            parameters = new[]
                            {
                                new { name = "id", @in = "path", required = true, description = "Product ID" }
                            },
                            responses = new
                            {
                                __200 = new { description = "Product found" },
                                __404 = new { description = "Product not found" },
                                __500 = new { description = "Internal server error" }
                            }
                        }
                    }
                }
            };

            var response = req.CreateResponse(HttpStatusCode.OK);
            response.Headers.Add("Content-Type", "application/json");
            response.Headers.Add("Access-Control-Allow-Origin", "*");
            
            var json = JsonSerializer.Serialize(openApiSpec, _jsonOptions)
                .Replace("__api_products", "/api/products")
                .Replace("__api_products_id", "/api/products/{id}")
                .Replace("__201", "201")
                .Replace("__200", "200")
                .Replace("__400", "400")
                .Replace("__404", "404")
                .Replace("__500", "500");
                
                await response.WriteStringAsync(json);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating OpenAPI spec");
            var response = req.CreateResponse(HttpStatusCode.InternalServerError);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { error = $"Failed to load API docs: {ex.Message}" }, _jsonOptions));
            return response;
        }
    }

    [Function("swaggerUI")]
    public async Task<HttpResponseData> SwaggerUI(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "swagger-ui.html")] HttpRequestData req)
    {
        try
        {
            var baseUrl = $"{req.Url.Scheme}://{req.Url.Authority}";
            var apiDocsUrl = $"{baseUrl}/api/v3/api-docs";

            var swaggerHtml = $@"<!DOCTYPE html>
<html>
<head>
    <title>Swagger UI - Product Management API</title>
    <link rel=""stylesheet"" type=""text/css"" href=""https://unpkg.com/swagger-ui-dist@5.17.14/swagger-ui.css"" />
    <style>
        html {{ box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }}
        *, *:before, *:after {{ box-sizing: inherit; }}
        body {{ margin: 0; background: #fafafa; }}
    </style>
</head>
<body>
    <div id=""swagger-ui""></div>
    <script src=""https://unpkg.com/swagger-ui-dist@5.17.14/swagger-ui-bundle.js""></script>
    <script src=""https://unpkg.com/swagger-ui-dist@5.17.14/swagger-ui-standalone-preset.js""></script>
    <script>
        window.onload = function() {{
            SwaggerUIBundle({{
                url: ""{apiDocsUrl}"",
                dom_id: ""#swagger-ui"",
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                layout: ""StandaloneLayout"",
                deepLinking: true,
                showExtensions: true,
                showCommonExtensions: true
            }});
        }};
    </script>
</body>
</html>";

            var response = req.CreateResponse(HttpStatusCode.OK);
            response.Headers.Add("Content-Type", "text/html; charset=utf-8");
                await response.WriteStringAsync(swaggerHtml);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating Swagger UI");
            var response = req.CreateResponse(HttpStatusCode.InternalServerError);
            response.Headers.Add("Content-Type", "text/html");
                await response.WriteStringAsync($"<html><body><h1>Error</h1><p>Failed to load Swagger UI: {ex.Message}</p></body></html>");
            return response;
        }
    }
}
