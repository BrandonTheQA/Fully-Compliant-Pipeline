package com.example;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

/**
 * Azure Functions for User Management.
 * Provides endpoints for user creation, profile retrieval, and authentication.
 */
public class UserFunction {
    private static final String VERSION = "v1.0.0";
    private static final String BUILD_DATE = "2024-01-15";
    
    // In-memory storage for demo purposes (in production, use a database)
    private static final Map<String, User> users = new HashMap<>();
    
    /**
     * POST /api/users - Create a new user
     * Request body should contain: {"name": "John Doe", "email": "john@example.com", "password": "password123"}
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
        context.getLogger().info("Creating a new user");
        
        try {
            String requestBody = request.getBody().orElse("");
            if (requestBody.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Request body is required")
                        .build();
            }
            
            // Parse request body (simplified - in production, use proper JSON parsing)
            String name = extractValue(requestBody, "name");
            String email = extractValue(requestBody, "email");
            String password = extractValue(requestBody, "password");
            
            if (name == null || email == null || password == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Missing required fields: name, email, password")
                        .build();
            }
            
            // Check if user already exists
            if (users.values().stream().anyMatch(user -> user.getEmail().equals(email))) {
                return request.createResponseBuilder(HttpStatus.CONFLICT)
                        .body("User with email " + email + " already exists")
                        .build();
            }
            
            // Create new user
            String userId = UUID.randomUUID().toString();
            User newUser = new User(userId, name, email, password);
            users.put(userId, newUser);
            
            String responseBody = String.format(
                "{\n" +
                "  \"message\": \"User created successfully\",\n" +
                "  \"userId\": \"%s\",\n" +
                "  \"name\": \"%s\",\n" +
                "  \"email\": \"%s\",\n" +
                "  \"createdAt\": \"%s\"\n" +
                "}",
                userId, name, email, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(responseBody)
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error creating user: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error")
                    .build();
        }
    }
    
    /**
     * GET /api/users/{id} - Get user profile by ID
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
        context.getLogger().info("Getting user profile for ID: " + userId);
        
        try {
            User user = users.get(userId);
            if (user == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("User not found")
                        .build();
            }
            
            String responseBody = String.format(
                "{\n" +
                "  \"userId\": \"%s\",\n" +
                "  \"name\": \"%s\",\n" +
                "  \"email\": \"%s\",\n" +
                "  \"createdAt\": \"%s\"\n" +
                "}",
                user.getId(), user.getName(), user.getEmail(), user.getCreatedAt()
            );
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(responseBody)
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error getting user profile: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error")
                    .build();
        }
    }
    
    /**
     * POST /api/login - Authenticate user
     * Request body should contain: {"email": "john@example.com", "password": "password123"}
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
        context.getLogger().info("User login attempt");
        
        try {
            String requestBody = request.getBody().orElse("");
            if (requestBody.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Request body is required")
                        .build();
            }
            
            String email = extractValue(requestBody, "email");
            String password = extractValue(requestBody, "password");
            
            if (email == null || password == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Missing required fields: email, password")
                        .build();
            }
            
            // Find user by email
            User user = users.values().stream()
                    .filter(u -> u.getEmail().equals(email))
                    .findFirst()
                    .orElse(null);
            
            if (user == null || !user.getPassword().equals(password)) {
                return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                        .body("Invalid email or password")
                        .build();
            }
            
            // Generate a simple token (in production, use proper JWT)
            String token = UUID.randomUUID().toString();
            
            String responseBody = String.format(
                "{\n" +
                "  \"message\": \"Login successful\",\n" +
                "  \"token\": \"%s\",\n" +
                "  \"userId\": \"%s\",\n" +
                "  \"name\": \"%s\",\n" +
                "  \"email\": \"%s\",\n" +
                "  \"loginTime\": \"%s\"\n" +
                "}",
                token, user.getId(), user.getName(), user.getEmail(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(responseBody)
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error during login: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error")
                    .build();
        }
    }
    
    /**
     * Helper method to extract values from simple JSON-like string
     * Note: This is a simplified parser for demo purposes. In production, use a proper JSON library.
     */
    private String extractValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return null;
    }
    
    /**
     * Simple User class for demo purposes
     */
    private static class User {
        private final String id;
        private final String name;
        private final String email;
        private final String password;
        private final String createdAt;
        
        public User(String id, String name, String email, String password) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
            this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getCreatedAt() { return createdAt; }
    }
}
