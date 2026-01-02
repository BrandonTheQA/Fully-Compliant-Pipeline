package com.example.ecompoc.returns.model;

import com.example.ecompoc.returns.enums.ReturnReason;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Return Item entity
 */
@Entity
@Table(name = "return_items")
public class ReturnItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_item_id")
    private Long returnItemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private Return returnEntity;
    
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;
    
    @Column(name = "product_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String productId;
    
    @Column(name = "product_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String productName;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "return_reason", nullable = false, columnDefinition = "NVARCHAR(50)")
    private ReturnReason returnReason;
    
    @Column(name = "condition", columnDefinition = "NVARCHAR(50)")
    private String condition;
    
    @Column(name = "comments", columnDefinition = "NVARCHAR(1000)")
    private String comments;
    
    @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    public ReturnItem() {}
    
    public ReturnItem(Long orderItemId, String productId, String productName, Integer quantity, 
                     ReturnReason returnReason, Double originalPrice) {
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.returnReason = returnReason;
        this.originalPrice = originalPrice != null ? BigDecimal.valueOf(originalPrice) : null;
    }
    
    public Long getReturnItemId() {
        return returnItemId;
    }
    
    public void setReturnItemId(Long returnItemId) {
        this.returnItemId = returnItemId;
    }
    
    public Return getReturnEntity() {
        return returnEntity;
    }
    
    public void setReturnEntity(Return returnEntity) {
        this.returnEntity = returnEntity;
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
    
    public ReturnReason getReturnReason() {
        return returnReason;
    }
    
    public void setReturnReason(ReturnReason returnReason) {
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
        return originalPrice != null ? originalPrice.doubleValue() : null;
    }
    
    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice != null ? BigDecimal.valueOf(originalPrice) : null;
    }
    
    public BigDecimal getOriginalPriceDecimal() {
        return originalPrice;
    }
    
    public void setOriginalPriceDecimal(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public Double getRefundAmount() {
        return refundAmount != null ? refundAmount.doubleValue() : null;
    }
    
    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount != null ? BigDecimal.valueOf(refundAmount) : null;
    }
    
    public BigDecimal getRefundAmountDecimal() {
        return refundAmount;
    }
    
    public void setRefundAmountDecimal(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
}

