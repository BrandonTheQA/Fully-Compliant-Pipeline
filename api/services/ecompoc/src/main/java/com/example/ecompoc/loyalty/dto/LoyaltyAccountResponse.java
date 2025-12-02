package com.example.ecompoc.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for loyalty account data
 */
public class LoyaltyAccountResponse {
    
    @JsonProperty("accountId")
    private String accountId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("currentPoints")
    private Integer currentPoints;
    
    @JsonProperty("currentTier")
    private String currentTier;
    
    @JsonProperty("highestTierAchieved")
    private String highestTierAchieved;
    
    @JsonProperty("lifetimePointsEarned")
    private Integer lifetimePointsEarned;
    
    @JsonProperty("lifetimePointsRedeemed")
    private Integer lifetimePointsRedeemed;
    
    @JsonProperty("referralCode")
    private String referralCode;
    
    @JsonProperty("enrollmentDate")
    private String enrollmentDate;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    // Default constructor
    public LoyaltyAccountResponse() {}
    
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
    
    public String getCurrentTier() {
        return currentTier;
    }
    
    public void setCurrentTier(String currentTier) {
        this.currentTier = currentTier;
    }
    
    public String getHighestTierAchieved() {
        return highestTierAchieved;
    }
    
    public void setHighestTierAchieved(String highestTierAchieved) {
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
    
    public String getEnrollmentDate() {
        return enrollmentDate;
    }
    
    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
