package com.example.ecompoc.pricealert.repository;

import com.example.ecompoc.pricealert.model.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for PriceAlert entities
 */
@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, String> {
    
    /**
     * Find price alerts by product ID and status
     */
    List<PriceAlert> findByProductIdAndStatus(String productId, String status);
    
    /**
     * Find price alerts by user email
     */
    List<PriceAlert> findByUserEmail(String email);
    
    /**
     * Find price alerts by user ID
     */
    List<PriceAlert> findByUserId(String userId);
    
    /**
     * Find active alerts for a product
     */
    @Query("SELECT pa FROM PriceAlert pa WHERE pa.productId = :productId AND pa.status = 'ACTIVE'")
    List<PriceAlert> findActiveAlertsForProduct(@Param("productId") String productId);
    
    /**
     * Find price alert by product ID and user email
     */
    Optional<PriceAlert> findByProductIdAndUserEmail(String productId, String userEmail);
    
    /**
     * Find price alerts by status
     */
    List<PriceAlert> findByStatus(String status);
}

