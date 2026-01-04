package com.example.ecompoc.pricealert.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Price History domain model
 */
@Entity
@Table(name = "price_history")
public class PriceHistory {
    @Id
    @Column(name = "price_history_id", columnDefinition = "VARCHAR(255)")
    private String priceHistoryId;
    
    @Column(name = "product_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String productId;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "previous_price", precision = 10, scale = 2)
    private BigDecimal previousPrice;
    
    @Column(name = "change_type", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String changeType; // INCREASE, DECREASE, NO_CHANGE
    
    @Column(name = "change_percentage", precision = 5, scale = 2)
    private BigDecimal changePercentage;
    
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
    
    // Default constructor
    public PriceHistory() {}
    
    // Getters and Setters
    public String getPriceHistoryId() {
        return priceHistoryId;
    }
    
    public void setPriceHistoryId(String priceHistoryId) {
        this.priceHistoryId = priceHistoryId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getPreviousPrice() {
        return previousPrice;
    }
    
    public void setPreviousPrice(BigDecimal previousPrice) {
        this.previousPrice = previousPrice;
    }
    
    public String getChangeType() {
        return changeType;
    }
    
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }
    
    public BigDecimal getChangePercentage() {
        return changePercentage;
    }
    
    public void setChangePercentage(BigDecimal changePercentage) {
        this.changePercentage = changePercentage;
    }
    
    public LocalDateTime getChangedAt() {
        return changedAt;
    }
    
    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}

