package com.example.ecompoc.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for point redemption
 */
public class RedeemPointsResponse {
    
    @JsonProperty("pointsRedeemed")
    private Integer pointsRedeemed;
    
    @JsonProperty("discountAmount")
    private Double discountAmount;
    
    @JsonProperty("remainingBalance")
    private Integer remainingBalance;
    
    @JsonProperty("message")
    private String message;
    
    // Default constructor
    public RedeemPointsResponse() {}
    
    // Getters and Setters
    public Integer getPointsRedeemed() {
        return pointsRedeemed;
    }
    
    public void setPointsRedeemed(Integer pointsRedeemed) {
        this.pointsRedeemed = pointsRedeemed;
    }
    
    public Double getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public Integer getRemainingBalance() {
        return remainingBalance;
    }
    
    public void setRemainingBalance(Integer remainingBalance) {
        this.remainingBalance = remainingBalance;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
