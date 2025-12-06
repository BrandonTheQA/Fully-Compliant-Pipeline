package com.example.ecompoc.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Request DTO for signing up for stock notifications
 */
public class NotificationSignupRequest {
    
    @JsonProperty("productId")
    @NotBlank(message = "Product ID is required")
    private String productId;
    
    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    // Default constructor
    public NotificationSignupRequest() {}
    
    // Constructor with all fields
    public NotificationSignupRequest(String productId, String email) {
        this.productId = productId;
        this.email = email;
    }
    
    // Getters and Setters
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}

