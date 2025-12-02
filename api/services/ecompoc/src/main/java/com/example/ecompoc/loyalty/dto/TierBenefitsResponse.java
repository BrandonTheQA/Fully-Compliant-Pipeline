package com.example.ecompoc.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for tier benefits information
 */
public class TierBenefitsResponse {
    
    @JsonProperty("tier")
    private String tier;
    
    @JsonProperty("multiplier")
    private Double multiplier;
    
    @JsonProperty("benefits")
    private List<String> benefits;
    
    @JsonProperty("pointsToNextTier")
    private Integer pointsToNextTier;
    
    // Default constructor
    public TierBenefitsResponse() {}
    
    // Getters and Setters
    public String getTier() {
        return tier;
    }
    
    public void setTier(String tier) {
        this.tier = tier;
    }
    
    public Double getMultiplier() {
        return multiplier;
    }
    
    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }
    
    public List<String> getBenefits() {
        return benefits;
    }
    
    public void setBenefits(List<String> benefits) {
        this.benefits = benefits;
    }
    
    public Integer getPointsToNextTier() {
        return pointsToNextTier;
    }
    
    public void setPointsToNextTier(Integer pointsToNextTier) {
        this.pointsToNextTier = pointsToNextTier;
    }
}
