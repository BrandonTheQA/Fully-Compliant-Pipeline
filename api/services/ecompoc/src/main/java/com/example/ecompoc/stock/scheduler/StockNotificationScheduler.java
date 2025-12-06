package com.example.ecompoc.stock.scheduler;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.model.StockNotification;
import com.example.ecompoc.stock.repository.StockNotificationRepository;
import com.example.ecompoc.stock.service.StockNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scheduler for processing back-in-stock notifications
 * Runs every 5 minutes to detect products that transitioned from 0 to >0 quantity
 */
@Component
@ConditionalOnProperty(name = "stock-management.enabled", havingValue = "true", matchIfMissing = true)
public class StockNotificationScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(StockNotificationScheduler.class);
    
    private final ProductRepository productRepository;
    private final StockNotificationRepository notificationRepository;
    private final StockNotificationService notificationService;
    
    @Value("${stock-management.enabled:true}")
    private boolean stockManagementEnabled;
    
    public StockNotificationScheduler(ProductRepository productRepository,
                                      StockNotificationRepository notificationRepository,
                                      StockNotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }
    
    /**
     * Process back-in-stock events and send notifications
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void processBackInStockNotifications() {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled, skipping scheduler execution");
            return;
        }
        
        try {
            // Find all products with pending notifications
            List<StockNotification> pendingNotifications = notificationRepository.findByStatus("PENDING");
            
            if (pendingNotifications.isEmpty()) {
                logger.debug("No pending stock notifications to process");
                return;
            }
            
            // Get unique product IDs with pending notifications
            Set<String> productIds = pendingNotifications.stream()
                .map(StockNotification::getProductId)
                .collect(Collectors.toSet());
            
            // Check which products are now in stock (quantity > 0)
            List<Product> productsInStock = productRepository.findAllById(productIds).stream()
                .filter(product -> product.getQuantity() != null && product.getQuantity() > 0)
                .collect(Collectors.toList());
            
            if (productsInStock.isEmpty()) {
                logger.debug("No products with pending notifications are back in stock");
                return;
            }
            
            logger.info("Found {} products back in stock with pending notifications", productsInStock.size());
            
            // Process back-in-stock events for each product
            for (Product product : productsInStock) {
                try {
                    notificationService.processBackInStockEvent(product.getId());
                } catch (Exception e) {
                    logger.error("Failed to process back-in-stock event for product: {}", product.getId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing back-in-stock notifications", e);
        }
    }
}

