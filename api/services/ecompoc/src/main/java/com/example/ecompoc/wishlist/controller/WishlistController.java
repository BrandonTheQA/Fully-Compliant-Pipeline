package com.example.ecompoc.wishlist.controller;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@Tag(name = "Wishlist", description = "Wishlist management API endpoints")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user wishlist", description = "Retrieves all products in the user's wishlist")
    public ResponseEntity<List<Product>> getWishlist(@PathVariable String userId) {
        return ResponseEntity.ok(wishlistService.getWishlist(userId));
    }

    @PostMapping("/{userId}/{productId}")
    @Operation(summary = "Add item to wishlist", description = "Adds a product to the user's wishlist")
    public ResponseEntity<Void> addItem(@PathVariable String userId, @PathVariable String productId) {
        try {
            wishlistService.addItem(userId, productId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{userId}/{productId}")
    @Operation(summary = "Remove item from wishlist", description = "Removes a product from the user's wishlist")
    public ResponseEntity<Void> removeItem(@PathVariable String userId, @PathVariable String productId) {
        wishlistService.removeItem(userId, productId);
        return ResponseEntity.ok().build();
    }
}
