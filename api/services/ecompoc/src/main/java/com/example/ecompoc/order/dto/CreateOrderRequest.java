package com.example.ecompoc.order.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for creating a new order
 */
public class CreateOrderRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;
    
    private Integer pointsToRedeem;
    
    private List<String> giftCardCodes;
    
    // Default constructor
    public CreateOrderRequest() {}
    
    // Constructor with all fields
    public CreateOrderRequest(String userId, List<OrderItemRequest> items) {
        this.userId = userId;
        this.items = items;
    }
    
    // Constructor with points redemption
    public CreateOrderRequest(String userId, List<OrderItemRequest> items, Integer pointsToRedeem) {
        this.userId = userId;
        this.items = items;
        this.pointsToRedeem = pointsToRedeem;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<OrderItemRequest> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
    
    public Integer getPointsToRedeem() {
        return pointsToRedeem;
    }
    
    public void setPointsToRedeem(Integer pointsToRedeem) {
        this.pointsToRedeem = pointsToRedeem;
    }
    
    public List<String> getGiftCardCodes() {
        return giftCardCodes;
    }
    
    public void setGiftCardCodes(List<String> giftCardCodes) {
        this.giftCardCodes = giftCardCodes;
    }
    
    /**
     * Inner class for order item request
     */
    public static class OrderItemRequest {
        @NotBlank(message = "Product ID is required")
        private String productId;
        
        @javax.validation.constraints.Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        public OrderItemRequest() {}
        
        public OrderItemRequest(String productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
        
        public String getProductId() {
            return productId;
        }
        
        public void setProductId(String productId) {
            this.productId = productId;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}

