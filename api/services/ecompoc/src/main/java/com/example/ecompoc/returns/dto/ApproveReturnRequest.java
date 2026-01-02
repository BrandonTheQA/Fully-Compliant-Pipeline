package com.example.ecompoc.returns.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for approving a return
 */
public class ApproveReturnRequest {
    
    @JsonProperty("notes")
    private String notes;
    
    public ApproveReturnRequest() {}
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}

