package com.example.ecompoc.abandonedcart.scheduler;

import com.example.ecompoc.abandonedcart.model.AbandonedCart;
import com.example.ecompoc.abandonedcart.repository.AbandonedCartRepository;
import com.example.ecompoc.abandonedcart.service.AbandonedCartEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduler for processing abandoned carts and sending recovery emails
 */
@Component
@ConditionalOnProperty(name = "abandoned-cart.enabled", havingValue = "true", matchIfMissing = false)
public class AbandonedCartScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(AbandonedCartScheduler.class);
    
    private final AbandonedCartRepository abandonedCartRepository;
    private final AbandonedCartEmailService emailService;
    
    @Value("${abandoned-cart.enabled:false}")
    private boolean abandonedCartEnabled;
    
    public AbandonedCartScheduler(AbandonedCartRepository abandonedCartRepository,
                                  AbandonedCartEmailService emailService) {
        this.abandonedCartRepository = abandonedCartRepository;
        this.emailService = emailService;
    }
    
    /**
     * Process abandoned carts and send first recovery emails
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void processAbandonedCarts() {
        if (!abandonedCartEnabled) {
            logger.debug("Abandoned cart feature is disabled, skipping scheduler execution");
            return;
        }
        
        try {
            // Find carts abandoned more than 30 minutes ago that need first email
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
            java.util.List<AbandonedCart> cartsNeedingEmail = 
                abandonedCartRepository.findCartsNeedingFirstEmail(threshold);
            
            logger.info("Found {} abandoned carts needing first email", cartsNeedingEmail.size());
            
            for (AbandonedCart cart : cartsNeedingEmail) {
                try {
                    emailService.sendFirstEmail(cart);
                } catch (Exception e) {
                    logger.error("Failed to send first email for abandoned cart: id={}", cart.getId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing abandoned carts", e);
        }
    }
    
    /**
     * Send 24h follow-up emails
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void send24hFollowupEmails() {
        if (!abandonedCartEnabled) {
            logger.debug("Abandoned cart feature is disabled, skipping scheduler execution");
            return;
        }
        
        try {
            // Find carts that need 24h follow-up (first email sent 24+ hours ago)
            LocalDateTime threshold = LocalDateTime.now().minusHours(24);
            java.util.List<AbandonedCart> cartsNeedingFollowup = 
                abandonedCartRepository.findCartsNeeding24hFollowup(threshold);
            
            logger.info("Found {} abandoned carts needing 24h follow-up email", cartsNeedingFollowup.size());
            
            for (AbandonedCart cart : cartsNeedingFollowup) {
                try {
                    emailService.send24hFollowupEmail(cart);
                } catch (Exception e) {
                    logger.error("Failed to send 24h follow-up email for abandoned cart: id={}", cart.getId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error sending 24h follow-up emails", e);
        }
    }
    
    /**
     * Send 72h follow-up emails
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void send72hFollowupEmails() {
        if (!abandonedCartEnabled) {
            logger.debug("Abandoned cart feature is disabled, skipping scheduler execution");
            return;
        }
        
        try {
            // Find carts that need 72h follow-up (first email sent 72+ hours ago)
            LocalDateTime threshold = LocalDateTime.now().minusHours(72);
            java.util.List<AbandonedCart> cartsNeedingFollowup = 
                abandonedCartRepository.findCartsNeeding72hFollowup(threshold);
            
            logger.info("Found {} abandoned carts needing 72h follow-up email", cartsNeedingFollowup.size());
            
            for (AbandonedCart cart : cartsNeedingFollowup) {
                try {
                    emailService.send72hFollowupEmail(cart);
                } catch (Exception e) {
                    logger.error("Failed to send 72h follow-up email for abandoned cart: id={}", cart.getId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error sending 72h follow-up emails", e);
        }
    }
}

