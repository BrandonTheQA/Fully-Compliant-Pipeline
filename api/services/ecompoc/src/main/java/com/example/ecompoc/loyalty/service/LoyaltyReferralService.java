package com.example.ecompoc.loyalty.service;

import com.example.ecompoc.loyalty.model.LoyaltyAccount;
import com.example.ecompoc.loyalty.model.LoyaltyReferral;
import com.example.ecompoc.loyalty.repository.LoyaltyAccountRepository;
import com.example.ecompoc.loyalty.repository.LoyaltyReferralRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing referral program
 */
@Service
public class LoyaltyReferralService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoyaltyReferralService.class);
    
    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyReferralRepository referralRepository;
    private final ApplicationContext applicationContext;
    
    public LoyaltyReferralService(LoyaltyAccountRepository accountRepository,
                                  LoyaltyReferralRepository referralRepository,
                                  ApplicationContext applicationContext) {
        this.accountRepository = accountRepository;
        this.referralRepository = referralRepository;
        this.applicationContext = applicationContext;
    }
    
    /**
     * Generate unique referral code for a user
     */
    public String generateReferralCode(String userId) {
        // Generate unique code: first 8 chars of UUID + userId hash
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String userIdHash = String.valueOf(userId.hashCode()).replace("-", "").substring(0, 4);
        return uuid + userIdHash;
    }
    
    /**
     * Get referral code for a user
     */
    public String getReferralCode(String userId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found for user: " + userId));
        return account.getReferralCode();
    }
    
    /**
     * Track a referral
     */
    public LoyaltyReferral trackReferral(String referralCode, String referredUserId, String method) {
        // Find referrer account
        LoyaltyAccount referrerAccount = accountRepository.findByReferralCode(referralCode)
            .orElseThrow(() -> new RuntimeException("Invalid referral code: " + referralCode));
        
        // Prevent self-referral
        if (referrerAccount.getUserId().equals(referredUserId)) {
            throw new IllegalArgumentException("Cannot refer yourself");
        }
        
        // Check if referral already exists
        if (referralRepository.existsByReferredUserId(referredUserId)) {
            throw new IllegalArgumentException("User already referred");
        }
        
        // Create referral record
        String referralId = UUID.randomUUID().toString();
        LoyaltyReferral referral = new LoyaltyReferral(
            referralId,
            referrerAccount.getAccountId(),
            referredUserId,
            referralCode,
            method,
            "PENDING"
        );
        
        return referralRepository.save(referral);
    }
    
    /**
     * Process referral completion when referred user makes first purchase
     */
    public void processReferralCompletion(String referredUserId, String orderId) {
        LoyaltyReferral referral = referralRepository.findByReferredUserId(referredUserId)
            .orElse(null);
        
        if (referral == null || !referral.getStatus().equals("PENDING")) {
            return; // No referral or already processed
        }
        
        // Mark referral as completed
        referral.setStatus("COMPLETED");
        referral.setCompletedAt(java.time.LocalDateTime.now());
        referral.setPointsAwarded(true);
        referralRepository.save(referral);
        
        // Award points to referrer
        try {
            LoyaltyAccount referrerAccount = accountRepository.findById(referral.getReferrerAccountId())
                .orElse(null);
            if (referrerAccount != null) {
                LoyaltyPointsService pointsService = applicationContext.getBean(LoyaltyPointsService.class);
                pointsService.awardReferralPoints(referrerAccount.getUserId(), referral.getReferralId());
                logger.info("Awarded referral points to referrer {} for referral {}", 
                    referrerAccount.getUserId(), referral.getReferralId());
            }
        } catch (Exception e) {
            logger.warn("Failed to award referral points: {}", e.getMessage());
        }
    }
    
    /**
     * Get referral statistics for a user
     */
    public ReferralStatistics getReferralStatistics(String userId) {
        LoyaltyAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Loyalty account not found"));
        
        List<LoyaltyReferral> referrals = referralRepository.findByReferrerAccountId(account.getAccountId());
        
        int totalReferrals = referrals.size();
        long successfulReferrals = referrals.stream()
            .filter(r -> "COMPLETED".equals(r.getStatus()))
            .count();
        
        long pointsEarned = referrals.stream()
            .filter(r -> r.getPointsAwarded() != null && r.getPointsAwarded())
            .count() * 100; // 100 points per successful referral
        
        double successRate = totalReferrals > 0 ? (double) successfulReferrals / totalReferrals * 100 : 0.0;
        
        return new ReferralStatistics(totalReferrals, (int) successfulReferrals, (int) pointsEarned, successRate);
    }
    
    /**
     * Validate referral code and prevent self-referral
     */
    public boolean validateReferral(String referralCode, String userId) {
        if (referralCode == null || userId == null) {
            return false;
        }
        
        LoyaltyAccount account = accountRepository.findByReferralCode(referralCode)
            .orElse(null);
        
        if (account == null) {
            return false;
        }
        
        // Prevent self-referral
        return !account.getUserId().equals(userId);
    }
    
    /**
     * Inner class for referral statistics
     */
    public static class ReferralStatistics {
        private final int totalReferrals;
        private final int successfulReferrals;
        private final int pointsEarned;
        private final double successRate;
        
        public ReferralStatistics(int totalReferrals, int successfulReferrals, int pointsEarned, double successRate) {
            this.totalReferrals = totalReferrals;
            this.successfulReferrals = successfulReferrals;
            this.pointsEarned = pointsEarned;
            this.successRate = successRate;
        }
        
        public int getTotalReferrals() {
            return totalReferrals;
        }
        
        public int getSuccessfulReferrals() {
            return successfulReferrals;
        }
        
        public int getPointsEarned() {
            return pointsEarned;
        }
        
        public double getSuccessRate() {
            return successRate;
        }
    }
}
