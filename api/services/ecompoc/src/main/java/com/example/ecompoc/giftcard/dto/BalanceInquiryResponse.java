package com.example.ecompoc.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Response DTO for balance inquiry
 */
public class BalanceInquiryResponse {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("balance")
    private BigDecimal balance;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("expirationDate")
    private String expirationDate;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    // Default constructor
    public BalanceInquiryResponse() {}
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
