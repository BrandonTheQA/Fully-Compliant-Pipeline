package com.example.ecompoc.returns.service;

import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.model.Return;
import com.example.ecompoc.returns.repository.ReturnRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for refund processing, payment gateway integration, and automated refund processing
 */
@Service
public class RefundService {
    
    private static final Logger logger = LoggerFactory.getLogger(RefundService.class);
    
    private final ReturnRepository returnRepository;
    private final ReturnApprovalService returnApprovalService;
    private ReturnEmailService returnEmailService;
    
    @Autowired
    public RefundService(ReturnRepository returnRepository,
                         ReturnApprovalService returnApprovalService) {
        this.returnRepository = returnRepository;
        this.returnApprovalService = returnApprovalService;
    }
    
    @Autowired(required = false)
    public void setReturnEmailService(ReturnEmailService returnEmailService) {
        this.returnEmailService = returnEmailService;
    }
    
    /**
     * Process refund for a return
     * Called automatically when return is marked as RECEIVED
     */
    @Transactional
    public void processRefund(String returnId, String processedBy) {
        Return returnEntity = returnRepository.findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
        
        if (returnEntity.getStatus() != ReturnStatus.RECEIVED) {
            throw new IllegalStateException(
                "Refund can only be processed for returns with status RECEIVED. Current status: " + 
                returnEntity.getStatus());
        }
        
        if (returnEntity.getRefundAmountDecimal() == null || 
            returnEntity.getRefundAmountDecimal().doubleValue() <= 0) {
            throw new IllegalStateException("Invalid refund amount: " + returnEntity.getRefundAmount());
        }
        
        // Update status to PROCESSING_REFUND
        returnEntity.setStatus(ReturnStatus.PROCESSING_REFUND);
        returnEntity.setUpdatedAt(LocalDateTime.now());
        returnRepository.save(returnEntity);
        
        // Process refund through payment gateway
        try {
            processRefundThroughGateway(returnEntity);
            
            // Update status to REFUNDED
            returnEntity.setStatus(ReturnStatus.REFUNDED);
            returnEntity.setRefundDate(LocalDateTime.now());
            returnEntity.setRefundMethod("ORIGINAL_PAYMENT_METHOD"); // Default
            returnEntity.setUpdatedAt(LocalDateTime.now());
            returnRepository.save(returnEntity);
            
            // Update status history
            returnApprovalService.updateReturnStatus(returnId, ReturnStatus.REFUNDED, 
                processedBy, "Refund processed successfully");
            
            // Send refund processed email
            if (returnEmailService != null) {
                try {
                    returnEmailService.sendRefundProcessedEmail(returnEntity);
                } catch (Exception e) {
                    logger.warn("Failed to send refund processed email for return {}: {}", 
                        returnId, e.getMessage());
                }
            }
            
            logger.info("Processed refund for return {}: ${}", returnId, returnEntity.getRefundAmount());
            
        } catch (Exception e) {
            logger.error("Failed to process refund for return {}: {}", returnId, e.getMessage());
            // Revert status back to RECEIVED on failure
            returnEntity.setStatus(ReturnStatus.RECEIVED);
            returnRepository.save(returnEntity);
            throw new RuntimeException("Failed to process refund: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process refund through payment gateway
     * Stub implementation - ready for payment gateway integration (Stripe, PayPal, etc.)
     */
    private void processRefundThroughGateway(Return returnEntity) {
        // TODO: Integrate with actual payment gateway API
        // For Stripe: stripe.refunds.create(params)
        // For PayPal: PayPal API refund call
        // For other gateways: respective API calls
        
        logger.info("Processing refund through payment gateway for return {}: ${}", 
            returnEntity.getReturnId(), returnEntity.getRefundAmount());
        logger.debug("TODO: Integrate with actual payment gateway API");
        logger.debug("Refund details: Order ID: {}, Amount: {}, Method: Original Payment Method", 
            returnEntity.getOrderId(), returnEntity.getRefundAmount());
        
        // Simulate processing delay
        try {
            Thread.sleep(100); // Simulate API call
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // In real implementation, this would:
        // 1. Look up original payment transaction ID from order
        // 2. Call payment gateway API to process refund
        // 3. Handle refund response and update return with transaction ID
        // 4. Handle errors and retries
    }
    
    /**
     * Calculate refund amount for a return
     * Takes into account restocking fees
     */
    public Double calculateRefundAmount(Return returnEntity) {
        if (returnEntity.getRefundAmountDecimal() == null) {
            return 0.0;
        }
        return returnEntity.getRefundAmountDecimal().doubleValue();
    }
}

