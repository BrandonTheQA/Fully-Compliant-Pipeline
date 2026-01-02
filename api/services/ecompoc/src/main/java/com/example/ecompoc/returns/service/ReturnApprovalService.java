package com.example.ecompoc.returns.service;

import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.model.Return;
import com.example.ecompoc.returns.model.ReturnStatusHistory;
import com.example.ecompoc.returns.repository.ReturnRepository;
import com.example.ecompoc.returns.repository.ReturnStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for automated and manual return approval logic
 */
@Service
public class ReturnApprovalService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReturnApprovalService.class);
    
    private final ReturnRepository returnRepository;
    private final ReturnStatusHistoryRepository statusHistoryRepository;
    private final ReturnPolicyService returnPolicyService;
    private final ReturnShippingService returnShippingService;
    private ReturnEmailService returnEmailService;
    
    @Autowired
    public ReturnApprovalService(ReturnRepository returnRepository,
                                ReturnStatusHistoryRepository statusHistoryRepository,
                                ReturnPolicyService returnPolicyService,
                                ReturnShippingService returnShippingService) {
        this.returnRepository = returnRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.returnPolicyService = returnPolicyService;
        this.returnShippingService = returnShippingService;
    }
    
    @Autowired(required = false)
    public void setReturnEmailService(ReturnEmailService returnEmailService) {
        this.returnEmailService = returnEmailService;
    }
    
    /**
     * Process automatic approval for eligible returns
     * Called after return request is created
     */
    @Transactional
    public void processAutomaticApproval(Return returnEntity) {
        if (returnEntity.getStatus() != ReturnStatus.PENDING_APPROVAL) {
            return; // Already processed
        }
        
        // Check if return should be auto-approved
        if (returnPolicyService.shouldAutoApprove(returnEntity.getRefundAmountDecimal())) {
            approveReturn(returnEntity.getReturnId(), "SYSTEM", 
                "Automatically approved - return value within auto-approve threshold");
            logger.info("Auto-approved return {} with RMA {}", 
                returnEntity.getReturnId(), returnEntity.getRmaNumber());
        } else {
            logger.info("Return {} with RMA {} requires manual review (value: {})", 
                returnEntity.getReturnId(), returnEntity.getRmaNumber(), 
                returnEntity.getRefundAmount());
        }
    }
    
    /**
     * Manually approve a return
     */
    @Transactional
    public void approveReturn(String returnId, String approvedBy, String notes) {
        Return returnEntity = returnRepository.findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
        
        if (returnEntity.getStatus() != ReturnStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                "Return cannot be approved. Current status: " + returnEntity.getStatus());
        }
        
        // Update status
        returnEntity.setStatus(ReturnStatus.APPROVED);
        returnEntity.setUpdatedAt(LocalDateTime.now());
        returnRepository.save(returnEntity);
        
        // Create status history
        createStatusHistory(returnEntity, ReturnStatus.APPROVED, notes, approvedBy);
        
        // Generate return shipping label if applicable
        try {
            if (returnPolicyService.qualifiesForFreeReturn(returnEntity.getRefundAmountDecimal())) {
                String labelUrl = returnShippingService.generateReturnLabel(returnEntity);
                returnEntity.setReturnLabelUrl(labelUrl);
                returnRepository.save(returnEntity);
            }
        } catch (Exception e) {
            logger.warn("Failed to generate return label for return {}: {}", returnId, e.getMessage());
        }
        
        // Send approval email
        if (returnEmailService != null) {
            try {
                returnEmailService.sendApprovalEmail(returnEntity);
            } catch (Exception e) {
                logger.warn("Failed to send approval email for return {}: {}", returnId, e.getMessage());
            }
        }
        
        logger.info("Approved return {} by {}", returnId, approvedBy);
    }
    
    /**
     * Reject a return
     */
    @Transactional
    public void rejectReturn(String returnId, String rejectedBy, String reason) {
        Return returnEntity = returnRepository.findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
        
        if (returnEntity.getStatus() != ReturnStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                "Return cannot be rejected. Current status: " + returnEntity.getStatus());
        }
        
        // Update status
        returnEntity.setStatus(ReturnStatus.REJECTED);
        returnEntity.setUpdatedAt(LocalDateTime.now());
        returnRepository.save(returnEntity);
        
        // Create status history
        createStatusHistory(returnEntity, ReturnStatus.REJECTED, 
            "Return rejected: " + reason, rejectedBy);
        
        // Send rejection email
        if (returnEmailService != null) {
            try {
                returnEmailService.sendRejectionEmail(returnEntity, reason);
            } catch (Exception e) {
                logger.warn("Failed to send rejection email for return {}: {}", returnId, e.getMessage());
            }
        }
        
        logger.info("Rejected return {} by {}: {}", returnId, rejectedBy, reason);
    }
    
    /**
     * Update return status manually
     */
    @Transactional
    public void updateReturnStatus(String returnId, ReturnStatus newStatus, String updatedBy, String notes) {
        Return returnEntity = returnRepository.findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
        
        ReturnStatus oldStatus = returnEntity.getStatus();
        returnEntity.setStatus(newStatus);
        returnEntity.setUpdatedAt(LocalDateTime.now());
        returnRepository.save(returnEntity);
        
        // Create status history
        createStatusHistory(returnEntity, newStatus, notes, updatedBy);
        
        // Send status update email
        if (returnEmailService != null) {
            try {
                returnEmailService.sendStatusUpdateEmail(returnEntity, oldStatus, newStatus);
            } catch (Exception e) {
                logger.warn("Failed to send status update email for return {}: {}", returnId, e.getMessage());
            }
        }
        
        logger.info("Updated return {} status from {} to {} by {}", 
            returnId, oldStatus, newStatus, updatedBy);
    }
    
    /**
     * Mark return as received
     */
    @Transactional
    public void markReturnReceived(String returnId, String receivedBy, String notes) {
        Return returnEntity = returnRepository.findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
        
        if (returnEntity.getStatus() != ReturnStatus.IN_TRANSIT && 
            returnEntity.getStatus() != ReturnStatus.APPROVED) {
            throw new IllegalStateException(
                "Return cannot be marked as received. Current status: " + returnEntity.getStatus());
        }
        
        returnEntity.setStatus(ReturnStatus.RECEIVED);
        returnEntity.setUpdatedAt(LocalDateTime.now());
        returnRepository.save(returnEntity);
        
        // Create status history
        createStatusHistory(returnEntity, ReturnStatus.RECEIVED, notes, receivedBy);
        
        // Send received email
        if (returnEmailService != null) {
            try {
                returnEmailService.sendReceivedEmail(returnEntity);
            } catch (Exception e) {
                logger.warn("Failed to send received email for return {}: {}", returnId, e.getMessage());
            }
        }
        
        logger.info("Marked return {} as received by {}", returnId, receivedBy);
    }
    
    /**
     * Create status history entry
     */
    private void createStatusHistory(Return returnEntity, ReturnStatus status, String notes, String updatedBy) {
        ReturnStatusHistory history = new ReturnStatusHistory(returnEntity, status, notes, updatedBy);
        statusHistoryRepository.save(history);
    }
}

