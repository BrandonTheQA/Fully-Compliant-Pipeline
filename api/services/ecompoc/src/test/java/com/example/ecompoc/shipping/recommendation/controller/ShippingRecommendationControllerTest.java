package com.example.ecompoc.shipping.recommendation.controller;

import com.example.ecompoc.shipping.recommendation.dto.OptimizationPath;
import com.example.ecompoc.shipping.recommendation.dto.RecommendationResponse;
import com.example.ecompoc.shipping.recommendation.service.ShippingRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShippingRecommendationController
 */
@DisplayName("ShippingRecommendationController Tests")
class ShippingRecommendationControllerTest {

    private ShippingRecommendationController controller;
    private ShippingRecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = mock(ShippingRecommendationService.class);
        controller = new ShippingRecommendationController(recommendationService);
    }

    @Test
    @DisplayName("Should get recommendations successfully")
    void shouldGetRecommendationsSuccessfully() {
        // Given
        BigDecimal cartTotal = new BigDecimal("35.00");
        String region = "US";
        String cartItems = "prod1,prod2";

        RecommendationResponse expectedResponse = new RecommendationResponse(
            new ArrayList<>(),
            false,
            new BigDecimal("15.00"),
            region,
            cartTotal,
            new BigDecimal("50.00")
        );

        when(recommendationService.generateRecommendations(
            eq(cartTotal),
            anyList(),
            eq(region),
            isNull()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<RecommendationResponse> response = controller.getRecommendations(
            cartTotal,
            cartItems,
            region,
            null
        );

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        RecommendationResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(region, body.getRegion());
        assertEquals(cartTotal, body.getCartTotal());
        assertFalse(body.getQualifiesForFreeShipping());

        verify(recommendationService).generateRecommendations(
            eq(cartTotal),
            argThat(list -> list.size() == 2 && list.contains("prod1") && list.contains("prod2")),
            eq(region),
            isNull()
        );
    }

    @Test
    @DisplayName("Should handle null cart total by using zero")
    void shouldHandleNullCartTotalByUsingZero() {
        // Given
        String region = "US";
        BigDecimal zeroTotal = BigDecimal.ZERO;

        RecommendationResponse expectedResponse = new RecommendationResponse(
            new ArrayList<>(),
            false,
            new BigDecimal("50.00"),
            region,
            zeroTotal,
            new BigDecimal("50.00")
        );

        when(recommendationService.generateRecommendations(
            eq(zeroTotal),
            anyList(),
            eq(region),
            isNull()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<RecommendationResponse> response = controller.getRecommendations(
            null,
            null,
            region,
            null
        );

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        RecommendationResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(zeroTotal, body.getCartTotal());

        verify(recommendationService).generateRecommendations(
            eq(zeroTotal),
            anyList(),
            eq(region),
            isNull()
        );
    }

    @Test
    @DisplayName("Should parse comma-separated cart items correctly")
    void shouldParseCommaSeparatedCartItemsCorrectly() {
        // Given
        BigDecimal cartTotal = new BigDecimal("30.00");
        String region = "US";
        String cartItems = "item1,item2,item3";

        RecommendationResponse expectedResponse = new RecommendationResponse(
            new ArrayList<>(),
            false,
            new BigDecimal("20.00"),
            region,
            cartTotal,
            new BigDecimal("50.00")
        );

        when(recommendationService.generateRecommendations(
            eq(cartTotal),
            anyList(),
            eq(region),
            isNull()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<RecommendationResponse> response = controller.getRecommendations(
            cartTotal,
            cartItems,
            region,
            null
        );

        // Then
        assertNotNull(response);
        verify(recommendationService).generateRecommendations(
            eq(cartTotal),
            argThat(list -> list.size() == 3 && 
                list.contains("item1") && 
                list.contains("item2") && 
                list.contains("item3")),
            eq(region),
            isNull()
        );
    }

    @Test
    @DisplayName("Should handle empty cart items string")
    void shouldHandleEmptyCartItemsString() {
        // Given
        BigDecimal cartTotal = new BigDecimal("30.00");
        String region = "US";
        String cartItems = "";

        RecommendationResponse expectedResponse = new RecommendationResponse(
            new ArrayList<>(),
            false,
            new BigDecimal("20.00"),
            region,
            cartTotal,
            new BigDecimal("50.00")
        );

        when(recommendationService.generateRecommendations(
            eq(cartTotal),
            anyList(),
            eq(region),
            isNull()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<RecommendationResponse> response = controller.getRecommendations(
            cartTotal,
            cartItems,
            region,
            null
        );

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        verify(recommendationService).generateRecommendations(
            eq(cartTotal),
            anyList(),
            eq(region),
            isNull()
        );
    }

    @Test
    @DisplayName("Should handle null cart items by using empty list")
    void shouldHandleNullCartItemsByUsingEmptyList() {
        // Given
        BigDecimal cartTotal = new BigDecimal("30.00");
        String region = "US";

        RecommendationResponse expectedResponse = new RecommendationResponse(
            new ArrayList<>(),
            false,
            new BigDecimal("20.00"),
            region,
            cartTotal,
            new BigDecimal("50.00")
        );

        when(recommendationService.generateRecommendations(
            eq(cartTotal),
            anyList(),
            eq(region),
            isNull()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<RecommendationResponse> response = controller.getRecommendations(
            cartTotal,
            null,
            region,
            null
        );

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        verify(recommendationService).generateRecommendations(
            eq(cartTotal),
            argThat(list -> list.isEmpty()),
            eq(region),
            isNull()
        );
    }

    @Test
    @DisplayName("Should pass userId when provided")
    void shouldPassUserIdWhenProvided() {
        // Given
        BigDecimal cartTotal = new BigDecimal("30.00");
        String region = "US";
        String userId = "user123";

        RecommendationResponse expectedResponse = new RecommendationResponse(
            new ArrayList<>(),
            false,
            new BigDecimal("20.00"),
            region,
            cartTotal,
            new BigDecimal("50.00")
        );

        when(recommendationService.generateRecommendations(
            eq(cartTotal),
            anyList(),
            eq(region),
            eq(userId)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<RecommendationResponse> response = controller.getRecommendations(
            cartTotal,
            null,
            region,
            userId
        );

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        verify(recommendationService).generateRecommendations(
            eq(cartTotal),
            anyList(),
            eq(region),
            eq(userId)
        );
    }

    @Test
    @DisplayName("Should return recommendations with optimization paths")
    void shouldReturnRecommendationsWithOptimizationPaths() {
        // Given
        BigDecimal cartTotal = new BigDecimal("35.00");
        String region = "US";

        OptimizationPath path1 = new OptimizationPath();
        path1.setPathType("single");
        path1.setTotalCost(new BigDecimal("20.00"));
        path1.setSavingsAmount(new BigDecimal("5.99"));
        path1.setMessage("Add Product 1 → FREE shipping");

        RecommendationResponse expectedResponse = new RecommendationResponse(
            Arrays.asList(path1),
            false,
            new BigDecimal("15.00"),
            region,
            cartTotal,
            new BigDecimal("50.00")
        );

        when(recommendationService.generateRecommendations(
            eq(cartTotal),
            anyList(),
            eq(region),
            isNull()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<RecommendationResponse> response = controller.getRecommendations(
            cartTotal,
            null,
            region,
            null
        );

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        RecommendationResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.getOptimizationPaths().size());
        assertEquals("single", body.getOptimizationPaths().get(0).getPathType());
    }
}

