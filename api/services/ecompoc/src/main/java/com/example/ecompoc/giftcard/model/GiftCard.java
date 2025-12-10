package com.example.ecompoc.giftcard.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Gift Card domain model
 */
@Entity
@Table(name = "gift_cards")
public class GiftCard {
    @Id
    @Column(name = "gift_card_id", columnDefinition = "VARCHAR(255)")
    private String giftCardId;
    
    @Column(name = "code", nullable = false, unique = true, columnDefinition = "VARCHAR(255)")
    private String code;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(50)")
    private GiftCardStatus status;
    
    @Column(name = "purchaser_id", columnDefinition = "VARCHAR(255)")
    private String purchaserId;
    
    @Column(name = "purchaser_email", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String purchaserEmail;
    
    @Column(name = "recipient_email", columnDefinition = "NVARCHAR(255)")
    private String recipientEmail;
    
    @Column(name = "recipient_name", columnDefinition = "NVARCHAR(255)")
    private String recipientName;
    
    @Column(name = "personal_message", columnDefinition = "NVARCHAR(500)")
    private String personalMessage;
    
    @Column(name = "design", columnDefinition = "NVARCHAR(50)")
    private String design;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;
    
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;
    
    @Column(name = "scheduled_delivery_date")
    private LocalDateTime scheduledDeliveryDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor
    public GiftCard() {}
    
    // Constructor with required fields
    public GiftCard(String giftCardId, String code, BigDecimal amount, String purchaserEmail) {
        this.giftCardId = giftCardId;
        this.code = code;
        this.amount = amount;
        this.balance = amount;
        this.purchaserEmail = purchaserEmail;
        this.status = GiftCardStatus.ACTIVE;
        LocalDateTime now = LocalDateTime.now();
        this.purchaseDate = now;
        this.expirationDate = now.plusMonths(12); // Default 12 months expiration
        this.createdAt = now;
        this.updatedAt = now;
    }
    
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
    
    public GiftCardStatus getStatus() {
        return status;
    }
    
    public void setStatus(GiftCardStatus status) {
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
    
    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public LocalDateTime getScheduledDeliveryDate() {
        return scheduledDeliveryDate;
    }
    
    public void setScheduledDeliveryDate(LocalDateTime scheduledDeliveryDate) {
        this.scheduledDeliveryDate = scheduledDeliveryDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
