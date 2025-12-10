package com.example.ecompoc.giftcard.repository;

import com.example.ecompoc.giftcard.model.GiftCardTransaction;
import com.example.ecompoc.giftcard.model.GiftCardTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for GiftCardTransaction entities
 */
@Repository
public interface GiftCardTransactionRepository extends JpaRepository<GiftCardTransaction, String> {
    
    /**
     * Find all transactions for a gift card
     */
    List<GiftCardTransaction> findByGiftCardIdOrderByCreatedAtDesc(String giftCardId);
    
    /**
     * Find transactions by type
     */
    List<GiftCardTransaction> findByGiftCardIdAndTransactionTypeOrderByCreatedAtDesc(
        String giftCardId, GiftCardTransactionType transactionType);
    
    /**
     * Find transactions for an order
     */
    List<GiftCardTransaction> findByOrderId(String orderId);
}
