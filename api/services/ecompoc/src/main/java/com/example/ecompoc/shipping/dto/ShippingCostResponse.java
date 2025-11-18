package com.example.ecompoc.shipping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response DTO for shipping cost information
 */
public class ShippingCostResponse {
    
    @JsonProperty("region")
    private String region;
    
    @JsonProperty("cartTotal")
    private BigDecimal cartTotal;
    
    @JsonProperty("shippingCost")
    private BigDecimal shippingCost;
    
    @JsonProperty("freeShippingThreshold")
    private BigDecimal freeShippingThreshold;
    
    @JsonProperty("remainingAmount")
    private BigDecimal remainingAmount;
    
    @JsonProperty("qualifiesForFreeShipping")
    private Boolean qualifiesForFreeShipping;
    
    @JsonProperty("defaultShippingCost")
    private BigDecimal defaultShippingCost;
    
    // Default constructor
    public ShippingCostResponse() {}
    
    // Constructor with all fields
    public ShippingCostResponse(String region, BigDecimal cartTotal, BigDecimal shippingCost,
                                BigDecimal freeShippingThreshold, BigDecimal remainingAmount,
                                Boolean qualifiesForFreeShipping, BigDecimal defaultShippingCost) {
        this.region = region;
        this.cartTotal = cartTotal;
        this.shippingCost = shippingCost;
        this.freeShippingThreshold = freeShippingThreshold;
        this.remainingAmount = remainingAmount;
        this.qualifiesForFreeShipping = qualifiesForFreeShipping;
        this.defaultShippingCost = defaultShippingCost;
    }
    
    // Getters and Setters
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
    
    public BigDecimal getShippingCost() {
        return shippingCost;
    }
    
    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }
    
    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }
    
    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
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
    
    public BigDecimal getDefaultShippingCost() {
        return defaultShippingCost;
    }
    
    public void setDefaultShippingCost(BigDecimal defaultShippingCost) {
        this.defaultShippingCost = defaultShippingCost;
    }
}





