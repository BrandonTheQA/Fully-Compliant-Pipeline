package com.example.ecompoc.pricealert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for price history data
 */
public class PriceHistoryResponse {
    
    @JsonProperty("priceHistoryId")
    private String priceHistoryId;
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("price")
    private Double price;
    
    @JsonProperty("previousPrice")
    private Double previousPrice;
    
    @JsonProperty("changeType")
    private String changeType;
    
    @JsonProperty("changePercentage")
    private Double changePercentage;
    
    @JsonProperty("changedAt")
    private String changedAt;
    
    // Default constructor
    public PriceHistoryResponse() {}
    
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
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Double getPreviousPrice() {
        return previousPrice;
    }
    
    public void setPreviousPrice(Double previousPrice) {
        this.previousPrice = previousPrice;
    }
    
    public String getChangeType() {
        return changeType;
    }
    
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }
    
    public Double getChangePercentage() {
        return changePercentage;
    }
    
    public void setChangePercentage(Double changePercentage) {
        this.changePercentage = changePercentage;
    }
    
    public String getChangedAt() {
        return changedAt;
    }
    
    public void setChangedAt(String changedAt) {
        this.changedAt = changedAt;
    }
}

