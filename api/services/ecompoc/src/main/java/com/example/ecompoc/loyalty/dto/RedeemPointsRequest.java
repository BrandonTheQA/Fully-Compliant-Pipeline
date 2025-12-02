package com.example.ecompoc.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Request DTO for redeeming points
 */
public class RedeemPointsRequest {
    
    @NotNull(message = "Points to redeem is required")
    @Min(value = 1, message = "Points must be at least 1")
    @JsonProperty("points")
    private Integer points;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("orderTotal")
    private Double orderTotal;
    
    // Default constructor
    public RedeemPointsRequest() {}
    
    // Getters and Setters
    public Integer getPoints() {
        return points;
    }
    
    public void setPoints(Integer points) {
        this.points = points;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public Double getOrderTotal() {
        return orderTotal;
    }
    
    public void setOrderTotal(Double orderTotal) {
        this.orderTotal = orderTotal;
    }
}
