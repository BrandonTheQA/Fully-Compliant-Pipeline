package com.example;

import com.example.controller.UserController;
import com.example.dto.CreateUserRequest;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.Paths;
import org.springdoc.core.OpenAPIService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Optional;

/**
 * Azure Functions Handler that bridges to Spring Boot controllers
 */
public class UserFunctionHandler {
    
    private static ConfigurableApplicationContext applicationContext;
    private static ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // Initialize Spring Boot application context
        applicationContext = SpringApplication.run(UserApplication.class);
    }
    
    private static UserController getUserController() {
        return applicationContext.getBean(UserController.class);
    }
    
    private static HealthIndicator getHealthIndicator() {
        try {
            return applicationContext.getBean(HealthIndicator.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static String getOpenApiJson() {
        try {
            // Try to get SpringDoc's OpenAPIService which builds the complete spec with paths
            try {
                OpenAPIService openAPIService = applicationContext.getBean(OpenAPIService.class);
                // Use reflection to access the built OpenAPI since API methods may vary
                java.lang.reflect.Method getOpenAPIMethod = null;
                try {
                    // Try getOpenAPI() with no parameters first
                    getOpenAPIMethod = openAPIService.getClass().getMethod("getOpenAPI");
                } catch (NoSuchMethodException e) {
                    // Try getOpenAPI(String) with default group
                    try {
                        getOpenAPIMethod = openAPIService.getClass().getMethod("getOpenAPI", String.class);
                        if (getOpenAPIMethod != null) {
                            OpenAPI builtOpenAPI = (OpenAPI) getOpenAPIMethod.invoke(openAPIService, "default");
                            if (builtOpenAPI != null && builtOpenAPI.getPaths() != null && !builtOpenAPI.getPaths().isEmpty()) {
                                return objectMapper.writeValueAsString(builtOpenAPI);
                            }
                            // Try with empty string
                            try {
                                builtOpenAPI = (OpenAPI) getOpenAPIMethod.invoke(openAPIService, "");
                                if (builtOpenAPI != null && builtOpenAPI.getPaths() != null && !builtOpenAPI.getPaths().isEmpty()) {
                                    return objectMapper.writeValueAsString(builtOpenAPI);
                                }
                            } catch (Exception e3) {
                                // Continue to fallback
                            }
                        }
                    } catch (Exception e2) {
                        // Continue to fallback
                    }
                }
                
                // If we found a no-param method, try it
                if (getOpenAPIMethod != null && getOpenAPIMethod.getParameterCount() == 0) {
                    OpenAPI builtOpenAPI = (OpenAPI) getOpenAPIMethod.invoke(openAPIService);
                    if (builtOpenAPI != null && builtOpenAPI.getPaths() != null && !builtOpenAPI.getPaths().isEmpty()) {
                        return objectMapper.writeValueAsString(builtOpenAPI);
                    }
                }
            } catch (Exception e) {
                // OpenAPIService might not be available or not built yet
                // Continue to fallback
            }
            
            // Manual build: Add paths manually since SpringDoc isn't scanning controllers
            OpenAPI openAPI = applicationContext.getBean(OpenAPI.class);
            Paths paths = new Paths();
            
            // POST /api/users
            PathItem createUserPath = new PathItem();
            Operation createUserOp = new Operation();
            createUserOp.setSummary("Create a new user");
            createUserOp.setDescription("Creates a new user account with the provided user details");
            createUserOp.setOperationId("createUser");
            
            ApiResponses createUserResponses = new ApiResponses();
            createUserResponses.addApiResponse("201", new ApiResponse().description("User created successfully"));
            createUserResponses.addApiResponse("400", new ApiResponse().description("Invalid request data"));
            createUserResponses.addApiResponse("409", new ApiResponse().description("User already exists"));
            createUserResponses.addApiResponse("500", new ApiResponse().description("Internal server error"));
            createUserOp.setResponses(createUserResponses);
            createUserOp.setRequestBody(new io.swagger.v3.oas.models.parameters.RequestBody().required(true)
                .description("User creation request"));
            createUserPath.setPost(createUserOp);
            paths.addPathItem("/api/users", createUserPath);
            
            // GET /api/users/{id}
            PathItem getUserPath = new PathItem();
            Operation getUserOp = new Operation();
            getUserOp.setSummary("Get user by ID");
            getUserOp.setDescription("Retrieves user profile details for the specified user ID");
            getUserOp.setOperationId("getUser");
            
            Parameter idParam = new Parameter();
            idParam.setName("id");
            idParam.setIn("path");
            idParam.setRequired(true);
            idParam.setDescription("User ID");
            getUserOp.addParametersItem(idParam);
            
            ApiResponses getUserResponses = new ApiResponses();
            getUserResponses.addApiResponse("200", new ApiResponse().description("User found"));
            getUserResponses.addApiResponse("404", new ApiResponse().description("User not found"));
            getUserResponses.addApiResponse("500", new ApiResponse().description("Internal server error"));
            getUserOp.setResponses(getUserResponses);
            getUserPath.setGet(getUserOp);
            paths.addPathItem("/api/users/{id}", getUserPath);
            
            // POST /api/login
            PathItem loginPath = new PathItem();
            Operation loginOp = new Operation();
            loginOp.setSummary("User login");
            loginOp.setDescription("Authenticates a user with email and password, returns login token on success");
            loginOp.setOperationId("login");
            
            ApiResponses loginResponses = new ApiResponses();
            loginResponses.addApiResponse("200", new ApiResponse().description("Login successful"));
            loginResponses.addApiResponse("401", new ApiResponse().description("Invalid credentials"));
            loginResponses.addApiResponse("400", new ApiResponse().description("Invalid request data"));
            loginResponses.addApiResponse("500", new ApiResponse().description("Internal server error"));
            loginOp.setResponses(loginResponses);
            loginOp.setRequestBody(new io.swagger.v3.oas.models.parameters.RequestBody().required(true)
                .description("Login credentials"));
            loginPath.setPost(loginOp);
            paths.addPathItem("/api/login", loginPath);
            
            // Set the manually built paths
            openAPI.setPaths(paths);
            
            return objectMapper.writeValueAsString(openAPI);
        } catch (Exception e) {
            return "{\"error\":\"Failed to generate OpenAPI spec: " + e.getMessage() + "\",\"paths\":{}}";
        }
    }
    
    /**
     * Azure Function for createUser endpoint
     */
    @FunctionName("createUser")
    public HttpResponseMessage createUser(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "users")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        try {
            String body = request.getBody().orElse("");
            if (body.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body("{\"error\":\"Request body is required\"}")
                        .build();
            }
            
            // Parse request body
            CreateUserRequest createRequest = objectMapper.readValue(body, CreateUserRequest.class);
            
            // Call Spring Boot controller directly
            UserController controller = getUserController();
            ResponseEntity<UserResponse> response = controller.createUser(createRequest);
            
            // Serialize response to JSON
            String jsonResponse = objectMapper.writeValueAsString(response.getBody());
            
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(jsonResponse)
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error calling Spring Boot controller: " + e.getMessage());
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    /**
     * Azure Function for getUserProfile endpoint
     */
    @FunctionName("getUserProfile")
    public HttpResponseMessage getUserProfile(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "users/{id}")
                HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String userId,
            final ExecutionContext context) {
        
        try {
            // Call Spring Boot controller directly
            UserController controller = getUserController();
            ResponseEntity<UserResponse> response = controller.getUser(userId);
            
            // Serialize response to JSON
            String jsonResponse = objectMapper.writeValueAsString(response.getBody());
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(jsonResponse)
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error calling Spring Boot controller: " + e.getMessage());
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    /**
     * Azure Function for login endpoint
     */
    @FunctionName("login")
    public HttpResponseMessage login(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "login")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        try {
            String body = request.getBody().orElse("");
            if (body.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body("{\"error\":\"Request body is required\"}")
                        .build();
            }
            
            // Parse request body
            LoginRequest loginRequest = objectMapper.readValue(body, LoginRequest.class);
            
            // Call Spring Boot controller directly
            UserController controller = getUserController();
            ResponseEntity<LoginResponse> response = controller.login(loginRequest);
            
            // Serialize response to JSON
            String jsonResponse = objectMapper.writeValueAsString(response.getBody());
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(jsonResponse)
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error calling Spring Boot controller: " + e.getMessage());
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    /**
     * Azure Function for health endpoint - delegates to Spring Boot Actuator
     */
    @FunctionName("health")
    public HttpResponseMessage health(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "health")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        try {
            // Try to get health indicator from Spring Boot
            HealthIndicator healthIndicator = getHealthIndicator();
            if (healthIndicator != null) {
                Health health = healthIndicator.health();
                String jsonResponse = objectMapper.writeValueAsString(health);
                HttpStatus status = health.getStatus().getCode().equals("UP") ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
                
                return request.createResponseBuilder(status)
                        .header("Content-Type", "application/json")
                        .body(jsonResponse)
                        .build();
            } else {
                // Fallback if no health indicator is available
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{\"status\":\"UP\"}")
                        .build();
            }
                    
        } catch (Exception e) {
            context.getLogger().severe("Error calling Spring Boot actuator: " + e.getMessage());
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"DOWN\",\"error\":\"Health check failed: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    /**
     * Azure Function for Swagger OpenAPI docs endpoint
     */
    @FunctionName("swaggerApiDocs")
    public HttpResponseMessage swaggerApiDocs(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "v3/api-docs")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        try {
            String openApiJson = getOpenApiJson();
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .header("Access-Control-Allow-Origin", "*")
                    .body(openApiJson)
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error getting OpenAPI spec: " + e.getMessage());
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Failed to load API docs: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    /**
     * Azure Function for Swagger UI - returns HTML page with embedded Swagger UI
     */
    @FunctionName("swaggerUI")
    public HttpResponseMessage swaggerUI(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "swagger-ui.html")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        try {
            // Get base URL from request
            URI requestUri = request.getUri();
            String baseUrl = requestUri.getScheme() + "://" + requestUri.getAuthority();
            // Azure Functions routes are typically prefixed with /api
            String apiDocsUrl = baseUrl + "/api/v3/api-docs";
            
            String swaggerHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Swagger UI - User Management API</title>\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://unpkg.com/swagger-ui-dist@5.17.14/swagger-ui.css\" />\n" +
                "    <style>\n" +
                "        html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }\n" +
                "        *, *:before, *:after { box-sizing: inherit; }\n" +
                "        body { margin: 0; background: #fafafa; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"swagger-ui\"></div>\n" +
                "    <script src=\"https://unpkg.com/swagger-ui-dist@5.17.14/swagger-ui-bundle.js\"></script>\n" +
                "    <script src=\"https://unpkg.com/swagger-ui-dist@5.17.14/swagger-ui-standalone-preset.js\"></script>\n" +
                "    <script>\n" +
                "        window.onload = function() {\n" +
                "            SwaggerUIBundle({\n" +
                "                url: \"" + apiDocsUrl + "\",\n" +
                "                dom_id: \"#swagger-ui\",\n" +
                "                presets: [\n" +
                "                    SwaggerUIBundle.presets.apis,\n" +
                "                    SwaggerUIStandalonePreset\n" +
                "                ],\n" +
                "                layout: \"StandaloneLayout\",\n" +
                "                deepLinking: true,\n" +
                "                showExtensions: true,\n" +
                "                showCommonExtensions: true\n" +
                "            });\n" +
                "        };\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "text/html; charset=utf-8")
                    .body(swaggerHtml)
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error generating Swagger UI: " + e.getMessage());
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "text/html")
                    .body("<html><body><h1>Error</h1><p>Failed to load Swagger UI: " + e.getMessage() + "</p></body></html>")
                    .build();
        }
    }
}
