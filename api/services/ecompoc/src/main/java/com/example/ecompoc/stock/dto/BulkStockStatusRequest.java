package com.example.ecompoc.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request DTO for bulk stock status query
 */
public class BulkStockStatusRequest {
    
    @JsonProperty("productIds")
    private List<String> productIds;
    
    // Default constructor
    public BulkStockStatusRequest() {}
    
    // Constructor with productIds
    public BulkStockStatusRequest(List<String> productIds) {
        this.productIds = productIds;
    }
    
    // Getters and Setters
    public List<String> getProductIds() {
        return productIds;
    }
    
    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }
}

