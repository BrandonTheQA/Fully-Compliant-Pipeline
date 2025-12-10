package com.example.ecompoc.giftcard.service;

import com.example.ecompoc.giftcard.model.GiftCard;
import com.example.ecompoc.giftcard.model.GiftCardStatus;
import com.example.ecompoc.giftcard.repository.GiftCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Core service for gift card business logic
 */
@Service
public class GiftCardService {
    
    private static final Logger logger = LoggerFactory.getLogger(GiftCardService.class);
    
    private final GiftCardRepository giftCardRepository;
    
    @Autowired
    public GiftCardService(GiftCardRepository giftCardRepository) {
        this.giftCardRepository = giftCardRepository;
    }
    
    /**
     * Find gift card by ID
     */
    public Optional<GiftCard> findById(String giftCardId) {
        return giftCardRepository.findById(giftCardId);
    }
    
    /**
     * Find gift card by code
     */
    public Optional<GiftCard> findByCode(String code) {
        return giftCardRepository.findByCode(code);
    }
    
    /**
     * Validate gift card for redemption
     * 
     * @param giftCard Gift card to validate
     * @param redemptionAmount Amount to redeem
     * @return true if valid for redemption
     * @throws IllegalArgumentException if validation fails
     */
    public void validateGiftCard(GiftCard giftCard, BigDecimal redemptionAmount) {
        if (giftCard == null) {
            throw new IllegalArgumentException("Gift card not found");
        }
        
        // Check status
        if (giftCard.getStatus() != GiftCardStatus.ACTIVE) {
            throw new IllegalArgumentException("Gift card is not active. Status: " + giftCard.getStatus());
        }
        
        // Check expiration
        if (giftCard.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Gift card has expired");
        }
        
        // Check balance
        if (giftCard.getBalance().compareTo(redemptionAmount) < 0) {
            throw new IllegalArgumentException(
                String.format("Insufficient balance. Available: %.2f, Requested: %.2f", 
                    giftCard.getBalance().doubleValue(), redemptionAmount.doubleValue()));
        }
        
        // Check if redemption amount is positive
        if (redemptionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Redemption amount must be greater than zero");
        }
    }
    
    /**
     * Check if gift card is expired
     */
    public boolean isExpired(GiftCard giftCard) {
        if (giftCard == null) {
            return false;
        }
        return giftCard.getExpirationDate().isBefore(LocalDateTime.now());
    }
    
    /**
     * Check expiration and update status if needed
     */
    @Transactional
    public void checkExpiration(GiftCard giftCard) {
        if (giftCard == null) {
            return;
        }
        
        if (isExpired(giftCard) && giftCard.getStatus() == GiftCardStatus.ACTIVE) {
            logger.info("Gift card {} expired, updating status to EXPIRED", giftCard.getCode());
            giftCard.setStatus(GiftCardStatus.EXPIRED);
            giftCard.setUpdatedAt(LocalDateTime.now());
            giftCardRepository.save(giftCard);
        }
    }
    
    /**
     * Update gift card balance atomically
     * Uses pessimistic locking to prevent race conditions
     */
    @Transactional
    public GiftCard updateBalance(String code, BigDecimal amount) {
        // Use pessimistic lock to prevent concurrent updates
        GiftCard giftCard = giftCardRepository.findByCodeWithLock(code)
            .orElseThrow(() -> new IllegalArgumentException("Gift card not found: " + code));
        
        // Validate before updating
        validateGiftCard(giftCard, amount);
        
        // Update balance
        BigDecimal newBalance = giftCard.getBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        
        giftCard.setBalance(newBalance);
        
        // Update status if fully redeemed
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            giftCard.setStatus(GiftCardStatus.REDEEMED);
        }
        
        giftCard.setUpdatedAt(LocalDateTime.now());
        
        return giftCardRepository.save(giftCard);
    }
    
    /**
     * Save gift card
     */
    @Transactional
    public GiftCard save(GiftCard giftCard) {
        if (giftCard.getUpdatedAt() == null) {
            giftCard.setUpdatedAt(LocalDateTime.now());
        }
        return giftCardRepository.save(giftCard);
    }
}
