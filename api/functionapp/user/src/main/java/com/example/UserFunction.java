package com.example;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;

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
     * Health check endpoint for monitoring and actuator purposes
     * GET /api/health - Returns application health status
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
        context.getLogger().info("Health check endpoint accessed");
        
        try {
            // Get system information
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            long uptime = runtimeBean.getUptime();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long freeMemory = maxMemory - usedMemory;
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String uptimeFormatted = formatUptime(uptime);
            
            String responseBody = String.format(
                "{\n" +
                "  \"status\": \"UP\",\n" +
                "  \"app\": \"User\",\n" +
                "  \"version\": \"%s\",\n" +
                "  \"buildDate\": \"%s\",\n" +
                "  \"timestamp\": \"%s\",\n" +
                "  \"uptime\": \"%s\",\n" +
                "  \"memory\": {\n" +
                "    \"used\": %d,\n" +
                "    \"free\": %d,\n" +
                "    \"max\": %d,\n" +
                "    \"usagePercent\": %.2f\n" +
                "  },\n" +
                "  \"jvm\": {\n" +
                "    \"name\": \"%s\",\n" +
                "    \"version\": \"%s\",\n" +
                "    \"vendor\": \"%s\"\n" +
                "  },\n" +
                "  \"users\": {\n" +
                "    \"total\": %d,\n" +
                "    \"active\": %d\n" +
                "  }\n" +
                "}",
                VERSION, BUILD_DATE, timestamp, uptimeFormatted,
                usedMemory, freeMemory, maxMemory, (double) usedMemory / maxMemory * 100,
                runtimeBean.getVmName(), runtimeBean.getVmVersion(), runtimeBean.getVmVendor(),
                users.size(), users.size() // For demo purposes, all users are considered active
            );
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(responseBody)
                    .build();
                    
        } catch (Exception e) {
            context.getLogger().severe("Error in health check: " + e.getMessage());
            String errorResponse = String.format(
                "{\n" +
                "  \"status\": \"DOWN\",\n" +
                "  \"app\": \"User\",\n" +
                "  \"error\": \"%s\",\n" +
                "  \"timestamp\": \"%s\"\n" +
                "}",
                e.getMessage(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
            
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(errorResponse)
                    .build();
        }
    }
    
    /**
     * Helper method to format uptime in human-readable format
     */
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
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
