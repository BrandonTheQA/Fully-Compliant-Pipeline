package com.example.ecompoc.loyalty.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Loyalty referral domain model
 */
@Entity
@Table(name = "loyalty_referrals")
public class LoyaltyReferral {
    @Id
    @Column(name = "referral_id", columnDefinition = "VARCHAR(255)")
    private String referralId;
    
    @Column(name = "referrer_account_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String referrerAccountId;
    
    @Column(name = "referred_user_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String referredUserId;
    
    @Column(name = "referral_code", nullable = false, columnDefinition = "VARCHAR(255)")
    private String referralCode;
    
    @Column(name = "referral_method", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String referralMethod;
    
    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String status;
    
    @Column(name = "points_awarded", nullable = false)
    private Boolean pointsAwarded = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Default constructor
    public LoyaltyReferral() {}
    
    // Constructor with required fields
    public LoyaltyReferral(String referralId, String referrerAccountId, String referredUserId, 
                          String referralCode, String referralMethod, String status) {
        this.referralId = referralId;
        this.referrerAccountId = referrerAccountId;
        this.referredUserId = referredUserId;
        this.referralCode = referralCode;
        this.referralMethod = referralMethod;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getReferralId() {
        return referralId;
    }
    
    public void setReferralId(String referralId) {
        this.referralId = referralId;
    }
    
    public String getReferrerAccountId() {
        return referrerAccountId;
    }
    
    public void setReferrerAccountId(String referrerAccountId) {
        this.referrerAccountId = referrerAccountId;
    }
    
    public String getReferredUserId() {
        return referredUserId;
    }
    
    public void setReferredUserId(String referredUserId) {
        this.referredUserId = referredUserId;
    }
    
    public String getReferralCode() {
        return referralCode;
    }
    
    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
    
    public String getReferralMethod() {
        return referralMethod;
    }
    
    public void setReferralMethod(String referralMethod) {
        this.referralMethod = referralMethod;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Boolean getPointsAwarded() {
        return pointsAwarded;
    }
    
    public void setPointsAwarded(Boolean pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
