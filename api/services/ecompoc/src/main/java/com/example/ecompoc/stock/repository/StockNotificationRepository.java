package com.example.ecompoc.stock.repository;

import com.example.ecompoc.stock.model.StockNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for StockNotification entities
 */
@Repository
public interface StockNotificationRepository extends JpaRepository<StockNotification, String> {
    
    /**
     * Find notifications by product ID
     */
    List<StockNotification> findByProductId(String productId);
    
    /**
     * Find notifications by user ID
     */
    List<StockNotification> findByUserId(String userId);
    
    /**
     * Find notifications by email
     */
    List<StockNotification> findByEmail(String email);
    
    /**
     * Find notifications by status
     */
    List<StockNotification> findByStatus(String status);
    
    /**
     * Find pending notifications for a product
     */
    @Query("SELECT sn FROM StockNotification sn WHERE sn.productId = ?1 AND sn.status = 'PENDING'")
    List<StockNotification> findPendingNotificationsByProductId(String productId);
    
    /**
     * Find notification by product ID and email
     */
    Optional<StockNotification> findByProductIdAndEmail(String productId, String email);
    
    /**
     * Find notifications by product ID and user ID
     */
    Optional<StockNotification> findByProductIdAndUserId(String productId, String userId);
}

