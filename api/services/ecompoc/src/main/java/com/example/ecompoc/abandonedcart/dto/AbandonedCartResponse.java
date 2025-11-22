package com.example.ecompoc.abandonedcart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for abandoned cart data
 */
public class AbandonedCartResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("cartItems")
    private List<CartItemResponse> cartItems;
    
    @JsonProperty("cartTotal")
    private Double cartTotal;
    
    @JsonProperty("shippingRegion")
    private String shippingRegion;
    
    @JsonProperty("discountCode")
    private String discountCode;
    
    @JsonProperty("discountType")
    private String discountType;
    
    @JsonProperty("discountValue")
    private Double discountValue;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("abandonedAt")
    private String abandonedAt;
    
    @JsonProperty("expiresAt")
    private String expiresAt;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    @JsonProperty("updatedAt")
    private String updatedAt;
    
    // Default constructor
    public AbandonedCartResponse() {}
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
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
    
    public String getShippingRegion() {
        return shippingRegion;
    }
    
    public void setShippingRegion(String shippingRegion) {
        this.shippingRegion = shippingRegion;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAbandonedAt() {
        return abandonedAt;
    }
    
    public void setAbandonedAt(String abandonedAt) {
        this.abandonedAt = abandonedAt;
    }
    
    public String getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
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

