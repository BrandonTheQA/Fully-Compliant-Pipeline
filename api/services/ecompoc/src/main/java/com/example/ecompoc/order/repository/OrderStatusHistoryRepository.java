package com.example.ecompoc.order.repository;

import com.example.ecompoc.order.model.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for order status history
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, String> {
    
    /**
     * Find all status history entries for an order, ordered by creation date descending
     */
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(String orderId);
    
    /**
     * Find all status history entries for an order, ordered by creation date ascending
     */
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(String orderId);
}
