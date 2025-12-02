package com.example.ecompoc.loyalty.service;

import com.example.ecompoc.loyalty.model.ActivityType;
import com.example.ecompoc.loyalty.model.LoyaltyTransaction;
import com.example.ecompoc.loyalty.model.TransactionType;
import com.example.ecompoc.loyalty.repository.LoyaltyTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing point expiration
 */
@Service
public class LoyaltyExpirationService {
    
    private final LoyaltyTransactionRepository transactionRepository;
    
    @Value("${loyalty.expiration.months:12}")
    private Integer expirationMonths;
    
    public LoyaltyExpirationService(LoyaltyTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    /**
     * Set expiration date for a transaction (12 months from creation)
     */
    public void setExpirationDate(LoyaltyTransaction transaction) {
        if (transaction == null || transaction.getTransactionType() != TransactionType.EARNED) {
            return;
        }
        
        // Only set expiration for earned points
        if (transaction.getTransactionType() == TransactionType.EARNED && transaction.getPoints() > 0) {
            LocalDateTime expirationDate = transaction.getCreatedAt().plusMonths(expirationMonths);
            transaction.setExpirationDate(expirationDate);
        }
    }
    
    /**
     * Get points expiring before a given date
     */
    public List<LoyaltyTransaction> getExpiringPoints(String userId, LocalDateTime expirationDate) {
        // This will be used by the repository query
        return transactionRepository.findExpiringTransactions(expirationDate);
    }
    
    /**
     * Check for points expiring soon (within specified days)
     */
    public List<LoyaltyTransaction> checkExpiringPoints(String userId, int days) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(days);
        return transactionRepository.findExpiringTransactions(expirationDate);
    }
    
    /**
     * Extend expiration on activity - reset expiration clock
     * When user earns or redeems points, extend expiration for all existing points
     */
    public void extendExpirationOnActivity(String userId) {
        // This would extend expiration dates for all existing earned points
        // Implementation: Update expiration_date for all EARNED transactions
        // For simplicity, we'll handle this in the scheduled job
    }
    
    /**
     * Process expired points using FIFO method
     */
    public void processExpirations() {
        LocalDateTime now = LocalDateTime.now();
        List<LoyaltyTransaction> expiredTransactions = transactionRepository.findExpiringTransactions(now);
        
        // Process expired points in FIFO order (oldest first)
        expiredTransactions.sort((t1, t2) -> {
            if (t1.getCreatedAt() == null || t2.getCreatedAt() == null) {
                return 0;
            }
            return t1.getCreatedAt().compareTo(t2.getCreatedAt());
        });
        
        // Create expiration transactions for expired points
        // This will be handled by the scheduler
    }
}
