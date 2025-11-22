package com.example.ecompoc.abandonedcart.service;

import com.example.ecompoc.shipping.service.ShippingRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AbandonedCartDiscountService Tests")
class AbandonedCartDiscountServiceTest {
    
    @Mock
    private ShippingRuleService shippingRuleService;
    
    private AbandonedCartDiscountService discountService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        discountService = new AbandonedCartDiscountService(shippingRuleService);
        
        // Set feature toggle to enabled for tests
        ReflectionTestUtils.setField(discountService, "abandonedCartEnabled", true);
        
        // Mock shipping rule
        com.example.ecompoc.shipping.model.ShippingRule mockRule = 
            new com.example.ecompoc.shipping.model.ShippingRule("US", 
                new BigDecimal("50.00"), new BigDecimal("5.99"));
        when(shippingRuleService.getShippingRule(anyString())).thenReturn(mockRule);
    }
    
    @Test
    @DisplayName("Should return null when feature is disabled")
    void shouldReturnNullWhenFeatureDisabled() {
        ReflectionTestUtils.setField(discountService, "abandonedCartEnabled", false);
        
        AbandonedCartDiscountService.DiscountInfo result = 
            discountService.calculateDiscount(new BigDecimal("30.00"), "US", false);
        
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should calculate Tier 1 discount (< $25)")
    void shouldCalculateTier1Discount() {
        AbandonedCartDiscountService.DiscountInfo result = 
            discountService.calculateDiscount(new BigDecimal("20.00"), "US", false);
        
        assertNotNull(result);
        assertEquals("SHIPPING_PERCENT", result.getType());
        assertTrue(result.getValue().compareTo(new BigDecimal("2.99")) >= 0);
    }
    
    @Test
    @DisplayName("Should calculate Tier 2 discount ($25-$49)")
    void shouldCalculateTier2Discount() {
        AbandonedCartDiscountService.DiscountInfo result = 
            discountService.calculateDiscount(new BigDecimal("35.00"), "US", false);
        
        assertNotNull(result);
        assertEquals("FREE_SHIPPING", result.getType());
        assertEquals(new BigDecimal("5.99"), result.getValue());
    }
    
    @Test
    @DisplayName("Should calculate Tier 3 discount ($50-$99)")
    void shouldCalculateTier3Discount() {
        AbandonedCartDiscountService.DiscountInfo result = 
            discountService.calculateDiscount(new BigDecimal("75.00"), "US", false);
        
        assertNotNull(result);
        assertEquals("CART_PERCENT", result.getType());
        // Free shipping (5.99) + 5% of cart (3.75) = 9.74
        assertTrue(result.getValue().compareTo(new BigDecimal("9.00")) > 0);
    }
    
    @Test
    @DisplayName("Should calculate Tier 4 discount (≥ $100)")
    void shouldCalculateTier4Discount() {
        AbandonedCartDiscountService.DiscountInfo result = 
            discountService.calculateDiscount(new BigDecimal("150.00"), "US", false);
        
        assertNotNull(result);
        assertEquals("CART_PERCENT", result.getType());
        // Free shipping (5.99) + 10% of cart (15.00) = 20.99
        assertTrue(result.getValue().compareTo(new BigDecimal("20.00")) > 0);
    }
    
    @Test
    @DisplayName("Should upgrade tier for returning customer")
    void shouldUpgradeTierForReturningCustomer() {
        // $35 cart normally gets Tier 2 (free shipping)
        // Returning customer should get Tier 3 (free shipping + 5% off)
        AbandonedCartDiscountService.DiscountInfo result = 
            discountService.calculateDiscount(new BigDecimal("35.00"), "US", true);
        
        assertNotNull(result);
        assertEquals("CART_PERCENT", result.getType()); // Tier 3
    }
    
    @Test
    @DisplayName("Should generate discount code when enabled")
    void shouldGenerateDiscountCode() {
        ReflectionTestUtils.setField(discountService, "abandonedCartEnabled", true);
        
        String code = discountService.generateDiscountCode();
        
        assertNotNull(code);
        assertTrue(code.startsWith("AC"));
        assertEquals(10, code.length());
    }
    
    @Test
    @DisplayName("Should return null discount code when disabled")
    void shouldReturnNullDiscountCodeWhenDisabled() {
        ReflectionTestUtils.setField(discountService, "abandonedCartEnabled", false);
        
        String code = discountService.generateDiscountCode();
        
        assertNull(code);
    }
}

