package com.example.ecompoc.returns.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for return policy information
 */
public class ReturnPolicyResponse {
    
    @JsonProperty("returnWindowDays")
    private Integer returnWindowDays;
    
    @JsonProperty("restockingFeePercentage")
    private Double restockingFeePercentage;
    
    @JsonProperty("freeReturnThreshold")
    private Double freeReturnThreshold;
    
    @JsonProperty("autoApproveThreshold")
    private Double autoApproveThreshold;
    
    public ReturnPolicyResponse() {}
    
    // Getters and Setters
    public Integer getReturnWindowDays() {
        return returnWindowDays;
    }
    
    public void setReturnWindowDays(Integer returnWindowDays) {
        this.returnWindowDays = returnWindowDays;
    }
    
    public Double getRestockingFeePercentage() {
        return restockingFeePercentage;
    }
    
    public void setRestockingFeePercentage(Double restockingFeePercentage) {
        this.restockingFeePercentage = restockingFeePercentage;
    }
    
    public Double getFreeReturnThreshold() {
        return freeReturnThreshold;
    }
    
    public void setFreeReturnThreshold(Double freeReturnThreshold) {
        this.freeReturnThreshold = freeReturnThreshold;
    }
    
    public Double getAutoApproveThreshold() {
        return autoApproveThreshold;
    }
    
    public void setAutoApproveThreshold(Double autoApproveThreshold) {
        this.autoApproveThreshold = autoApproveThreshold;
    }
}

