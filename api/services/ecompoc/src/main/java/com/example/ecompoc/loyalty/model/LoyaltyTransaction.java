package com.example.ecompoc.loyalty.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Loyalty transaction domain model
 */
@Entity
@Table(name = "loyalty_transactions")
public class LoyaltyTransaction {
    @Id
    @Column(name = "transaction_id", columnDefinition = "VARCHAR(255)")
    private String transactionId;
    
    @Column(name = "account_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String accountId;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, columnDefinition = "NVARCHAR(50)")
    private TransactionType transactionType;
    
    @Column(name = "points", nullable = false)
    private Integer points;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, columnDefinition = "NVARCHAR(50)")
    private ActivityType activityType;
    
    @Column(name = "related_order_id", columnDefinition = "VARCHAR(255)")
    private String relatedOrderId;
    
    @Column(name = "related_review_id", columnDefinition = "VARCHAR(255)")
    private String relatedReviewId;
    
    @Column(name = "related_referral_id", columnDefinition = "VARCHAR(255)")
    private String relatedReferralId;
    
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
    
    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Default constructor
    public LoyaltyTransaction() {}
    
    // Constructor with required fields
    public LoyaltyTransaction(String transactionId, String accountId, String userId, 
                            TransactionType transactionType, Integer points, ActivityType activityType) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.userId = userId;
        this.transactionType = transactionType;
        this.points = points;
        this.activityType = activityType;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getAccountId() {
        return accountId;
    }
    
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public Integer getPoints() {
        return points;
    }
    
    public void setPoints(Integer points) {
        this.points = points;
    }
    
    public ActivityType getActivityType() {
        return activityType;
    }
    
    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }
    
    public String getRelatedOrderId() {
        return relatedOrderId;
    }
    
    public void setRelatedOrderId(String relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }
    
    public String getRelatedReviewId() {
        return relatedReviewId;
    }
    
    public void setRelatedReviewId(String relatedReviewId) {
        this.relatedReviewId = relatedReviewId;
    }
    
    public String getRelatedReferralId() {
        return relatedReferralId;
    }
    
    public void setRelatedReferralId(String relatedReferralId) {
        this.relatedReferralId = relatedReferralId;
    }
    
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
