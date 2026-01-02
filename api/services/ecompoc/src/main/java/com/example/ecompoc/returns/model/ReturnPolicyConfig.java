package com.example.ecompoc.returns.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Return policy configuration entity
 */
@Entity
@Table(name = "return_policy_config")
public class ReturnPolicyConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;
    
    @Column(name = "return_window_days", nullable = false)
    private Integer returnWindowDays;
    
    @Column(name = "restocking_fee_percentage", precision = 5, scale = 2)
    private BigDecimal restockingFeePercentage;
    
    @Column(name = "free_return_threshold", precision = 10, scale = 2)
    private BigDecimal freeReturnThreshold;
    
    @Column(name = "auto_approve_threshold", precision = 10, scale = 2)
    private BigDecimal autoApproveThreshold;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public ReturnPolicyConfig() {}
    
    public ReturnPolicyConfig(Integer returnWindowDays, Double restockingFeePercentage, 
                             Double freeReturnThreshold, Double autoApproveThreshold) {
        this.returnWindowDays = returnWindowDays;
        this.restockingFeePercentage = restockingFeePercentage != null 
            ? BigDecimal.valueOf(restockingFeePercentage) : null;
        this.freeReturnThreshold = freeReturnThreshold != null 
            ? BigDecimal.valueOf(freeReturnThreshold) : null;
        this.autoApproveThreshold = autoApproveThreshold != null 
            ? BigDecimal.valueOf(autoApproveThreshold) : null;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    public Long getPolicyId() {
        return policyId;
    }
    
    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }
    
    public Integer getReturnWindowDays() {
        return returnWindowDays;
    }
    
    public void setReturnWindowDays(Integer returnWindowDays) {
        this.returnWindowDays = returnWindowDays;
    }
    
    public Double getRestockingFeePercentage() {
        return restockingFeePercentage != null ? restockingFeePercentage.doubleValue() : null;
    }
    
    public void setRestockingFeePercentage(Double restockingFeePercentage) {
        this.restockingFeePercentage = restockingFeePercentage != null 
            ? BigDecimal.valueOf(restockingFeePercentage) : null;
        this.updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getRestockingFeePercentageDecimal() {
        return restockingFeePercentage;
    }
    
    public void setRestockingFeePercentageDecimal(BigDecimal restockingFeePercentage) {
        this.restockingFeePercentage = restockingFeePercentage;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Double getFreeReturnThreshold() {
        return freeReturnThreshold != null ? freeReturnThreshold.doubleValue() : null;
    }
    
    public void setFreeReturnThreshold(Double freeReturnThreshold) {
        this.freeReturnThreshold = freeReturnThreshold != null 
            ? BigDecimal.valueOf(freeReturnThreshold) : null;
        this.updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getFreeReturnThresholdDecimal() {
        return freeReturnThreshold;
    }
    
    public void setFreeReturnThresholdDecimal(BigDecimal freeReturnThreshold) {
        this.freeReturnThreshold = freeReturnThreshold;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Double getAutoApproveThreshold() {
        return autoApproveThreshold != null ? autoApproveThreshold.doubleValue() : null;
    }
    
    public void setAutoApproveThreshold(Double autoApproveThreshold) {
        this.autoApproveThreshold = autoApproveThreshold != null 
            ? BigDecimal.valueOf(autoApproveThreshold) : null;
        this.updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getAutoApproveThresholdDecimal() {
        return autoApproveThreshold;
    }
    
    public void setAutoApproveThresholdDecimal(BigDecimal autoApproveThreshold) {
        this.autoApproveThreshold = autoApproveThreshold;
        this.updatedAt = LocalDateTime.now();
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

