package com.example.ecompoc.wishlist.controller;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.wishlist.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WishlistControllerTest {

    @Mock
    private WishlistService wishlistService;

    @InjectMocks
    private WishlistController wishlistController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getWishlist_ShouldReturnListOfProducts() {
        String userId = "user1";
        Product product = new Product();
        product.setId("prod1");
        List<Product> expectedProducts = Collections.singletonList(product);

        when(wishlistService.getWishlist(userId)).thenReturn(expectedProducts);

        ResponseEntity<List<Product>> response = wishlistController.getWishlist(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedProducts, response.getBody());
        verify(wishlistService).getWishlist(userId);
    }

    @Test
    void addItem_ShouldReturnOk_WhenServiceSucceeds() {
        String userId = "user1";
        String productId = "prod1";

        doNothing().when(wishlistService).addItem(userId, productId);

        ResponseEntity<Void> response = wishlistController.addItem(userId, productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(wishlistService).addItem(userId, productId);
    }

    @Test
    void addItem_ShouldReturnNotFound_WhenServiceThrowsException() {
        String userId = "user1";
        String productId = "prod1";

        doThrow(new RuntimeException("Product not found")).when(wishlistService).addItem(userId, productId);

        ResponseEntity<Void> response = wishlistController.addItem(userId, productId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(wishlistService).addItem(userId, productId);
    }

    @Test
    void removeItem_ShouldReturnOk() {
        String userId = "user1";
        String productId = "prod1";

        doNothing().when(wishlistService).removeItem(userId, productId);

        ResponseEntity<Void> response = wishlistController.removeItem(userId, productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(wishlistService).removeItem(userId, productId);
    }
}
