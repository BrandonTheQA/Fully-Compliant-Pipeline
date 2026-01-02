package com.example.ecompoc.returns.controller;

import com.example.ecompoc.order.dto.OrderResponse;
import com.example.ecompoc.returns.dto.*;
import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.model.Return;
import com.example.ecompoc.returns.repository.ReturnRepository;
import com.example.ecompoc.returns.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * REST Controller for customer-facing return management endpoints
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Returns", description = "Return management API endpoints")
public class ReturnController {
    
    private final ReturnRequestService returnRequestService;
    private final ReturnApprovalService returnApprovalService;
    private final ExchangeService exchangeService;
    private final ReturnPolicyService returnPolicyService;
    private final ReturnShippingService returnShippingService;
    private final ReturnRepository returnRepository;
    
    public ReturnController(ReturnRequestService returnRequestService,
                           ReturnApprovalService returnApprovalService,
                           ExchangeService exchangeService,
                           ReturnPolicyService returnPolicyService,
                           ReturnShippingService returnShippingService,
                           ReturnRepository returnRepository) {
        this.returnRequestService = returnRequestService;
        this.returnApprovalService = returnApprovalService;
        this.exchangeService = exchangeService;
        this.returnPolicyService = returnPolicyService;
        this.returnShippingService = returnShippingService;
        this.returnRepository = returnRepository;
    }
    
    /**
     * POST /api/returns - Create return request
     */
    @Operation(
        summary = "Create a return request",
        description = "Creates a new return request with RMA number generation"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Return request created successfully",
            content = @Content(schema = @Schema(implementation = ReturnResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/returns")
    public ResponseEntity<ReturnResponse> createReturnRequest(
        @Parameter(description = "Return creation request", required = true)
        @Valid @RequestBody CreateReturnRequest request) {
        ReturnResponse returnResponse = returnRequestService.createReturnRequest(request);
        
        // Trigger automatic approval processing
        try {
            Return returnEntity = returnRepository.findById(returnResponse.getReturnId()).orElse(null);
            if (returnEntity != null) {
                returnApprovalService.processAutomaticApproval(returnEntity);
                // Refresh response after approval processing
                returnResponse = returnRequestService.getReturnById(returnResponse.getReturnId());
            }
        } catch (Exception e) {
            // Log but don't fail the request
            org.slf4j.LoggerFactory.getLogger(ReturnController.class)
                .warn("Failed to process automatic approval: {}", e.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(returnResponse);
    }
    
    /**
     * GET /api/returns/{rmaNumber} - Get return by RMA number (guest access)
     */
    @Operation(
        summary = "Get return by RMA number",
        description = "Retrieves return details using RMA number (accessible without login)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return found",
            content = @Content(schema = @Schema(implementation = ReturnResponse.class))),
        @ApiResponse(responseCode = "404", description = "Return not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/returns/rma/{rmaNumber}")
    public ResponseEntity<ReturnResponse> getReturnByRMA(
        @Parameter(description = "RMA number", required = true, example = "RMA-20241217-12345")
        @PathVariable String rmaNumber) {
        ReturnResponse returnResponse = returnRequestService.getReturnByRMA(rmaNumber);
        return ResponseEntity.ok(returnResponse);
    }
    
    /**
     * GET /api/returns/user/{userId} - Get user's returns
     */
    @Operation(
        summary = "Get user returns",
        description = "Retrieves all returns for the specified user ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReturnResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/returns/user/{userId}")
    public ResponseEntity<List<ReturnResponse>> getUserReturns(
        @Parameter(description = "User ID", required = true, example = "user123")
        @PathVariable String userId) {
        List<ReturnResponse> returns = returnRequestService.getUserReturns(userId);
        return ResponseEntity.ok(returns);
    }
    
    /**
     * GET /api/returns/{returnId}/tracking - Get return tracking information
     */
    @Operation(
        summary = "Get return tracking information",
        description = "Retrieves comprehensive tracking information for the specified return ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tracking information found",
            content = @Content(schema = @Schema(implementation = ReturnTrackingResponse.class))),
        @ApiResponse(responseCode = "404", description = "Return not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/returns/{returnId}/tracking")
    public ResponseEntity<ReturnTrackingResponse> getReturnTracking(
        @Parameter(description = "Return ID", required = true)
        @PathVariable String returnId) {
        ReturnResponse returnResponse = returnRequestService.getReturnById(returnId);
        
        ReturnTrackingResponse tracking = new ReturnTrackingResponse();
        tracking.setReturnId(returnResponse.getReturnId());
        tracking.setRmaNumber(returnResponse.getRmaNumber());
        tracking.setStatus(returnResponse.getStatus());
        tracking.setReturnType(returnResponse.getReturnType());
        tracking.setReturnTrackingNumber(returnResponse.getReturnTrackingNumber());
        tracking.setReturnCarrier(returnResponse.getReturnCarrier());
        tracking.setReturnLabelUrl(returnResponse.getReturnLabelUrl());
        tracking.setRefundAmount(returnResponse.getRefundAmount());
        tracking.setRefundMethod(returnResponse.getRefundMethod());
        tracking.setRefundDate(returnResponse.getRefundDate());
        
        // Calculate estimated refund date (1-3 business days after received)
        if (returnResponse.getStatus().equals("RECEIVED") || 
            returnResponse.getStatus().equals("PROCESSING_REFUND")) {
            String estimatedDate = LocalDateTime.now().plusDays(2)
                .format(DateTimeFormatter.ISO_DATE);
            tracking.setEstimatedRefundDate(estimatedDate);
        }
        
        tracking.setStatusHistory(returnResponse.getStatusHistory());
        tracking.setItems(returnResponse.getItems());
        
        return ResponseEntity.ok(tracking);
    }
    
    /**
     * GET /api/returns/policy - Get return policy
     */
    @Operation(
        summary = "Get return policy",
        description = "Retrieves the current return policy configuration"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return policy retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReturnPolicyResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/returns/policy")
    public ResponseEntity<ReturnPolicyResponse> getReturnPolicy() {
        com.example.ecompoc.returns.model.ReturnPolicyConfig policy = 
            returnPolicyService.getActivePolicy();
        
        ReturnPolicyResponse response = new ReturnPolicyResponse();
        response.setReturnWindowDays(policy.getReturnWindowDays());
        response.setRestockingFeePercentage(policy.getRestockingFeePercentage());
        response.setFreeReturnThreshold(policy.getFreeReturnThreshold());
        response.setAutoApproveThreshold(policy.getAutoApproveThreshold());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/returns/{returnId}/exchange - Create exchange request
     */
    @Operation(
        summary = "Create exchange request",
        description = "Creates an exchange order for a return"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Exchange order created successfully",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Return not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/returns/{returnId}/exchange")
    public ResponseEntity<OrderResponse> createExchange(
        @Parameter(description = "Return ID", required = true)
        @PathVariable String returnId,
        @Parameter(description = "Exchange request", required = true)
        @Valid @RequestBody ExchangeRequest exchangeRequest) {
        OrderResponse exchangeOrder = exchangeService.processExchange(returnId, exchangeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(exchangeOrder);
    }
}

