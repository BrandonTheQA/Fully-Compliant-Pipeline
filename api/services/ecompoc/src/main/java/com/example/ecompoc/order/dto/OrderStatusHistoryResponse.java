package com.example.ecompoc.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for order status history entry
 */
public class OrderStatusHistoryResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    public OrderStatusHistoryResponse() {}
    
    public OrderStatusHistoryResponse(String id, String status, String location, String notes, String createdAt) {
        this.id = id;
        this.status = status;
        this.location = location;
        this.notes = notes;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
