package com.example.ecompoc.order.notification.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Order notification entity to track notification delivery
 */
@Entity
@Table(name = "order_notifications")
public class OrderNotification {
    
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    private String id;
    
    @Column(name = "order_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String orderId;
    
    @Column(name = "notification_type", nullable = false, columnDefinition = "NVARCHAR(20)")
    private String notificationType; // "EMAIL", "SMS"
    
    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(20)")
    private String status; // "PENDING", "SENT", "FAILED"
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "error_message", columnDefinition = "NVARCHAR(500)")
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public OrderNotification() {}
    
    public OrderNotification(String id, String orderId, String notificationType, String status) {
        this.id = id;
        this.orderId = orderId;
        this.notificationType = notificationType;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
