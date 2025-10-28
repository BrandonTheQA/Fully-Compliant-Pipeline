package com.example;

import com.example.controller.OrderController;
import com.example.dto.CreateOrderRequest;
import com.example.dto.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

/**
 * Azure Functions Handler that bridges to Spring Boot controllers
 */
public class OrderFunctionHandler {
    
    private static ConfigurableApplicationContext applicationContext;
    private static ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // Initialize Spring Boot application context
        applicationContext = SpringApplication.run(OrderApplication.class);
    }
    
    private static OrderController getOrderController() {
        return applicationContext.getBean(OrderController.class);
    }
    
    private static HealthIndicator getHealthIndicator() {
        try {
            return applicationContext.getBean(HealthIndicator.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Azure Function for createOrder endpoint
     */
    @FunctionName("createOrder")
    public HttpResponseMessage createOrder(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "orders")
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
            CreateOrderRequest createRequest = objectMapper.readValue(body, CreateOrderRequest.class);
            
            // Call Spring Boot controller directly
            OrderController controller = getOrderController();
            ResponseEntity<OrderResponse> response = controller.createOrder(createRequest);
            
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
     * Azure Function for getOrder endpoint
     */
    @FunctionName("getOrder")
    public HttpResponseMessage getOrder(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "orders/{id}")
                HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String orderId,
            final ExecutionContext context) {
        
        try {
            // Call Spring Boot controller directly
            OrderController controller = getOrderController();
            ResponseEntity<OrderResponse> response = controller.getOrder(orderId);
            
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
     * Azure Function for getUserOrders endpoint
     */
    @FunctionName("getUserOrders")
    public HttpResponseMessage getUserOrders(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "orders/user/{userId}")
                HttpRequestMessage<Optional<String>> request,
            @BindingName("userId") String userId,
            final ExecutionContext context) {
        
        try {
            // Call Spring Boot controller directly
            OrderController controller = getOrderController();
            ResponseEntity<List<OrderResponse>> response = controller.getUserOrders(userId);
            
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
}

