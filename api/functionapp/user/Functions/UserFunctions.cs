using System.Net;
using System.Text.Json;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.Extensions.Logging;
using UserFunction.DTOs;
using UserFunction.Exceptions;
using UserFunction.Services;

namespace UserFunction.Functions;

public class UserFunctions
{
    private readonly ILogger<UserFunctions> _logger;
    private readonly IUserService _userService;
    private readonly JsonSerializerOptions _jsonOptions;

    public UserFunctions(ILogger<UserFunctions> logger, IUserService userService)
    {
        _logger = logger;
        _userService = userService;
        _jsonOptions = new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
            WriteIndented = false
        };
    }

    [Function("createUser")]
    public async Task<HttpResponseData> CreateUser(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "users")] HttpRequestData req)
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

            var createRequest = JsonSerializer.Deserialize<CreateUserRequest>(requestBody, _jsonOptions);
            if (createRequest == null)
            {
                var badRequestResponse = req.CreateResponse(HttpStatusCode.BadRequest);
                badRequestResponse.Headers.Add("Content-Type", "application/json");
                await badRequestResponse.WriteStringAsync(JsonSerializer.Serialize(new { error = "Invalid request body" }, _jsonOptions));
                return badRequestResponse;
            }

            var user = _userService.CreateUser(createRequest);
            var response = req.CreateResponse(HttpStatusCode.Created);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(user, _jsonOptions));
            return response;
        }
        catch (UserAlreadyExistsException ex)
        {
            _logger.LogWarning(ex, "User already exists");
            var response = req.CreateResponse(HttpStatusCode.Conflict);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { error = ex.Message }, _jsonOptions));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error creating user");
            var response = req.CreateResponse(HttpStatusCode.InternalServerError);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { error = $"Internal server error: {ex.Message}" }, _jsonOptions));
            return response;
        }
    }

    [Function("getUserProfile")]
    public async Task<HttpResponseData> GetUserProfile(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "users/{id}")] HttpRequestData req,
        string id)
    {
        try
        {
            var user = _userService.GetUser(id);
            var response = req.CreateResponse(HttpStatusCode.OK);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(user, _jsonOptions));
            return response;
        }
        catch (UserNotFoundException ex)
        {
            _logger.LogWarning(ex, "User not found: {UserId}", id);
            var response = req.CreateResponse(HttpStatusCode.NotFound);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { error = ex.Message }, _jsonOptions));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving user: {UserId}", id);
            var response = req.CreateResponse(HttpStatusCode.InternalServerError);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { error = $"Internal server error: {ex.Message}" }, _jsonOptions));
            return response;
        }
    }

    [Function("login")]
    public async Task<HttpResponseData> Login(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "login")] HttpRequestData req)
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

            var loginRequest = JsonSerializer.Deserialize<LoginRequest>(requestBody, _jsonOptions);
            if (loginRequest == null)
            {
                var badRequestResponse = req.CreateResponse(HttpStatusCode.BadRequest);
                badRequestResponse.Headers.Add("Content-Type", "application/json");
                await badRequestResponse.WriteStringAsync(JsonSerializer.Serialize(new { error = "Invalid request body" }, _jsonOptions));
                return badRequestResponse;
            }

            var loginResponse = _userService.Login(loginRequest);
            var response = req.CreateResponse(HttpStatusCode.OK);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(loginResponse, _jsonOptions));
            return response;
        }
        catch (AuthenticationException ex)
        {
            _logger.LogWarning(ex, "Authentication failed");
            var response = req.CreateResponse(HttpStatusCode.Unauthorized);
            response.Headers.Add("Content-Type", "application/json");
                await response.WriteStringAsync(JsonSerializer.Serialize(new { error = ex.Message }, _jsonOptions));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error during login");
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
                    title = "User Management API",
                    version = "v1.0.0",
                    description = "A User Management Azure Function with endpoints for user creation, profile retrieval, and authentication"
                },
                paths = new
                {
                    __api_users = new
                    {
                        post = new
                        {
                            summary = "Create a new user",
                            description = "Creates a new user account with the provided user details",
                            operationId = "createUser",
                            responses = new
                            {
                                __201 = new { description = "User created successfully" },
                                __400 = new { description = "Invalid request data" },
                                __409 = new { description = "User already exists" },
                                __500 = new { description = "Internal server error" }
                            }
                        }
                    },
                    __api_users_id = new
                    {
                        get = new
                        {
                            summary = "Get user by ID",
                            description = "Retrieves user profile details for the specified user ID",
                            operationId = "getUser",
                            parameters = new[]
                            {
                                new { name = "id", @in = "path", required = true, description = "User ID" }
                            },
                            responses = new
                            {
                                __200 = new { description = "User found" },
                                __404 = new { description = "User not found" },
                                __500 = new { description = "Internal server error" }
                            }
                        }
                    },
                    __api_login = new
                    {
                        post = new
                        {
                            summary = "User login",
                            description = "Authenticates a user with email and password, returns login token on success",
                            operationId = "login",
                            responses = new
                            {
                                __200 = new { description = "Login successful" },
                                __401 = new { description = "Invalid credentials" },
                                __400 = new { description = "Invalid request data" },
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
                .Replace("__api_users", "/api/users")
                .Replace("__api_users_id", "/api/users/{id}")
                .Replace("__api_login", "/api/login")
                .Replace("__201", "201")
                .Replace("__200", "200")
                .Replace("__400", "400")
                .Replace("__401", "401")
                .Replace("__404", "404")
                .Replace("__409", "409")
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
    <title>Swagger UI - User Management API</title>
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
