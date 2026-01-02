package com.example.ecompoc.returns.repository;

import com.example.ecompoc.returns.model.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for ReturnItem entities
 */
@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    
    /**
     * Find all return items for a return
     */
    List<ReturnItem> findByReturnEntity_ReturnId(String returnId);
    
    /**
     * Find return items by order item ID
     */
    List<ReturnItem> findByOrderItemId(Long orderItemId);
}

