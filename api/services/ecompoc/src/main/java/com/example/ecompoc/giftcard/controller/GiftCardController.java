package com.example.ecompoc.giftcard.controller;

import com.example.ecompoc.giftcard.dto.*;
import com.example.ecompoc.giftcard.model.GiftCard;
import com.example.ecompoc.giftcard.repository.GiftCardRepository;
import com.example.ecompoc.giftcard.repository.GiftCardTransactionRepository;
import com.example.ecompoc.giftcard.service.GiftCardEmailService;
import com.example.ecompoc.giftcard.service.GiftCardPurchaseService;
import com.example.ecompoc.giftcard.service.GiftCardRedemptionService;
import com.example.ecompoc.giftcard.service.GiftCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for gift card endpoints
 */
@RestController
@RequestMapping("/api/gift-cards")
@Tag(name = "Gift Cards", description = "Gift card purchase, redemption, and management API endpoints")
public class GiftCardController {
    
    private final GiftCardPurchaseService purchaseService;
    private final GiftCardRedemptionService redemptionService;
    private final GiftCardService giftCardService;
    private final GiftCardEmailService emailService;
    private final GiftCardRepository giftCardRepository;
    private final GiftCardTransactionRepository transactionRepository;
    private final GiftCardMapper mapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    @Autowired
    public GiftCardController(GiftCardPurchaseService purchaseService,
                              GiftCardRedemptionService redemptionService,
                              GiftCardService giftCardService,
                              GiftCardEmailService emailService,
                              GiftCardRepository giftCardRepository,
                              GiftCardTransactionRepository transactionRepository,
                              GiftCardMapper mapper) {
        this.purchaseService = purchaseService;
        this.redemptionService = redemptionService;
        this.giftCardService = giftCardService;
        this.emailService = emailService;
        this.giftCardRepository = giftCardRepository;
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
    }
    
    /**
     * POST /api/gift-cards/purchase - Purchase gift card(s)
     */
    @Operation(
        summary = "Purchase gift card(s)",
        description = "Purchase one or more gift cards with optional recipient information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gift card(s) purchased successfully",
            content = @Content(schema = @Schema(implementation = PurchaseGiftCardResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/purchase")
    public ResponseEntity<PurchaseGiftCardResponse> purchaseGiftCard(
        @Valid @RequestBody PurchaseGiftCardRequest request) {
        
        // Parse scheduled delivery date if provided
        LocalDateTime scheduledDeliveryDate = null;
        if (request.getScheduledDeliveryDate() != null && !request.getScheduledDeliveryDate().isEmpty()) {
            scheduledDeliveryDate = LocalDateTime.parse(request.getScheduledDeliveryDate(), DATE_FORMATTER);
        }
        
        // Purchase gift cards
        List<GiftCard> giftCards;
        if (request.getQuantity() != null && request.getQuantity() > 1) {
            giftCards = purchaseService.purchaseMultipleGiftCards(
                request.getAmount(),
                request.getQuantity(),
                request.getPurchaserId(),
                request.getPurchaserEmail(),
                request.getRecipientEmail(),
                request.getRecipientName(),
                request.getPersonalMessage(),
                request.getDesign(),
                scheduledDeliveryDate
            );
        } else {
            GiftCard giftCard = purchaseService.purchaseGiftCard(
                request.getAmount(),
                request.getPurchaserId(),
                request.getPurchaserEmail(),
                request.getRecipientEmail(),
                request.getRecipientName(),
                request.getPersonalMessage(),
                request.getDesign(),
                scheduledDeliveryDate
            );
            giftCards = List.of(giftCard);
        }
        
        // Build response
        PurchaseGiftCardResponse response = new PurchaseGiftCardResponse();
        response.setGiftCards(mapper.toResponseList(giftCards));
        response.setTotalAmount(request.getAmount().multiply(BigDecimal.valueOf(giftCards.size())));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/gift-cards/redeem - Redeem gift card (standalone)
     */
    @Operation(
        summary = "Redeem gift card",
        description = "Redeem a gift card for a specific amount"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gift card redeemed successfully",
            content = @Content(schema = @Schema(implementation = RedeemGiftCardResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance"),
        @ApiResponse(responseCode = "404", description = "Gift card not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/redeem")
    public ResponseEntity<RedeemGiftCardResponse> redeemGiftCard(
        @Valid @RequestBody RedeemGiftCardRequest request) {
        
        GiftCard giftCard = redemptionService.redeemGiftCard(
            request.getCode(), request.getRedemptionAmount());
        
        RedeemGiftCardResponse response = new RedeemGiftCardResponse();
        response.setSuccess(true);
        response.setRemainingBalance(giftCard.getBalance());
        response.setAppliedAmount(request.getRedemptionAmount());
        response.setGiftCard(mapper.toResponse(giftCard));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/gift-cards/apply - Apply gift card to order (checkout)
     */
    @Operation(
        summary = "Apply gift card to order",
        description = "Apply a gift card to an order during checkout"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gift card applied successfully",
            content = @Content(schema = @Schema(implementation = ApplyGiftCardResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance"),
        @ApiResponse(responseCode = "404", description = "Gift card not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/apply")
    public ResponseEntity<ApplyGiftCardResponse> applyGiftCard(
        @Valid @RequestBody ApplyGiftCardRequest request) {
        
        Map<String, Object> result = redemptionService.applyGiftCardToOrder(
            request.getCode(), request.getOrderTotal(), null);
        
        ApplyGiftCardResponse response = new ApplyGiftCardResponse();
        response.setAppliedAmount((BigDecimal) result.get("appliedAmount"));
        response.setRemainingBalance((BigDecimal) result.get("remainingBalance"));
        response.setOrderTotal(request.getOrderTotal().subtract(response.getAppliedAmount()));
        response.setGiftCard(mapper.toResponse((GiftCard) result.get("giftCard")));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/gift-cards/balance/{code} - Check balance by code
     */
    @Operation(
        summary = "Check gift card balance",
        description = "Check the balance and status of a gift card by code"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
            content = @Content(schema = @Schema(implementation = BalanceInquiryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Gift card not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/balance/{code}")
    public ResponseEntity<BalanceInquiryResponse> checkBalance(
        @Parameter(description = "Gift card code", required = true)
        @PathVariable String code) {
        
        GiftCard giftCard = giftCardService.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Gift card not found: " + code));
        
        // Check expiration
        giftCardService.checkExpiration(giftCard);
        
        BalanceInquiryResponse response = new BalanceInquiryResponse();
        response.setCode(giftCard.getCode());
        response.setBalance(giftCard.getBalance());
        response.setAmount(giftCard.getAmount());
        response.setStatus(giftCard.getStatus() != null ? giftCard.getStatus().name() : null);
        response.setExpirationDate(giftCard.getExpirationDate() != null ? 
            giftCard.getExpirationDate().format(DATE_FORMATTER) : null);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/gift-cards/user/{userId} - Get user's gift cards
     */
    @Operation(
        summary = "Get user's gift cards",
        description = "Retrieve all gift cards associated with a user account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gift cards retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GiftCardResponse>> getUserGiftCards(
        @Parameter(description = "User ID", required = true)
        @PathVariable String userId) {
        
        List<GiftCard> giftCards = giftCardRepository.findByPurchaserId(userId);
        return ResponseEntity.ok(mapper.toResponseList(giftCards));
    }
    
    /**
     * GET /api/gift-cards/{giftCardId} - Get gift card details
     */
    @Operation(
        summary = "Get gift card details",
        description = "Retrieve detailed information about a specific gift card"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gift card retrieved successfully",
            content = @Content(schema = @Schema(implementation = GiftCardResponse.class))),
        @ApiResponse(responseCode = "404", description = "Gift card not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{giftCardId}")
    public ResponseEntity<GiftCardResponse> getGiftCardDetails(
        @Parameter(description = "Gift card ID", required = true)
        @PathVariable String giftCardId) {
        
        GiftCard giftCard = giftCardService.findById(giftCardId)
            .orElseThrow(() -> new IllegalArgumentException("Gift card not found: " + giftCardId));
        
        return ResponseEntity.ok(mapper.toResponse(giftCard));
    }
    
    /**
     * GET /api/gift-cards/{giftCardId}/transactions - Get transaction history
     */
    @Operation(
        summary = "Get gift card transaction history",
        description = "Retrieve transaction history for a specific gift card"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Gift card not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{giftCardId}/transactions")
    public ResponseEntity<List<GiftCardTransactionResponse>> getTransactionHistory(
        @Parameter(description = "Gift card ID", required = true)
        @PathVariable String giftCardId) {
        
        // Verify gift card exists
        giftCardService.findById(giftCardId)
            .orElseThrow(() -> new IllegalArgumentException("Gift card not found: " + giftCardId));
        
        List<com.example.ecompoc.giftcard.model.GiftCardTransaction> transactions = 
            transactionRepository.findByGiftCardIdOrderByCreatedAtDesc(giftCardId);
        
        return ResponseEntity.ok(mapper.toTransactionResponseList(transactions));
    }
    
    /**
     * POST /api/gift-cards/resend/{giftCardId} - Resend gift card email
     */
    @Operation(
        summary = "Resend gift card email",
        description = "Resend the gift card email to the recipient"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email resent successfully"),
        @ApiResponse(responseCode = "404", description = "Gift card not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/resend/{giftCardId}")
    public ResponseEntity<Map<String, String>> resendEmail(
        @Parameter(description = "Gift card ID", required = true)
        @PathVariable String giftCardId) {
        
        GiftCard giftCard = giftCardService.findById(giftCardId)
            .orElseThrow(() -> new IllegalArgumentException("Gift card not found: " + giftCardId));
        
        emailService.resendGiftCardEmail(giftCard);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Gift card email resent successfully");
        response.put("giftCardId", giftCardId);
        
        return ResponseEntity.ok(response);
    }
}
