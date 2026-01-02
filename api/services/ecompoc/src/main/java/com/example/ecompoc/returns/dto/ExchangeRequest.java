package com.example.ecompoc.returns.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;

/**
 * Request DTO for creating an exchange
 */
public class ExchangeRequest {
    
    @NotBlank(message = "Exchange product ID is required")
    @JsonProperty("exchangeProductId")
    private String exchangeProductId;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("notes")
    private String notes;
    
    public ExchangeRequest() {}
    
    public String getExchangeProductId() {
        return exchangeProductId;
    }
    
    public void setExchangeProductId(String exchangeProductId) {
        this.exchangeProductId = exchangeProductId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}

