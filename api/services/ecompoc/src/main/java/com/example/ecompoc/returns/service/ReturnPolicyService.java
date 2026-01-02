package com.example.ecompoc.returns.service;

import com.example.ecompoc.returns.model.ReturnPolicyConfig;
import com.example.ecompoc.returns.repository.ReturnPolicyConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing return policy configuration and validation
 */
@Service
public class ReturnPolicyService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReturnPolicyService.class);
    
    private final ReturnPolicyConfigRepository policyConfigRepository;
    
    @Autowired
    public ReturnPolicyService(ReturnPolicyConfigRepository policyConfigRepository) {
        this.policyConfigRepository = policyConfigRepository;
    }
    
    /**
     * Get the active return policy configuration
     */
    public ReturnPolicyConfig getActivePolicy() {
        return policyConfigRepository.findFirstByOrderByUpdatedAtDesc()
            .orElseGet(() -> {
                logger.warn("No return policy configuration found, using defaults");
                return createDefaultPolicy();
            });
    }
    
    /**
     * Update return policy configuration
     */
    @Transactional
    public ReturnPolicyConfig updatePolicy(Integer returnWindowDays, Double restockingFeePercentage,
                                          Double freeReturnThreshold, Double autoApproveThreshold) {
        ReturnPolicyConfig policy = getActivePolicy();
        
        if (returnWindowDays != null) {
            policy.setReturnWindowDays(returnWindowDays);
        }
        if (restockingFeePercentage != null) {
            policy.setRestockingFeePercentage(restockingFeePercentage);
        }
        if (freeReturnThreshold != null) {
            policy.setFreeReturnThreshold(freeReturnThreshold);
        }
        if (autoApproveThreshold != null) {
            policy.setAutoApproveThreshold(autoApproveThreshold);
        }
        
        policy.setUpdatedAt(LocalDateTime.now());
        return policyConfigRepository.save(policy);
    }
    
    /**
     * Validate if an order is within the return window
     * 
     * @param orderDeliveryDate Delivery date of the order
     * @return true if within return window
     */
    public boolean isWithinReturnWindow(LocalDate orderDeliveryDate) {
        if (orderDeliveryDate == null) {
            return false;
        }
        
        ReturnPolicyConfig policy = getActivePolicy();
        LocalDate cutoffDate = LocalDate.now().minusDays(policy.getReturnWindowDays());
        return orderDeliveryDate.isAfter(cutoffDate) || orderDeliveryDate.isEqual(cutoffDate);
    }
    
    /**
     * Calculate restocking fee for a return amount
     * 
     * @param returnAmount Return amount
     * @return Restocking fee amount
     */
    public BigDecimal calculateRestockingFee(BigDecimal returnAmount) {
        if (returnAmount == null || returnAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        ReturnPolicyConfig policy = getActivePolicy();
        if (policy.getRestockingFeePercentageDecimal() == null || 
            policy.getRestockingFeePercentageDecimal().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return returnAmount.multiply(policy.getRestockingFeePercentageDecimal())
            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * Check if return qualifies for free return shipping
     * 
     * @param orderTotal Original order total
     * @return true if free return shipping applies
     */
    public boolean qualifiesForFreeReturn(BigDecimal orderTotal) {
        if (orderTotal == null) {
            return false;
        }
        
        ReturnPolicyConfig policy = getActivePolicy();
        if (policy.getFreeReturnThresholdDecimal() == null) {
            return false;
        }
        
        return orderTotal.compareTo(policy.getFreeReturnThresholdDecimal()) >= 0;
    }
    
    /**
     * Check if return should be auto-approved based on value
     * 
     * @param returnAmount Return amount
     * @return true if should be auto-approved
     */
    public boolean shouldAutoApprove(BigDecimal returnAmount) {
        if (returnAmount == null) {
            return false;
        }
        
        ReturnPolicyConfig policy = getActivePolicy();
        if (policy.getAutoApproveThresholdDecimal() == null) {
            return false;
        }
        
        return returnAmount.compareTo(policy.getAutoApproveThresholdDecimal()) <= 0;
    }
    
    /**
     * Create default policy configuration
     */
    private ReturnPolicyConfig createDefaultPolicy() {
        ReturnPolicyConfig policy = new ReturnPolicyConfig(30, 0.0, 0.0, 100.0);
        return policyConfigRepository.save(policy);
    }
}

