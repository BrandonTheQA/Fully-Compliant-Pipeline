package com.example;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;

/**
 * Azure Functions with HTTP Trigger.
 */
public class HelloWorldFunction {
    private static final String VERSION = "v2.1.0";
    private static final String BUILD_DATE = "2024-01-15";
    
    /**
     * This function listens at endpoint "/api/hello". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/hello
     * 2. curl {your host}/api/hello?name=HTTP%20Query
     */
    @FunctionName("hello")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        final String query = request.getQueryParameters().get("name");
        final String name = request.getBody().orElse(query);

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Please pass a name on the query string or in the request body")
                    .build();
        } else {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String responseBody = String.format("Hello, %s! Welcome to Azure Functions!\n\nApp: HelloWorld\nVersion: %s\nBuild Date: %s\nDeployed At: %s", 
                name, VERSION, BUILD_DATE, timestamp);
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(responseBody)
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
                "  \"app\": \"HelloWorld\",\n" +
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
                "  }\n" +
                "}",
                VERSION, BUILD_DATE, timestamp, uptimeFormatted,
                usedMemory, freeMemory, maxMemory, (double) usedMemory / maxMemory * 100,
                runtimeBean.getVmName(), runtimeBean.getVmVersion(), runtimeBean.getVmVendor()
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
                "  \"app\": \"HelloWorld\",\n" +
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
}
