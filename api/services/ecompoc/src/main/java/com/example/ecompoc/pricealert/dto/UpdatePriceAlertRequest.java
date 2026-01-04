package com.example.ecompoc.pricealert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for updating a price alert
 */
public class UpdatePriceAlertRequest {
    
    @JsonProperty("targetPrice")
    private Double targetPrice;
    
    @JsonProperty("notificationFrequency")
    private String notificationFrequency;
    
    @JsonProperty("status")
    private String status;
    
    // Default constructor
    public UpdatePriceAlertRequest() {}
    
    // Getters and Setters
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}

