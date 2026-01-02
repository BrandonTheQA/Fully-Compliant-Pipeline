package com.example.ecompoc.returns.controller;

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
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for admin return management endpoints
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Returns", description = "Admin return management API endpoints")
public class ReturnAdminController {
    
    private final ReturnRepository returnRepository;
    private final ReturnRequestService returnRequestService;
    private final ReturnApprovalService returnApprovalService;
    private final RefundService refundService;
    private final ReturnAnalyticsService returnAnalyticsService;
    
    public ReturnAdminController(ReturnRepository returnRepository,
                                ReturnRequestService returnRequestService,
                                ReturnApprovalService returnApprovalService,
                                RefundService refundService,
                                ReturnAnalyticsService returnAnalyticsService) {
        this.returnRepository = returnRepository;
        this.returnRequestService = returnRequestService;
        this.returnApprovalService = returnApprovalService;
        this.refundService = refundService;
        this.returnAnalyticsService = returnAnalyticsService;
    }
    
    /**
     * GET /api/admin/returns - List returns with search/filter
     */
    @Operation(
        summary = "List returns with filters",
        description = "Retrieves returns with optional filtering by status, user, order, date range"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReturnResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/returns")
    public ResponseEntity<List<ReturnResponse>> getReturns(
        @Parameter(description = "Filter by status")
        @RequestParam(required = false) String status,
        @Parameter(description = "Filter by user ID")
        @RequestParam(required = false) String userId,
        @Parameter(description = "Filter by order ID")
        @RequestParam(required = false) String orderId,
        @Parameter(description = "Search by RMA number")
        @RequestParam(required = false) String rmaNumber) {
        
        List<Return> returns;
        
        if (rmaNumber != null && !rmaNumber.isEmpty()) {
            // Search by RMA number
            Return returnEntity = returnRepository.findByRmaNumber(rmaNumber).orElse(null);
            returns = returnEntity != null ? List.of(returnEntity) : List.of();
        } else if (status != null && !status.isEmpty()) {
            // Filter by status
            ReturnStatus returnStatus = ReturnStatus.valueOf(status.toUpperCase());
            if (userId != null && !userId.isEmpty()) {
                returns = returnRepository.findByUserIdAndStatus(userId, returnStatus);
            } else {
                returns = returnRepository.findByStatus(returnStatus);
            }
        } else if (userId != null && !userId.isEmpty()) {
            // Filter by user
            returns = returnRepository.findByUserId(userId);
        } else if (orderId != null && !orderId.isEmpty()) {
            // Filter by order
            returns = returnRepository.findByOrderId(orderId);
        } else {
            // Get all returns
            returns = returnRepository.findAll();
        }
        
        List<ReturnResponse> responses = returns.stream()
            .map(r -> returnRequestService.getReturnById(r.getReturnId()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * GET /api/admin/returns/{returnId} - Get return details
     */
    @Operation(
        summary = "Get return details",
        description = "Retrieves detailed information for a specific return"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return found",
            content = @Content(schema = @Schema(implementation = ReturnResponse.class))),
        @ApiResponse(responseCode = "404", description = "Return not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/returns/{returnId}")
    public ResponseEntity<ReturnResponse> getReturn(
        @Parameter(description = "Return ID", required = true)
        @PathVariable String returnId) {
        ReturnResponse returnResponse = returnRequestService.getReturnById(returnId);
        return ResponseEntity.ok(returnResponse);
    }
    
    /**
     * POST /api/admin/returns/{returnId}/approve - Approve return
     */
    @Operation(
        summary = "Approve return",
        description = "Manually approves a return request"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return approved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or return cannot be approved"),
        @ApiResponse(responseCode = "404", description = "Return not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/returns/{returnId}/approve")
    public ResponseEntity<Void> approveReturn(
        @Parameter(description = "Return ID", required = true)
        @PathVariable String returnId,
        @Parameter(description = "Approval request with notes")
        @RequestBody(required = false) ApproveReturnRequest request,
        @RequestParam(required = false, defaultValue = "ADMIN") String approvedBy) {
        
        String notes = request != null ? request.getNotes() : "Approved by admin";
        returnApprovalService.approveReturn(returnId, approvedBy, notes);
        return ResponseEntity.ok().build();
    }
    
    /**
     * POST /api/admin/returns/{returnId}/reject - Reject return
     */
    @Operation(
        summary = "Reject return",
        description = "Rejects a return request with a reason"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return rejected successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or return cannot be rejected"),
        @ApiResponse(responseCode = "404", description = "Return not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/returns/{returnId}/reject")
    public ResponseEntity<Void> rejectReturn(
        @Parameter(description = "Return ID", required = true)
        @PathVariable String returnId,
        @Parameter(description = "Rejection reason", required = true)
        @RequestParam String reason,
        @RequestParam(required = false, defaultValue = "ADMIN") String rejectedBy) {
        
        returnApprovalService.rejectReturn(returnId, rejectedBy, reason);
        return ResponseEntity.ok().build();
    }
    
    /**
     * PUT /api/admin/returns/{returnId}/status - Update return status
     */
    @Operation(
        summary = "Update return status",
        description = "Manually updates the status of a return"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Return not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/returns/{returnId}/status")
    public ResponseEntity<Void> updateReturnStatus(
        @Parameter(description = "Return ID", required = true)
        @PathVariable String returnId,
        @Parameter(description = "Status update request", required = true)
        @Valid @RequestBody UpdateReturnStatusRequest request,
        @RequestParam(required = false, defaultValue = "ADMIN") String updatedBy) {
        
        ReturnStatus newStatus = ReturnStatus.valueOf(request.getStatus().toUpperCase());
        returnApprovalService.updateReturnStatus(returnId, newStatus, updatedBy, request.getNotes());
        return ResponseEntity.ok().build();
    }
    
    /**
     * POST /api/admin/returns/{returnId}/received - Mark return as received
     */
    @Operation(
        summary = "Mark return as received",
        description = "Marks a return as received, triggering refund processing"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return marked as received successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or return cannot be marked as received"),
        @ApiResponse(responseCode = "404", description = "Return not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/returns/{returnId}/received")
    public ResponseEntity<Void> markReturnReceived(
        @Parameter(description = "Return ID", required = true)
        @PathVariable String returnId,
        @RequestParam(required = false) String notes,
        @RequestParam(required = false, defaultValue = "ADMIN") String receivedBy) {
        
        returnApprovalService.markReturnReceived(returnId, receivedBy, notes);
        
        // Trigger automatic refund processing
        try {
            refundService.processRefund(returnId, receivedBy);
        } catch (Exception e) {
            // Log but don't fail - refund can be processed later
            org.slf4j.LoggerFactory.getLogger(ReturnAdminController.class)
                .warn("Failed to process refund automatically for return {}: {}", returnId, e.getMessage());
        }
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * POST /api/admin/returns/{returnId}/refund - Process refund manually
     */
    @Operation(
        summary = "Process refund manually",
        description = "Manually triggers refund processing for a return"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or refund cannot be processed"),
        @ApiResponse(responseCode = "404", description = "Return not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/returns/{returnId}/refund")
    public ResponseEntity<Void> processRefund(
        @Parameter(description = "Return ID", required = true)
        @PathVariable String returnId,
        @RequestParam(required = false, defaultValue = "ADMIN") String processedBy) {
        
        refundService.processRefund(returnId, processedBy);
        return ResponseEntity.ok().build();
    }
    
    /**
     * GET /api/admin/returns/analytics - Get return analytics
     */
    @Operation(
        summary = "Get return analytics",
        description = "Retrieves comprehensive return analytics and metrics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReturnAnalyticsResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/returns/analytics")
    public ResponseEntity<ReturnAnalyticsResponse> getAnalytics() {
        ReturnAnalyticsResponse analytics = returnAnalyticsService.getAnalytics();
        return ResponseEntity.ok(analytics);
    }
}

