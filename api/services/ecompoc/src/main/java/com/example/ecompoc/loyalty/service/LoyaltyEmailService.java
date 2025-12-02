package com.example.ecompoc.loyalty.service;

import com.example.ecompoc.loyalty.model.LoyaltyTier;
import com.example.ecompoc.user.model.User;
import com.example.ecompoc.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for sending loyalty program email notifications
 */
@Service
public class LoyaltyEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoyaltyEmailService.class);
    
    private final UserRepository userRepository;
    
    public LoyaltyEmailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Send welcome email when user enrolls
     */
    public void sendWelcomeEmail(String userId) {
        User user = userRepository.findById(userId)
            .orElse(null);
        
        if (user == null) {
            logger.warn("Cannot send welcome email: user not found: {}", userId);
            return;
        }
        
        logger.info("Sending welcome email to user: {} ({})", userId, user.getEmail());
        // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
        // Email content: Welcome message, program overview, initial points notification
    }
    
    /**
     * Send tier upgrade email
     */
    public void sendTierUpgradeEmail(String userId, LoyaltyTier newTier) {
        User user = userRepository.findById(userId)
            .orElse(null);
        
        if (user == null) {
            logger.warn("Cannot send tier upgrade email: user not found: {}", userId);
            return;
        }
        
        logger.info("Sending tier upgrade email to user: {} ({}) - New tier: {}", userId, user.getEmail(), newTier);
        // TODO: Integrate with actual email service
        // Email content: Congratulations message, new tier benefits, next tier information
    }
    
    /**
     * Send point expiration warning email
     */
    public void sendPointExpirationWarning(String userId, Integer points, LocalDateTime expirationDate) {
        User user = userRepository.findById(userId)
            .orElse(null);
        
        if (user == null) {
            logger.warn("Cannot send expiration warning: user not found: {}", userId);
            return;
        }
        
        String expirationDateStr = expirationDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        logger.info("Sending point expiration warning to user: {} ({}) - {} points expiring on {}", 
            userId, user.getEmail(), points, expirationDateStr);
        // TODO: Integrate with actual email service
        // Email content: Points expiring warning, expiration date, reminder to use points
    }
    
    /**
     * Send point expired email
     */
    public void sendPointExpiredEmail(String userId, Integer points) {
        User user = userRepository.findById(userId)
            .orElse(null);
        
        if (user == null) {
            logger.warn("Cannot send expired points email: user not found: {}", userId);
            return;
        }
        
        logger.info("Sending point expired email to user: {} ({}) - {} points expired", 
            userId, user.getEmail(), points);
        // TODO: Integrate with actual email service
        // Email content: Points expired notification, how to earn more points
    }
    
    /**
     * Send referral success email when referred user makes purchase
     */
    public void sendReferralSuccessEmail(String referrerUserId, String referredUserId) {
        User referrer = userRepository.findById(referrerUserId)
            .orElse(null);
        
        if (referrer == null) {
            logger.warn("Cannot send referral success email: referrer not found: {}", referrerUserId);
            return;
        }
        
        logger.info("Sending referral success email to referrer: {} ({}) - Referred user: {}", 
            referrerUserId, referrer.getEmail(), referredUserId);
        // TODO: Integrate with actual email service
        // Email content: Referral successful notification, points awarded, referral statistics
    }
}
