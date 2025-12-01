package com.example.ecompoc.order.notification.service;

import com.example.ecompoc.order.enums.OrderStatus;
import com.example.ecompoc.order.model.Order;
import com.example.ecompoc.order.notification.model.NotificationPreferences;
import com.example.ecompoc.order.notification.model.OrderNotification;
import com.example.ecompoc.order.notification.repository.OrderNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing order notifications
 */
@Service
public class OrderNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderNotificationService.class);
    
    private final OrderEmailService emailService;
    private final NotificationPreferencesService preferencesService;
    private final OrderNotificationRepository notificationRepository;
    
    public OrderNotificationService(OrderEmailService emailService,
                                   NotificationPreferencesService preferencesService,
                                   OrderNotificationRepository notificationRepository) {
        this.emailService = emailService;
        this.preferencesService = preferencesService;
        this.notificationRepository = notificationRepository;
    }
    
    /**
     * Send status change notification
     */
    @Transactional
    public void sendStatusChangeNotification(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        // Get user preferences
        NotificationPreferences preferences = preferencesService.getPreferences(order.getUserId());
        
        // Check if notification should be sent
        if (!shouldSendNotification(newStatus, preferences)) {
            logger.debug("Notification skipped for order {} status change {} -> {} based on preferences", 
                order.getId(), oldStatus, newStatus);
            return;
        }
        
        // Send email notification if enabled
        if (preferences.getEmailEnabled()) {
            emailService.sendStatusChangeEmail(order, newStatus.name());
        }
        
        // TODO: SMS notifications (optional, Phase 3)
        // if (preferences.getSmsEnabled() && isCriticalStatus(newStatus)) {
        //     smsService.sendStatusChangeSms(order, newStatus.name());
        // }
        
        logger.info("Sent status change notification for order {}: {} -> {}", 
            order.getId(), oldStatus, newStatus);
    }
    
    /**
     * Check if notification should be sent based on preferences
     */
    private boolean shouldSendNotification(OrderStatus status, NotificationPreferences preferences) {
        if (preferences == null) {
            return true; // Default to sending if no preferences
        }
        
        String frequency = preferences.getNotificationFrequency();
        if (frequency == null || "NONE".equals(frequency)) {
            return false;
        }
        
        if ("ALL".equals(frequency)) {
            return true;
        }
        
        // CRITICAL_ONLY - only send for important status changes
        if ("CRITICAL_ONLY".equals(frequency)) {
            return isCriticalStatus(status);
        }
        
        return true; // Default to sending
    }
    
    /**
     * Check if status is critical (requires notification)
     */
    private boolean isCriticalStatus(OrderStatus status) {
        return status == OrderStatus.SHIPPED ||
               status == OrderStatus.OUT_FOR_DELIVERY ||
               status == OrderStatus.DELIVERED ||
               status == OrderStatus.CANCELLED;
    }
    
    /**
     * Get notification history for an order
     */
    public java.util.List<OrderNotification> getNotificationHistory(String orderId) {
        return notificationRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }
}
