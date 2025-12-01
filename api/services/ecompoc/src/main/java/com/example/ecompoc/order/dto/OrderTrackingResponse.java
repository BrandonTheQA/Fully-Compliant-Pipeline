package com.example.ecompoc.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for order tracking information
 */
public class OrderTrackingResponse {
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("trackingNumber")
    private String trackingNumber;
    
    @JsonProperty("carrierName")
    private String carrierName;
    
    @JsonProperty("estimatedDeliveryDate")
    private String estimatedDeliveryDate;
    
    @JsonProperty("shippingAddress")
    private String shippingAddress;
    
    @JsonProperty("shippingMethod")
    private String shippingMethod;
    
    @JsonProperty("currentLocation")
    private String currentLocation;
    
    @JsonProperty("statusHistory")
    private List<OrderStatusHistoryResponse> statusHistory;
    
    public OrderTrackingResponse() {}
    
    public OrderTrackingResponse(String orderId, String status, String trackingNumber, 
                                 String carrierName, String estimatedDeliveryDate,
                                 String shippingAddress, String shippingMethod,
                                 String currentLocation, List<OrderStatusHistoryResponse> statusHistory) {
        this.orderId = orderId;
        this.status = status;
        this.trackingNumber = trackingNumber;
        this.carrierName = carrierName;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.shippingAddress = shippingAddress;
        this.shippingMethod = shippingMethod;
        this.currentLocation = currentLocation;
        this.statusHistory = statusHistory;
    }
    
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public String getCarrierName() {
        return carrierName;
    }
    
    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }
    
    public String getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }
    
    public void setEstimatedDeliveryDate(String estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public String getShippingMethod() {
        return shippingMethod;
    }
    
    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }
    
    public String getCurrentLocation() {
        return currentLocation;
    }
    
    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }
    
    public List<OrderStatusHistoryResponse> getStatusHistory() {
        return statusHistory;
    }
    
    public void setStatusHistory(List<OrderStatusHistoryResponse> statusHistory) {
        this.statusHistory = statusHistory;
    }
}
