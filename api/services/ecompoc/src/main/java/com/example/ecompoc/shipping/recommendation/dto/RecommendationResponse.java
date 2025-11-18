package com.example.ecompoc.shipping.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for shipping optimization recommendations
 */
public class RecommendationResponse {
    
    @JsonProperty("optimizationPaths")
    private List<OptimizationPath> optimizationPaths;
    
    @JsonProperty("qualifiesForFreeShipping")
    private Boolean qualifiesForFreeShipping;
    
    @JsonProperty("remainingAmount")
    private BigDecimal remainingAmount;
    
    @JsonProperty("region")
    private String region;
    
    @JsonProperty("cartTotal")
    private BigDecimal cartTotal;
    
    @JsonProperty("freeShippingThreshold")
    private BigDecimal freeShippingThreshold;
    
    // Default constructor
    public RecommendationResponse() {}
    
    // Constructor with all fields
    public RecommendationResponse(List<OptimizationPath> optimizationPaths, 
                                 Boolean qualifiesForFreeShipping, BigDecimal remainingAmount,
                                 String region, BigDecimal cartTotal, BigDecimal freeShippingThreshold) {
        this.optimizationPaths = optimizationPaths;
        this.qualifiesForFreeShipping = qualifiesForFreeShipping;
        this.remainingAmount = remainingAmount;
        this.region = region;
        this.cartTotal = cartTotal;
        this.freeShippingThreshold = freeShippingThreshold;
    }
    
    // Getters and Setters
    public List<OptimizationPath> getOptimizationPaths() {
        return optimizationPaths;
    }
    
    public void setOptimizationPaths(List<OptimizationPath> optimizationPaths) {
        this.optimizationPaths = optimizationPaths;
    }
    
    public Boolean getQualifiesForFreeShipping() {
        return qualifiesForFreeShipping;
    }
    
    public void setQualifiesForFreeShipping(Boolean qualifiesForFreeShipping) {
        this.qualifiesForFreeShipping = qualifiesForFreeShipping;
    }
    
    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }
    
    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public BigDecimal getCartTotal() {
        return cartTotal;
    }
    
    public void setCartTotal(BigDecimal cartTotal) {
        this.cartTotal = cartTotal;
    }
    
    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }
    
    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }
}

