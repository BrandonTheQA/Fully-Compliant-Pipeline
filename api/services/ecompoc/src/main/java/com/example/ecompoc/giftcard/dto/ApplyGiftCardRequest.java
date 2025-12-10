package com.example.ecompoc.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Request DTO for applying gift card during checkout
 */
public class ApplyGiftCardRequest {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("orderTotal")
    private BigDecimal orderTotal;
    
    // Default constructor
    public ApplyGiftCardRequest() {}
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public BigDecimal getOrderTotal() {
        return orderTotal;
    }
    
    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
    }
}
