package com.example.ecompoc.loyalty.controller;

import com.example.ecompoc.loyalty.dto.*;
import com.example.ecompoc.loyalty.service.LoyaltyPointsService;
import com.example.ecompoc.loyalty.service.LoyaltyReferralService;
import com.example.ecompoc.loyalty.service.LoyaltyService;
import com.example.ecompoc.loyalty.service.LoyaltyTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST Controller for loyalty program endpoints
 */
@RestController
@RequestMapping("/api/loyalty")
@Tag(name = "Loyalty", description = "Loyalty program API endpoints")
public class LoyaltyController {
    
    private final LoyaltyService loyaltyService;
    private final LoyaltyPointsService pointsService;
    private final LoyaltyTierService tierService;
    private final LoyaltyReferralService referralService;
    
    public LoyaltyController(LoyaltyService loyaltyService,
                            LoyaltyPointsService pointsService,
                            LoyaltyTierService tierService,
                            LoyaltyReferralService referralService) {
        this.loyaltyService = loyaltyService;
        this.pointsService = pointsService;
        this.tierService = tierService;
        this.referralService = referralService;
    }
    
    /**
     * GET /api/loyalty/balance - Get current balance and tier
     */
    @Operation(
        summary = "Get loyalty account balance",
        description = "Retrieves current point balance and tier information for the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
            content = @Content(schema = @Schema(implementation = LoyaltyAccountResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loyalty account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/balance")
    public ResponseEntity<LoyaltyAccountResponse> getBalance(
        @Parameter(description = "User ID", required = true)
        @RequestParam String userId) {
        LoyaltyAccountResponse account = loyaltyService.getAccount(userId);
        return ResponseEntity.ok(account);
    }
    
    /**
     * GET /api/loyalty/dashboard - Get complete dashboard data
     */
    @Operation(
        summary = "Get loyalty dashboard",
        description = "Retrieves complete loyalty dashboard data including balance, tier, history, and benefits"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully",
            content = @Content(schema = @Schema(implementation = LoyaltyDashboardResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loyalty account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<LoyaltyDashboardResponse> getDashboard(
        @Parameter(description = "User ID", required = true)
        @RequestParam String userId) {
        LoyaltyDashboardResponse dashboard = loyaltyService.getDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * GET /api/loyalty/history - Get transaction history
     */
    @Operation(
        summary = "Get transaction history",
        description = "Retrieves paginated transaction history for the user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "History retrieved successfully",
            content = @Content(schema = @Schema(implementation = LoyaltyHistoryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loyalty account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/history")
    public ResponseEntity<LoyaltyHistoryResponse> getHistory(
        @Parameter(description = "User ID", required = true)
        @RequestParam String userId,
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        LoyaltyHistoryResponse history = loyaltyService.getHistory(userId, pageable);
        return ResponseEntity.ok(history);
    }
    
    /**
     * POST /api/loyalty/redeem - Redeem points for discount
     */
    @Operation(
        summary = "Redeem points",
        description = "Redeems loyalty points for order discount"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Points redeemed successfully",
            content = @Content(schema = @Schema(implementation = RedeemPointsResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid redemption request"),
        @ApiResponse(responseCode = "404", description = "Loyalty account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/redeem")
    public ResponseEntity<RedeemPointsResponse> redeemPoints(
        @Parameter(description = "User ID", required = true)
        @RequestParam String userId,
        @Parameter(description = "Redemption request", required = true)
        @Valid @RequestBody RedeemPointsRequest request) {
        
        // Validate redemption
        pointsService.validateRedemption(userId, request.getPoints(), request.getOrderTotal());
        
        // Process redemption
        LoyaltyPointsService.RedeemResult result = pointsService.redeemPoints(
            userId, request.getPoints(), request.getOrderId(), request.getOrderTotal());
        
        RedeemPointsResponse response = new RedeemPointsResponse();
        response.setPointsRedeemed(result.getPointsRedeemed());
        response.setDiscountAmount(result.getDiscountAmount());
        response.setRemainingBalance(result.getRemainingBalance());
        response.setMessage(String.format("Successfully redeemed %d points for $%.2f discount",
            result.getPointsRedeemed(), result.getDiscountAmount()));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/loyalty/referral-code - Get user's referral code
     */
    @Operation(
        summary = "Get referral code",
        description = "Retrieves the user's unique referral code and shareable link"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Referral code retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReferralCodeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loyalty account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/referral-code")
    public ResponseEntity<ReferralCodeResponse> getReferralCode(
        @Parameter(description = "User ID", required = true)
        @RequestParam String userId) {
        String referralCode = referralService.getReferralCode(userId);
        
        ReferralCodeResponse response = new ReferralCodeResponse();
        response.setReferralCode(referralCode);
        response.setReferralLink("https://example.com/ref/" + referralCode);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/loyalty/enroll - Manual enrollment
     */
    @Operation(
        summary = "Enroll in loyalty program",
        description = "Manually enrolls a user in the loyalty program"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Enrolled successfully",
            content = @Content(schema = @Schema(implementation = LoyaltyAccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Already enrolled or invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/enroll")
    public ResponseEntity<LoyaltyAccountResponse> enroll(
        @Parameter(description = "User ID", required = true)
        @RequestParam String userId,
        @Parameter(description = "Enrollment request with optional referral code")
        @RequestBody(required = false) EnrollmentRequest request) {
        
        String referralCode = request != null ? request.getReferralCode() : null;
        LoyaltyAccountResponse account = loyaltyService.enrollUser(
            userId, com.example.ecompoc.loyalty.model.EnrollmentSource.MANUAL, referralCode);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
    
    /**
     * POST /api/loyalty/opt-out - Opt out of loyalty program
     */
    @Operation(
        summary = "Opt out of loyalty program",
        description = "Allows user to opt out of the loyalty program"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Opted out successfully"),
        @ApiResponse(responseCode = "404", description = "Loyalty account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/opt-out")
    public ResponseEntity<Void> optOut(
        @Parameter(description = "User ID", required = true)
        @RequestParam String userId) {
        loyaltyService.optOut(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * GET /api/loyalty/tier-benefits - Get tier benefits information
     */
    @Operation(
        summary = "Get tier benefits",
        description = "Retrieves benefits information for the user's current tier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tier benefits retrieved successfully",
            content = @Content(schema = @Schema(implementation = TierBenefitsResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loyalty account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/tier-benefits")
    public ResponseEntity<TierBenefitsResponse> getTierBenefits(
        @Parameter(description = "User ID", required = true)
        @RequestParam String userId) {
        com.example.ecompoc.loyalty.dto.LoyaltyAccountResponse account = loyaltyService.getAccount(userId);
        com.example.ecompoc.loyalty.model.LoyaltyTier tier = com.example.ecompoc.loyalty.model.LoyaltyTier.valueOf(account.getCurrentTier());
        
        TierBenefitsResponse response = new TierBenefitsResponse();
        response.setTier(tier.name());
        response.setMultiplier(tierService.getTierMultiplier(tier));
        response.setBenefits(tierService.getTierBenefits(tier));
        response.setPointsToNextTier(tierService.getPointsToNextTier(
            account.getLifetimePointsEarned(), tier));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/loyalty/referral-stats - Get referral statistics
     */
    @Operation(
        summary = "Get referral statistics",
        description = "Retrieves referral statistics for the user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReferralStatisticsResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loyalty account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/referral-stats")
    public ResponseEntity<ReferralStatisticsResponse> getReferralStats(
        @Parameter(description = "User ID", required = true)
        @RequestParam String userId) {
        LoyaltyReferralService.ReferralStatistics stats = referralService.getReferralStatistics(userId);
        
        ReferralStatisticsResponse response = new ReferralStatisticsResponse();
        response.setTotalReferrals(stats.getTotalReferrals());
        response.setSuccessfulReferrals(stats.getSuccessfulReferrals());
        response.setPointsEarned(stats.getPointsEarned());
        response.setSuccessRate(stats.getSuccessRate());
        
        return ResponseEntity.ok(response);
    }
}
