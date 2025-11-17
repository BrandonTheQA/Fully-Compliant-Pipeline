package com.example.ecompoc.shipping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response DTO for shipping threshold information
 */
public class ShippingThresholdResponse {
    
    @JsonProperty("region")
    private String region;
    
    @JsonProperty("freeShippingThreshold")
    private BigDecimal freeShippingThreshold;
    
    @JsonProperty("currentCartTotal")
    private BigDecimal currentCartTotal;
    
    @JsonProperty("remainingAmount")
    private BigDecimal remainingAmount;
    
    @JsonProperty("qualifiesForFreeShipping")
    private Boolean qualifiesForFreeShipping;
    
    // Default constructor
    public ShippingThresholdResponse() {}
    
    // Constructor with all fields
    public ShippingThresholdResponse(String region, BigDecimal freeShippingThreshold, 
                                     BigDecimal currentCartTotal, BigDecimal remainingAmount, 
                                     Boolean qualifiesForFreeShipping) {
        this.region = region;
        this.freeShippingThreshold = freeShippingThreshold;
        this.currentCartTotal = currentCartTotal;
        this.remainingAmount = remainingAmount;
        this.qualifiesForFreeShipping = qualifiesForFreeShipping;
    }
    
    // Getters and Setters
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }
    
    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }
    
    public BigDecimal getCurrentCartTotal() {
        return currentCartTotal;
    }
    
    public void setCurrentCartTotal(BigDecimal currentCartTotal) {
        this.currentCartTotal = currentCartTotal;
    }
    
    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }
    
    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
    
    public Boolean getQualifiesForFreeShipping() {
        return qualifiesForFreeShipping;
    }
    
    public void setQualifiesForFreeShipping(Boolean qualifiesForFreeShipping) {
        this.qualifiesForFreeShipping = qualifiesForFreeShipping;
    }
}

