package com.example.ecompoc.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for complete loyalty dashboard data
 */
public class LoyaltyDashboardResponse {
    
    @JsonProperty("account")
    private LoyaltyAccountResponse account;
    
    @JsonProperty("recentTransactions")
    private List<LoyaltyTransactionResponse> recentTransactions;
    
    @JsonProperty("pointsToNextTier")
    private Integer pointsToNextTier;
    
    @JsonProperty("expiringPoints")
    private Integer expiringPoints;
    
    @JsonProperty("expiringPointsDate")
    private String expiringPointsDate;
    
    @JsonProperty("tierBenefits")
    private TierBenefitsResponse tierBenefits;
    
    // Default constructor
    public LoyaltyDashboardResponse() {}
    
    // Getters and Setters
    public LoyaltyAccountResponse getAccount() {
        return account;
    }
    
    public void setAccount(LoyaltyAccountResponse account) {
        this.account = account;
    }
    
    public List<LoyaltyTransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }
    
    public void setRecentTransactions(List<LoyaltyTransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }
    
    public Integer getPointsToNextTier() {
        return pointsToNextTier;
    }
    
    public void setPointsToNextTier(Integer pointsToNextTier) {
        this.pointsToNextTier = pointsToNextTier;
    }
    
    public Integer getExpiringPoints() {
        return expiringPoints;
    }
    
    public void setExpiringPoints(Integer expiringPoints) {
        this.expiringPoints = expiringPoints;
    }
    
    public String getExpiringPointsDate() {
        return expiringPointsDate;
    }
    
    public void setExpiringPointsDate(String expiringPointsDate) {
        this.expiringPointsDate = expiringPointsDate;
    }
    
    public TierBenefitsResponse getTierBenefits() {
        return tierBenefits;
    }
    
    public void setTierBenefits(TierBenefitsResponse tierBenefits) {
        this.tierBenefits = tierBenefits;
    }
}
