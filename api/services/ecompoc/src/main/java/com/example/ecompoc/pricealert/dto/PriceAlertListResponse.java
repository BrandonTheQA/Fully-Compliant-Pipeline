package com.example.ecompoc.pricealert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for list of price alerts
 */
public class PriceAlertListResponse {
    
    @JsonProperty("alerts")
    private List<PriceAlertResponse> alerts;
    
    // Default constructor
    public PriceAlertListResponse() {}
    
    public PriceAlertListResponse(List<PriceAlertResponse> alerts) {
        this.alerts = alerts;
    }
    
    // Getters and Setters
    public List<PriceAlertResponse> getAlerts() {
        return alerts;
    }
    
    public void setAlerts(List<PriceAlertResponse> alerts) {
        this.alerts = alerts;
    }
}

