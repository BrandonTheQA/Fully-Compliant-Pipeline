package com.example.ecompoc.returns.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for return data
 */
public class ReturnResponse {
    
    @JsonProperty("returnId")
    private String returnId;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("rmaNumber")
    private String rmaNumber;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("returnType")
    private String returnType;
    
    @JsonProperty("refundAmount")
    private Double refundAmount;
    
    @JsonProperty("refundMethod")
    private String refundMethod;
    
    @JsonProperty("refundDate")
    private String refundDate;
    
    @JsonProperty("returnTrackingNumber")
    private String returnTrackingNumber;
    
    @JsonProperty("returnCarrier")
    private String returnCarrier;
    
    @JsonProperty("returnLabelUrl")
    private String returnLabelUrl;
    
    @JsonProperty("items")
    private List<ReturnItemResponse> items;
    
    @JsonProperty("statusHistory")
    private List<ReturnStatusHistoryResponse> statusHistory;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    @JsonProperty("updatedAt")
    private String updatedAt;
    
    public ReturnResponse() {}
    
    // Getters and Setters
    public String getReturnId() {
        return returnId;
    }
    
    public void setReturnId(String returnId) {
        this.returnId = returnId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getRmaNumber() {
        return rmaNumber;
    }
    
    public void setRmaNumber(String rmaNumber) {
        this.rmaNumber = rmaNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    public Double getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public String getRefundMethod() {
        return refundMethod;
    }
    
    public void setRefundMethod(String refundMethod) {
        this.refundMethod = refundMethod;
    }
    
    public String getRefundDate() {
        return refundDate;
    }
    
    public void setRefundDate(String refundDate) {
        this.refundDate = refundDate;
    }
    
    public String getReturnTrackingNumber() {
        return returnTrackingNumber;
    }
    
    public void setReturnTrackingNumber(String returnTrackingNumber) {
        this.returnTrackingNumber = returnTrackingNumber;
    }
    
    public String getReturnCarrier() {
        return returnCarrier;
    }
    
    public void setReturnCarrier(String returnCarrier) {
        this.returnCarrier = returnCarrier;
    }
    
    public String getReturnLabelUrl() {
        return returnLabelUrl;
    }
    
    public void setReturnLabelUrl(String returnLabelUrl) {
        this.returnLabelUrl = returnLabelUrl;
    }
    
    public List<ReturnItemResponse> getItems() {
        return items;
    }
    
    public void setItems(List<ReturnItemResponse> items) {
        this.items = items;
    }
    
    public List<ReturnStatusHistoryResponse> getStatusHistory() {
        return statusHistory;
    }
    
    public void setStatusHistory(List<ReturnStatusHistoryResponse> statusHistory) {
        this.statusHistory = statusHistory;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

