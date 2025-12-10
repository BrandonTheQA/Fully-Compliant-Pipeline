package com.example.ecompoc.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for balance inquiry
 */
public class BalanceInquiryRequest {
    
    @JsonProperty("code")
    private String code;
    
    // Default constructor
    public BalanceInquiryRequest() {}
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
}
