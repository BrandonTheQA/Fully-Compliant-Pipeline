package com.example.ecompoc.returns.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for return item data
 */
public class ReturnItemResponse {
    
    @JsonProperty("returnItemId")
    private Long returnItemId;
    
    @JsonProperty("orderItemId")
    private Long orderItemId;
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("productName")
    private String productName;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("returnReason")
    private String returnReason;
    
    @JsonProperty("condition")
    private String condition;
    
    @JsonProperty("comments")
    private String comments;
    
    @JsonProperty("originalPrice")
    private Double originalPrice;
    
    @JsonProperty("refundAmount")
    private Double refundAmount;
    
    public ReturnItemResponse() {}
    
    // Getters and Setters
    public Long getReturnItemId() {
        return returnItemId;
    }
    
    public void setReturnItemId(Long returnItemId) {
        this.returnItemId = returnItemId;
    }
    
    public Long getOrderItemId() {
        return orderItemId;
    }
    
    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getReturnReason() {
        return returnReason;
    }
    
    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public Double getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public Double getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }
}

