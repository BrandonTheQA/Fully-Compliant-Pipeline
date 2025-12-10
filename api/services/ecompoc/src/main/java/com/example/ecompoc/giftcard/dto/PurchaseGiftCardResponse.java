package com.example.ecompoc.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for gift card purchase
 */
public class PurchaseGiftCardResponse {
    
    @JsonProperty("giftCards")
    private List<GiftCardResponse> giftCards;
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    // Default constructor
    public PurchaseGiftCardResponse() {}
    
    // Getters and Setters
    public List<GiftCardResponse> getGiftCards() {
        return giftCards;
    }
    
    public void setGiftCards(List<GiftCardResponse> giftCards) {
        this.giftCards = giftCards;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
