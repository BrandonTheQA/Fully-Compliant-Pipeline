package com.example.ecompoc.giftcard.service;

import com.example.ecompoc.giftcard.model.GiftCard;
import com.example.ecompoc.giftcard.model.GiftCardStatus;
import com.example.ecompoc.giftcard.repository.GiftCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for handling gift card expiration logic
 */
@Service
public class GiftCardExpirationService {
    
    private static final Logger logger = LoggerFactory.getLogger(GiftCardExpirationService.class);
    
    private final GiftCardRepository giftCardRepository;
    private final GiftCardEmailService emailService;
    
    @Autowired
    public GiftCardExpirationService(GiftCardRepository giftCardRepository,
                                     GiftCardEmailService emailService) {
        this.giftCardRepository = giftCardRepository;
        this.emailService = emailService;
    }
    
    /**
     * Process expired gift cards and update their status
     */
    @Transactional
    public void processExpiredCards() {
        logger.info("Processing expired gift cards");
        
        LocalDateTime now = LocalDateTime.now();
        List<GiftCard> expiredCards = giftCardRepository.findExpiredCards(GiftCardStatus.ACTIVE, now);
        
        for (GiftCard card : expiredCards) {
            card.setStatus(GiftCardStatus.EXPIRED);
            card.setUpdatedAt(now);
            giftCardRepository.save(card);
            logger.info("Gift card {} expired and status updated", card.getCode());
        }
        
        logger.info("Processed {} expired gift cards", expiredCards.size());
    }
    
    /**
     * Send expiration warnings for cards expiring soon
     */
    @Transactional
    public void sendExpirationWarnings(List<Integer> warningDays) {
        logger.info("Sending expiration warnings");
        
        LocalDateTime now = LocalDateTime.now();
        
        for (Integer days : warningDays) {
            LocalDateTime warningDate = now.plusDays(days);
            LocalDateTime startDate = warningDate.minusDays(1);
            
            List<GiftCard> expiringCards = giftCardRepository.findExpiringSoon(
                GiftCardStatus.ACTIVE, startDate, warningDate);
            
            for (GiftCard card : expiringCards) {
                emailService.sendExpirationWarningEmail(card, days);
                logger.debug("Sent {} day expiration warning for gift card {}", days, card.getCode());
            }
            
            logger.info("Sent {} day expiration warnings for {} gift cards", days, expiringCards.size());
        }
    }
}
