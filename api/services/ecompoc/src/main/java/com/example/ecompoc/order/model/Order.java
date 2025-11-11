package com.example.ecompoc.order.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order domain model
 */
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    private String id;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String userId;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Order() {}
    
    // Constructor with all fields
    public Order(String id, String userId, List<OrderItem> items, Double totalAmount, String status) {
        this.id = id;
        this.userId = userId;
        this.totalAmount = totalAmount != null ? BigDecimal.valueOf(totalAmount) : null;
        this.status = status;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (items != null) {
            this.items = new ArrayList<>();
            for (OrderItem item : items) {
                addItem(item);
            }
        }
    }
    
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
    
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
    
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
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public Double getTotalAmount() {
        return totalAmount != null ? totalAmount.doubleValue() : null;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount != null ? BigDecimal.valueOf(totalAmount) : null;
    }
    
    public BigDecimal getTotalAmountDecimal() {
        return totalAmount;
    }
    
    public void setTotalAmountDecimal(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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

