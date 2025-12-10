package com.example.ecompoc.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response DTO for gift card details
 */
public class GiftCardResponse {
    
    @JsonProperty("giftCardId")
    private String giftCardId;
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("balance")
    private BigDecimal balance;
    
    @JsonProperty("status")
    private String status;
    
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
    
    @JsonProperty("purchaseDate")
    private String purchaseDate;
    
    @JsonProperty("expirationDate")
    private String expirationDate;
    
    @JsonProperty("scheduledDeliveryDate")
    private String scheduledDeliveryDate;
    
    // Default constructor
    public GiftCardResponse() {}
    
    // Getters and Setters
    public String getGiftCardId() {
        return giftCardId;
    }
    
    public void setGiftCardId(String giftCardId) {
        this.giftCardId = giftCardId;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public String getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    
    public String getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public String getScheduledDeliveryDate() {
        return scheduledDeliveryDate;
    }
    
    public void setScheduledDeliveryDate(String scheduledDeliveryDate) {
        this.scheduledDeliveryDate = scheduledDeliveryDate;
    }
}
