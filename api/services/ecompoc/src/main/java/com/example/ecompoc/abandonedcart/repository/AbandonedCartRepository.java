package com.example.ecompoc.abandonedcart.repository;

import com.example.ecompoc.abandonedcart.model.AbandonedCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for AbandonedCart entities
 */
@Repository
public interface AbandonedCartRepository extends JpaRepository<AbandonedCart, String> {
    
    /**
     * Find abandoned carts by user ID
     */
    List<AbandonedCart> findByUserId(String userId);
    
    /**
     * Find abandoned carts by email
     */
    List<AbandonedCart> findByEmail(String email);
    
    /**
     * Find abandoned carts by status
     */
    List<AbandonedCart> findByStatus(String status);
    
    /**
     * Find abandoned carts that need first email (abandoned >30 minutes ago, no first email sent)
     */
    @Query("SELECT ac FROM AbandonedCart ac WHERE ac.status = 'ABANDONED' " +
           "AND ac.abandonedAt <= ?1 " +
           "AND ac.email IS NOT NULL " +
           "AND NOT EXISTS (SELECT 1 FROM AbandonedCartEmail ace WHERE ace.abandonedCartId = ac.id AND ace.emailType = 'FIRST')")
    List<AbandonedCart> findCartsNeedingFirstEmail(LocalDateTime threshold);
    
    /**
     * Find abandoned carts that need 24h follow-up email
     */
    @Query("SELECT ac FROM AbandonedCart ac WHERE ac.status = 'ABANDONED' " +
           "AND EXISTS (SELECT 1 FROM AbandonedCartEmail ace WHERE ace.abandonedCartId = ac.id AND ace.emailType = 'FIRST' AND ace.sentAt <= ?1) " +
           "AND NOT EXISTS (SELECT 1 FROM AbandonedCartEmail ace WHERE ace.abandonedCartId = ac.id AND ace.emailType = 'FOLLOWUP_24H')")
    List<AbandonedCart> findCartsNeeding24hFollowup(LocalDateTime threshold);
    
    /**
     * Find abandoned carts that need 72h follow-up email
     */
    @Query("SELECT ac FROM AbandonedCart ac WHERE ac.status = 'ABANDONED' " +
           "AND EXISTS (SELECT 1 FROM AbandonedCartEmail ace WHERE ace.abandonedCartId = ac.id AND ace.emailType = 'FIRST' AND ace.sentAt <= ?1) " +
           "AND NOT EXISTS (SELECT 1 FROM AbandonedCartEmail ace WHERE ace.abandonedCartId = ac.id AND ace.emailType = 'FOLLOWUP_72H')")
    List<AbandonedCart> findCartsNeeding72hFollowup(LocalDateTime threshold);
    
    /**
     * Find abandoned cart by discount code
     */
    Optional<AbandonedCart> findByDiscountCode(String discountCode);
}

