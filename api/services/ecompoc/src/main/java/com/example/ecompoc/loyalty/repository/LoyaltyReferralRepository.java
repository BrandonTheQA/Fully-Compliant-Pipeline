package com.example.ecompoc.loyalty.repository;

import com.example.ecompoc.loyalty.model.LoyaltyReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for LoyaltyReferral entities
 */
@Repository
public interface LoyaltyReferralRepository extends JpaRepository<LoyaltyReferral, String> {
    
    /**
     * Find referrals by referrer account ID
     */
    List<LoyaltyReferral> findByReferrerAccountId(String referrerAccountId);
    
    /**
     * Find referral by referred user ID
     */
    Optional<LoyaltyReferral> findByReferredUserId(String referredUserId);
    
    /**
     * Find referral by referral code
     */
    Optional<LoyaltyReferral> findByReferralCode(String referralCode);
    
    /**
     * Check if referral exists for user
     */
    boolean existsByReferredUserId(String referredUserId);
}
