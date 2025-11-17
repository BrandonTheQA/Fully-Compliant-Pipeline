package com.example.ecompoc.shipping.model;

import java.math.BigDecimal;

/**
 * Shipping rule model for region-specific shipping thresholds
 */
public class ShippingRule {
    
    private String region;
    private BigDecimal freeShippingThreshold;
    private BigDecimal defaultShippingCost;
    
    // Default constructor
    public ShippingRule() {}
    
    // Constructor with all fields
    public ShippingRule(String region, BigDecimal freeShippingThreshold, BigDecimal defaultShippingCost) {
        this.region = region;
        this.freeShippingThreshold = freeShippingThreshold;
        this.defaultShippingCost = defaultShippingCost;
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
    
    public BigDecimal getDefaultShippingCost() {
        return defaultShippingCost;
    }
    
    public void setDefaultShippingCost(BigDecimal defaultShippingCost) {
        this.defaultShippingCost = defaultShippingCost;
    }
}

