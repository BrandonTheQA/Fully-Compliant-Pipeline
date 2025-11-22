package com.example.ecompoc.abandonedcart.service;

import com.example.ecompoc.abandonedcart.model.AbandonedCart;
import com.example.ecompoc.abandonedcart.model.AbandonedCartEmail;
import com.example.ecompoc.abandonedcart.repository.AbandonedCartEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing abandoned cart emails
 */
@Service
public class AbandonedCartEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(AbandonedCartEmailService.class);
    
    private final AbandonedCartEmailRepository emailRepository;
    
    @Value("${abandoned-cart.enabled:false}")
    private boolean abandonedCartEnabled;
    
    public AbandonedCartEmailService(AbandonedCartEmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }
    
    /**
     * Send first recovery email
     */
    @Transactional
    public void sendFirstEmail(AbandonedCart abandonedCart) {
        if (!abandonedCartEnabled) {
            logger.debug("Abandoned cart feature is disabled, skipping email send");
            return;
        }
        
        if (abandonedCart.getEmail() == null || abandonedCart.getEmail().isEmpty()) {
            logger.debug("No email address for abandoned cart: id={}", abandonedCart.getId());
            return;
        }
        
        // Check if first email already sent
        if (emailRepository.findByAbandonedCartIdAndEmailType(abandonedCart.getId(), "FIRST").isPresent()) {
            logger.debug("First email already sent for abandoned cart: id={}", abandonedCart.getId());
            return;
        }
        
        // Create email record
        AbandonedCartEmail email = new AbandonedCartEmail();
        email.setId(UUID.randomUUID().toString());
        email.setAbandonedCartId(abandonedCart.getId());
        email.setEmailType("FIRST");
        email.setSentAt(LocalDateTime.now());
        email.setCreatedAt(LocalDateTime.now());
        emailRepository.save(email);
        
        // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
        logger.info("First recovery email sent for abandoned cart: id={}, email={}", 
            abandonedCart.getId(), abandonedCart.getEmail());
    }
    
    /**
     * Send 24h follow-up email
     */
    @Transactional
    public void send24hFollowupEmail(AbandonedCart abandonedCart) {
        if (!abandonedCartEnabled) {
            logger.debug("Abandoned cart feature is disabled, skipping email send");
            return;
        }
        
        if (abandonedCart.getEmail() == null || abandonedCart.getEmail().isEmpty()) {
            logger.debug("No email address for abandoned cart: id={}", abandonedCart.getId());
            return;
        }
        
        // Check if 24h follow-up already sent
        if (emailRepository.findByAbandonedCartIdAndEmailType(abandonedCart.getId(), "FOLLOWUP_24H").isPresent()) {
            logger.debug("24h follow-up email already sent for abandoned cart: id={}", abandonedCart.getId());
            return;
        }
        
        // Create email record
        AbandonedCartEmail email = new AbandonedCartEmail();
        email.setId(UUID.randomUUID().toString());
        email.setAbandonedCartId(abandonedCart.getId());
        email.setEmailType("FOLLOWUP_24H");
        email.setSentAt(LocalDateTime.now());
        email.setCreatedAt(LocalDateTime.now());
        emailRepository.save(email);
        
        // TODO: Integrate with actual email service
        logger.info("24h follow-up email sent for abandoned cart: id={}, email={}", 
            abandonedCart.getId(), abandonedCart.getEmail());
    }
    
    /**
     * Send 72h follow-up email
     */
    @Transactional
    public void send72hFollowupEmail(AbandonedCart abandonedCart) {
        if (!abandonedCartEnabled) {
            logger.debug("Abandoned cart feature is disabled, skipping email send");
            return;
        }
        
        if (abandonedCart.getEmail() == null || abandonedCart.getEmail().isEmpty()) {
            logger.debug("No email address for abandoned cart: id={}", abandonedCart.getId());
            return;
        }
        
        // Check if 72h follow-up already sent
        if (emailRepository.findByAbandonedCartIdAndEmailType(abandonedCart.getId(), "FOLLOWUP_72H").isPresent()) {
            logger.debug("72h follow-up email already sent for abandoned cart: id={}", abandonedCart.getId());
            return;
        }
        
        // Create email record
        AbandonedCartEmail email = new AbandonedCartEmail();
        email.setId(UUID.randomUUID().toString());
        email.setAbandonedCartId(abandonedCart.getId());
        email.setEmailType("FOLLOWUP_72H");
        email.setSentAt(LocalDateTime.now());
        email.setCreatedAt(LocalDateTime.now());
        emailRepository.save(email);
        
        // TODO: Integrate with actual email service
        logger.info("72h follow-up email sent for abandoned cart: id={}, email={}", 
            abandonedCart.getId(), abandonedCart.getEmail());
    }
    
    /**
     * Track email open
     */
    @Transactional
    public void trackEmailOpen(String emailId) {
        if (!abandonedCartEnabled || emailId == null) {
            return;
        }
        
        emailRepository.findById(emailId).ifPresent(email -> {
            if (email.getOpenedAt() == null) {
                email.setOpenedAt(LocalDateTime.now());
                emailRepository.save(email);
                logger.info("Email open tracked: emailId={}", emailId);
            }
        });
    }
    
    /**
     * Track email click
     */
    @Transactional
    public void trackEmailClick(String emailId) {
        if (!abandonedCartEnabled || emailId == null) {
            return;
        }
        
        emailRepository.findById(emailId).ifPresent(email -> {
            if (email.getClickedAt() == null) {
                email.setClickedAt(LocalDateTime.now());
                emailRepository.save(email);
                logger.info("Email click tracked: emailId={}", emailId);
            }
        });
    }
}

