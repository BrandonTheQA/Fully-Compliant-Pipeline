package com.example.ecompoc.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for stock notification subscription
 */
public class NotificationResponse {
    
    @JsonProperty("notificationId")
    private String notificationId;
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("productName")
    private String productName;
    
    @JsonProperty("status")
    private String status; // PENDING, NOTIFIED, UNSUBSCRIBED
    
    @JsonProperty("signupDate")
    private String signupDate;
    
    @JsonProperty("notifiedDate")
    private String notifiedDate;
    
    // Default constructor
    public NotificationResponse() {}
    
    // Constructor with all fields
    public NotificationResponse(String notificationId, String productId, String productName,
                               String status, String signupDate, String notifiedDate) {
        this.notificationId = notificationId;
        this.productId = productId;
        this.productName = productName;
        this.status = status;
        this.signupDate = signupDate;
        this.notifiedDate = notifiedDate;
    }
    
    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getSignupDate() {
        return signupDate;
    }
    
    public void setSignupDate(String signupDate) {
        this.signupDate = signupDate;
    }
    
    public String getNotifiedDate() {
        return notifiedDate;
    }
    
    public void setNotifiedDate(String notifiedDate) {
        this.notifiedDate = notifiedDate;
    }
}

