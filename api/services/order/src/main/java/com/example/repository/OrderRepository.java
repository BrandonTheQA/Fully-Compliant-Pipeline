package com.example.repository;

import com.example.model.Order;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory repository for Order entities
 */
@Repository
public class OrderRepository {
    
    private final Map<String, Order> orders = new HashMap<>();
    
    /**
     * Save an order
     */
    public Order save(Order order) {
        orders.put(order.getId(), order);
        return order;
    }
    
    /**
     * Find order by ID
     */
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }
    
    /**
     * Find all orders for a user
     */
    public List<Order> findByUserId(String userId) {
        return orders.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Get total order count
     */
    public long count() {
        return orders.size();
    }
}

