package com.example;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Azure Functions Handler that bridges to Spring Boot controllers
 */
public class UserFunctionHandler {
    
    private static ConfigurableApplicationContext applicationContext;
    private static RestTemplate restTemplate = new RestTemplate();
    
    static {
        // Initialize Spring Boot application context
        applicationContext = SpringApplication.run(UserApplication.class);
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
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            // Call Spring Boot controller
            ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/api/users", 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(response.getBody())
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error calling Spring Boot controller: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error")
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
            // Call Spring Boot controller
            ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:8080/api/users/" + userId, 
                String.class
            );
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response.getBody())
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error calling Spring Boot controller: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error")
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
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            // Call Spring Boot controller
            ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/api/login", 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response.getBody())
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error calling Spring Boot controller: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error")
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
            // Call Spring Boot Actuator health endpoint
            ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:8080/actuator/health", 
                String.class
            );
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response.getBody())
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error calling Spring Boot actuator: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Health check failed")
                    .build();
        }
    }
}
