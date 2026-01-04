package com.example.ecompoc.pricealert.controller;

import com.example.ecompoc.pricealert.dto.CreatePriceAlertRequest;
import com.example.ecompoc.pricealert.dto.PriceAlertListResponse;
import com.example.ecompoc.pricealert.dto.PriceAlertResponse;
import com.example.ecompoc.pricealert.dto.PriceHistoryResponse;
import com.example.ecompoc.pricealert.dto.UpdatePriceAlertRequest;
import com.example.ecompoc.pricealert.service.PriceAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for price alert management endpoints
 */
@RestController
@RequestMapping("/api/v2/price-alerts")
@Tag(name = "Price Alerts", description = "Price drop alert API endpoints")
public class PriceAlertController {
    
    private final PriceAlertService priceAlertService;
    
    @Value("${price-alert.enabled:true}")
    private boolean priceAlertEnabled;
    
    public PriceAlertController(PriceAlertService priceAlertService) {
        this.priceAlertService = priceAlertService;
    }
    
    /**
     * POST /api/v2/price-alerts - Create price alert
     */
    @Operation(
            summary = "Create a price alert",
            description = "Creates a new price alert for a product. The user will be notified when the price drops."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Price alert created successfully",
                    content = @Content(schema = @Schema(implementation = PriceAlertResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Feature disabled or product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<PriceAlertResponse> createPriceAlert(
            @Parameter(description = "Price alert creation request", required = true)
            @Valid @RequestBody CreatePriceAlertRequest request) {
        if (!priceAlertEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        PriceAlertResponse response = priceAlertService.createPriceAlert(request);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * GET /api/v2/price-alerts - List user's price alerts
     */
    @Operation(
            summary = "List user's price alerts",
            description = "Retrieves all price alerts for a user, filtered by email and optionally userId"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price alerts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PriceAlertListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email or userId required"),
            @ApiResponse(responseCode = "404", description = "Feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<PriceAlertListResponse> getPriceAlerts(
            @Parameter(description = "User email", required = true)
            @RequestParam(required = false) String email,
            @Parameter(description = "User ID (optional)")
            @RequestParam(required = false) String userId) {
        if (!priceAlertEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        PriceAlertListResponse response = priceAlertService.getPriceAlerts(email, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v2/price-alerts/{alertId} - Get specific price alert
     */
    @Operation(
            summary = "Get price alert by ID",
            description = "Retrieves price alert details for the specified alert ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price alert found",
                    content = @Content(schema = @Schema(implementation = PriceAlertResponse.class))),
            @ApiResponse(responseCode = "404", description = "Price alert not found or feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{alertId}")
    public ResponseEntity<PriceAlertResponse> getPriceAlert(
            @Parameter(description = "Price alert ID", required = true)
            @PathVariable String alertId) {
        if (!priceAlertEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        PriceAlertResponse response = priceAlertService.getPriceAlert(alertId);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * PUT /api/v2/price-alerts/{alertId} - Update price alert
     */
    @Operation(
            summary = "Update price alert",
            description = "Updates a price alert (target price, notification frequency, status)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price alert updated successfully",
                    content = @Content(schema = @Schema(implementation = PriceAlertResponse.class))),
            @ApiResponse(responseCode = "404", description = "Price alert not found or feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{alertId}")
    public ResponseEntity<PriceAlertResponse> updatePriceAlert(
            @Parameter(description = "Price alert ID", required = true)
            @PathVariable String alertId,
            @Parameter(description = "Price alert update request", required = true)
            @Valid @RequestBody UpdatePriceAlertRequest request) {
        if (!priceAlertEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        PriceAlertResponse response = priceAlertService.updatePriceAlert(alertId, request);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/v2/price-alerts/{alertId} - Cancel/delete price alert
     */
    @Operation(
            summary = "Cancel price alert",
            description = "Cancels (deletes) a price alert"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Price alert cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Price alert not found or feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deletePriceAlert(
            @Parameter(description = "Price alert ID", required = true)
            @PathVariable String alertId) {
        if (!priceAlertEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        priceAlertService.deletePriceAlert(alertId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    /**
     * GET /api/v2/price-alerts/{alertId}/history - Get price history for alert's product
     */
    @Operation(
            summary = "Get price history for product",
            description = "Retrieves price history for the product associated with the alert"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PriceHistoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Price alert not found or feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{alertId}/history")
    public ResponseEntity<List<PriceHistoryResponse>> getPriceHistory(
            @Parameter(description = "Price alert ID", required = true)
            @PathVariable String alertId) {
        if (!priceAlertEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        PriceAlertResponse alert = priceAlertService.getPriceAlert(alertId);
        if (alert == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        List<PriceHistoryResponse> history = priceAlertService.getPriceHistory(alert.getProductId());
        return ResponseEntity.ok(history);
    }
}

