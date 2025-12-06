package com.example.ecompoc.stock.service;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.model.StockNotification;
import com.example.ecompoc.stock.repository.StockNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing stock notifications
 */
@Service
public class StockNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(StockNotificationService.class);
    
    private final StockNotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final StockEmailService emailService;
    
    @Value("${stock-management.enabled:true}")
    private boolean stockManagementEnabled;
    
    public StockNotificationService(StockNotificationRepository notificationRepository,
                                    ProductRepository productRepository,
                                    StockEmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
    }
    
    /**
     * Sign up for back-in-stock notification
     */
    @Transactional
    public StockNotification signUpForNotification(String productId, String userId, String email) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled");
            return null;
        }
        
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }
        
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        // Verify product exists
        productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        // Check if already subscribed
        Optional<StockNotification> existing = userId != null
            ? notificationRepository.findByProductIdAndUserId(productId, userId)
            : notificationRepository.findByProductIdAndEmail(productId, email);
        
        if (existing.isPresent()) {
            StockNotification notification = existing.get();
            if ("UNSUBSCRIBED".equals(notification.getStatus())) {
                // Resubscribe
                notification.setStatus("PENDING");
                notification.setSignupDate(LocalDateTime.now());
                notification.setUpdatedAt(LocalDateTime.now());
                return notificationRepository.save(notification);
            }
            logger.debug("User already subscribed to notifications for product: {}", productId);
            return notification;
        }
        
        // Create new notification subscription
        StockNotification notification = new StockNotification();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setProductId(productId);
        notification.setUserId(userId);
        notification.setEmail(email);
        notification.setSignupDate(LocalDateTime.now());
        notification.setStatus("PENDING");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Unsubscribe from notification
     */
    @Transactional
    public void unsubscribe(String notificationId) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled");
            return;
        }
        
        StockNotification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        
        notification.setStatus("UNSUBSCRIBED");
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        
        logger.info("Unsubscribed from stock notification: {}", notificationId);
    }
    
    /**
     * Get user's notification subscriptions
     */
    public List<StockNotification> getUserNotifications(String userId) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled");
            return List.of();
        }
        
        if (userId == null || userId.isBlank()) {
            return List.of();
        }
        
        return notificationRepository.findByUserId(userId);
    }
    
    /**
     * Get notifications by email
     */
    public List<StockNotification> getNotificationsByEmail(String email) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled");
            return List.of();
        }
        
        if (email == null || email.isBlank()) {
            return List.of();
        }
        
        return notificationRepository.findByEmail(email);
    }
    
    /**
     * Process back-in-stock event for a product
     */
    @Transactional
    public void processBackInStockEvent(String productId) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled");
            return;
        }
        
        // Find all pending notifications for this product
        List<StockNotification> pendingNotifications = 
            notificationRepository.findPendingNotificationsByProductId(productId);
        
        if (pendingNotifications.isEmpty()) {
            logger.debug("No pending notifications for product: {}", productId);
            return;
        }
        
        // Get product details
        Product product = productRepository.findById(productId)
            .orElse(null);
        
        if (product == null) {
            logger.warn("Product not found for back-in-stock notification: {}", productId);
            return;
        }
        
        // Send notifications
        LocalDateTime now = LocalDateTime.now();
        for (StockNotification notification : pendingNotifications) {
            try {
                emailService.sendBackInStockEmail(notification, product);
                notification.setNotifiedDate(now);
                notification.setStatus("NOTIFIED");
                notification.setUpdatedAt(now);
                notificationRepository.save(notification);
                logger.info("Sent back-in-stock notification: {}", notification.getNotificationId());
            } catch (Exception e) {
                logger.error("Failed to send back-in-stock notification: {}", 
                    notification.getNotificationId(), e);
            }
        }
    }
}

