package com.example.ecompoc.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response DTO for gift card transaction
 */
public class GiftCardTransactionResponse {
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("giftCardId")
    private String giftCardId;
    
    @JsonProperty("transactionType")
    private String transactionType;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    // Default constructor
    public GiftCardTransactionResponse() {}
    
    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getGiftCardId() {
        return giftCardId;
    }
    
    public void setGiftCardId(String giftCardId) {
        this.giftCardId = giftCardId;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
