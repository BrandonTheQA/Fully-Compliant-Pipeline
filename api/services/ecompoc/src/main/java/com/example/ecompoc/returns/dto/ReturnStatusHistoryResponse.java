package com.example.ecompoc.returns.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for return status history
 */
public class ReturnStatusHistoryResponse {
    
    @JsonProperty("historyId")
    private Long historyId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("updatedBy")
    private String updatedBy;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    public ReturnStatusHistoryResponse() {}
    
    // Getters and Setters
    public Long getHistoryId() {
        return historyId;
    }
    
    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

