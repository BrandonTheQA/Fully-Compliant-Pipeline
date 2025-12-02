package com.example.ecompoc.loyalty.service;

import com.example.ecompoc.loyalty.model.*;
import com.example.ecompoc.loyalty.repository.LoyaltyAccountRepository;
import com.example.ecompoc.loyalty.repository.LoyaltyTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing loyalty points
 */
@Service
public class LoyaltyPointsService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoyaltyPointsService.class);
    
    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final LoyaltyTierService tierService;
    private final LoyaltyExpirationService expirationService;
    private final ApplicationContext applicationContext;
    
    public LoyaltyPointsService(LoyaltyAccountRepository accountRepository,
                               LoyaltyTransactionRepository transactionRepository,
                               LoyaltyTierService tierService,
                               LoyaltyExpirationService expirationService,
                               ApplicationContext applicationContext) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.tierService = tierService;
        this.expirationService = expirationService;
        this.applicationContext = applicationContext;
    }
    
    @Value("${loyalty.points.purchase-rate:1}")
    private Integer purchaseRate;
    
    @Value("${loyalty.points.review:50}")
    private Integer reviewPoints;
    
    @Value("${loyalty.points.referral:100}")
    private Integer referralPoints;
    
    @Value("${loyalty.points.social-share:25}")
    private Integer socialSharePoints;
    
    @Value("${loyalty.points.welcome:100}")
    private Integer welcomePoints;
    
    @Value("${loyalty.points.birthday:50}")
    private Integer birthdayPoints;
    
    @Value("${loyalty.points.anniversary:100}")
    private Integer anniversaryPoints;
    
    @Value("${loyalty.redemption.rate:100}")
    private Integer redemptionRate;
    
    @Value("${loyalty.redemption.minimum:500}")
    private Integer redemptionMinimum;
    
    @Value("${loyalty.redemption.max-percentage:50}")
    private Integer maxRedemptionPercentage;
    
    /**
     * Award points for purchase
     */
    @Transactional
    public void awardPurchasePoints(String userId, String orderId, Double orderAmount) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        // Check for duplicate
        if (transactionRepository.findByAccountIdAndActivityTypeAndRelatedOrderId(
                account.getAccountId(), ActivityType.PURCHASE, orderId).isPresent()) {
            logger.warn("Duplicate purchase points attempt for order: {}", orderId);
            return;
        }
        
        // Calculate base points
        int basePoints = (int) Math.round(orderAmount * purchaseRate);
        
        // Apply tier multiplier
        int points = tierService.applyTierMultiplier(account.getCurrentTier(), basePoints);
        
        // Create transaction
        String transactionId = UUID.randomUUID().toString();
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            transactionId,
            account.getAccountId(),
            userId,
            TransactionType.EARNED,
            points,
            ActivityType.PURCHASE
        );
        transaction.setRelatedOrderId(orderId);
        transaction.setDescription(String.format("Purchase points: $%.2f = %d points", orderAmount, points));
        
        // Set expiration date
        expirationService.setExpirationDate(transaction);
        
        // Update account
        account.setCurrentPoints(account.getCurrentPoints() + points);
        account.setLifetimePointsEarned(account.getLifetimePointsEarned() + points);
        account.setLastActivityDate(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        accountRepository.save(account);
        
        // Check for tier upgrade
        checkTierUpgrade(userId);
        
        logger.info("Awarded {} points to user {} for purchase order {}", points, userId, orderId);
    }
    
    /**
     * Award points for review submission
     */
    @Transactional
    public void awardReviewPoints(String userId, String reviewId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        // Check for duplicate
        if (transactionRepository.findByAccountIdAndActivityTypeAndRelatedReviewId(
                account.getAccountId(), ActivityType.REVIEW, reviewId).isPresent()) {
            logger.warn("Duplicate review points attempt for review: {}", reviewId);
            return;
        }
        
        // Create transaction
        String transactionId = UUID.randomUUID().toString();
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            transactionId,
            account.getAccountId(),
            userId,
            TransactionType.EARNED,
            reviewPoints,
            ActivityType.REVIEW
        );
        transaction.setRelatedReviewId(reviewId);
        transaction.setDescription(String.format("Review submission: %d points", reviewPoints));
        
        // Set expiration date
        expirationService.setExpirationDate(transaction);
        
        // Update account
        account.setCurrentPoints(account.getCurrentPoints() + reviewPoints);
        account.setLifetimePointsEarned(account.getLifetimePointsEarned() + reviewPoints);
        account.setLastActivityDate(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        accountRepository.save(account);
        
        // Check for tier upgrade
        checkTierUpgrade(userId);
        
        logger.info("Awarded {} points to user {} for review {}", reviewPoints, userId, reviewId);
    }
    
    /**
     * Award points for referral
     */
    @Transactional
    public void awardReferralPoints(String referrerUserId, String referralId) {
        LoyaltyAccount account = accountRepository.findByUserId(referrerUserId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + referrerUserId));
        
        // Create transaction
        String transactionId = UUID.randomUUID().toString();
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            transactionId,
            account.getAccountId(),
            referrerUserId,
            TransactionType.EARNED,
            referralPoints,
            ActivityType.REFERRAL
        );
        transaction.setRelatedReferralId(referralId);
        transaction.setDescription(String.format("Referral bonus: %d points", referralPoints));
        
        // Set expiration date
        expirationService.setExpirationDate(transaction);
        
        // Update account
        account.setCurrentPoints(account.getCurrentPoints() + referralPoints);
        account.setLifetimePointsEarned(account.getLifetimePointsEarned() + referralPoints);
        account.setLastActivityDate(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        accountRepository.save(account);
        
        // Check for tier upgrade
        checkTierUpgrade(referrerUserId);
        
        logger.info("Awarded {} referral points to user {}", referralPoints, referrerUserId);
    }
    
    /**
     * Award social share points
     */
    @Transactional
    public void awardSocialSharePoints(String userId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        // Create transaction
        String transactionId = UUID.randomUUID().toString();
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            transactionId,
            account.getAccountId(),
            userId,
            TransactionType.EARNED,
            socialSharePoints,
            ActivityType.SOCIAL_SHARE
        );
        transaction.setDescription(String.format("Social share: %d points", socialSharePoints));
        
        // Set expiration date
        expirationService.setExpirationDate(transaction);
        
        // Update account
        account.setCurrentPoints(account.getCurrentPoints() + socialSharePoints);
        account.setLifetimePointsEarned(account.getLifetimePointsEarned() + socialSharePoints);
        account.setLastActivityDate(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        accountRepository.save(account);
        
        // Check for tier upgrade
        checkTierUpgrade(userId);
    }
    
    /**
     * Award welcome points
     */
    @Transactional
    public void awardWelcomePoints(String userId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        // Create transaction
        String transactionId = UUID.randomUUID().toString();
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            transactionId,
            account.getAccountId(),
            userId,
            TransactionType.EARNED,
            welcomePoints,
            ActivityType.WELCOME
        );
        transaction.setDescription(String.format("Welcome bonus: %d points", welcomePoints));
        
        // Set expiration date
        expirationService.setExpirationDate(transaction);
        
        // Update account
        account.setCurrentPoints(account.getCurrentPoints() + welcomePoints);
        account.setLifetimePointsEarned(account.getLifetimePointsEarned() + welcomePoints);
        account.setLastActivityDate(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        accountRepository.save(account);
        
        // Check for tier upgrade
        checkTierUpgrade(userId);
    }
    
    /**
     * Award birthday points
     */
    @Transactional
    public void awardBirthdayPoints(String userId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        // Create transaction
        String transactionId = UUID.randomUUID().toString();
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            transactionId,
            account.getAccountId(),
            userId,
            TransactionType.EARNED,
            birthdayPoints,
            ActivityType.BIRTHDAY
        );
        transaction.setDescription(String.format("Birthday bonus: %d points", birthdayPoints));
        
        // Set expiration date
        expirationService.setExpirationDate(transaction);
        
        // Update account
        account.setCurrentPoints(account.getCurrentPoints() + birthdayPoints);
        account.setLifetimePointsEarned(account.getLifetimePointsEarned() + birthdayPoints);
        account.setLastActivityDate(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        accountRepository.save(account);
        
        // Check for tier upgrade
        checkTierUpgrade(userId);
    }
    
    /**
     * Award anniversary points
     */
    @Transactional
    public void awardAnniversaryPoints(String userId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        // Create transaction
        String transactionId = UUID.randomUUID().toString();
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            transactionId,
            account.getAccountId(),
            userId,
            TransactionType.EARNED,
            anniversaryPoints,
            ActivityType.ANNIVERSARY
        );
        transaction.setDescription(String.format("Anniversary bonus: %d points", anniversaryPoints));
        
        // Set expiration date
        expirationService.setExpirationDate(transaction);
        
        // Update account
        account.setCurrentPoints(account.getCurrentPoints() + anniversaryPoints);
        account.setLifetimePointsEarned(account.getLifetimePointsEarned() + anniversaryPoints);
        account.setLastActivityDate(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        accountRepository.save(account);
        
        // Check for tier upgrade
        checkTierUpgrade(userId);
    }
    
    /**
     * Check and upgrade tier if needed
     */
    private void checkTierUpgrade(String userId) {
        try {
            // Use ApplicationContext to avoid circular dependency
            LoyaltyService loyaltyService = applicationContext.getBean(LoyaltyService.class);
            if (loyaltyService != null) {
                loyaltyService.checkAndUpgradeTier(userId);
            }
        } catch (Exception e) {
            // Silently fail if LoyaltyService is not available (circular dependency protection)
            logger.debug("Could not check tier upgrade (this is normal during initialization): {}", e.getMessage());
        }
    }
    
    /**
     * Redeem points for discount
     */
    @Transactional
    public RedeemResult redeemPoints(String userId, Integer points, String orderId, Double orderTotal) {
        // Validate redemption (orderTotal can be null for pre-order calculations)
        if (orderTotal != null) {
            validateRedemption(userId, points, orderTotal);
        } else {
            // Basic validation without order total check
            if (points == null || points <= 0) {
                throw new IllegalArgumentException("Points must be greater than 0");
            }
            if (points < redemptionMinimum) {
                throw new IllegalArgumentException("Minimum redemption is " + redemptionMinimum + " points");
            }
            LoyaltyAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
            if (account.getCurrentPoints() < points) {
                throw new IllegalArgumentException("Insufficient points. Available: " + account.getCurrentPoints());
            }
        }
        
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        // Calculate discount amount
        double discountAmount = (double) points / redemptionRate;
        
        // Apply maximum redemption limit (50% of order value) if orderTotal is provided
        if (orderTotal != null) {
            double maxDiscount = orderTotal * (maxRedemptionPercentage / 100.0);
            if (discountAmount > maxDiscount) {
                discountAmount = maxDiscount;
                points = (int) Math.round(discountAmount * redemptionRate);
            }
        }
        
        // Create transaction
        String transactionId = UUID.randomUUID().toString();
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            transactionId,
            account.getAccountId(),
            userId,
            TransactionType.REDEEMED,
            -points,
            ActivityType.REDEMPTION
        );
        if (orderId != null) {
            transaction.setRelatedOrderId(orderId);
        }
        transaction.setDescription(String.format("Redeemed %d points for $%.2f discount", points, discountAmount));
        
        // Update account
        account.setCurrentPoints(account.getCurrentPoints() - points);
        account.setLifetimePointsRedeemed(account.getLifetimePointsRedeemed() + points);
        account.setLastActivityDate(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        accountRepository.save(account);
        
        logger.info("Redeemed {} points (${}) for user {} on order {}", points, discountAmount, userId, orderId);
        
        return new RedeemResult(points, discountAmount, account.getCurrentPoints());
    }
    
    /**
     * Reverse points for order cancellation/refund
     */
    @Transactional
    public void reversePoints(String userId, String orderId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElse(null);
        
        if (account == null) {
            logger.warn("Cannot reverse points: account not found for user: {}", userId);
            return;
        }
        
        // Find the original purchase transaction
        LoyaltyTransaction purchaseTransaction = transactionRepository
            .findByAccountIdAndActivityTypeAndRelatedOrderId(account.getAccountId(), ActivityType.PURCHASE, orderId)
            .orElse(null);
        
        if (purchaseTransaction == null) {
            logger.warn("No purchase transaction found to reverse for order: {}", orderId);
            return;
        }
        
        int pointsToReverse = purchaseTransaction.getPoints();
        
        // Create reversal transaction
        String transactionId = UUID.randomUUID().toString();
        LoyaltyTransaction reversalTransaction = new LoyaltyTransaction(
            transactionId,
            account.getAccountId(),
            userId,
            TransactionType.EARNED,
            -pointsToReverse,
            ActivityType.PURCHASE
        );
        reversalTransaction.setRelatedOrderId(orderId);
        reversalTransaction.setDescription(String.format("Reversed %d points for cancelled order", pointsToReverse));
        
        // Update account
        account.setCurrentPoints(Math.max(0, account.getCurrentPoints() - pointsToReverse));
        account.setLifetimePointsEarned(Math.max(0, account.getLifetimePointsEarned() - pointsToReverse));
        account.setLastActivityDate(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(reversalTransaction);
        accountRepository.save(account);
        
        logger.info("Reversed {} points for user {} on cancelled order {}", pointsToReverse, userId, orderId);
    }
    
    /**
     * Validate redemption request
     */
    public void validateRedemption(String userId, Integer points, Double orderTotal) {
        if (points == null || points <= 0) {
            throw new IllegalArgumentException("Points must be greater than 0");
        }
        
        if (points < redemptionMinimum) {
            throw new IllegalArgumentException("Minimum redemption is " + redemptionMinimum + " points");
        }
        
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        if (account.getCurrentPoints() < points) {
            throw new IllegalArgumentException("Insufficient points. Available: " + account.getCurrentPoints());
        }
        
        // Note: Max redemption limit is enforced during actual redemption, not in validation
        // This allows the redemption method to cap the amount automatically
    }
    
    /**
     * Inner class for redemption result
     */
    public static class RedeemResult {
        private final int pointsRedeemed;
        private final double discountAmount;
        private final int remainingBalance;
        
        public RedeemResult(int pointsRedeemed, double discountAmount, int remainingBalance) {
            this.pointsRedeemed = pointsRedeemed;
            this.discountAmount = discountAmount;
            this.remainingBalance = remainingBalance;
        }
        
        public int getPointsRedeemed() {
            return pointsRedeemed;
        }
        
        public double getDiscountAmount() {
            return discountAmount;
        }
        
        public int getRemainingBalance() {
            return remainingBalance;
        }
    }
}
