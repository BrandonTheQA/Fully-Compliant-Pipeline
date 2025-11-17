package com.example.ecompoc.shipping.service;

import com.example.ecompoc.shipping.model.ShippingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ShippingRuleService
 */
@DisplayName("ShippingRuleService Tests")
class ShippingRuleServiceTest {

    private ShippingRuleService shippingRuleService;

    @BeforeEach
    void setUp() {
        shippingRuleService = new ShippingRuleService();
        
        // Set test values using reflection
        ReflectionTestUtils.setField(shippingRuleService, "usThreshold", new BigDecimal("50.00"));
        ReflectionTestUtils.setField(shippingRuleService, "usShippingCost", new BigDecimal("5.99"));
        ReflectionTestUtils.setField(shippingRuleService, "caThreshold", new BigDecimal("75.00"));
        ReflectionTestUtils.setField(shippingRuleService, "caShippingCost", new BigDecimal("9.99"));
        ReflectionTestUtils.setField(shippingRuleService, "defaultThreshold", new BigDecimal("50.00"));
        ReflectionTestUtils.setField(shippingRuleService, "defaultShippingCost", new BigDecimal("5.99"));
        
        // Initialize rules
        ReflectionTestUtils.invokeMethod(shippingRuleService, "initializeRules");
    }

    @Test
    @DisplayName("Should get US shipping rule successfully")
    void shouldGetUSShippingRuleSuccessfully() {
        // When
        ShippingRule rule = shippingRuleService.getShippingRule("US");

        // Then
        assertNotNull(rule);
        assertEquals("US", rule.getRegion());
        assertEquals(new BigDecimal("50.00"), rule.getFreeShippingThreshold());
        assertEquals(new BigDecimal("5.99"), rule.getDefaultShippingCost());
    }

    @Test
    @DisplayName("Should get CA shipping rule successfully")
    void shouldGetCAShippingRuleSuccessfully() {
        // When
        ShippingRule rule = shippingRuleService.getShippingRule("CA");

        // Then
        assertNotNull(rule);
        assertEquals("CA", rule.getRegion());
        assertEquals(new BigDecimal("75.00"), rule.getFreeShippingThreshold());
        assertEquals(new BigDecimal("9.99"), rule.getDefaultShippingCost());
    }

    @Test
    @DisplayName("Should return default rule for unknown region")
    void shouldReturnDefaultRuleForUnknownRegion() {
        // When
        ShippingRule rule = shippingRuleService.getShippingRule("UK");

        // Then
        assertNotNull(rule);
        assertEquals("default", rule.getRegion());
        assertEquals(new BigDecimal("50.00"), rule.getFreeShippingThreshold());
    }

    @Test
    @DisplayName("Should return default rule for null region")
    void shouldReturnDefaultRuleForNullRegion() {
        // When
        ShippingRule rule = shippingRuleService.getShippingRule(null);

        // Then
        assertNotNull(rule);
        assertEquals("default", rule.getRegion());
    }

    @Test
    @DisplayName("Should get free shipping threshold for US")
    void shouldGetFreeShippingThresholdForUS() {
        // When
        BigDecimal threshold = shippingRuleService.getFreeShippingThreshold("US");

        // Then
        assertEquals(new BigDecimal("50.00"), threshold);
    }

    @Test
    @DisplayName("Should get free shipping threshold for CA")
    void shouldGetFreeShippingThresholdForCA() {
        // When
        BigDecimal threshold = shippingRuleService.getFreeShippingThreshold("CA");

        // Then
        assertEquals(new BigDecimal("75.00"), threshold);
    }

    @Test
    @DisplayName("Should calculate shipping cost as zero when order qualifies for free shipping")
    void shouldCalculateShippingCostAsZeroWhenOrderQualifies() {
        // Given
        BigDecimal orderTotal = new BigDecimal("60.00");
        String region = "US";

        // When
        BigDecimal shippingCost = shippingRuleService.calculateShippingCost(orderTotal, region);

        // Then
        assertEquals(BigDecimal.ZERO, shippingCost);
    }

    @Test
    @DisplayName("Should calculate shipping cost when order does not qualify")
    void shouldCalculateShippingCostWhenOrderDoesNotQualify() {
        // Given
        BigDecimal orderTotal = new BigDecimal("30.00");
        String region = "US";

        // When
        BigDecimal shippingCost = shippingRuleService.calculateShippingCost(orderTotal, region);

        // Then
        assertEquals(new BigDecimal("5.99"), shippingCost);
    }

    @Test
    @DisplayName("Should return default shipping cost for null order total")
    void shouldReturnDefaultShippingCostForNullOrderTotal() {
        // When
        BigDecimal shippingCost = shippingRuleService.calculateShippingCost(null, "US");

        // Then
        assertEquals(new BigDecimal("5.99"), shippingCost);
    }

    @Test
    @DisplayName("Should return default shipping cost for zero order total")
    void shouldReturnDefaultShippingCostForZeroOrderTotal() {
        // When
        BigDecimal shippingCost = shippingRuleService.calculateShippingCost(BigDecimal.ZERO, "US");

        // Then
        assertEquals(new BigDecimal("5.99"), shippingCost);
    }

    @Test
    @DisplayName("Should correctly identify when order qualifies for free shipping")
    void shouldCorrectlyIdentifyWhenOrderQualifiesForFreeShipping() {
        // Given
        BigDecimal orderTotal = new BigDecimal("50.00");
        String region = "US";

        // When
        boolean qualifies = shippingRuleService.qualifiesForFreeShipping(orderTotal, region);

        // Then
        assertTrue(qualifies);
    }

    @Test
    @DisplayName("Should correctly identify when order does not qualify for free shipping")
    void shouldCorrectlyIdentifyWhenOrderDoesNotQualify() {
        // Given
        BigDecimal orderTotal = new BigDecimal("40.00");
        String region = "US";

        // When
        boolean qualifies = shippingRuleService.qualifiesForFreeShipping(orderTotal, region);

        // Then
        assertFalse(qualifies);
    }

    @Test
    @DisplayName("Should return false for null order total")
    void shouldReturnFalseForNullOrderTotal() {
        // When
        boolean qualifies = shippingRuleService.qualifiesForFreeShipping(null, "US");

        // Then
        assertFalse(qualifies);
    }

    @Test
    @DisplayName("Should calculate remaining amount correctly")
    void shouldCalculateRemainingAmountCorrectly() {
        // Given
        BigDecimal orderTotal = new BigDecimal("35.00");
        String region = "US";

        // When
        BigDecimal remaining = shippingRuleService.calculateRemainingAmount(orderTotal, region);

        // Then
        assertEquals(new BigDecimal("15.00"), remaining);
    }

    @Test
    @DisplayName("Should return zero remaining amount when order qualifies")
    void shouldReturnZeroRemainingAmountWhenOrderQualifies() {
        // Given
        BigDecimal orderTotal = new BigDecimal("60.00");
        String region = "US";

        // When
        BigDecimal remaining = shippingRuleService.calculateRemainingAmount(orderTotal, region);

        // Then
        assertEquals(BigDecimal.ZERO, remaining);
    }

    @Test
    @DisplayName("Should return full threshold for null order total")
    void shouldReturnFullThresholdForNullOrderTotal() {
        // When
        BigDecimal remaining = shippingRuleService.calculateRemainingAmount(null, "US");

        // Then
        assertEquals(new BigDecimal("50.00"), remaining);
    }

    @Test
    @DisplayName("Should handle case-insensitive region codes")
    void shouldHandleCaseInsensitiveRegionCodes() {
        // When
        ShippingRule rule1 = shippingRuleService.getShippingRule("us");
        ShippingRule rule2 = shippingRuleService.getShippingRule("US");
        ShippingRule rule3 = shippingRuleService.getShippingRule("Us");

        // Then
        assertNotNull(rule1);
        assertNotNull(rule2);
        assertNotNull(rule3);
        assertEquals("US", rule1.getRegion());
        assertEquals("US", rule2.getRegion());
        assertEquals("US", rule3.getRegion());
    }
}

