package com.example.ecompoc.pricealert.service;

import com.example.ecompoc.pricealert.model.PriceAlert;
import com.example.ecompoc.pricealert.model.PriceHistory;
import com.example.ecompoc.pricealert.repository.PriceAlertRepository;
import com.example.ecompoc.pricealert.repository.PriceHistoryRepository;
import com.example.ecompoc.product.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for detecting price changes and evaluating alerts
 */
@Service
public class PriceDropDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceDropDetectionService.class);
    
    private final PriceAlertRepository priceAlertRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private PriceAlertEmailService emailService;
    
    @Value("${price-alert.enabled:true}")
    private boolean priceAlertEnabled;
    
    @Value("${price-alert.minimum-drop-percentage:5.0}")
    private double minimumDropPercentage;
    
    public PriceDropDetectionService(PriceAlertRepository priceAlertRepository,
                                    PriceHistoryRepository priceHistoryRepository) {
        this.priceAlertRepository = priceAlertRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }
    
    @Autowired(required = false)
    public void setPriceAlertEmailService(PriceAlertEmailService emailService) {
        this.emailService = emailService;
    }
    
    /**
     * Detect and record price change for a product
     */
    @Transactional
    public void detectPriceChange(Product product, BigDecimal oldPrice, BigDecimal newPrice) {
        if (!priceAlertEnabled) {
            return;
        }
        
        if (product == null || newPrice == null) {
            return;
        }
        
        // If oldPrice is null, this is a new product (no price change to record)
        if (oldPrice == null) {
            return;
        }
        
        // Calculate price change
        BigDecimal priceDifference = newPrice.subtract(oldPrice);
        BigDecimal changePercentage = null;
        String changeType;
        
        if (priceDifference.compareTo(BigDecimal.ZERO) > 0) {
            changeType = "INCREASE";
            changePercentage = priceDifference.divide(oldPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        } else if (priceDifference.compareTo(BigDecimal.ZERO) < 0) {
            changeType = "DECREASE";
            changePercentage = priceDifference.abs().divide(oldPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        } else {
            changeType = "NO_CHANGE";
        }
        
        // Record price history
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setPriceHistoryId(UUID.randomUUID().toString());
        priceHistory.setProductId(product.getId());
        priceHistory.setPrice(newPrice);
        priceHistory.setPreviousPrice(oldPrice);
        priceHistory.setChangeType(changeType);
        priceHistory.setChangePercentage(changePercentage);
        priceHistory.setChangedAt(LocalDateTime.now());
        priceHistoryRepository.save(priceHistory);
        
        logger.debug("Recorded price change for product={}: {} -> {} ({}%)", 
            product.getId(), oldPrice, newPrice, changePercentage);
        
        // Evaluate alerts for this product if price decreased
        if ("DECREASE".equals(changeType)) {
            evaluateAlertsForProduct(product.getId());
        }
    }
    
    /**
     * Evaluate all active alerts for a product and trigger qualifying alerts
     */
    @Transactional
    public void evaluateAlertsForProduct(String productId) {
        if (!priceAlertEnabled) {
            return;
        }
        
        List<PriceAlert> activeAlerts = priceAlertRepository.findActiveAlertsForProduct(productId);
        
        // Get current product price
        // Note: We'll need to get the latest price from price history or product
        // For now, we'll get it from the latest price history entry
        PriceHistory latestHistory = priceHistoryRepository.findLatestByProductId(productId)
            .orElse(null);
        
        if (latestHistory == null) {
            logger.debug("No price history found for product={}, skipping alert evaluation", productId);
            return;
        }
        
        BigDecimal currentPrice = latestHistory.getPrice();
        BigDecimal previousPrice = latestHistory.getPreviousPrice();
        
        if (previousPrice == null) {
            logger.debug("No previous price found for product={}, skipping alert evaluation", productId);
            return;
        }
        
        for (PriceAlert alert : activeAlerts) {
            try {
                if (shouldTriggerAlert(alert, currentPrice, previousPrice)) {
                    triggerAlert(alert, currentPrice, previousPrice);
                }
            } catch (Exception e) {
                logger.error("Error evaluating alert: alertId={}", alert.getAlertId(), e);
            }
        }
    }
    
    /**
     * Check if an alert should be triggered
     */
    private boolean shouldTriggerAlert(PriceAlert alert, BigDecimal currentPrice, BigDecimal previousPrice) {
        // Check if price dropped by minimum percentage
        BigDecimal priceDrop = previousPrice.subtract(currentPrice);
        BigDecimal dropPercentage = priceDrop.divide(previousPrice, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        if (dropPercentage.compareTo(BigDecimal.valueOf(minimumDropPercentage)) < 0) {
            return false; // Drop is less than minimum threshold
        }
        
        // Check if target price is met (if specified)
        if (alert.getTargetPrice() != null) {
            return currentPrice.compareTo(alert.getTargetPrice()) <= 0;
        }
        
        // No target price specified, trigger on any drop meeting minimum percentage
        return true;
    }
    
    /**
     * Trigger an alert (mark as triggered and send email)
     */
    @Transactional
    public void triggerAlert(PriceAlert alert, BigDecimal currentPrice, BigDecimal previousPrice) {
        if (!priceAlertEnabled) {
            return;
        }
        
        alert.setStatus("TRIGGERED");
        alert.setLastTriggeredAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());
        alert.setCurrentPrice(currentPrice);
        priceAlertRepository.save(alert);
        
        logger.info("Triggered price alert: alertId={}, productId={}, price={} -> {}", 
            alert.getAlertId(), alert.getProductId(), previousPrice, currentPrice);
        
        // Send email notification
        if (emailService != null) {
            try {
                emailService.sendPriceDropEmail(alert, currentPrice, previousPrice);
            } catch (Exception e) {
                logger.error("Failed to send price drop email for alert: alertId={}", 
                    alert.getAlertId(), e);
            }
        }
    }
}

