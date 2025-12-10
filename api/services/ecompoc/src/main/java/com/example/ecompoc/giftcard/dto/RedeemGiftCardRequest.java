package com.example.ecompoc.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Request DTO for redeeming gift cards
 */
public class RedeemGiftCardRequest {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("redemptionAmount")
    private BigDecimal redemptionAmount;
    
    // Default constructor
    public RedeemGiftCardRequest() {}
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public BigDecimal getRedemptionAmount() {
        return redemptionAmount;
    }
    
    public void setRedemptionAmount(BigDecimal redemptionAmount) {
        this.redemptionAmount = redemptionAmount;
    }
}
