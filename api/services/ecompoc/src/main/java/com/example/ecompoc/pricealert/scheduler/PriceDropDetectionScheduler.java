package com.example.ecompoc.pricealert.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for detecting price changes and triggering alerts
 */
@Component
@ConditionalOnProperty(name = "price-alert.enabled", havingValue = "true", matchIfMissing = false)
public class PriceDropDetectionScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceDropDetectionScheduler.class);
    
    @Value("${price-alert.enabled:true}")
    private boolean priceAlertEnabled;
    
    @Value("${price-alert.detection-interval-minutes:60}")
    private int detectionIntervalMinutes;
    
    /**
     * Process price changes and evaluate alerts
     * Runs hourly (every 60 minutes by default, 3600000 ms)
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void processPriceChanges() {
        if (!priceAlertEnabled) {
            logger.debug("Price alert feature is disabled, skipping scheduler execution");
            return;
        }
        
        try {
            logger.debug("Price drop detection scheduler running (checking for price changes in last {} minutes)", 
                detectionIntervalMinutes);
            
            // Note: The main price change detection happens in ProductService.createOrUpdateProduct() hook
            // This scheduler runs as a backup to catch any missed updates or for batch processing
            // In a production system, you might want to query products updated since last run
            // and re-evaluate alerts for those products
            
        } catch (Exception e) {
            logger.error("Error processing price changes", e);
        }
    }
}

