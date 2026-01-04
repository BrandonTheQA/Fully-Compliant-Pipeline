package com.example.ecompoc.pricealert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * Request DTO for creating a price alert
 */
public class CreatePriceAlertRequest {
    
    @JsonProperty("productId")
    @NotNull(message = "Product ID is required")
    private String productId;
    
    @JsonProperty("email")
    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @JsonProperty("targetPrice")
    private Double targetPrice;
    
    @JsonProperty("notificationFrequency")
    private String notificationFrequency; // IMMEDIATE, DAILY_DIGEST, WEEKLY_DIGEST
    
    @JsonProperty("userId")
    private String userId;
    
    // Default constructor
    public CreatePriceAlertRequest() {}
    
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
    
    public Double getTargetPrice() {
        return targetPrice;
    }
    
    public void setTargetPrice(Double targetPrice) {
        this.targetPrice = targetPrice;
    }
    
    public String getNotificationFrequency() {
        return notificationFrequency;
    }
    
    public void setNotificationFrequency(String notificationFrequency) {
        this.notificationFrequency = notificationFrequency;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
}

