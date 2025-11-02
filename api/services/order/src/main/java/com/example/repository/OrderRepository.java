package com.example.repository;

import com.example.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Order entities
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    /**
     * Find all orders for a user
     */
    List<Order> findByUserId(String userId);
}

