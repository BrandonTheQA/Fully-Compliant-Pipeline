package com.example.ecompoc.product.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product domain model
 */
@Entity
@Table(name = "products")
public class Product {
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    private String id;
    
    @Column(name = "name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;
    
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "category", columnDefinition = "NVARCHAR(255)")
    private String category;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;
    
    // Default constructor
    public Product() {}
    
    // Constructor with all fields
    public Product(String id, String name, String description, Double price, Integer quantity, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price != null ? BigDecimal.valueOf(price) : null;
        this.quantity = quantity;
        this.category = category;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getPrice() {
        return price != null ? price.doubleValue() : null;
    }
    
    public void setPrice(Double price) {
        this.price = price != null ? BigDecimal.valueOf(price) : null;
    }
    
    public BigDecimal getPriceDecimal() {
        return price;
    }
    
    public void setPriceDecimal(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
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
    
    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }
    
    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
}

