package com.example.ecompoc.returns.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for updating return status
 */
public class UpdateReturnStatusRequest {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("notes")
    private String notes;
    
    public UpdateReturnStatusRequest() {}
    
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
}

