package com.example.ecompoc.loyalty.repository;

import com.example.ecompoc.loyalty.model.ActivityType;
import com.example.ecompoc.loyalty.model.LoyaltyTransaction;
import com.example.ecompoc.loyalty.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for LoyaltyTransaction entities
 */
@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, String> {
    
    /**
     * Find transactions by account ID, ordered by created date descending
     */
    Page<LoyaltyTransaction> findByAccountIdOrderByCreatedAtDesc(String accountId, Pageable pageable);
    
    /**
     * Find transactions by user ID and transaction type
     */
    Page<LoyaltyTransaction> findByUserIdAndTransactionType(String userId, TransactionType transactionType, Pageable pageable);
    
    /**
     * Find transactions expiring before a given date
     */
    @Query("SELECT t FROM LoyaltyTransaction t WHERE t.expirationDate IS NOT NULL AND t.expirationDate <= :expirationDate AND t.transactionType = 'EARNED'")
    List<LoyaltyTransaction> findExpiringTransactions(@Param("expirationDate") LocalDateTime expirationDate);
    
    /**
     * Find transaction by account ID, activity type, and related order ID (for duplicate prevention)
     */
    Optional<LoyaltyTransaction> findByAccountIdAndActivityTypeAndRelatedOrderId(String accountId, ActivityType activityType, String orderId);
    
    /**
     * Find transaction by account ID, activity type, and related review ID (for duplicate prevention)
     */
    Optional<LoyaltyTransaction> findByAccountIdAndActivityTypeAndRelatedReviewId(String accountId, ActivityType activityType, String reviewId);
}
