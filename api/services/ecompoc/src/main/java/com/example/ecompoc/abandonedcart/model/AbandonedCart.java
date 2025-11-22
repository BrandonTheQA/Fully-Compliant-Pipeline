package com.example.ecompoc.abandonedcart.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Abandoned Cart domain model
 */
@Entity
@Table(name = "abandoned_carts")
public class AbandonedCart {
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    private String id;
    
    @Column(name = "user_id", columnDefinition = "VARCHAR(255)")
    private String userId;
    
    @Column(name = "email", columnDefinition = "NVARCHAR(255)")
    private String email;
    
    @Column(name = "cart_items", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String cartItems; // JSON string
    
    @Column(name = "cart_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal cartTotal;
    
    @Column(name = "shipping_region", columnDefinition = "NVARCHAR(50)")
    private String shippingRegion;
    
    @Column(name = "discount_code", columnDefinition = "VARCHAR(255)")
    private String discountCode;
    
    @Column(name = "discount_type", columnDefinition = "NVARCHAR(50)")
    private String discountType;
    
    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String status; // ABANDONED, RECOVERED, EXPIRED
    
    @Column(name = "abandoned_at", nullable = false)
    private LocalDateTime abandonedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor
    public AbandonedCart() {}
    
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
    
    public String getCartItems() {
        return cartItems;
    }
    
    public void setCartItems(String cartItems) {
        this.cartItems = cartItems;
    }
    
    public BigDecimal getCartTotal() {
        return cartTotal;
    }
    
    public void setCartTotal(BigDecimal cartTotal) {
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
    
    public BigDecimal getDiscountValue() {
        return discountValue;
    }
    
    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getAbandonedAt() {
        return abandonedAt;
    }
    
    public void setAbandonedAt(LocalDateTime abandonedAt) {
        this.abandonedAt = abandonedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

