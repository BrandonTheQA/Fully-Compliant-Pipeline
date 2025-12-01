package com.example.ecompoc.order.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Order status history entity to track all status changes
 */
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {
    
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String status;
    
    @Column(name = "location", columnDefinition = "NVARCHAR(200)")
    private String location;
    
    @Column(name = "notes", columnDefinition = "NVARCHAR(500)")
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public OrderStatusHistory() {}
    
    public OrderStatusHistory(String id, Order order, String status, String location, String notes) {
        this.id = id;
        this.order = order;
        this.status = status;
        this.location = location;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
