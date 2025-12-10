package com.example.ecompoc.giftcard.service;

import com.example.ecompoc.giftcard.model.GiftCard;
import com.example.ecompoc.giftcard.model.GiftCardStatus;
import com.example.ecompoc.giftcard.model.GiftCardTransaction;
import com.example.ecompoc.giftcard.model.GiftCardTransactionType;
import com.example.ecompoc.giftcard.repository.GiftCardRepository;
import com.example.ecompoc.giftcard.repository.GiftCardTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling gift card purchases
 */
@Service
public class GiftCardPurchaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(GiftCardPurchaseService.class);
    
    private final GiftCardRepository giftCardRepository;
    private final GiftCardTransactionRepository transactionRepository;
    private final GiftCardCodeGenerator codeGenerator;
    private final GiftCardEmailService emailService;
    
    @Value("${gift-card.min-amount:10}")
    private BigDecimal minAmount;
    
    @Value("${gift-card.max-amount:1000}")
    private BigDecimal maxAmount;
    
    @Value("${gift-card.expiration-months:12}")
    private int expirationMonths;
    
    @Autowired
    public GiftCardPurchaseService(GiftCardRepository giftCardRepository,
                                   GiftCardTransactionRepository transactionRepository,
                                   GiftCardCodeGenerator codeGenerator,
                                   GiftCardEmailService emailService) {
        this.giftCardRepository = giftCardRepository;
        this.transactionRepository = transactionRepository;
        this.codeGenerator = codeGenerator;
        this.emailService = emailService;
    }
    
    /**
     * Purchase a single gift card
     */
    @Transactional
    public GiftCard purchaseGiftCard(BigDecimal amount, String purchaserId, String purchaserEmail,
                                    String recipientEmail, String recipientName, String personalMessage,
                                    String design, LocalDateTime scheduledDeliveryDate) {
        // Validate amount
        validateAmount(amount);
        
        // Generate unique code
        String code = codeGenerator.generateUniqueCode();
        
        // Create gift card
        String giftCardId = UUID.randomUUID().toString();
        GiftCard giftCard = new GiftCard(giftCardId, code, amount, purchaserEmail);
        giftCard.setPurchaserId(purchaserId);
        giftCard.setRecipientEmail(recipientEmail);
        giftCard.setRecipientName(recipientName);
        giftCard.setPersonalMessage(personalMessage);
        giftCard.setDesign(design);
        giftCard.setScheduledDeliveryDate(scheduledDeliveryDate);
        giftCard.setExpirationDate(LocalDateTime.now().plusMonths(expirationMonths));
        
        giftCard = giftCardRepository.save(giftCard);
        
        // Create purchase transaction
        createPurchaseTransaction(giftCard, amount);
        
        // Send emails
        emailService.sendPurchaseConfirmationEmail(giftCard);
        
        // Send gift card email if recipient specified and not scheduled
        if (recipientEmail != null && !recipientEmail.isEmpty()) {
            if (scheduledDeliveryDate == null || scheduledDeliveryDate.isBefore(LocalDateTime.now())) {
                emailService.sendGiftCardEmail(giftCard);
            }
        }
        
        logger.info("Gift card purchased: code={}, amount={}, purchaser={}", code, amount, purchaserEmail);
        
        return giftCard;
    }
    
    /**
     * Purchase multiple gift cards in one transaction
     */
    @Transactional
    public List<GiftCard> purchaseMultipleGiftCards(BigDecimal amount, int quantity,
                                                     String purchaserId, String purchaserEmail,
                                                     String recipientEmail, String recipientName,
                                                     String personalMessage, String design,
                                                     LocalDateTime scheduledDeliveryDate) {
        validateAmount(amount);
        
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        
        if (quantity > 10) {
            throw new IllegalArgumentException("Maximum quantity is 10 per transaction");
        }
        
        List<GiftCard> giftCards = new ArrayList<>();
        
        for (int i = 0; i < quantity; i++) {
            GiftCard giftCard = purchaseGiftCard(amount, purchaserId, purchaserEmail,
                recipientEmail, recipientName, personalMessage, design, scheduledDeliveryDate);
            giftCards.add(giftCard);
        }
        
        logger.info("Purchased {} gift cards in one transaction", quantity);
        
        return giftCards;
    }
    
    /**
     * Validate gift card amount
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Gift card amount is required");
        }
        
        if (amount.compareTo(minAmount) < 0) {
            throw new IllegalArgumentException(
                String.format("Gift card amount must be at least $%.2f", minAmount.doubleValue()));
        }
        
        if (amount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException(
                String.format("Gift card amount cannot exceed $%.2f", maxAmount.doubleValue()));
        }
    }
    
    /**
     * Create purchase transaction record
     */
    private void createPurchaseTransaction(GiftCard giftCard, BigDecimal amount) {
        String transactionId = UUID.randomUUID().toString();
        GiftCardTransaction transaction = new GiftCardTransaction(
            transactionId, giftCard.getGiftCardId(), GiftCardTransactionType.PURCHASE, amount);
        transaction.setDescription("Gift card purchase");
        transactionRepository.save(transaction);
    }
}
