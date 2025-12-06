package com.example.ecompoc.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for stock status
 */
public class StockStatusResponse {
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("status")
    private String status; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("lowStockThreshold")
    private Integer lowStockThreshold;
    
    @JsonProperty("message")
    private String message;
    
    // Default constructor
    public StockStatusResponse() {}
    
    // Constructor with all fields
    public StockStatusResponse(String productId, String status, Integer quantity, 
                              Integer lowStockThreshold, String message) {
        this.productId = productId;
        this.status = status;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
        this.message = message;
    }
    
    // Getters and Setters
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }
    
    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

