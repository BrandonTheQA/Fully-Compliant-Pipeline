package com.example.ecompoc.abandonedcart.controller;

import com.example.ecompoc.abandonedcart.dto.AbandonedCartRequest;
import com.example.ecompoc.abandonedcart.dto.AbandonedCartResponse;
import com.example.ecompoc.abandonedcart.dto.CartRestorationResponse;
import com.example.ecompoc.abandonedcart.service.AbandonedCartEmailService;
import com.example.ecompoc.abandonedcart.service.AbandonedCartService;
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

/**
 * REST Controller for abandoned cart management endpoints
 */
@RestController
@RequestMapping("/api/abandoned-carts")
@Tag(name = "Abandoned Carts", description = "Abandoned cart recovery API endpoints")
public class AbandonedCartController {
    
    private final AbandonedCartService abandonedCartService;
    private final AbandonedCartEmailService emailService;
    
    @Value("${abandoned-cart.enabled:false}")
    private boolean abandonedCartEnabled;
    
    public AbandonedCartController(AbandonedCartService abandonedCartService,
                                   AbandonedCartEmailService emailService) {
        this.abandonedCartService = abandonedCartService;
        this.emailService = emailService;
    }
    
    /**
     * POST /api/abandoned-carts - Save an abandoned cart
     */
    @Operation(
            summary = "Save an abandoned cart",
            description = "Saves an abandoned cart for recovery email processing"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Abandoned cart saved successfully",
                    content = @Content(schema = @Schema(implementation = AbandonedCartResponse.class))),
            @ApiResponse(responseCode = "404", description = "Feature disabled"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<AbandonedCartResponse> saveAbandonedCart(
            @Parameter(description = "Abandoned cart request", required = true)
            @Valid @RequestBody AbandonedCartRequest request) {
        if (!abandonedCartEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        AbandonedCartResponse response = abandonedCartService.saveAbandonedCart(request);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * GET /api/abandoned-carts/{id} - Get abandoned cart by ID
     */
    @Operation(
            summary = "Get abandoned cart by ID",
            description = "Retrieves abandoned cart details for the specified ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Abandoned cart found",
                    content = @Content(schema = @Schema(implementation = AbandonedCartResponse.class))),
            @ApiResponse(responseCode = "404", description = "Abandoned cart not found or feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AbandonedCartResponse> getAbandonedCart(
            @Parameter(description = "Abandoned cart ID", required = true)
            @PathVariable String id) {
        if (!abandonedCartEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        AbandonedCartResponse response = abandonedCartService.getAbandonedCart(id);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/abandoned-carts/{id}/restore - Restore abandoned cart and apply discount
     */
    @Operation(
            summary = "Restore abandoned cart",
            description = "Restores an abandoned cart with discount applied"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart restored successfully",
                    content = @Content(schema = @Schema(implementation = CartRestorationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Abandoned cart not found or feature disabled"),
            @ApiResponse(responseCode = "400", description = "Cart expired or already recovered"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/restore")
    public ResponseEntity<CartRestorationResponse> restoreCart(
            @Parameter(description = "Abandoned cart ID", required = true)
            @PathVariable String id) {
        if (!abandonedCartEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        AbandonedCartResponse abandonedCart = abandonedCartService.restoreCart(id);
        if (abandonedCart == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        CartRestorationResponse response = new CartRestorationResponse();
        response.setSuccess(true);
        response.setMessage("Cart restored successfully");
        
        // Convert cart items
        if (abandonedCart.getCartItems() != null) {
            java.util.List<CartRestorationResponse.CartItemResponse> cartItems = 
                abandonedCart.getCartItems().stream()
                    .map(item -> {
                        CartRestorationResponse.CartItemResponse cartItem = 
                            new CartRestorationResponse.CartItemResponse();
                        cartItem.setProductId(item.getProductId());
                        cartItem.setProductName(item.getProductName());
                        cartItem.setQuantity(item.getQuantity());
                        cartItem.setPrice(item.getPrice());
                        return cartItem;
                    })
                    .collect(java.util.stream.Collectors.toList());
            response.setCartItems(cartItems);
        }
        
        response.setCartTotal(abandonedCart.getCartTotal());
        response.setDiscountCode(abandonedCart.getDiscountCode());
        response.setDiscountType(abandonedCart.getDiscountType());
        response.setDiscountValue(abandonedCart.getDiscountValue());
        
        // Calculate final total with discount
        Double finalTotal = abandonedCart.getCartTotal();
        if (abandonedCart.getDiscountValue() != null) {
            finalTotal = finalTotal - abandonedCart.getDiscountValue();
        }
        response.setFinalTotal(Math.max(0.0, finalTotal));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/abandoned-carts/email/{emailId}/track/open - Track email open
     */
    @Operation(
            summary = "Track email open",
            description = "Tracks when a recovery email is opened"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email open tracked"),
            @ApiResponse(responseCode = "404", description = "Email not found or feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/email/{emailId}/track/open")
    public ResponseEntity<Void> trackEmailOpen(
            @Parameter(description = "Email ID", required = true)
            @PathVariable String emailId) {
        if (!abandonedCartEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        emailService.trackEmailOpen(emailId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * GET /api/abandoned-carts/email/{emailId}/track/click - Track email click and redirect
     */
    @Operation(
            summary = "Track email click and redirect",
            description = "Tracks when a recovery email link is clicked and redirects to cart restoration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirect to cart restoration"),
            @ApiResponse(responseCode = "404", description = "Email not found or feature disabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/email/{emailId}/track/click")
    public ResponseEntity<Void> trackEmailClick(
            @Parameter(description = "Email ID", required = true)
            @PathVariable String emailId,
            @Parameter(description = "Abandoned cart ID", required = true)
            @RequestParam String cartId) {
        if (!abandonedCartEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        emailService.trackEmailClick(emailId);
        
        // Redirect to cart restoration (in a real implementation, this would redirect to frontend)
        return ResponseEntity.status(HttpStatus.FOUND)
            .header("Location", "/api/abandoned-carts/" + cartId + "/restore")
            .build();
    }
}

