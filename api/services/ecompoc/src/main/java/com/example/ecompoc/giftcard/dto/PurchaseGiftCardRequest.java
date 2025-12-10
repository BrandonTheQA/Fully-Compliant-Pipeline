package com.example.ecompoc.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for purchasing gift cards
 */
public class PurchaseGiftCardRequest {
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("quantity")
    private Integer quantity = 1;
    
    @JsonProperty("purchaserId")
    private String purchaserId;
    
    @JsonProperty("purchaserEmail")
    private String purchaserEmail;
    
    @JsonProperty("recipientEmail")
    private String recipientEmail;
    
    @JsonProperty("recipientName")
    private String recipientName;
    
    @JsonProperty("personalMessage")
    private String personalMessage;
    
    @JsonProperty("design")
    private String design;
    
    @JsonProperty("scheduledDeliveryDate")
    private String scheduledDeliveryDate; // ISO format string
    
    // Default constructor
    public PurchaseGiftCardRequest() {}
    
    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getPurchaserId() {
        return purchaserId;
    }
    
    public void setPurchaserId(String purchaserId) {
        this.purchaserId = purchaserId;
    }
    
    public String getPurchaserEmail() {
        return purchaserEmail;
    }
    
    public void setPurchaserEmail(String purchaserEmail) {
        this.purchaserEmail = purchaserEmail;
    }
    
    public String getRecipientEmail() {
        return recipientEmail;
    }
    
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
    
    public String getRecipientName() {
        return recipientName;
    }
    
    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
    
    public String getPersonalMessage() {
        return personalMessage;
    }
    
    public void setPersonalMessage(String personalMessage) {
        this.personalMessage = personalMessage;
    }
    
    public String getDesign() {
        return design;
    }
    
    public void setDesign(String design) {
        this.design = design;
    }
    
    public String getScheduledDeliveryDate() {
        return scheduledDeliveryDate;
    }
    
    public void setScheduledDeliveryDate(String scheduledDeliveryDate) {
        this.scheduledDeliveryDate = scheduledDeliveryDate;
    }
}
