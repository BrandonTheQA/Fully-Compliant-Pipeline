package com.example.ecompoc.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response DTO for applying gift card to order
 */
public class ApplyGiftCardResponse {
    
    @JsonProperty("appliedAmount")
    private BigDecimal appliedAmount;
    
    @JsonProperty("remainingBalance")
    private BigDecimal remainingBalance;
    
    @JsonProperty("orderTotal")
    private BigDecimal orderTotal;
    
    @JsonProperty("giftCard")
    private GiftCardResponse giftCard;
    
    // Default constructor
    public ApplyGiftCardResponse() {}
    
    // Getters and Setters
    public BigDecimal getAppliedAmount() {
        return appliedAmount;
    }
    
    public void setAppliedAmount(BigDecimal appliedAmount) {
        this.appliedAmount = appliedAmount;
    }
    
    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }
    
    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }
    
    public BigDecimal getOrderTotal() {
        return orderTotal;
    }
    
    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
    }
    
    public GiftCardResponse getGiftCard() {
        return giftCard;
    }
    
    public void setGiftCard(GiftCardResponse giftCard) {
        this.giftCard = giftCard;
    }
}
