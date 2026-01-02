package com.example.ecompoc.returns.model;

import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.enums.ReturnType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Return domain model
 */
@Entity
@Table(name = "returns")
public class Return {
    
    @Id
    @Column(name = "return_id", columnDefinition = "VARCHAR(255)")
    private String returnId;
    
    @Column(name = "order_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String orderId;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String userId;
    
    @Column(name = "rma_number", nullable = false, unique = true, columnDefinition = "VARCHAR(50)")
    private String rmaNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(50)")
    private ReturnStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "return_type", nullable = false, columnDefinition = "NVARCHAR(50)")
    private ReturnType returnType;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "refund_method", columnDefinition = "NVARCHAR(50)")
    private String refundMethod;
    
    @Column(name = "refund_date")
    private LocalDateTime refundDate;
    
    @Column(name = "return_tracking_number", columnDefinition = "VARCHAR(100)")
    private String returnTrackingNumber;
    
    @Column(name = "return_carrier", columnDefinition = "NVARCHAR(50)")
    private String returnCarrier;
    
    @Column(name = "return_label_url", columnDefinition = "NVARCHAR(500)")
    private String returnLabelUrl;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReturnItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReturnStatusHistory> statusHistory = new ArrayList<>();
    
    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReturnAttachment> attachments = new ArrayList<>();
    
    public Return() {}
    
    public Return(String returnId, String orderId, String userId, String rmaNumber, ReturnStatus status, ReturnType returnType) {
        this.returnId = returnId;
        this.orderId = orderId;
        this.userId = userId;
        this.rmaNumber = rmaNumber;
        this.status = status;
        this.returnType = returnType;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    public void addItem(ReturnItem item) {
        items.add(item);
        item.setReturnEntity(this);
    }
    
    public void removeItem(ReturnItem item) {
        items.remove(item);
        item.setReturnEntity(null);
    }
    
    public void addStatusHistory(ReturnStatusHistory history) {
        statusHistory.add(history);
        history.setReturnEntity(this);
    }
    
    public void addAttachment(ReturnAttachment attachment) {
        attachments.add(attachment);
        attachment.setReturnEntity(this);
    }
    
    // Getters and Setters
    public String getReturnId() {
        return returnId;
    }
    
    public void setReturnId(String returnId) {
        this.returnId = returnId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getRmaNumber() {
        return rmaNumber;
    }
    
    public void setRmaNumber(String rmaNumber) {
        this.rmaNumber = rmaNumber;
    }
    
    public ReturnStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReturnStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public ReturnType getReturnType() {
        return returnType;
    }
    
    public void setReturnType(ReturnType returnType) {
        this.returnType = returnType;
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
    
    public String getRefundMethod() {
        return refundMethod;
    }
    
    public void setRefundMethod(String refundMethod) {
        this.refundMethod = refundMethod;
    }
    
    public LocalDateTime getRefundDate() {
        return refundDate;
    }
    
    public void setRefundDate(LocalDateTime refundDate) {
        this.refundDate = refundDate;
    }
    
    public String getReturnTrackingNumber() {
        return returnTrackingNumber;
    }
    
    public void setReturnTrackingNumber(String returnTrackingNumber) {
        this.returnTrackingNumber = returnTrackingNumber;
    }
    
    public String getReturnCarrier() {
        return returnCarrier;
    }
    
    public void setReturnCarrier(String returnCarrier) {
        this.returnCarrier = returnCarrier;
    }
    
    public String getReturnLabelUrl() {
        return returnLabelUrl;
    }
    
    public void setReturnLabelUrl(String returnLabelUrl) {
        this.returnLabelUrl = returnLabelUrl;
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
    
    public List<ReturnItem> getItems() {
        return items;
    }
    
    public void setItems(List<ReturnItem> items) {
        this.items = items;
    }
    
    public List<ReturnStatusHistory> getStatusHistory() {
        return statusHistory;
    }
    
    public void setStatusHistory(List<ReturnStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }
    
    public List<ReturnAttachment> getAttachments() {
        return attachments;
    }
    
    public void setAttachments(List<ReturnAttachment> attachments) {
        this.attachments = attachments;
    }
}

