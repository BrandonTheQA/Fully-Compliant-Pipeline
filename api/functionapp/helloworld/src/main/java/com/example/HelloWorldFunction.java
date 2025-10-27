package com.example;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
}
