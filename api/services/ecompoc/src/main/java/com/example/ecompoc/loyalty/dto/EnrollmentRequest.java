package com.example.ecompoc.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for loyalty program enrollment
 */
public class EnrollmentRequest {
    
    @JsonProperty("referralCode")
    private String referralCode;
    
    // Default constructor
    public EnrollmentRequest() {}
    
    // Getters and Setters
    public String getReferralCode() {
        return referralCode;
    }
    
    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
}
