package com.example.ecompoc.pricealert.repository;

import com.example.ecompoc.pricealert.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for PriceHistory entities
 */
@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, String> {
    
    /**
     * Find price history by product ID, ordered by changed date descending
     */
    @Query("SELECT ph FROM PriceHistory ph WHERE ph.productId = :productId ORDER BY ph.changedAt DESC")
    List<PriceHistory> findByProductIdOrderByChangedAtDesc(@Param("productId") String productId);
    
    /**
     * Find latest price history entry for a product
     */
    @Query("SELECT ph FROM PriceHistory ph WHERE ph.productId = :productId ORDER BY ph.changedAt DESC")
    Optional<PriceHistory> findLatestByProductId(@Param("productId") String productId);
}

