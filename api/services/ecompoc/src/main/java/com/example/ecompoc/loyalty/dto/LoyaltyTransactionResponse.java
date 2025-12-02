package com.example.ecompoc.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for loyalty transaction data
 */
public class LoyaltyTransactionResponse {
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("transactionType")
    private String transactionType;
    
    @JsonProperty("points")
    private Integer points;
    
    @JsonProperty("activityType")
    private String activityType;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    @JsonProperty("expirationDate")
    private String expirationDate;
    
    // Default constructor
    public LoyaltyTransactionResponse() {}
    
    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public Integer getPoints() {
        return points;
    }
    
    public void setPoints(Integer points) {
        this.points = points;
    }
    
    public String getActivityType() {
        return activityType;
    }
    
    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
