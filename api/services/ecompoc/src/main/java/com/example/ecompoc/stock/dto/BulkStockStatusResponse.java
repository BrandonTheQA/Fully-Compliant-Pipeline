package com.example.ecompoc.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for bulk stock status query
 */
public class BulkStockStatusResponse {
    
    @JsonProperty("statuses")
    private List<StockStatusResponse> statuses;
    
    // Default constructor
    public BulkStockStatusResponse() {}
    
    // Constructor with statuses
    public BulkStockStatusResponse(List<StockStatusResponse> statuses) {
        this.statuses = statuses;
    }
    
    // Getters and Setters
    public List<StockStatusResponse> getStatuses() {
        return statuses;
    }
    
    public void setStatuses(List<StockStatusResponse> statuses) {
        this.statuses = statuses;
    }
}

