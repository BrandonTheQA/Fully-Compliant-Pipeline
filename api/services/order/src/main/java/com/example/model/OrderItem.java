package com.example.model;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Order Item entity
 */
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "product_id", nullable = false, length = 255)
    private String productId;
    
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    public OrderItem() {}
    
    public OrderItem(String productId, String productName, Integer quantity, Double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price != null ? BigDecimal.valueOf(price) : null;
        this.subtotal = this.price != null && quantity != null 
            ? this.price.multiply(BigDecimal.valueOf(quantity)) 
            : null;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
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
        recalculateSubtotal();
    }
    
    public Double getPrice() {
        return price != null ? price.doubleValue() : null;
    }
    
    public void setPrice(Double price) {
        this.price = price != null ? BigDecimal.valueOf(price) : null;
        recalculateSubtotal();
    }
    
    public BigDecimal getPriceDecimal() {
        return price;
    }
    
    public void setPriceDecimal(BigDecimal price) {
        this.price = price;
        recalculateSubtotal();
    }
    
    public Double getSubtotal() {
        return subtotal != null ? subtotal.doubleValue() : null;
    }
    
    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal != null ? BigDecimal.valueOf(subtotal) : null;
    }
    
    public BigDecimal getSubtotalDecimal() {
        return subtotal;
    }
    
    public void setSubtotalDecimal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    private void recalculateSubtotal() {
        if (this.price != null && this.quantity != null) {
            this.subtotal = this.price.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}

