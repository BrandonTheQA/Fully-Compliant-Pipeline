package com.example.ecompoc.abandonedcart.integration;

import com.example.ecompoc.abandonedcart.dto.AbandonedCartRequest;
import com.example.ecompoc.abandonedcart.dto.AbandonedCartResponse;
import com.example.ecompoc.abandonedcart.repository.AbandonedCartRepository;
import com.example.ecompoc.abandonedcart.service.AbandonedCartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for abandoned cart functionality
 * CRITICAL: Tests with feature toggle disabled to verify feature is properly gated
 */
@SpringBootTest(properties = {"abandoned-cart.enabled=false"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Abandoned Cart Integration Tests - Feature Disabled")
class AbandonedCartIntegrationTest {
    
    @Autowired
    private AbandonedCartService abandonedCartService;
    
    @Autowired
    private AbandonedCartRepository abandonedCartRepository;
    
    @Test
    @DisplayName("Should not save cart when feature is disabled")
    void shouldNotSaveCartWhenFeatureDisabled() {
        AbandonedCartRequest request = new AbandonedCartRequest();
        request.setUserId("user123");
        request.setEmail("test@example.com");
        request.setCartTotal(50.00);
        request.setShippingRegion("US");
        
        AbandonedCartRequest.CartItemRequest item = new AbandonedCartRequest.CartItemRequest();
        item.setProductId("prod1");
        item.setProductName("Product 1");
        item.setQuantity(1);
        item.setPrice(50.00);
        request.setCartItems(Arrays.asList(item));
        
        AbandonedCartResponse response = abandonedCartService.saveAbandonedCart(request);
        
        assertNull(response, "Service should return null when feature is disabled");
        
        // Verify no cart was saved
        long count = abandonedCartRepository.count();
        assertEquals(0, count, "No carts should be saved when feature is disabled");
    }
    
    @Test
    @DisplayName("Should return null when getting cart with feature disabled")
    void shouldReturnNullWhenGettingCartWithFeatureDisabled() {
        AbandonedCartResponse response = abandonedCartService.getAbandonedCart("test-id");
        
        assertNull(response, "Service should return null when feature is disabled");
    }
    
    @Test
    @DisplayName("Should return null when restoring cart with feature disabled")
    void shouldReturnNullWhenRestoringCartWithFeatureDisabled() {
        AbandonedCartResponse response = abandonedCartService.restoreCart("test-id");
        
        assertNull(response, "Service should return null when feature is disabled");
    }
}

