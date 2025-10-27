package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response DTO for Hello World endpoint
 */
public class HelloResponse {
    
    @JsonProperty("greeting")
    private String greeting;
    
    @JsonProperty("app")
    private String app;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("buildDate")
    private String buildDate;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    // Default constructor
    public HelloResponse() {}
    
    // Constructor with all fields
    public HelloResponse(String greeting, String app, String version, String buildDate, String timestamp) {
        this.greeting = greeting;
        this.app = app;
        this.version = version;
        this.buildDate = buildDate;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getGreeting() {
        return greeting;
    }
    
    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
    
    public String getApp() {
        return app;
    }
    
    public void setApp(String app) {
        this.app = app;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getBuildDate() {
        return buildDate;
    }
    
    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
