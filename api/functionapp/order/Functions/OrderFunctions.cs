using System.Net;
using System.Text.Json;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.Extensions.Logging;
using OrderFunction.DTOs;
using OrderFunction.Exceptions;
using OrderFunction.Services;

namespace OrderFunction.Functions;

public class OrderFunctions
{
    private readonly ILogger<OrderFunctions> _logger;
    private readonly IOrderService _orderService;
    private readonly JsonSerializerOptions _jsonOptions;

    public OrderFunctions(ILogger<OrderFunctions> logger, IOrderService orderService)
    {
        _logger = logger;
        _orderService = orderService;
        _jsonOptions = new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
            WriteIndented = false
        };
    }

    [Function("createOrder")]
    public async Task<HttpResponseData> CreateOrder(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "orders")] HttpRequestData req)
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

            var createRequest = JsonSerializer.Deserialize<CreateOrderRequest>(requestBody, _jsonOptions);
            if (createRequest == null)
            {
                var badRequestResponse = req.CreateResponse(HttpStatusCode.BadRequest);
                badRequestResponse.Headers.Add("Content-Type", "application/json");
                await badRequestResponse.WriteStringAsync(JsonSerializer.Serialize(new { error = "Invalid request body" }, _jsonOptions));
                return badRequestResponse;
            }

            var order = await _orderService.CreateOrderAsync(createRequest);
            var response = req.CreateResponse(HttpStatusCode.Created);
            response.Headers.Add("Content-Type", "application/json");
            await response.WriteStringAsync(JsonSerializer.Serialize(order, _jsonOptions));
            return response;
        }
        catch (OrderValidationException ex)
        {
            _logger.LogWarning(ex, "Order validation failed");
            var response = req.CreateResponse(HttpStatusCode.BadRequest);
            response.Headers.Add("Content-Type", "application/json");
            await response.WriteStringAsync(JsonSerializer.Serialize(new { error = ex.Message }, _jsonOptions));
            return response;
        }
        catch (ServiceUnavailableException ex)
        {
            _logger.LogError(ex, "External service unavailable");
            var response = req.CreateResponse(HttpStatusCode.ServiceUnavailable);
            response.Headers.Add("Content-Type", "application/json");
            await response.WriteStringAsync(JsonSerializer.Serialize(new { error = ex.Message }, _jsonOptions));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error creating order");
            var response = req.CreateResponse(HttpStatusCode.InternalServerError);
            response.Headers.Add("Content-Type", "application/json");
            await response.WriteStringAsync(JsonSerializer.Serialize(new { error = $"Internal server error: {ex.Message}" }, _jsonOptions));
            return response;
        }
    }

    [Function("getOrder")]
    public async Task<HttpResponseData> GetOrder(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "orders/{id}")] HttpRequestData req,
        string id)
    {
        try
        {
            var order = _orderService.GetOrder(id);
            var response = req.CreateResponse(HttpStatusCode.OK);
            response.Headers.Add("Content-Type", "application/json");
            await response.WriteStringAsync(JsonSerializer.Serialize(order, _jsonOptions));
            return response;
        }
        catch (OrderNotFoundException ex)
        {
            _logger.LogWarning(ex, "Order not found: {OrderId}", id);
            var response = req.CreateResponse(HttpStatusCode.NotFound);
            response.Headers.Add("Content-Type", "application/json");
            await response.WriteStringAsync(JsonSerializer.Serialize(new { error = ex.Message }, _jsonOptions));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving order: {OrderId}", id);
            var response = req.CreateResponse(HttpStatusCode.InternalServerError);
            response.Headers.Add("Content-Type", "application/json");
            await response.WriteStringAsync(JsonSerializer.Serialize(new { error = $"Internal server error: {ex.Message}" }, _jsonOptions));
            return response;
        }
    }

    [Function("getUserOrders")]
    public async Task<HttpResponseData> GetUserOrders(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "orders/user/{userId}")] HttpRequestData req,
        string userId)
    {
        try
        {
            var orders = _orderService.GetUserOrders(userId);
            var response = req.CreateResponse(HttpStatusCode.OK);
            response.Headers.Add("Content-Type", "application/json");
            await response.WriteStringAsync(JsonSerializer.Serialize(orders, _jsonOptions));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving orders for user: {UserId}", userId);
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
                    title = "Order Management API",
                    version = "v1.0.0",
                    description = "An Order Management Azure Function with endpoints for order creation, retrieval, and user order listing"
                },
                paths = new
                {
                    __api_orders = new
                    {
                        post = new
                        {
                            summary = "Create a new order",
                            description = "Creates a new order with the provided order details",
                            operationId = "createOrder",
                            responses = new
                            {
                                __201 = new { description = "Order created successfully" },
                                __400 = new { description = "Invalid request data" },
                                __500 = new { description = "Internal server error" }
                            }
                        }
                    },
                    __api_orders_id = new
                    {
                        get = new
                        {
                            summary = "Get order by ID",
                            description = "Retrieves order details for the specified order ID",
                            operationId = "getOrder",
                            parameters = new[]
                            {
                                new { name = "id", @in = "path", required = true, description = "Order ID" }
                            },
                            responses = new
                            {
                                __200 = new { description = "Order found" },
                                __404 = new { description = "Order not found" },
                                __500 = new { description = "Internal server error" }
                            }
                        }
                    },
                    __api_orders_user_userId = new
                    {
                        get = new
                        {
                            summary = "Get user orders",
                            description = "Retrieves all orders for the specified user ID",
                            operationId = "getUserOrders",
                            parameters = new[]
                            {
                                new { name = "userId", @in = "path", required = true, description = "User ID" }
                            },
                            responses = new
                            {
                                __200 = new { description = "Orders retrieved successfully" },
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
                .Replace("__api_orders", "/api/orders")
                .Replace("__api_orders_id", "/api/orders/{id}")
                .Replace("__api_orders_user_userId", "/api/orders/user/{userId}")
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
    <title>Swagger UI - Order Management API</title>
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
