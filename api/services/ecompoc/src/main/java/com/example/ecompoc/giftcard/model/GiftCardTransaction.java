package com.example.ecompoc.giftcard.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Gift Card Transaction domain model
 */
@Entity
@Table(name = "gift_card_transactions")
public class GiftCardTransaction {
    @Id
    @Column(name = "transaction_id", columnDefinition = "VARCHAR(255)")
    private String transactionId;
    
    @Column(name = "gift_card_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String giftCardId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, columnDefinition = "NVARCHAR(50)")
    private GiftCardTransactionType transactionType;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "order_id", columnDefinition = "VARCHAR(255)")
    private String orderId;
    
    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Default constructor
    public GiftCardTransaction() {}
    
    // Constructor with required fields
    public GiftCardTransaction(String transactionId, String giftCardId, 
                              GiftCardTransactionType transactionType, BigDecimal amount) {
        this.transactionId = transactionId;
        this.giftCardId = giftCardId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }
    
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
    
    public GiftCardTransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(GiftCardTransactionType transactionType) {
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
