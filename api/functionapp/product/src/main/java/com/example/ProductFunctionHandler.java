package com.example;

import com.example.controller.ProductController;
import com.example.dto.CreateProductRequest;
import com.example.dto.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Azure Functions Handler that bridges to Spring Boot controllers
 */
public class ProductFunctionHandler {
    
    private static ConfigurableApplicationContext applicationContext;
    private static ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // Initialize Spring Boot application context
        applicationContext = SpringApplication.run(ProductApplication.class);
    }
    
    private static ProductController getProductController() {
        return applicationContext.getBean(ProductController.class);
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
            // Get OpenAPI bean from Spring context and serialize it to JSON
            OpenAPI openAPI = applicationContext.getBean(OpenAPI.class);
            return objectMapper.writeValueAsString(openAPI);
        } catch (Exception e) {
            return "{\"error\":\"Failed to generate OpenAPI spec: " + e.getMessage() + "\"}";
        }
    }
    
    /**
     * Azure Function for getAllProducts endpoint
     */
    @FunctionName("getAllProducts")
    public HttpResponseMessage getAllProducts(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "products")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        try {
            // Call Spring Boot controller directly
            ProductController controller = getProductController();
            ResponseEntity<List<ProductResponse>> response = controller.getAllProducts();
            
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
     * Azure Function for getProduct endpoint
     */
    @FunctionName("getProduct")
    public HttpResponseMessage getProduct(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "products/{id}")
                HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String productId,
            final ExecutionContext context) {
        
        try {
            // Call Spring Boot controller directly
            ProductController controller = getProductController();
            ResponseEntity<ProductResponse> response = controller.getProduct(productId);
            
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
     * Azure Function for createOrUpdateProduct endpoint
     */
    @FunctionName("createOrUpdateProduct")
    public HttpResponseMessage createOrUpdateProduct(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "products")
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
            CreateProductRequest createRequest = objectMapper.readValue(body, CreateProductRequest.class);
            
            // Call Spring Boot controller directly
            ProductController controller = getProductController();
            ResponseEntity<ProductResponse> response = controller.createOrUpdateProduct(createRequest);
            
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
                "    <title>Swagger UI - Product Management API</title>\n" +
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

