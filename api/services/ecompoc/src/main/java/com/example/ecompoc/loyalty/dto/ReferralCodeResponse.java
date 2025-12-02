package com.example.ecompoc.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for referral code
 */
public class ReferralCodeResponse {
    
    @JsonProperty("referralCode")
    private String referralCode;
    
    @JsonProperty("referralLink")
    private String referralLink;
    
    // Default constructor
    public ReferralCodeResponse() {}
    
    // Getters and Setters
    public String getReferralCode() {
        return referralCode;
    }
    
    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
    
    public String getReferralLink() {
        return referralLink;
    }
    
    public void setReferralLink(String referralLink) {
        this.referralLink = referralLink;
    }
}
