package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for login
 */
public class LoginResponse {
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("loginTime")
    private String loginTime;
    
    // Default constructor
    public LoginResponse() {}
    
    // Constructor with all fields
    public LoginResponse(String message, String token, String userId, String name, String email, String loginTime) {
        this.message = message;
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.loginTime = loginTime;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
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
    
    public String getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }
}
