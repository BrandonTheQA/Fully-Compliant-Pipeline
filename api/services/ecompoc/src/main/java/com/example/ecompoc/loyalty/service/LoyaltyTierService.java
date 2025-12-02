package com.example.ecompoc.loyalty.service;

import com.example.ecompoc.loyalty.model.LoyaltyTier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing loyalty tiers and tier benefits
 */
@Service
public class LoyaltyTierService {
    
    @Value("${loyalty.tiers.bronze.threshold:0}")
    private Integer bronzeThreshold;
    
    @Value("${loyalty.tiers.bronze.multiplier:1.0}")
    private Double bronzeMultiplier;
    
    @Value("${loyalty.tiers.silver.threshold:1000}")
    private Integer silverThreshold;
    
    @Value("${loyalty.tiers.silver.multiplier:1.25}")
    private Double silverMultiplier;
    
    @Value("${loyalty.tiers.gold.threshold:2500}")
    private Integer goldThreshold;
    
    @Value("${loyalty.tiers.gold.multiplier:1.5}")
    private Double goldMultiplier;
    
    @Value("${loyalty.tiers.platinum.threshold:5000}")
    private Integer platinumThreshold;
    
    @Value("${loyalty.tiers.platinum.multiplier:2.0}")
    private Double platinumMultiplier;
    
    /**
     * Calculate tier based on lifetime points earned
     */
    public LoyaltyTier calculateTier(Integer lifetimePoints) {
        if (lifetimePoints == null || lifetimePoints < 0) {
            return LoyaltyTier.BRONZE;
        }
        
        if (lifetimePoints >= platinumThreshold) {
            return LoyaltyTier.PLATINUM;
        } else if (lifetimePoints >= goldThreshold) {
            return LoyaltyTier.GOLD;
        } else if (lifetimePoints >= silverThreshold) {
            return LoyaltyTier.SILVER;
        } else {
            return LoyaltyTier.BRONZE;
        }
    }
    
    /**
     * Get tier multiplier for point earning
     */
    public Double getTierMultiplier(LoyaltyTier tier) {
        if (tier == null) {
            return bronzeMultiplier;
        }
        
        switch (tier) {
            case PLATINUM:
                return platinumMultiplier;
            case GOLD:
                return goldMultiplier;
            case SILVER:
                return silverMultiplier;
            case BRONZE:
            default:
                return bronzeMultiplier;
        }
    }
    
    /**
     * Apply tier multiplier to base points
     */
    public Integer applyTierMultiplier(LoyaltyTier tier, Integer basePoints) {
        if (basePoints == null || basePoints <= 0) {
            return 0;
        }
        
        Double multiplier = getTierMultiplier(tier);
        return (int) Math.round(basePoints * multiplier);
    }
    
    /**
     * Get points needed to reach next tier
     */
    public Integer getPointsToNextTier(Integer currentPoints, LoyaltyTier currentTier) {
        if (currentPoints == null || currentTier == null) {
            return silverThreshold;
        }
        
        Integer nextThreshold;
        switch (currentTier) {
            case BRONZE:
                nextThreshold = silverThreshold;
                break;
            case SILVER:
                nextThreshold = goldThreshold;
                break;
            case GOLD:
                nextThreshold = platinumThreshold;
                break;
            case PLATINUM:
            default:
                return 0; // Already at highest tier
        }
        
        int pointsNeeded = nextThreshold - currentPoints;
        return Math.max(0, pointsNeeded);
    }
    
    /**
     * Get tier benefits as list of strings
     */
    public List<String> getTierBenefits(LoyaltyTier tier) {
        List<String> benefits = new ArrayList<>();
        
        if (tier == null) {
            tier = LoyaltyTier.BRONZE;
        }
        
        switch (tier) {
            case PLATINUM:
                benefits.add("2x points on all purchases");
                benefits.add("Free shipping on all orders");
                benefits.add("Early access to sales");
                benefits.add("Exclusive products");
                benefits.add("Dedicated support");
                break;
            case GOLD:
                benefits.add("1.5x points on all purchases");
                benefits.add("Free shipping on all orders");
                benefits.add("Early access to sales");
                break;
            case SILVER:
                benefits.add("1.25x points on all purchases");
                benefits.add("Free shipping on orders $25+");
                break;
            case BRONZE:
            default:
                benefits.add("1x points on all purchases");
                break;
        }
        
        return benefits;
    }
    
    /**
     * Get threshold for a specific tier
     */
    public Integer getTierThreshold(LoyaltyTier tier) {
        if (tier == null) {
            return bronzeThreshold;
        }
        
        switch (tier) {
            case PLATINUM:
                return platinumThreshold;
            case GOLD:
                return goldThreshold;
            case SILVER:
                return silverThreshold;
            case BRONZE:
            default:
                return bronzeThreshold;
        }
    }
}
