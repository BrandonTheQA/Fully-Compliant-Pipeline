package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for user data (excluding password)
 */
public class UserResponse {
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    // Default constructor
    public UserResponse() {}
    
    // Constructor with all fields
    public UserResponse(String userId, String name, String email, String createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
