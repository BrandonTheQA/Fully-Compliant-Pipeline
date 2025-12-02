package com.example.ecompoc.loyalty.service;

import com.example.ecompoc.loyalty.dto.*;
import com.example.ecompoc.loyalty.model.*;
import com.example.ecompoc.loyalty.repository.LoyaltyAccountRepository;
import com.example.ecompoc.loyalty.repository.LoyaltyTransactionRepository;
import com.example.ecompoc.loyalty.service.LoyaltyExpirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Main service for loyalty program management
 */
@Service
public class LoyaltyService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoyaltyService.class);
    
    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final LoyaltyPointsService pointsService;
    private final LoyaltyTierService tierService;
    private final LoyaltyReferralService referralService;
    private final LoyaltyExpirationService expirationService;
    private final LoyaltyEmailService emailService;
    
    public LoyaltyService(LoyaltyAccountRepository accountRepository,
                         LoyaltyTransactionRepository transactionRepository,
                         LoyaltyPointsService pointsService,
                         LoyaltyTierService tierService,
                         LoyaltyReferralService referralService,
                         LoyaltyExpirationService expirationService,
                         LoyaltyEmailService emailService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.pointsService = pointsService;
        this.tierService = tierService;
        this.referralService = referralService;
        this.expirationService = expirationService;
        this.emailService = emailService;
    }
    
    /**
     * Enroll user in loyalty program
     */
    @Transactional
    public LoyaltyAccountResponse enrollUser(String userId, EnrollmentSource source, String referralCode) {
        // Check if already enrolled
        if (accountRepository.existsByUserId(userId)) {
            LoyaltyAccount existing = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account exists but not found"));
            
            if (existing.getIsActive()) {
                throw new IllegalArgumentException("User is already enrolled in loyalty program");
            } else {
                // Re-enroll
                return reEnroll(userId);
            }
        }
        
        // Generate referral code
        String userReferralCode = referralService.generateReferralCode(userId);
        
        // Ensure uniqueness
        while (accountRepository.findByReferralCode(userReferralCode).isPresent()) {
            userReferralCode = referralService.generateReferralCode(userId);
        }
        
        // Create account
        String accountId = UUID.randomUUID().toString();
        LoyaltyAccount account = new LoyaltyAccount(accountId, userId, userReferralCode, source);
        account = accountRepository.save(account);
        
        // Award welcome points
        pointsService.awardWelcomePoints(userId);
        
        // Handle referral enrollment
        if (referralCode != null && !referralCode.isEmpty()) {
            try {
                referralService.trackReferral(referralCode, userId, "CODE");
                // Award bonus points to referred customer (50 bonus points in addition to 100 welcome = 150 total)
                // The welcome points are already awarded above, so we just need to add 50 more
                LoyaltyAccount referredAccount = accountRepository.findByUserId(userId)
                    .orElse(null);
                if (referredAccount != null) {
                    // Award 50 bonus points for referral enrollment
                    String transactionId = UUID.randomUUID().toString();
                    LoyaltyTransaction bonusTransaction = new LoyaltyTransaction(
                        transactionId,
                        referredAccount.getAccountId(),
                        userId,
                        TransactionType.EARNED,
                        50, // 50 bonus points for referred customer
                        ActivityType.REFERRAL
                    );
                    bonusTransaction.setDescription("Referral enrollment bonus: 50 points");
                    expirationService.setExpirationDate(bonusTransaction);
                    
                    referredAccount.setCurrentPoints(referredAccount.getCurrentPoints() + 50);
                    referredAccount.setLifetimePointsEarned(referredAccount.getLifetimePointsEarned() + 50);
                    referredAccount.setLastActivityDate(java.time.LocalDateTime.now());
                    referredAccount.setUpdatedAt(java.time.LocalDateTime.now());
                    
                    transactionRepository.save(bonusTransaction);
                    accountRepository.save(referredAccount);
                    logger.info("Awarded 50 referral bonus points to referred user {}", userId);
                }
            } catch (Exception e) {
                logger.warn("Failed to track referral during enrollment: {}", e.getMessage());
            }
        }
        
        // Send welcome email
        emailService.sendWelcomeEmail(userId);
        
        logger.info("Enrolled user {} in loyalty program with source {}", userId, source);
        
        return mapToAccountResponse(account);
    }
    
    /**
     * Get loyalty account for user
     */
    public LoyaltyAccountResponse getAccount(String userId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        return mapToAccountResponse(account);
    }
    
    /**
     * Get complete dashboard data
     */
    public LoyaltyDashboardResponse getDashboard(String userId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        LoyaltyDashboardResponse dashboard = new LoyaltyDashboardResponse();
        dashboard.setAccount(mapToAccountResponse(account));
        
        // Get recent transactions (last 10)
        Pageable pageable = PageRequest.of(0, 10);
        Page<LoyaltyTransaction> transactions = transactionRepository
            .findByAccountIdOrderByCreatedAtDesc(account.getAccountId(), pageable);
        
        List<LoyaltyTransactionResponse> transactionResponses = transactions.getContent().stream()
            .map(this::mapToTransactionResponse)
            .collect(Collectors.toList());
        dashboard.setRecentTransactions(transactionResponses);
        
        // Get points to next tier
        int pointsToNextTier = tierService.getPointsToNextTier(
            account.getLifetimePointsEarned(), account.getCurrentTier());
        dashboard.setPointsToNextTier(pointsToNextTier);
        
        // Check for expiring points (within 30 days)
        List<LoyaltyTransaction> expiring = expirationService.checkExpiringPoints(userId, 30);
        if (!expiring.isEmpty()) {
            int totalExpiring = expiring.stream()
                .mapToInt(LoyaltyTransaction::getPoints)
                .sum();
            dashboard.setExpiringPoints(totalExpiring);
            if (!expiring.isEmpty()) {
                dashboard.setExpiringPointsDate(expiring.get(0).getExpirationDate()
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            }
        }
        
        // Get tier benefits
        TierBenefitsResponse tierBenefits = new TierBenefitsResponse();
        tierBenefits.setTier(account.getCurrentTier().name());
        tierBenefits.setMultiplier(tierService.getTierMultiplier(account.getCurrentTier()));
        tierBenefits.setBenefits(tierService.getTierBenefits(account.getCurrentTier()));
        tierBenefits.setPointsToNextTier(pointsToNextTier);
        dashboard.setTierBenefits(tierBenefits);
        
        return dashboard;
    }
    
    /**
     * Opt out of loyalty program
     */
    @Transactional
    public void optOut(String userId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        account.setIsActive(false);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        
        logger.info("User {} opted out of loyalty program", userId);
    }
    
    /**
     * Re-enroll in loyalty program
     */
    @Transactional
    public LoyaltyAccountResponse reEnroll(String userId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        account.setIsActive(true);
        account.setUpdatedAt(LocalDateTime.now());
        account = accountRepository.save(account);
        
        // Award welcome points again
        pointsService.awardWelcomePoints(userId);
        
        emailService.sendWelcomeEmail(userId);
        
        logger.info("User {} re-enrolled in loyalty program", userId);
        
        return mapToAccountResponse(account);
    }
    
    /**
     * Get transaction history
     */
    public LoyaltyHistoryResponse getHistory(String userId, Pageable pageable) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        Page<LoyaltyTransaction> transactions = transactionRepository
            .findByAccountIdOrderByCreatedAtDesc(account.getAccountId(), pageable);
        
        List<LoyaltyTransactionResponse> transactionResponses = transactions.getContent().stream()
            .map(this::mapToTransactionResponse)
            .collect(Collectors.toList());
        
        LoyaltyHistoryResponse response = new LoyaltyHistoryResponse();
        response.setTransactions(transactionResponses);
        response.setTotalElements(transactions.getTotalElements());
        response.setTotalPages(transactions.getTotalPages());
        response.setCurrentPage(transactions.getNumber());
        response.setPageSize(transactions.getSize());
        
        return response;
    }
    
    /**
     * Check and upgrade tier if needed
     */
    @Transactional
    public void checkAndUpgradeTier(String userId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        
        LoyaltyTier newTier = tierService.calculateTier(account.getLifetimePointsEarned());
        
        if (newTier.ordinal() > account.getCurrentTier().ordinal()) {
            LoyaltyTier oldTier = account.getCurrentTier();
            account.setCurrentTier(newTier);
            account.setHighestTierAchieved(newTier);
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
            
            // Send tier upgrade email
            emailService.sendTierUpgradeEmail(userId, newTier);
            
            logger.info("User {} upgraded from {} to {} tier", userId, oldTier, newTier);
        }
    }
    
    /**
     * Map LoyaltyAccount to LoyaltyAccountResponse
     */
    private LoyaltyAccountResponse mapToAccountResponse(LoyaltyAccount account) {
        LoyaltyAccountResponse response = new LoyaltyAccountResponse();
        response.setAccountId(account.getAccountId());
        response.setUserId(account.getUserId());
        response.setCurrentPoints(account.getCurrentPoints());
        response.setCurrentTier(account.getCurrentTier().name());
        response.setHighestTierAchieved(account.getHighestTierAchieved().name());
        response.setLifetimePointsEarned(account.getLifetimePointsEarned());
        response.setLifetimePointsRedeemed(account.getLifetimePointsRedeemed());
        response.setReferralCode(account.getReferralCode());
        response.setEnrollmentDate(account.getEnrollmentDate()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.setIsActive(account.getIsActive());
        return response;
    }
    
    /**
     * Map LoyaltyTransaction to LoyaltyTransactionResponse
     */
    private LoyaltyTransactionResponse mapToTransactionResponse(LoyaltyTransaction transaction) {
        LoyaltyTransactionResponse response = new LoyaltyTransactionResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setTransactionType(transaction.getTransactionType().name());
        response.setPoints(transaction.getPoints());
        response.setActivityType(transaction.getActivityType().name());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (transaction.getExpirationDate() != null) {
            response.setExpirationDate(transaction.getExpirationDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        return response;
    }
}
