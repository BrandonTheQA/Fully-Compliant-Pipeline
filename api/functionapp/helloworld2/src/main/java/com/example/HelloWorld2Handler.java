package com.example;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * Azure Functions Handler that bridges to Spring Boot controllers
 */
public class HelloWorld2Handler {
    
    private static ConfigurableApplicationContext applicationContext;
    private static RestTemplate restTemplate = new RestTemplate();
    
    static {
        // Initialize Spring Boot application context
        applicationContext = SpringApplication.run(HelloWorld2Application.class);
    }
    
    /**
     * Azure Function for hello endpoint
     */
    @FunctionName("hello")
    public HttpResponseMessage hello(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        try {
            String method = request.getHttpMethod().toString();
            String queryParams = request.getQueryParameters().get("name");
            String body = request.getBody().orElse("");
            
            // Build URL for Spring Boot controller
            String url = "http://localhost:8080/api/hello";
            if (queryParams != null) {
                url += "?name=" + queryParams;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity;
            if ("POST".equals(method) && body != null && !body.isEmpty()) {
                entity = new HttpEntity<>(body, headers);
            } else {
                entity = new HttpEntity<>(headers);
            }
            
            // Call Spring Boot controller
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                org.springframework.http.HttpMethod.valueOf(method), 
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
            String url = "http://localhost:8080/actuator/health";
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
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
