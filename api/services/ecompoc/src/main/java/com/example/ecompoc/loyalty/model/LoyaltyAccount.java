package com.example.ecompoc.loyalty.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Loyalty account domain model
 */
@Entity
@Table(name = "loyalty_accounts")
public class LoyaltyAccount {
    @Id
    @Column(name = "account_id", columnDefinition = "VARCHAR(255)")
    private String accountId;
    
    @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "VARCHAR(255)")
    private String userId;
    
    @Column(name = "current_points", nullable = false)
    private Integer currentPoints = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_tier", nullable = false, columnDefinition = "NVARCHAR(50)")
    private LoyaltyTier currentTier = LoyaltyTier.BRONZE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "highest_tier_achieved", nullable = false, columnDefinition = "NVARCHAR(50)")
    private LoyaltyTier highestTierAchieved = LoyaltyTier.BRONZE;
    
    @Column(name = "lifetime_points_earned", nullable = false)
    private Integer lifetimePointsEarned = 0;
    
    @Column(name = "lifetime_points_redeemed", nullable = false)
    private Integer lifetimePointsRedeemed = 0;
    
    @Column(name = "referral_code", nullable = false, unique = true, columnDefinition = "VARCHAR(255)")
    private String referralCode;
    
    @Column(name = "enrollment_date", nullable = false)
    private LocalDateTime enrollmentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_source", nullable = false, columnDefinition = "NVARCHAR(50)")
    private EnrollmentSource enrollmentSource;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "last_activity_date", nullable = false)
    private LocalDateTime lastActivityDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor
    public LoyaltyAccount() {}
    
    // Constructor with required fields
    public LoyaltyAccount(String accountId, String userId, String referralCode, EnrollmentSource enrollmentSource) {
        this.accountId = accountId;
        this.userId = userId;
        this.referralCode = referralCode;
        this.enrollmentSource = enrollmentSource;
        LocalDateTime now = LocalDateTime.now();
        this.enrollmentDate = now;
        this.lastActivityDate = now;
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    // Getters and Setters
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
    
    public Integer getCurrentPoints() {
        return currentPoints;
    }
    
    public void setCurrentPoints(Integer currentPoints) {
        this.currentPoints = currentPoints;
    }
    
    public LoyaltyTier getCurrentTier() {
        return currentTier;
    }
    
    public void setCurrentTier(LoyaltyTier currentTier) {
        this.currentTier = currentTier;
    }
    
    public LoyaltyTier getHighestTierAchieved() {
        return highestTierAchieved;
    }
    
    public void setHighestTierAchieved(LoyaltyTier highestTierAchieved) {
        this.highestTierAchieved = highestTierAchieved;
    }
    
    public Integer getLifetimePointsEarned() {
        return lifetimePointsEarned;
    }
    
    public void setLifetimePointsEarned(Integer lifetimePointsEarned) {
        this.lifetimePointsEarned = lifetimePointsEarned;
    }
    
    public Integer getLifetimePointsRedeemed() {
        return lifetimePointsRedeemed;
    }
    
    public void setLifetimePointsRedeemed(Integer lifetimePointsRedeemed) {
        this.lifetimePointsRedeemed = lifetimePointsRedeemed;
    }
    
    public String getReferralCode() {
        return referralCode;
    }
    
    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
    
    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }
    
    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    public EnrollmentSource getEnrollmentSource() {
        return enrollmentSource;
    }
    
    public void setEnrollmentSource(EnrollmentSource enrollmentSource) {
        this.enrollmentSource = enrollmentSource;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getLastActivityDate() {
        return lastActivityDate;
    }
    
    public void setLastActivityDate(LocalDateTime lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
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
