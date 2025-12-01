package com.example.ecompoc.wishlist.service;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.wishlist.model.WishlistItem;
import com.example.ecompoc.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getWishlist_ShouldReturnProducts() {
        String userId = "user1";
        String productId = "prod1";
        WishlistItem item = new WishlistItem(userId, productId);
        Product product = new Product();
        product.setId(productId);

        when(wishlistRepository.findByUserId(userId)).thenReturn(Collections.singletonList(item));
        when(productRepository.findAllById(Collections.singletonList(productId))).thenReturn(Collections.singletonList(product));

        List<Product> result = wishlistService.getWishlist(userId);

        assertEquals(1, result.size());
        assertEquals(productId, result.get(0).getId());
        verify(wishlistRepository).findByUserId(userId);
        verify(productRepository).findAllById(Collections.singletonList(productId));
    }

    @Test
    void addItem_ShouldAddItem_WhenProductExistsAndNotAlreadyInWishlist() {
        String userId = "user1";
        String productId = "prod1";

        when(wishlistRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());
        when(productRepository.existsById(productId)).thenReturn(true);

        wishlistService.addItem(userId, productId);

        verify(wishlistRepository).save(any(WishlistItem.class));
    }

    @Test
    void addItem_ShouldNotAddItem_WhenAlreadyInWishlist() {
        String userId = "user1";
        String productId = "prod1";
        WishlistItem existingItem = new WishlistItem(userId, productId);

        when(wishlistRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(existingItem));

        wishlistService.addItem(userId, productId);

        verify(wishlistRepository, never()).save(any(WishlistItem.class));
    }

    @Test
    void addItem_ShouldThrowException_WhenProductDoesNotExist() {
        String userId = "user1";
        String productId = "prod1";

        when(wishlistRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> wishlistService.addItem(userId, productId));
        verify(wishlistRepository, never()).save(any(WishlistItem.class));
    }

    @Test
    void removeItem_ShouldCallDelete() {
        String userId = "user1";
        String productId = "prod1";

        wishlistService.removeItem(userId, productId);

        verify(wishlistRepository).deleteByUserIdAndProductId(userId, productId);
    }
}
