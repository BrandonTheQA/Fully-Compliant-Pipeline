package com.example.ecompoc.shipping.controller;

import com.example.ecompoc.shipping.dto.ShippingCostResponse;
import com.example.ecompoc.shipping.dto.ShippingThresholdResponse;
import com.example.ecompoc.shipping.model.ShippingRule;
import com.example.ecompoc.shipping.service.GeolocationService;
import com.example.ecompoc.shipping.service.ShippingRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShippingController
 */
@DisplayName("ShippingController Tests")
class ShippingControllerTest {

    private ShippingController shippingController;
    private ShippingRuleService shippingRuleService;
    private GeolocationService geolocationService;

    @BeforeEach
    void setUp() {
        shippingRuleService = mock(ShippingRuleService.class);
        geolocationService = mock(GeolocationService.class);
        shippingController = new ShippingController(shippingRuleService, geolocationService);

        // Setup default mock behavior
        when(geolocationService.detectRegion()).thenReturn("US");
        when(geolocationService.detectRegion(any(String.class))).thenAnswer(invocation -> {
            String region = invocation.getArgument(0);
            return region != null && !region.isEmpty() ? region.toUpperCase() : "US";
        });
    }

    @Test
    @DisplayName("Should get shipping threshold successfully")
    void shouldGetShippingThresholdSuccessfully() {
        // Given
        BigDecimal cartTotal = new BigDecimal("35.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal remaining = new BigDecimal("15.00");
        String region = "US";

        when(shippingRuleService.getFreeShippingThreshold(region)).thenReturn(threshold);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, region)).thenReturn(remaining);
        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, region)).thenReturn(false);

        // When
        ResponseEntity<ShippingThresholdResponse> response = shippingController.getShippingThreshold(cartTotal, region);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(region, response.getBody().getRegion());
        assertEquals(cartTotal, response.getBody().getCurrentCartTotal());
        assertEquals(threshold, response.getBody().getFreeShippingThreshold());
        assertEquals(remaining, response.getBody().getRemainingAmount());
        assertFalse(response.getBody().getQualifiesForFreeShipping());

        verify(shippingRuleService).getFreeShippingThreshold(region);
        verify(shippingRuleService).calculateRemainingAmount(cartTotal, region);
        verify(shippingRuleService).qualifiesForFreeShipping(cartTotal, region);
    }

    @Test
    @DisplayName("Should get shipping cost successfully when cart is below threshold")
    void shouldGetShippingCostSuccessfullyWhenCartIsBelowThreshold() {
        // Given
        BigDecimal cartTotal = new BigDecimal("35.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        BigDecimal shippingCost = defaultCost;
        BigDecimal remaining = new BigDecimal("15.00");
        String region = "US";

        ShippingRule rule = new ShippingRule(region, threshold, defaultCost);

        when(geolocationService.detectRegion(region)).thenReturn(region);
        when(shippingRuleService.getShippingRule(region)).thenReturn(rule);
        when(shippingRuleService.calculateShippingCost(cartTotal, region)).thenReturn(shippingCost);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, region)).thenReturn(remaining);
        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, region)).thenReturn(false);

        // When
        ResponseEntity<ShippingCostResponse> response = shippingController.getShippingCost(cartTotal, region);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(region, response.getBody().getRegion());
        assertEquals(cartTotal, response.getBody().getCartTotal());
        assertEquals(shippingCost, response.getBody().getShippingCost());
        assertEquals(threshold, response.getBody().getFreeShippingThreshold());
        assertEquals(remaining, response.getBody().getRemainingAmount());
        assertEquals(defaultCost, response.getBody().getDefaultShippingCost());
        assertFalse(response.getBody().getQualifiesForFreeShipping());

        verify(shippingRuleService).getShippingRule(region);
        verify(shippingRuleService).calculateShippingCost(cartTotal, region);
        verify(shippingRuleService).calculateRemainingAmount(cartTotal, region);
        verify(shippingRuleService).qualifiesForFreeShipping(cartTotal, region);
    }

    @Test
    @DisplayName("Should get shipping cost successfully when cart qualifies for free shipping")
    void shouldGetShippingCostSuccessfullyWhenCartQualifiesForFreeShipping() {
        // Given
        BigDecimal cartTotal = new BigDecimal("60.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        BigDecimal shippingCost = BigDecimal.ZERO;
        BigDecimal remaining = BigDecimal.ZERO;
        String region = "US";

        ShippingRule rule = new ShippingRule(region, threshold, defaultCost);

        when(geolocationService.detectRegion(region)).thenReturn(region);
        when(shippingRuleService.getShippingRule(region)).thenReturn(rule);
        when(shippingRuleService.calculateShippingCost(cartTotal, region)).thenReturn(shippingCost);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, region)).thenReturn(remaining);
        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, region)).thenReturn(true);

        // When
        ResponseEntity<ShippingCostResponse> response = shippingController.getShippingCost(cartTotal, region);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(region, response.getBody().getRegion());
        assertEquals(cartTotal, response.getBody().getCartTotal());
        assertEquals(shippingCost, response.getBody().getShippingCost());
        assertEquals(threshold, response.getBody().getFreeShippingThreshold());
        assertEquals(remaining, response.getBody().getRemainingAmount());
        assertEquals(defaultCost, response.getBody().getDefaultShippingCost());
        assertTrue(response.getBody().getQualifiesForFreeShipping());

        verify(shippingRuleService).getShippingRule(region);
        verify(shippingRuleService).calculateShippingCost(cartTotal, region);
        verify(shippingRuleService).calculateRemainingAmount(cartTotal, region);
        verify(shippingRuleService).qualifiesForFreeShipping(cartTotal, region);
    }

    @Test
    @DisplayName("Should get shipping cost with auto-detected region when region not provided")
    void shouldGetShippingCostWithAutoDetectedRegionWhenRegionNotProvided() {
        // Given
        BigDecimal cartTotal = new BigDecimal("40.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        BigDecimal shippingCost = defaultCost;
        BigDecimal remaining = new BigDecimal("10.00");
        String detectedRegion = "US";

        ShippingRule rule = new ShippingRule(detectedRegion, threshold, defaultCost);

        when(geolocationService.detectRegion()).thenReturn(detectedRegion);
        when(shippingRuleService.getShippingRule(detectedRegion)).thenReturn(rule);
        when(shippingRuleService.calculateShippingCost(cartTotal, detectedRegion)).thenReturn(shippingCost);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, detectedRegion)).thenReturn(remaining);
        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, detectedRegion)).thenReturn(false);

        // When
        ResponseEntity<ShippingCostResponse> response = shippingController.getShippingCost(cartTotal, null);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(detectedRegion, response.getBody().getRegion());
        assertEquals(cartTotal, response.getBody().getCartTotal());
        assertEquals(shippingCost, response.getBody().getShippingCost());

        verify(geolocationService).detectRegion();
        verify(shippingRuleService).getShippingRule(detectedRegion);
    }

    @Test
    @DisplayName("Should get shipping cost with default cart total when cart total not provided")
    void shouldGetShippingCostWithDefaultCartTotalWhenCartTotalNotProvided() {
        // Given
        BigDecimal cartTotal = BigDecimal.ZERO;
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        BigDecimal shippingCost = defaultCost;
        BigDecimal remaining = threshold;
        String region = "US";

        ShippingRule rule = new ShippingRule(region, threshold, defaultCost);

        when(geolocationService.detectRegion(region)).thenReturn(region);
        when(shippingRuleService.getShippingRule(region)).thenReturn(rule);
        when(shippingRuleService.calculateShippingCost(cartTotal, region)).thenReturn(shippingCost);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, region)).thenReturn(remaining);
        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, region)).thenReturn(false);

        // When
        ResponseEntity<ShippingCostResponse> response = shippingController.getShippingCost(null, region);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(cartTotal, response.getBody().getCartTotal());
        assertEquals(shippingCost, response.getBody().getShippingCost());

        verify(shippingRuleService).calculateShippingCost(cartTotal, region);
    }

    @Test
    @DisplayName("Should get shipping cost for CA region")
    void shouldGetShippingCostForCARegion() {
        // Given
        BigDecimal cartTotal = new BigDecimal("60.00");
        BigDecimal threshold = new BigDecimal("75.00");
        BigDecimal defaultCost = new BigDecimal("9.99");
        BigDecimal shippingCost = defaultCost;
        BigDecimal remaining = new BigDecimal("15.00");
        String region = "CA";

        ShippingRule rule = new ShippingRule(region, threshold, defaultCost);

        when(geolocationService.detectRegion(region)).thenReturn(region);
        when(shippingRuleService.getShippingRule(region)).thenReturn(rule);
        when(shippingRuleService.calculateShippingCost(cartTotal, region)).thenReturn(shippingCost);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, region)).thenReturn(remaining);
        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, region)).thenReturn(false);

        // When
        ResponseEntity<ShippingCostResponse> response = shippingController.getShippingCost(cartTotal, region);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(region, response.getBody().getRegion());
        assertEquals(threshold, response.getBody().getFreeShippingThreshold());
        assertEquals(defaultCost, response.getBody().getDefaultShippingCost());
        assertEquals(shippingCost, response.getBody().getShippingCost());

        verify(shippingRuleService).getShippingRule(region);
    }

    @Test
    @DisplayName("Should handle empty region parameter by auto-detecting")
    void shouldHandleEmptyRegionParameterByAutoDetecting() {
        // Given
        BigDecimal cartTotal = new BigDecimal("30.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        BigDecimal shippingCost = defaultCost;
        BigDecimal remaining = new BigDecimal("20.00");
        String detectedRegion = "US";

        ShippingRule rule = new ShippingRule(detectedRegion, threshold, defaultCost);

        when(geolocationService.detectRegion()).thenReturn(detectedRegion);
        when(shippingRuleService.getShippingRule(detectedRegion)).thenReturn(rule);
        when(shippingRuleService.calculateShippingCost(cartTotal, detectedRegion)).thenReturn(shippingCost);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, detectedRegion)).thenReturn(remaining);
        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, detectedRegion)).thenReturn(false);

        // When
        ResponseEntity<ShippingCostResponse> response = shippingController.getShippingCost(cartTotal, "");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(detectedRegion, response.getBody().getRegion());

        verify(geolocationService).detectRegion();
    }

    @Test
    @DisplayName("Should get shipping cost when cart total exactly equals threshold")
    void shouldGetShippingCostWhenCartTotalExactlyEqualsThreshold() {
        // Given
        BigDecimal cartTotal = new BigDecimal("50.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        BigDecimal shippingCost = BigDecimal.ZERO;
        BigDecimal remaining = BigDecimal.ZERO;
        String region = "US";

        ShippingRule rule = new ShippingRule(region, threshold, defaultCost);

        when(geolocationService.detectRegion(region)).thenReturn(region);
        when(shippingRuleService.getShippingRule(region)).thenReturn(rule);
        when(shippingRuleService.calculateShippingCost(cartTotal, region)).thenReturn(shippingCost);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, region)).thenReturn(remaining);
        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, region)).thenReturn(true);

        // When
        ResponseEntity<ShippingCostResponse> response = shippingController.getShippingCost(cartTotal, region);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(BigDecimal.ZERO, response.getBody().getShippingCost());
        assertEquals(BigDecimal.ZERO, response.getBody().getRemainingAmount());
        assertTrue(response.getBody().getQualifiesForFreeShipping());
    }
}

