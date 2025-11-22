package com.example.ecompoc.abandonedcart.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Abandoned Cart Email domain model
 */
@Entity
@Table(name = "abandoned_cart_emails")
public class AbandonedCartEmail {
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    private String id;
    
    @Column(name = "abandoned_cart_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String abandonedCartId;
    
    @Column(name = "email_type", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String emailType; // FIRST, FOLLOWUP_24H, FOLLOWUP_72H
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "opened_at")
    private LocalDateTime openedAt;
    
    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Default constructor
    public AbandonedCartEmail() {}
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAbandonedCartId() {
        return abandonedCartId;
    }
    
    public void setAbandonedCartId(String abandonedCartId) {
        this.abandonedCartId = abandonedCartId;
    }
    
    public String getEmailType() {
        return emailType;
    }
    
    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public LocalDateTime getOpenedAt() {
        return openedAt;
    }
    
    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }
    
    public LocalDateTime getClickedAt() {
        return clickedAt;
    }
    
    public void setClickedAt(LocalDateTime clickedAt) {
        this.clickedAt = clickedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

