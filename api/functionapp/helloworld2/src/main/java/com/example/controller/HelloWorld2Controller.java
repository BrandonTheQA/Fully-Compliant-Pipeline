package com.example.controller;

import com.example.dto.HelloResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Spring MVC Controller for HelloWorld2 endpoints
 */
@RestController
@RequestMapping("/api")
public class HelloWorld2Controller {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version}")
    private String version;
    
    @Value("${app.build-date}")
    private String buildDate;
    
    /**
     * GET /api/hello - Say hello with query parameter
     */
    @GetMapping("/hello")
    public ResponseEntity<HelloResponse> hello(@RequestParam(required = false) String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        String greeting = String.format("Hello, %s! Welcome to Azure Functions!", name);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        HelloResponse response = new HelloResponse(greeting, appName, version, buildDate, timestamp);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/hello - Say hello with request body
     */
    @PostMapping("/hello")
    public ResponseEntity<HelloResponse> helloPost(@RequestBody(required = false) String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        String greeting = String.format("Hello, %s! Welcome to Azure Functions!", name);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        HelloResponse response = new HelloResponse(greeting, appName, version, buildDate, timestamp);
        return ResponseEntity.ok(response);
    }
}
