package com.example.ecompoc.stock.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Stock notification domain model
 */
@Entity
@Table(name = "stock_notifications")
public class StockNotification {
    @Id
    @Column(name = "notification_id", columnDefinition = "VARCHAR(255)")
    private String notificationId;
    
    @Column(name = "product_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String productId;
    
    @Column(name = "user_id", columnDefinition = "VARCHAR(255)")
    private String userId;
    
    @Column(name = "email", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String email;
    
    @Column(name = "signup_date", nullable = false)
    private LocalDateTime signupDate;
    
    @Column(name = "notified_date")
    private LocalDateTime notifiedDate;
    
    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String status; // PENDING, NOTIFIED, UNSUBSCRIBED
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor
    public StockNotification() {}
    
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
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getSignupDate() {
        return signupDate;
    }
    
    public void setSignupDate(LocalDateTime signupDate) {
        this.signupDate = signupDate;
    }
    
    public LocalDateTime getNotifiedDate() {
        return notifiedDate;
    }
    
    public void setNotifiedDate(LocalDateTime notifiedDate) {
        this.notifiedDate = notifiedDate;
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

