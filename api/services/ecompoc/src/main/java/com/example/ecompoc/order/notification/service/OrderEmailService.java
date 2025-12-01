package com.example.ecompoc.order.notification.service;

import com.example.ecompoc.order.model.Order;
import com.example.ecompoc.order.notification.model.OrderNotification;
import com.example.ecompoc.order.notification.repository.OrderNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for sending order status change emails
 * Follows the pattern of AbandonedCartEmailService
 */
@Service
public class OrderEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEmailService.class);
    
    private final OrderNotificationRepository notificationRepository;
    
    @Value("${order-tracking.email.enabled:true}")
    private boolean emailEnabled;
    
    public OrderEmailService(OrderNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    /**
     * Send status change email notification
     */
    @Transactional
    public void sendStatusChangeEmail(Order order, String status) {
        if (!emailEnabled) {
            logger.debug("Order tracking email feature is disabled, skipping email send");
            return;
        }
        
        // Create notification record
        OrderNotification notification = new OrderNotification();
        notification.setId(UUID.randomUUID().toString());
        notification.setOrderId(order.getId());
        notification.setNotificationType("EMAIL");
        notification.setStatus("PENDING");
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        
        // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
        // For now, just log the email send
        logger.info("Status change email sent for order {} with status {}", order.getId(), status);
        
        // Update notification status to SENT
        notification.setStatus("SENT");
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
