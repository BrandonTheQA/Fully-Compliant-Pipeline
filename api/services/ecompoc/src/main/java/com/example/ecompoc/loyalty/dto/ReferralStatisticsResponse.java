package com.example.ecompoc.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for referral statistics
 */
public class ReferralStatisticsResponse {
    
    @JsonProperty("totalReferrals")
    private Integer totalReferrals;
    
    @JsonProperty("successfulReferrals")
    private Integer successfulReferrals;
    
    @JsonProperty("pointsEarned")
    private Integer pointsEarned;
    
    @JsonProperty("successRate")
    private Double successRate;
    
    // Default constructor
    public ReferralStatisticsResponse() {}
    
    // Getters and Setters
    public Integer getTotalReferrals() {
        return totalReferrals;
    }
    
    public void setTotalReferrals(Integer totalReferrals) {
        this.totalReferrals = totalReferrals;
    }
    
    public Integer getSuccessfulReferrals() {
        return successfulReferrals;
    }
    
    public void setSuccessfulReferrals(Integer successfulReferrals) {
        this.successfulReferrals = successfulReferrals;
    }
    
    public Integer getPointsEarned() {
        return pointsEarned;
    }
    
    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }
    
    public Double getSuccessRate() {
        return successRate;
    }
    
    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }
}
