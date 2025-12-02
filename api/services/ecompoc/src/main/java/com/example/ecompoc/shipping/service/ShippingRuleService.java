package com.example.ecompoc.shipping.service;

import com.example.ecompoc.shipping.model.ShippingRule;
import com.example.ecompoc.loyalty.service.LoyaltyService;
import com.example.ecompoc.loyalty.model.LoyaltyTier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing shipping rules and calculations
 */
@Service
public class ShippingRuleService {
    
    private final Map<String, ShippingRule> shippingRules = new HashMap<>();
    
    @Value("${shipping.rules.US.free-shipping-threshold:50.00}")
    private BigDecimal usThreshold;
    
    @Value("${shipping.rules.US.default-shipping-cost:5.99}")
    private BigDecimal usShippingCost;
    
    @Value("${shipping.rules.CA.free-shipping-threshold:75.00}")
    private BigDecimal caThreshold;
    
    @Value("${shipping.rules.CA.default-shipping-cost:9.99}")
    private BigDecimal caShippingCost;
    
    @Value("${shipping.rules.default.free-shipping-threshold:50.00}")
    private BigDecimal defaultThreshold;
    
    @Value("${shipping.rules.default.default-shipping-cost:5.99}")
    private BigDecimal defaultShippingCost;
    
    private LoyaltyService loyaltyService;
    
    @Autowired(required = false)
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }
    
    @PostConstruct
    public void initializeRules() {
        // Initialize shipping rules from configuration
        shippingRules.put("US", new ShippingRule("US", usThreshold, usShippingCost));
        shippingRules.put("CA", new ShippingRule("CA", caThreshold, caShippingCost));
        shippingRules.put("default", new ShippingRule("default", defaultThreshold, defaultShippingCost));
    }
    
    /**
     * Get shipping rule for a specific region
     * 
     * @param region Region code (e.g., "US", "CA")
     * @return ShippingRule for the region, or default rule if region not found
     */
    public ShippingRule getShippingRule(String region) {
        if (region == null || region.isEmpty()) {
            return shippingRules.get("default");
        }
        
        String normalizedRegion = region.toUpperCase();
        ShippingRule rule = shippingRules.get(normalizedRegion);
        
        // Fall back to default if region not found
        return rule != null ? rule : shippingRules.get("default");
    }
    
    /**
     * Get free shipping threshold for a region
     * 
     * @param region Region code
     * @return Free shipping threshold amount
     */
    public BigDecimal getFreeShippingThreshold(String region) {
        ShippingRule rule = getShippingRule(region);
        return rule.getFreeShippingThreshold();
    }
    
    /**
     * Calculate shipping cost based on order total and region
     * 
     * @param orderTotal Total order amount
     * @param region Region code
     * @return Shipping cost (0 if qualifies for free shipping, otherwise default cost)
     */
    public BigDecimal calculateShippingCost(BigDecimal orderTotal, String region) {
        return calculateShippingCost(orderTotal, region, null);
    }
    
    /**
     * Calculate shipping cost based on order total, region, and user tier
     * 
     * @param orderTotal Total order amount
     * @param region Region code
     * @param userId User ID for tier-based free shipping (optional)
     * @return Shipping cost (0 if qualifies for free shipping, otherwise default cost)
     */
    public BigDecimal calculateShippingCost(BigDecimal orderTotal, String region, String userId) {
        ShippingRule rule = getShippingRule(region);
        
        if (orderTotal == null || orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return rule.getDefaultShippingCost();
        }
        
        // Check tier-based free shipping
        if (userId != null && loyaltyService != null) {
            try {
                com.example.ecompoc.loyalty.dto.LoyaltyAccountResponse account = loyaltyService.getAccount(userId);
                LoyaltyTier tier = LoyaltyTier.valueOf(account.getCurrentTier());
                
                // Gold and Platinum: Free shipping on all orders
                if (tier == LoyaltyTier.GOLD || tier == LoyaltyTier.PLATINUM) {
                    return BigDecimal.ZERO;
                }
                
                // Silver: Free shipping on orders $25+
                if (tier == LoyaltyTier.SILVER && orderTotal.compareTo(new BigDecimal("25.00")) >= 0) {
                    return BigDecimal.ZERO;
                }
            } catch (Exception e) {
                // If loyalty service fails, fall back to standard rules
                // Log but don't fail shipping calculation
            }
        }
        
        // If order total meets or exceeds threshold, shipping is free
        if (orderTotal.compareTo(rule.getFreeShippingThreshold()) >= 0) {
            return BigDecimal.ZERO;
        }
        
        return rule.getDefaultShippingCost();
    }
    
    /**
     * Check if order qualifies for free shipping
     * 
     * @param orderTotal Total order amount
     * @param region Region code
     * @return true if qualifies for free shipping, false otherwise
     */
    public boolean qualifiesForFreeShipping(BigDecimal orderTotal, String region) {
        if (orderTotal == null || orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        ShippingRule rule = getShippingRule(region);
        return orderTotal.compareTo(rule.getFreeShippingThreshold()) >= 0;
    }
    
    /**
     * Calculate remaining amount needed to qualify for free shipping
     * 
     * @param orderTotal Current order total
     * @param region Region code
     * @return Remaining amount needed (0 if already qualifies)
     */
    public BigDecimal calculateRemainingAmount(BigDecimal orderTotal, String region) {
        ShippingRule rule = getShippingRule(region);
        BigDecimal threshold = rule.getFreeShippingThreshold();
        
        if (orderTotal == null || orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return threshold;
        }
        
        BigDecimal remaining = threshold.subtract(orderTotal);
        return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
    }
}

