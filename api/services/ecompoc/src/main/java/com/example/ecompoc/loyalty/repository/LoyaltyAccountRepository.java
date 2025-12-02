package com.example.ecompoc.loyalty.repository;

import com.example.ecompoc.loyalty.model.LoyaltyAccount;
import com.example.ecompoc.loyalty.model.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for LoyaltyAccount entities
 */
@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, String> {
    
    /**
     * Find loyalty account by user ID
     */
    Optional<LoyaltyAccount> findByUserId(String userId);
    
    /**
     * Find loyalty account by referral code
     */
    Optional<LoyaltyAccount> findByReferralCode(String referralCode);
    
    /**
     * Find active accounts by tier
     */
    List<LoyaltyAccount> findByCurrentTierAndIsActive(LoyaltyTier tier, Boolean isActive);
    
    /**
     * Check if account exists for user
     */
    boolean existsByUserId(String userId);
}
