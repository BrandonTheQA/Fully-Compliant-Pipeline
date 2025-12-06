package com.example.ecompoc.stock.repository;

import com.example.ecompoc.stock.model.LowStockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for LowStockAlert entities
 */
@Repository
public interface LowStockAlertRepository extends JpaRepository<LowStockAlert, String> {
    
    /**
     * Find alerts by product ID
     */
    List<LowStockAlert> findByProductId(String productId);
    
    /**
     * Find alerts by status
     */
    List<LowStockAlert> findByStatus(String status);
    
    /**
     * Find pending alerts
     */
    @Query("SELECT lsa FROM LowStockAlert lsa WHERE lsa.status = 'PENDING'")
    List<LowStockAlert> findPendingAlerts();
    
    /**
     * Find active alert for a product (PENDING or SENT, not RESOLVED)
     */
    @Query("SELECT lsa FROM LowStockAlert lsa WHERE lsa.productId = ?1 AND lsa.status IN ('PENDING', 'SENT')")
    Optional<LowStockAlert> findActiveAlertByProductId(String productId);
}

