package com.example.ecompoc.stock.service;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.stock.model.StockNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending stock-related emails
 * Follows the pattern of AbandonedCartEmailService
 */
@Service
public class StockEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(StockEmailService.class);
    
    @Value("${stock-management.enabled:true}")
    private boolean stockManagementEnabled;
    
    @Value("${stock-management.admin-email:admin@example.com}")
    private String adminEmail;
    
    /**
     * Send back-in-stock email notification
     */
    public void sendBackInStockEmail(StockNotification notification, Product product) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled, skipping email send");
            return;
        }
        
        if (notification == null || product == null) {
            logger.warn("Invalid parameters for back-in-stock email");
            return;
        }
        
        if (notification.getEmail() == null || notification.getEmail().isEmpty()) {
            logger.debug("No email address for notification: {}", notification.getNotificationId());
            return;
        }
        
        // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
        // For now, just log the email send
        logger.info("Back-in-stock email sent for product {} to {}: Product: {}, Stock: {}", 
            product.getId(), notification.getEmail(), product.getName(), product.getQuantity());
        
        // Email template would include:
        // - Product name, image, and link
        // - Personalized greeting
        // - "Shop Now" button
        // - Stock quantity available
        // - "Limited stock available - order soon!" message
        // - Unsubscribe link
    }
    
    /**
     * Send low stock alert email to administrators
     */
    public void sendLowStockAlertEmail(Product product, Integer stockLevel, Integer threshold) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled, skipping alert email");
            return;
        }
        
        if (product == null) {
            logger.warn("Invalid product for low stock alert");
            return;
        }
        
        // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
        // For now, just log the email send
        logger.info("Low stock alert email sent to {}: Product: {} (ID: {}), Stock: {}, Threshold: {}", 
            adminEmail, product.getName(), product.getId(), stockLevel, threshold);
        
        // Email template would include:
        // - Product name, SKU, and current stock level
        // - Low stock threshold and difference
        // - Product category and sales velocity (if available)
        // - Direct link to product management page
        // - Recommended reorder quantity (if configured)
    }
}

