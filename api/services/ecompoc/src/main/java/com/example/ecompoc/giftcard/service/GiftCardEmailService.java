package com.example.ecompoc.giftcard.service;

import com.example.ecompoc.giftcard.model.GiftCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for sending gift card emails
 */
@Service
public class GiftCardEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(GiftCardEmailService.class);
    
    @Value("${gift-card.email.enabled:true}")
    private boolean emailEnabled;
    
    /**
     * Send gift card email to recipient
     */
    @Transactional
    public void sendGiftCardEmail(GiftCard giftCard) {
        if (!emailEnabled) {
            logger.debug("Gift card email feature is disabled, skipping email send");
            return;
        }
        
        if (giftCard.getRecipientEmail() == null || giftCard.getRecipientEmail().isEmpty()) {
            logger.debug("No recipient email for gift card: {}", giftCard.getCode());
            return;
        }
        
        // Check if scheduled delivery date is in the future
        if (giftCard.getScheduledDeliveryDate() != null && 
            giftCard.getScheduledDeliveryDate().isAfter(LocalDateTime.now())) {
            logger.info("Gift card {} scheduled for delivery on {}", 
                giftCard.getCode(), giftCard.getScheduledDeliveryDate());
            return;
        }
        
        // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
        // Email content should include:
        // - Gift card code prominently displayed
        // - Gift card amount
        // - Personal message from sender (if provided)
        // - Gift card design/image
        // - Redemption instructions
        // - Link to website
        // - Expiration date
        // - Terms and conditions
        
        logger.info("Gift card email sent to recipient: email={}, code={}, amount={}", 
            giftCard.getRecipientEmail(), giftCard.getCode(), giftCard.getAmount());
    }
    
    /**
     * Send confirmation email to purchaser
     */
    @Transactional
    public void sendPurchaseConfirmationEmail(GiftCard giftCard) {
        if (!emailEnabled) {
            logger.debug("Gift card email feature is disabled, skipping confirmation email");
            return;
        }
        
        // TODO: Integrate with actual email service
        // Email content should include:
        // - Purchase confirmation
        // - Gift card code (if not sent to recipient)
        // - Recipient information (if provided)
        // - Delivery status
        
        logger.info("Purchase confirmation email sent to purchaser: email={}, code={}", 
            giftCard.getPurchaserEmail(), giftCard.getCode());
    }
    
    /**
     * Resend gift card email
     */
    @Transactional
    public void resendGiftCardEmail(GiftCard giftCard) {
        if (!emailEnabled) {
            logger.debug("Gift card email feature is disabled, skipping resend");
            return;
        }
        
        sendGiftCardEmail(giftCard);
        logger.info("Gift card email resent: code={}", giftCard.getCode());
    }
    
    /**
     * Send expiration warning email
     */
    @Transactional
    public void sendExpirationWarningEmail(GiftCard giftCard, int daysUntilExpiration) {
        if (!emailEnabled) {
            logger.debug("Gift card email feature is disabled, skipping expiration warning");
            return;
        }
        
        // Determine recipient email (prefer recipient, fallback to purchaser)
        String emailTo = giftCard.getRecipientEmail();
        if (emailTo == null || emailTo.isEmpty()) {
            emailTo = giftCard.getPurchaserEmail();
        }
        
        if (emailTo == null || emailTo.isEmpty()) {
            logger.debug("No email address for expiration warning: code={}", giftCard.getCode());
            return;
        }
        
        // TODO: Integrate with actual email service
        // Email content should include:
        // - Expiration warning (X days remaining)
        // - Gift card code
        // - Remaining balance
        // - Reminder to use the gift card
        
        logger.info("Expiration warning email sent: email={}, code={}, daysUntilExpiration={}", 
            emailTo, giftCard.getCode(), daysUntilExpiration);
    }
}
