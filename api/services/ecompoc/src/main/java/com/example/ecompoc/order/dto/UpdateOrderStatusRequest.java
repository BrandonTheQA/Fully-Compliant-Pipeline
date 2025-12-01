package com.example.ecompoc.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;

/**
 * Request DTO for updating order status
 */
public class UpdateOrderStatusRequest {
    
    @NotBlank(message = "Status is required")
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("notes")
    private String notes;
    
    public UpdateOrderStatusRequest() {}
    
    public UpdateOrderStatusRequest(String status, String location, String notes) {
        this.status = status;
        this.location = location;
        this.notes = notes;
    }
    
    // Getters and Setters
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
}
