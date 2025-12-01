package com.example.ecompoc.wishlist.service;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.wishlist.model.WishlistItem;
import com.example.ecompoc.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistRepository wishlistRepository, ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
    }

    public List<Product> getWishlist(String userId) {
        List<WishlistItem> items = wishlistRepository.findByUserId(userId);
        List<String> productIds = items.stream()
                .map(WishlistItem::getProductId)
                .collect(Collectors.toList());
        
        return productRepository.findAllById(productIds);
    }

    public void addItem(String userId, String productId) {
        if (wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            return; // Already in wishlist
        }
        // Verify product exists
        if (productRepository.existsById(productId)) {
            WishlistItem item = new WishlistItem(userId, productId);
            wishlistRepository.save(item);
        } else {
             throw new RuntimeException("Product not found: " + productId);
        }
    }

    public void removeItem(String userId, String productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
