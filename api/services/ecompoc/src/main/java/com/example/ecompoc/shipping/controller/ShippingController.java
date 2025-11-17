package com.example.ecompoc.shipping.controller;

import com.example.ecompoc.shipping.dto.ShippingThresholdResponse;
import com.example.ecompoc.shipping.service.GeolocationService;
import com.example.ecompoc.shipping.service.ShippingRuleService;
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

/**
 * REST Controller for shipping-related endpoints
 */
@RestController
@RequestMapping("/api/shipping")
@Tag(name = "Shipping", description = "Shipping threshold and rules API endpoints")
public class ShippingController {
    
    private final ShippingRuleService shippingRuleService;
    private final GeolocationService geolocationService;
    
    public ShippingController(ShippingRuleService shippingRuleService, GeolocationService geolocationService) {
        this.shippingRuleService = shippingRuleService;
        this.geolocationService = geolocationService;
    }
    
    /**
     * GET /api/shipping/threshold - Get shipping threshold information
     * Auto-detects region from request or accepts region parameter
     */
    @Operation(
            summary = "Get shipping threshold information",
            description = "Retrieves free shipping threshold and remaining amount needed based on cart total and region"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipping threshold retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ShippingThresholdResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/threshold")
    public ResponseEntity<ShippingThresholdResponse> getShippingThreshold(
            @Parameter(description = "Cart total amount", example = "35.00")
            @RequestParam(required = false) BigDecimal cartTotal,
            @Parameter(description = "Region code (e.g., US, CA). If not provided, auto-detected from request", example = "US")
            @RequestParam(required = false) String region) {
        
        // Auto-detect region if not provided
        String detectedRegion = region != null && !region.isEmpty() 
            ? geolocationService.detectRegion(region) 
            : geolocationService.detectRegion();
        
        // Default cart total to 0 if not provided
        BigDecimal currentCartTotal = cartTotal != null ? cartTotal : BigDecimal.ZERO;
        
        // Get threshold for the region
        BigDecimal freeShippingThreshold = shippingRuleService.getFreeShippingThreshold(detectedRegion);
        
        // Calculate remaining amount
        BigDecimal remainingAmount = shippingRuleService.calculateRemainingAmount(currentCartTotal, detectedRegion);
        
        // Check if qualifies for free shipping
        boolean qualifiesForFreeShipping = shippingRuleService.qualifiesForFreeShipping(currentCartTotal, detectedRegion);
        
        ShippingThresholdResponse response = new ShippingThresholdResponse(
                detectedRegion,
                freeShippingThreshold,
                currentCartTotal,
                remainingAmount,
                qualifiesForFreeShipping
        );
        
        return ResponseEntity.ok(response);
    }
}

