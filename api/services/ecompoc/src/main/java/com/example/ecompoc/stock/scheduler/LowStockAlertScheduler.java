package com.example.ecompoc.stock.scheduler;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.model.LowStockAlert;
import com.example.ecompoc.stock.repository.LowStockAlertRepository;
import com.example.ecompoc.stock.service.StockEmailService;
import com.example.ecompoc.stock.service.StockStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Scheduler for detecting low stock conditions and sending admin alerts
 * Runs every hour
 */
@Component
@ConditionalOnProperty(name = "stock-management.enabled", havingValue = "true", matchIfMissing = true)
public class LowStockAlertScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(LowStockAlertScheduler.class);
    
    private final ProductRepository productRepository;
    private final LowStockAlertRepository alertRepository;
    private final StockEmailService emailService;
    
    @Value("${stock-management.enabled:true}")
    private boolean stockManagementEnabled;
    
    @Value("${stock-management.low-stock-threshold-default:10}")
    private Integer defaultLowStockThreshold;
    
    public LowStockAlertScheduler(ProductRepository productRepository,
                                  LowStockAlertRepository alertRepository,
                                  StockEmailService emailService) {
        this.productRepository = productRepository;
        this.alertRepository = alertRepository;
        this.emailService = emailService;
    }
    
    /**
     * Detect low stock conditions and send admin alerts
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void processLowStockAlerts() {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled, skipping scheduler execution");
            return;
        }
        
        try {
            // Get all products
            List<Product> products = productRepository.findAll();
            
            if (products.isEmpty()) {
                logger.debug("No products found for low stock check");
                return;
            }
            
            int alertsCreated = 0;
            int alertsSent = 0;
            
            for (Product product : products) {
                try {
                    // Skip products with no quantity
                    if (product.getQuantity() == null || product.getQuantity() <= 0) {
                        continue;
                    }
                    
                    // Calculate threshold for this product
                    Integer threshold = product.getLowStockThreshold() != null
                        ? product.getLowStockThreshold()
                        : defaultLowStockThreshold;
                    
                    // Check if product is below threshold
                    if (product.getQuantity() <= threshold) {
                        // Check if there's already an active alert for this product
                        boolean hasActiveAlert = alertRepository.findActiveAlertByProductId(product.getId())
                            .isPresent();
                        
                        if (!hasActiveAlert) {
                            // Create new alert
                            LowStockAlert alert = new LowStockAlert();
                            alert.setAlertId(UUID.randomUUID().toString());
                            alert.setProductId(product.getId());
                            alert.setStockLevel(product.getQuantity());
                            alert.setThreshold(threshold);
                            alert.setStatus("PENDING");
                            alert.setCreatedAt(LocalDateTime.now());
                            alertRepository.save(alert);
                            alertsCreated++;
                            
                            // Send email alert
                            try {
                                emailService.sendLowStockAlertEmail(product, product.getQuantity(), threshold);
                                alert.setAlertSentAt(LocalDateTime.now());
                                alert.setStatus("SENT");
                                alertRepository.save(alert);
                                alertsSent++;
                                logger.info("Sent low stock alert for product: {} (ID: {}), Stock: {}, Threshold: {}",
                                    product.getName(), product.getId(), product.getQuantity(), threshold);
                            } catch (Exception e) {
                                logger.error("Failed to send low stock alert email for product: {}", product.getId(), e);
                            }
                        }
                    } else {
                        // Product is above threshold, resolve any active alerts
                        alertRepository.findActiveAlertByProductId(product.getId())
                            .ifPresent(alert -> {
                                if (!"RESOLVED".equals(alert.getStatus())) {
                                    alert.setStatus("RESOLVED");
                                    alertRepository.save(alert);
                                    logger.debug("Resolved low stock alert for product: {}", product.getId());
                                }
                            });
                    }
                } catch (Exception e) {
                    logger.error("Error processing low stock alert for product: {}", product.getId(), e);
                }
            }
            
            logger.info("Low stock alert processing complete. Created: {}, Sent: {}", alertsCreated, alertsSent);
        } catch (Exception e) {
            logger.error("Error processing low stock alerts", e);
        }
    }
}

