package com.example.ecompoc.shipping.recommendation.controller;

import com.example.ecompoc.shipping.recommendation.dto.RecommendationResponse;
import com.example.ecompoc.shipping.recommendation.service.ShippingRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * REST Controller for shipping recommendation endpoints
 */
@RestController
@RequestMapping("/api/shipping")
@Tag(name = "Shipping Recommendations", description = "Shipping optimization recommendation API endpoints")
public class ShippingRecommendationController {
    
    private final ShippingRecommendationService recommendationService;
    
    public ShippingRecommendationController(ShippingRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }
    
    /**
     * GET /api/shipping/recommendations - Get shipping optimization recommendations
     * Auto-detects region from request or accepts region parameter
     */
    @Operation(
            summary = "Get shipping optimization recommendations",
            description = "Retrieves product recommendations to help reach free shipping threshold based on cart total, cart items, and region"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RecommendationResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @Parameter(description = "Cart total amount", required = true, example = "35.00")
            @RequestParam(required = false) BigDecimal cartTotal,
            @Parameter(description = "Comma-separated list of product IDs already in cart", example = "prod1,prod2")
            @RequestParam(required = false) String cartItems,
            @Parameter(description = "Region code (e.g., US, CA). If not provided, auto-detected from request", example = "US")
            @RequestParam(required = false) String region,
            @Parameter(description = "User ID for personalized recommendations (optional)", example = "user123")
            @RequestParam(required = false) String userId) {
        
        // Default cart total to 0 if not provided
        BigDecimal currentCartTotal = cartTotal != null ? cartTotal : BigDecimal.ZERO;
        
        // Parse cart items from comma-separated string
        List<String> cartItemIds = Collections.emptyList();
        if (cartItems != null && !cartItems.isEmpty()) {
            cartItemIds = Arrays.asList(cartItems.split(","));
        }
        
        // Generate recommendations
        RecommendationResponse response = recommendationService.generateRecommendations(
            currentCartTotal,
            cartItemIds,
            region,
            userId
        );
        
        return ResponseEntity.ok(response);
    }
}

