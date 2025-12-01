package com.example.ecompoc.order.notification.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Notification preferences entity for user notification settings
 */
@Entity
@Table(name = "notification_preferences")
public class NotificationPreferences {
    
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    private String id;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(255)", unique = true)
    private String userId;
    
    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled;
    
    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled;
    
    @Column(name = "phone_number", columnDefinition = "VARCHAR(20)")
    private String phoneNumber;
    
    @Column(name = "notification_frequency", nullable = false, columnDefinition = "NVARCHAR(20)")
    private String notificationFrequency; // "ALL", "CRITICAL_ONLY", "NONE"
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public NotificationPreferences() {
        this.emailEnabled = true;
        this.smsEnabled = false;
        this.notificationFrequency = "ALL";
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Boolean getEmailEnabled() {
        return emailEnabled;
    }
    
    public void setEmailEnabled(Boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }
    
    public Boolean getSmsEnabled() {
        return smsEnabled;
    }
    
    public void setSmsEnabled(Boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getNotificationFrequency() {
        return notificationFrequency;
    }
    
    public void setNotificationFrequency(String notificationFrequency) {
        this.notificationFrequency = notificationFrequency;
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
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
