package com.example.ecompoc.pricealert.service;

import com.example.ecompoc.pricealert.model.PriceAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service for managing price alert emails
 */
@Service
public class PriceAlertEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceAlertEmailService.class);
    
    @Value("${price-alert.enabled:true}")
    private boolean priceAlertEnabled;
    
    /**
     * Send confirmation email when alert is created
     */
    @Transactional
    public void sendConfirmationEmail(PriceAlert alert) {
        if (!priceAlertEnabled) {
            logger.debug("Price alert feature is disabled, skipping email send");
            return;
        }
        
        if (alert == null || alert.getUserEmail() == null || alert.getUserEmail().isEmpty()) {
            logger.debug("No email address for price alert: alertId={}", 
                alert != null ? alert.getAlertId() : "null");
            return;
        }
        
        // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
        logger.info("Confirmation email sent for price alert: alertId={}, email={}, productId={}", 
            alert.getAlertId(), alert.getUserEmail(), alert.getProductId());
    }
    
    /**
     * Send price drop notification email
     */
    @Transactional
    public void sendPriceDropEmail(PriceAlert alert, BigDecimal currentPrice, BigDecimal previousPrice) {
        if (!priceAlertEnabled) {
            logger.debug("Price alert feature is disabled, skipping email send");
            return;
        }
        
        if (alert == null || alert.getUserEmail() == null || alert.getUserEmail().isEmpty()) {
            logger.debug("No email address for price alert: alertId={}", 
                alert != null ? alert.getAlertId() : "null");
            return;
        }
        
        BigDecimal savings = previousPrice.subtract(currentPrice);
        
        // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
        logger.info("Price drop email sent for price alert: alertId={}, email={}, productId={}, " +
            "price={} -> {}, savings={}", 
            alert.getAlertId(), alert.getUserEmail(), alert.getProductId(), 
            previousPrice, currentPrice, savings);
    }
}

