package com.example.ecompoc.pricealert.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Price Alert domain model
 */
@Entity
@Table(name = "price_alerts")
public class PriceAlert {
    @Id
    @Column(name = "alert_id", columnDefinition = "VARCHAR(255)")
    private String alertId;
    
    @Column(name = "product_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String productId;
    
    @Column(name = "user_email", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String userEmail;
    
    @Column(name = "user_id", columnDefinition = "VARCHAR(255)")
    private String userId;
    
    @Column(name = "target_price", precision = 10, scale = 2)
    private BigDecimal targetPrice;
    
    @Column(name = "current_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(name = "notification_frequency", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String notificationFrequency; // IMMEDIATE, DAILY_DIGEST, WEEKLY_DIGEST
    
    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String status; // ACTIVE, TRIGGERED, EXPIRED, CANCELLED
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor
    public PriceAlert() {}
    
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
    
    public BigDecimal getTargetPrice() {
        return targetPrice;
    }
    
    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }
    
    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

