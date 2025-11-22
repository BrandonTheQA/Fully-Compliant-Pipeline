package com.example.ecompoc.abandonedcart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for cart restoration
 */
public class CartRestorationResponse {
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("cartItems")
    private List<CartItemResponse> cartItems;
    
    @JsonProperty("cartTotal")
    private Double cartTotal;
    
    @JsonProperty("discountCode")
    private String discountCode;
    
    @JsonProperty("discountType")
    private String discountType;
    
    @JsonProperty("discountValue")
    private Double discountValue;
    
    @JsonProperty("finalTotal")
    private Double finalTotal;
    
    // Default constructor
    public CartRestorationResponse() {}
    
    // Getters and Setters
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<CartItemResponse> getCartItems() {
        return cartItems;
    }
    
    public void setCartItems(List<CartItemResponse> cartItems) {
        this.cartItems = cartItems;
    }
    
    public Double getCartTotal() {
        return cartTotal;
    }
    
    public void setCartTotal(Double cartTotal) {
        this.cartTotal = cartTotal;
    }
    
    public String getDiscountCode() {
        return discountCode;
    }
    
    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }
    
    public String getDiscountType() {
        return discountType;
    }
    
    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }
    
    public Double getDiscountValue() {
        return discountValue;
    }
    
    public void setDiscountValue(Double discountValue) {
        this.discountValue = discountValue;
    }
    
    public Double getFinalTotal() {
        return finalTotal;
    }
    
    public void setFinalTotal(Double finalTotal) {
        this.finalTotal = finalTotal;
    }
    
    /**
     * Inner class for cart item response
     */
    public static class CartItemResponse {
        @JsonProperty("productId")
        private String productId;
        
        @JsonProperty("productName")
        private String productName;
        
        @JsonProperty("quantity")
        private Integer quantity;
        
        @JsonProperty("price")
        private Double price;
        
        public CartItemResponse() {}
        
        public CartItemResponse(String productId, String productName, Integer quantity, Double price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
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
        
        public Double getPrice() {
            return price;
        }
        
        public void setPrice(Double price) {
            this.price = price;
        }
    }
}

