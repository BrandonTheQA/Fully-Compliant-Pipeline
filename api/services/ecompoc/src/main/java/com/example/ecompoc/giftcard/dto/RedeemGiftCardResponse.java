package com.example.ecompoc.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response DTO for gift card redemption
 */
public class RedeemGiftCardResponse {
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("remainingBalance")
    private BigDecimal remainingBalance;
    
    @JsonProperty("appliedAmount")
    private BigDecimal appliedAmount;
    
    @JsonProperty("giftCard")
    private GiftCardResponse giftCard;
    
    // Default constructor
    public RedeemGiftCardResponse() {}
    
    // Getters and Setters
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }
    
    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }
    
    public BigDecimal getAppliedAmount() {
        return appliedAmount;
    }
    
    public void setAppliedAmount(BigDecimal appliedAmount) {
        this.appliedAmount = appliedAmount;
    }
    
    public GiftCardResponse getGiftCard() {
        return giftCard;
    }
    
    public void setGiftCard(GiftCardResponse giftCard) {
        this.giftCard = giftCard;
    }
}
