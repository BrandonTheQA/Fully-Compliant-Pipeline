package com.example.ecompoc.stock.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Low stock alert domain model
 */
@Entity
@Table(name = "low_stock_alerts")
public class LowStockAlert {
    @Id
    @Column(name = "alert_id", columnDefinition = "VARCHAR(255)")
    private String alertId;
    
    @Column(name = "product_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String productId;
    
    @Column(name = "stock_level", nullable = false)
    private Integer stockLevel;
    
    @Column(name = "threshold", nullable = false)
    private Integer threshold;
    
    @Column(name = "alert_sent_at")
    private LocalDateTime alertSentAt;
    
    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String status; // PENDING, SENT, RESOLVED
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Default constructor
    public LowStockAlert() {}
    
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
    
    public Integer getStockLevel() {
        return stockLevel;
    }
    
    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }
    
    public Integer getThreshold() {
        return threshold;
    }
    
    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }
    
    public LocalDateTime getAlertSentAt() {
        return alertSentAt;
    }
    
    public void setAlertSentAt(LocalDateTime alertSentAt) {
        this.alertSentAt = alertSentAt;
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
}

