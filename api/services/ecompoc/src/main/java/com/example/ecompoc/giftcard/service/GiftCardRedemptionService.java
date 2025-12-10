package com.example.ecompoc.giftcard.service;

import com.example.ecompoc.giftcard.model.GiftCard;
import com.example.ecompoc.giftcard.model.GiftCardTransaction;
import com.example.ecompoc.giftcard.model.GiftCardTransactionType;
import com.example.ecompoc.giftcard.repository.GiftCardRepository;
import com.example.ecompoc.giftcard.repository.GiftCardTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling gift card redemptions
 */
@Service
public class GiftCardRedemptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(GiftCardRedemptionService.class);
    
    private final GiftCardRepository giftCardRepository;
    private final GiftCardTransactionRepository transactionRepository;
    private final GiftCardService giftCardService;
    
    @Autowired
    public GiftCardRedemptionService(GiftCardRepository giftCardRepository,
                                     GiftCardTransactionRepository transactionRepository,
                                     GiftCardService giftCardService) {
        this.giftCardRepository = giftCardRepository;
        this.transactionRepository = transactionRepository;
        this.giftCardService = giftCardService;
    }
    
    /**
     * Redeem gift card (standalone redemption)
     * 
     * @param code Gift card code
     * @param redemptionAmount Amount to redeem
     * @return Updated gift card
     */
    @Transactional
    public GiftCard redeemGiftCard(String code, BigDecimal redemptionAmount) {
        // Find and lock gift card
        GiftCard giftCard = giftCardRepository.findByCodeWithLock(code)
            .orElseThrow(() -> new IllegalArgumentException("Gift card not found: " + code));
        
        // Validate
        giftCardService.validateGiftCard(giftCard, redemptionAmount);
        
        // Update balance
        giftCard = giftCardService.updateBalance(code, redemptionAmount);
        
        // Create redemption transaction
        createRedemptionTransaction(giftCard, redemptionAmount, null);
        
        logger.info("Gift card redeemed: code={}, amount={}, remainingBalance={}", 
            code, redemptionAmount, giftCard.getBalance());
        
        return giftCard;
    }
    
    /**
     * Apply gift card to order (for checkout)
     * Returns the amount that can be applied (may be less than requested if balance is insufficient)
     * 
     * @param code Gift card code
     * @param orderTotal Order total amount
     * @param orderId Order ID (if order already created)
     * @return Map with appliedAmount, remainingBalance, and updated giftCard
     */
    @Transactional
    public Map<String, Object> applyGiftCardToOrder(String code, BigDecimal orderTotal, String orderId) {
        // Find and lock gift card
        GiftCard giftCard = giftCardRepository.findByCodeWithLock(code)
            .orElseThrow(() -> new IllegalArgumentException("Gift card not found: " + code));
        
        // Check expiration
        giftCardService.checkExpiration(giftCard);
        
        // Validate
        giftCardService.validateGiftCard(giftCard, BigDecimal.ONE); // Validate with $1 to check status/expiration
        
        // Calculate amount to apply (cannot exceed order total or gift card balance)
        BigDecimal amountToApply = orderTotal.min(giftCard.getBalance());
        
        if (amountToApply.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cannot apply gift card: insufficient balance or order total is zero");
        }
        
        // Update balance
        giftCard = giftCardService.updateBalance(code, amountToApply);
        
        // Create redemption transaction
        createRedemptionTransaction(giftCard, amountToApply, orderId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("appliedAmount", amountToApply);
        result.put("remainingBalance", giftCard.getBalance());
        result.put("giftCard", giftCard);
        
        logger.info("Gift card applied to order: code={}, appliedAmount={}, remainingBalance={}, orderId={}", 
            code, amountToApply, giftCard.getBalance(), orderId);
        
        return result;
    }
    
    /**
     * Apply multiple gift cards to order
     * 
     * @param codes List of gift card codes
     * @param orderTotal Order total amount
     * @param orderId Order ID
     * @return List of applied amounts and remaining balances
     */
    @Transactional
    public List<Map<String, Object>> applyMultipleGiftCards(List<String> codes, BigDecimal orderTotal, String orderId) {
        List<Map<String, Object>> results = new ArrayList<>();
        BigDecimal remainingOrderTotal = orderTotal;
        
        for (String code : codes) {
            if (remainingOrderTotal.compareTo(BigDecimal.ZERO) <= 0) {
                break; // Order fully paid
            }
            
            try {
                Map<String, Object> result = applyGiftCardToOrder(code, remainingOrderTotal, orderId);
                BigDecimal appliedAmount = (BigDecimal) result.get("appliedAmount");
                remainingOrderTotal = remainingOrderTotal.subtract(appliedAmount);
                results.add(result);
            } catch (Exception e) {
                logger.warn("Failed to apply gift card {}: {}", code, e.getMessage());
                // Continue with other cards
            }
        }
        
        return results;
    }
    
    /**
     * Create redemption transaction record
     */
    private void createRedemptionTransaction(GiftCard giftCard, BigDecimal amount, String orderId) {
        String transactionId = UUID.randomUUID().toString();
        GiftCardTransaction transaction = new GiftCardTransaction(
            transactionId, giftCard.getGiftCardId(), GiftCardTransactionType.REDEMPTION, amount);
        transaction.setOrderId(orderId);
        transaction.setDescription(orderId != null ? 
            "Gift card redemption for order " + orderId : "Gift card redemption");
        transactionRepository.save(transaction);
    }
}
