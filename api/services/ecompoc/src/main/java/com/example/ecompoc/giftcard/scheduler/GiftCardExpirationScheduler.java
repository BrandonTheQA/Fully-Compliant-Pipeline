package com.example.ecompoc.giftcard.scheduler;

import com.example.ecompoc.giftcard.service.GiftCardExpirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scheduled job for processing gift card expirations and sending warnings
 */
@Component
public class GiftCardExpirationScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(GiftCardExpirationScheduler.class);
    
    private final GiftCardExpirationService expirationService;
    
    @Value("${gift-card.expiration-warning-days:30,7,1}")
    private String warningDaysConfig;
    
    public GiftCardExpirationScheduler(GiftCardExpirationService expirationService) {
        this.expirationService = expirationService;
    }
    
    /**
     * Process expired gift cards and send expiration warnings
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void processExpirations() {
        logger.info("Starting gift card expiration processing");
        
        try {
            // Process expired cards
            expirationService.processExpiredCards();
            
            // Send expiration warnings
            List<Integer> warningDays = parseWarningDays(warningDaysConfig);
            expirationService.sendExpirationWarnings(warningDays);
            
            logger.info("Completed gift card expiration processing");
        } catch (Exception e) {
            logger.error("Error processing gift card expirations", e);
        }
    }
    
    /**
     * Parse warning days from configuration string
     */
    private List<Integer> parseWarningDays(String config) {
        if (config == null || config.trim().isEmpty()) {
            return Arrays.asList(30, 7, 1); // Default
        }
        
        return Arrays.stream(config.split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }
}
