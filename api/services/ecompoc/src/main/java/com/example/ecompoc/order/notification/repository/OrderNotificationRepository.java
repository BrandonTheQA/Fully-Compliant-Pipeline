package com.example.ecompoc.order.notification.repository;

import com.example.ecompoc.order.notification.model.OrderNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for order notifications
 */
@Repository
public interface OrderNotificationRepository extends JpaRepository<OrderNotification, String> {
    
    /**
     * Find all notifications for an order
     */
    List<OrderNotification> findByOrderIdOrderByCreatedAtDesc(String orderId);
    
    /**
     * Find notifications by order ID and type
     */
    List<OrderNotification> findByOrderIdAndNotificationTypeOrderByCreatedAtDesc(String orderId, String notificationType);
}
