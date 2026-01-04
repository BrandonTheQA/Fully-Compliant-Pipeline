package com.example.ecompoc.pricealert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for price alert data
 */
public class PriceAlertResponse {
    
    @JsonProperty("alertId")
    private String alertId;
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("userEmail")
    private String userEmail;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("targetPrice")
    private Double targetPrice;
    
    @JsonProperty("currentPrice")
    private Double currentPrice;
    
    @JsonProperty("notificationFrequency")
    private String notificationFrequency;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    @JsonProperty("lastTriggeredAt")
    private String lastTriggeredAt;
    
    @JsonProperty("updatedAt")
    private String updatedAt;
    
    // Default constructor
    public PriceAlertResponse() {}
    
    // Getters and Setters
    public String getAlertId() {
        return alertId;
    }
    
    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Double getTargetPrice() {
        return targetPrice;
    }
    
    public void setTargetPrice(Double targetPrice) {
        this.targetPrice = targetPrice;
    }
    
    public Double getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public String getNotificationFrequency() {
        return notificationFrequency;
    }
    
    public void setNotificationFrequency(String notificationFrequency) {
        this.notificationFrequency = notificationFrequency;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getLastTriggeredAt() {
        return lastTriggeredAt;
    }
    
    public void setLastTriggeredAt(String lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

