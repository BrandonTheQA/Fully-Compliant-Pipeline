package com.example.ecompoc.abandonedcart.service;

import com.example.ecompoc.shipping.service.ShippingRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for calculating abandoned cart discounts
 */
@Service
public class AbandonedCartDiscountService {
    
    private static final Logger logger = LoggerFactory.getLogger(AbandonedCartDiscountService.class);
    
    private final ShippingRuleService shippingRuleService;
    
    @Value("${abandoned-cart.enabled:false}")
    private boolean abandonedCartEnabled;
    
    public AbandonedCartDiscountService(ShippingRuleService shippingRuleService) {
        this.shippingRuleService = shippingRuleService;
    }
    
    /**
     * Calculate discount based on cart value and user status
     * 
     * @param cartTotal Cart total amount
     * @param shippingRegion Shipping region
     * @param isReturningCustomer Whether user is a returning customer (2+ orders)
     * @return DiscountInfo with type and value, or null if feature disabled
     */
    public DiscountInfo calculateDiscount(BigDecimal cartTotal, String shippingRegion, boolean isReturningCustomer) {
        if (!abandonedCartEnabled) {
            logger.debug("Abandoned cart feature is disabled, returning null discount");
            return null;
        }
        
        if (cartTotal == null || cartTotal.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid cart total: {}", cartTotal);
            return null;
        }
        
        // Determine base tier
        DiscountTier baseTier = determineTier(cartTotal);
        
        // Upgrade tier for returning customers
        DiscountTier finalTier = isReturningCustomer ? upgradeTier(baseTier) : baseTier;
        
        // Calculate discount based on tier
        return calculateDiscountForTier(finalTier, cartTotal, shippingRegion);
    }
    
    /**
     * Determine discount tier based on cart value
     */
    private DiscountTier determineTier(BigDecimal cartTotal) {
        if (cartTotal.compareTo(new BigDecimal("25.00")) < 0) {
            return DiscountTier.TIER_1; // < $25
        } else if (cartTotal.compareTo(new BigDecimal("50.00")) < 0) {
            return DiscountTier.TIER_2; // $25-$49
        } else if (cartTotal.compareTo(new BigDecimal("100.00")) < 0) {
            return DiscountTier.TIER_3; // $50-$99
        } else {
            return DiscountTier.TIER_4; // ≥ $100
        }
    }
    
    /**
     * Upgrade tier for returning customers (one tier higher)
     */
    private DiscountTier upgradeTier(DiscountTier tier) {
        switch (tier) {
            case TIER_1:
                return DiscountTier.TIER_2;
            case TIER_2:
                return DiscountTier.TIER_3;
            case TIER_3:
            case TIER_4:
                return DiscountTier.TIER_4; // Max tier
            default:
                return tier;
        }
    }
    
    /**
     * Calculate discount for a specific tier
     */
    private DiscountInfo calculateDiscountForTier(DiscountTier tier, BigDecimal cartTotal, String shippingRegion) {
        ShippingRuleService shippingRuleService = this.shippingRuleService;
        BigDecimal shippingCost = shippingRuleService.getShippingRule(shippingRegion).getDefaultShippingCost();
        
        switch (tier) {
            case TIER_1:
                // 50% off shipping or $2.99 minimum
                BigDecimal shippingDiscount = shippingCost.multiply(new BigDecimal("0.5"));
                BigDecimal discountValue = shippingDiscount.max(new BigDecimal("2.99"));
                return new DiscountInfo("SHIPPING_PERCENT", discountValue, "50% off shipping (min $2.99)");
                
            case TIER_2:
                // Free shipping (up to shipping cost value)
                return new DiscountInfo("FREE_SHIPPING", shippingCost, "Free shipping");
                
            case TIER_3:
                // Free shipping + 5% off cart
                BigDecimal cartDiscount5 = cartTotal.multiply(new BigDecimal("0.05"));
                return new DiscountInfo("CART_PERCENT", cartDiscount5.add(shippingCost), 
                    "Free shipping + 5% off cart");
                
            case TIER_4:
                // Free shipping + 10% off cart
                BigDecimal cartDiscount10 = cartTotal.multiply(new BigDecimal("0.10"));
                return new DiscountInfo("CART_PERCENT", cartDiscount10.add(shippingCost), 
                    "Free shipping + 10% off cart");
                
            default:
                return null;
        }
    }
    
    /**
     * Generate unique discount code
     */
    public String generateDiscountCode() {
        if (!abandonedCartEnabled) {
            return null;
        }
        return "AC" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
    
    /**
     * Discount tier enum
     */
    private enum DiscountTier {
        TIER_1, // < $25
        TIER_2, // $25-$49
        TIER_3, // $50-$99
        TIER_4  // ≥ $100
    }
    
    /**
     * Discount information
     */
    public static class DiscountInfo {
        private final String type;
        private final BigDecimal value;
        private final String description;
        
        public DiscountInfo(String type, BigDecimal value, String description) {
            this.type = type;
            this.value = value.setScale(2, RoundingMode.HALF_UP);
            this.description = description;
        }
        
        public String getType() {
            return type;
        }
        
        public BigDecimal getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
    }
}

