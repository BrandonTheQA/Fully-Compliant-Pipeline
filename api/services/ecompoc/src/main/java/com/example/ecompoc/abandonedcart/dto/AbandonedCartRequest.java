package com.example.ecompoc.abandonedcart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request DTO for creating/saving an abandoned cart
 */
public class AbandonedCartRequest {
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("cartItems")
    private List<CartItemRequest> cartItems;
    
    @JsonProperty("cartTotal")
    private Double cartTotal;
    
    @JsonProperty("shippingRegion")
    private String shippingRegion;
    
    // Default constructor
    public AbandonedCartRequest() {}
    
    // Getters and Setters
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
    
    public List<CartItemRequest> getCartItems() {
        return cartItems;
    }
    
    public void setCartItems(List<CartItemRequest> cartItems) {
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
    
    /**
     * Inner class for cart item request
     */
    public static class CartItemRequest {
        @JsonProperty("productId")
        private String productId;
        
        @JsonProperty("productName")
        private String productName;
        
        @JsonProperty("quantity")
        private Integer quantity;
        
        @JsonProperty("price")
        private Double price;
        
        public CartItemRequest() {}
        
        public CartItemRequest(String productId, String productName, Integer quantity, Double price) {
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

