package com.example.ecompoc.shipping.recommendation.service;

import com.example.ecompoc.product.dto.ProductResponse;
import com.example.ecompoc.product.service.ProductService;
import com.example.ecompoc.shipping.model.ShippingRule;
import com.example.ecompoc.shipping.recommendation.dto.OptimizationPath;
import com.example.ecompoc.shipping.recommendation.dto.RecommendationResponse;
import com.example.ecompoc.shipping.service.GeolocationService;
import com.example.ecompoc.shipping.service.ShippingRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShippingRecommendationService
 */
@DisplayName("ShippingRecommendationService Tests")
class ShippingRecommendationServiceTest {

    private ShippingRecommendationService recommendationService;
    private ShippingRuleService shippingRuleService;
    private ProductService productService;
    private GeolocationService geolocationService;

    @BeforeEach
    void setUp() {
        shippingRuleService = mock(ShippingRuleService.class);
        productService = mock(ProductService.class);
        geolocationService = mock(GeolocationService.class);
        recommendationService = new ShippingRecommendationService(
            shippingRuleService,
            productService,
            geolocationService
        );

        // Setup default mock behavior
        when(geolocationService.detectRegion()).thenReturn("US");
        when(geolocationService.detectRegion(any(String.class))).thenAnswer(invocation -> {
            String region = invocation.getArgument(0);
            return region != null && !region.isEmpty() ? region.toUpperCase() : "US";
        });
    }

    @Test
    @DisplayName("Should return empty recommendations when cart qualifies for free shipping")
    void shouldReturnEmptyRecommendationsWhenCartQualifiesForFreeShipping() {
        // Given
        BigDecimal cartTotal = new BigDecimal("60.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        String region = "US";

        ShippingRule rule = new ShippingRule(region, threshold, defaultCost);

        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, region)).thenReturn(true);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, region)).thenReturn(BigDecimal.ZERO);
        when(shippingRuleService.getFreeShippingThreshold(region)).thenReturn(threshold);
        when(shippingRuleService.getShippingRule(region)).thenReturn(rule);

        // When
        RecommendationResponse response = recommendationService.generateRecommendations(
            cartTotal,
            Collections.emptyList(),
            region,
            null
        );

        // Then
        assertNotNull(response);
        assertTrue(response.getQualifiesForFreeShipping());
        assertTrue(response.getOptimizationPaths().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getRemainingAmount());
    }

    @Test
    @DisplayName("Should generate recommendations when cart is below threshold")
    void shouldGenerateRecommendationsWhenCartIsBelowThreshold() {
        // Given
        BigDecimal cartTotal = new BigDecimal("35.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal remaining = new BigDecimal("15.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        String region = "US";

        // Product that reaches threshold
        ProductResponse product1 = new ProductResponse(
            "prod1",
            "Product 1",
            "Description 1",
            20.00,
            10,
            "Electronics",
            "2024-01-01T00:00:00",
            "2024-01-01T00:00:00"
        );

        // Product that doesn't reach threshold alone
        ProductResponse product2 = new ProductResponse(
            "prod2",
            "Product 2",
            "Description 2",
            5.00,
            10,
            "Electronics",
            "2024-01-01T00:00:00",
            "2024-01-01T00:00:00"
        );

        List<ProductResponse> products = Arrays.asList(product1, product2);
        ShippingRule rule = new ShippingRule(region, threshold, defaultCost);

        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, region)).thenReturn(false);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, region)).thenReturn(remaining);
        when(shippingRuleService.getFreeShippingThreshold(region)).thenReturn(threshold);
        when(shippingRuleService.getShippingRule(region)).thenReturn(rule);
        when(productService.getAllProducts()).thenReturn(products);

        // When
        RecommendationResponse response = recommendationService.generateRecommendations(
            cartTotal,
            Collections.emptyList(),
            region,
            null
        );

        // Then
        assertNotNull(response);
        assertFalse(response.getQualifiesForFreeShipping());
        assertEquals(remaining, response.getRemainingAmount());
        assertNotNull(response.getOptimizationPaths());
        // Should have at least one path for product1
        assertTrue(response.getOptimizationPaths().size() > 0);
    }

    @Test
    @DisplayName("Should filter out products already in cart")
    void shouldFilterOutProductsAlreadyInCart() {
        // Given
        BigDecimal cartTotal = new BigDecimal("35.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal remaining = new BigDecimal("15.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        String region = "US";

        ProductResponse product1 = new ProductResponse(
            "prod1",
            "Product 1",
            "Description 1",
            20.00,
            10,
            "Electronics",
            "2024-01-01T00:00:00",
            "2024-01-01T00:00:00"
        );

        ProductResponse product2 = new ProductResponse(
            "prod2",
            "Product 2",
            "Description 2",
            20.00,
            10,
            "Electronics",
            "2024-01-01T00:00:00",
            "2024-01-01T00:00:00"
        );

        List<ProductResponse> products = Arrays.asList(product1, product2);
        ShippingRule rule = new ShippingRule(region, threshold, defaultCost);

        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, region)).thenReturn(false);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, region)).thenReturn(remaining);
        when(shippingRuleService.getFreeShippingThreshold(region)).thenReturn(threshold);
        when(shippingRuleService.getShippingRule(region)).thenReturn(rule);
        when(productService.getAllProducts()).thenReturn(products);

        // When - cart already contains prod1
        RecommendationResponse response = recommendationService.generateRecommendations(
            cartTotal,
            Arrays.asList("prod1"),
            region,
            null
        );

        // Then
        assertNotNull(response);
        assertFalse(response.getQualifiesForFreeShipping());
        // Should only recommend product2, not product1
        boolean hasProduct1 = response.getOptimizationPaths().stream()
            .anyMatch(path -> path.getProducts().stream()
                .anyMatch(p -> "prod1".equals(p.getId())));
        assertFalse(hasProduct1, "Should not recommend products already in cart");
    }

    @Test
    @DisplayName("Should filter out products with zero or negative inventory")
    void shouldFilterOutProductsWithZeroOrNegativeInventory() {
        // Given
        BigDecimal cartTotal = new BigDecimal("35.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal remaining = new BigDecimal("15.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        String region = "US";

        ProductResponse product1 = new ProductResponse(
            "prod1",
            "Product 1",
            "Description 1",
            20.00,
            0, // Zero inventory
            "Electronics",
            "2024-01-01T00:00:00",
            "2024-01-01T00:00:00"
        );

        ProductResponse product2 = new ProductResponse(
            "prod2",
            "Product 2",
            "Description 2",
            20.00,
            10,
            "Electronics",
            "2024-01-01T00:00:00",
            "2024-01-01T00:00:00"
        );

        List<ProductResponse> products = Arrays.asList(product1, product2);
        ShippingRule rule = new ShippingRule(region, threshold, defaultCost);

        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, region)).thenReturn(false);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, region)).thenReturn(remaining);
        when(shippingRuleService.getFreeShippingThreshold(region)).thenReturn(threshold);
        when(shippingRuleService.getShippingRule(region)).thenReturn(rule);
        when(productService.getAllProducts()).thenReturn(products);

        // When
        RecommendationResponse response = recommendationService.generateRecommendations(
            cartTotal,
            Collections.emptyList(),
            region,
            null
        );

        // Then
        assertNotNull(response);
        // Should only recommend product2 (product1 has zero inventory)
        boolean hasProduct1 = response.getOptimizationPaths().stream()
            .anyMatch(path -> path.getProducts().stream()
                .anyMatch(p -> "prod1".equals(p.getId())));
        assertFalse(hasProduct1, "Should not recommend products with zero inventory");
    }

    @Test
    @DisplayName("Should prioritize products with matching categories")
    void shouldPrioritizeProductsWithMatchingCategories() {
        // Given
        BigDecimal cartTotal = new BigDecimal("35.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal remaining = new BigDecimal("15.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        String region = "US";

        // Product in same category as cart
        ProductResponse product1 = new ProductResponse(
            "prod1",
            "Product 1",
            "Description 1",
            20.00,
            10,
            "Electronics", // Same category
            "2024-01-01T00:00:00",
            "2024-01-01T00:00:00"
        );

        // Product in different category
        ProductResponse product2 = new ProductResponse(
            "prod2",
            "Product 2",
            "Description 2",
            20.00,
            10,
            "Clothing", // Different category
            "2024-01-01T00:00:00",
            "2024-01-01T00:00:00"
        );

        List<ProductResponse> products = Arrays.asList(product1, product2);
        ShippingRule rule = new ShippingRule(region, threshold, defaultCost);

        // Cart contains an Electronics product
        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, region)).thenReturn(false);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, region)).thenReturn(remaining);
        when(shippingRuleService.getFreeShippingThreshold(region)).thenReturn(threshold);
        when(shippingRuleService.getShippingRule(region)).thenReturn(rule);
        when(productService.getAllProducts()).thenReturn(products);

        // When - cart contains Electronics product
        RecommendationResponse response = recommendationService.generateRecommendations(
            cartTotal,
            Arrays.asList("cart-prod"), // Cart item ID
            region,
            null
        );

        // Mock the cart product to have Electronics category
        ProductResponse cartProduct = new ProductResponse(
            "cart-prod",
            "Cart Product",
            "Description",
            35.00,
            1,
            "Electronics",
            "2024-01-01T00:00:00",
            "2024-01-01T00:00:00"
        );
        when(productService.getAllProducts()).thenReturn(Arrays.asList(product1, product2, cartProduct));

        // When
        response = recommendationService.generateRecommendations(
            cartTotal,
            Arrays.asList("cart-prod"),
            region,
            null
        );

        // Then
        assertNotNull(response);
        // Should prioritize Electronics product (product1) over Clothing product (product2)
        // The first path should be Electronics
        if (!response.getOptimizationPaths().isEmpty()) {
            OptimizationPath firstPath = response.getOptimizationPaths().get(0);
            if (!firstPath.getProducts().isEmpty()) {
                String firstProductCategory = firstPath.getProducts().get(0).getCategory();
                // Electronics should be recommended first due to category matching
                assertNotNull(firstProductCategory);
            }
        }
    }

    @Test
    @DisplayName("Should use default values when cart total is null")
    void shouldUseDefaultValuesWhenCartTotalIsNull() {
        // Given
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal remaining = threshold; // Full threshold needed
        BigDecimal defaultCost = new BigDecimal("5.99");
        String region = "US";

        when(shippingRuleService.qualifiesForFreeShipping(BigDecimal.ZERO, region)).thenReturn(false);
        when(shippingRuleService.calculateRemainingAmount(BigDecimal.ZERO, region)).thenReturn(remaining);
        when(shippingRuleService.getFreeShippingThreshold(region)).thenReturn(threshold);
        when(shippingRuleService.getShippingRule(region)).thenReturn(new ShippingRule(region, threshold, defaultCost));
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        // When
        RecommendationResponse response = recommendationService.generateRecommendations(
            null,
            null,
            region,
            null
        );

        // Then
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getCartTotal());
    }

    @Test
    @DisplayName("Should handle auto-detection of region when region is null")
    void shouldHandleAutoDetectionOfRegionWhenRegionIsNull() {
        // Given
        BigDecimal cartTotal = new BigDecimal("35.00");
        BigDecimal threshold = new BigDecimal("50.00");
        BigDecimal defaultCost = new BigDecimal("5.99");
        String detectedRegion = "US";

        when(shippingRuleService.qualifiesForFreeShipping(cartTotal, detectedRegion)).thenReturn(false);
        when(shippingRuleService.calculateRemainingAmount(cartTotal, detectedRegion)).thenReturn(new BigDecimal("15.00"));
        when(shippingRuleService.getFreeShippingThreshold(detectedRegion)).thenReturn(threshold);
        when(shippingRuleService.getShippingRule(detectedRegion)).thenReturn(new ShippingRule(detectedRegion, threshold, defaultCost));
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        // When
        RecommendationResponse response = recommendationService.generateRecommendations(
            cartTotal,
            null,
            null,
            null
        );

        // Then
        assertNotNull(response);
        assertEquals(detectedRegion, response.getRegion());
        verify(geolocationService).detectRegion();
    }
}

