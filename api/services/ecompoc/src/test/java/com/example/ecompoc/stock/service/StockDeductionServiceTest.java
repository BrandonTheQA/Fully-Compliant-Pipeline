package com.example.ecompoc.stock.service;

import com.example.ecompoc.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("StockDeductionService Tests")
class StockDeductionServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    private StockDeductionService deductionService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deductionService = new StockDeductionService(productRepository);
        
        // Set feature toggle to enabled for tests
        ReflectionTestUtils.setField(deductionService, "stockManagementEnabled", true);
    }
    
    @Test
    @DisplayName("Should deduct stock successfully")
    void shouldDeductStockSuccessfully() {
        // Given
        String productId = "product-1";
        Integer quantity = 5;
        
        when(productRepository.updateQuantity(productId, -quantity)).thenReturn(1);
        
        // When
        deductionService.deductStock(productId, quantity);
        
        // Then
        verify(productRepository).updateQuantity(productId, -quantity);
    }
    
    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void shouldThrowExceptionWhenInsufficientStock() {
        // Given
        String productId = "product-1";
        Integer quantity = 10;
        com.example.ecompoc.product.model.Product product = 
            new com.example.ecompoc.product.model.Product(productId, "Product", "Description", 10.0, 5, "Category");
        
        when(productRepository.updateQuantity(productId, -quantity)).thenReturn(0);
        when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> 
            deductionService.deductStock(productId, quantity));
        
        verify(productRepository).updateQuantity(productId, -quantity);
        verify(productRepository).findById(productId);
    }
    
    @Test
    @DisplayName("Should throw exception for invalid product ID")
    void shouldThrowExceptionForInvalidProductId() {
        // Given
        String productId = null;
        Integer quantity = 5;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            deductionService.deductStock(productId, quantity));
        
        verify(productRepository, never()).updateQuantity(anyString(), anyInt());
    }
    
    @Test
    @DisplayName("Should throw exception for invalid quantity")
    void shouldThrowExceptionForInvalidQuantity() {
        // Given
        String productId = "product-1";
        Integer quantity = -5;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            deductionService.deductStock(productId, quantity));
        
        verify(productRepository, never()).updateQuantity(anyString(), anyInt());
    }
    
    @Test
    @DisplayName("Should restore stock successfully")
    void shouldRestoreStockSuccessfully() {
        // Given
        String productId = "product-1";
        Integer quantity = 5;
        
        when(productRepository.updateQuantity(productId, quantity)).thenReturn(1);
        
        // When
        deductionService.restoreStock(productId, quantity);
        
        // Then
        verify(productRepository).updateQuantity(productId, quantity);
    }
    
    @Test
    @DisplayName("Should throw exception when product not found for restore")
    void shouldThrowExceptionWhenProductNotFoundForRestore() {
        // Given
        String productId = "non-existent";
        Integer quantity = 5;
        
        when(productRepository.updateQuantity(productId, quantity)).thenReturn(0);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            deductionService.restoreStock(productId, quantity));
    }
    
    @Test
    @DisplayName("Should verify stock availability correctly")
    void shouldVerifyStockAvailabilityCorrectly() {
        // Given
        String productId = "product-1";
        Integer requestedQuantity = 5;
        
        when(productRepository.findById(productId)).thenReturn(
            java.util.Optional.of(new com.example.ecompoc.product.model.Product(
                productId, "Product", "Description", 10.0, 10, "Category")));
        
        // When
        boolean available = deductionService.verifyStockAvailability(productId, requestedQuantity);
        
        // Then
        assertTrue(available);
    }
    
    @Test
    @DisplayName("Should return false when insufficient stock")
    void shouldReturnFalseWhenInsufficientStock() {
        // Given
        String productId = "product-1";
        Integer requestedQuantity = 15;
        
        when(productRepository.findById(productId)).thenReturn(
            java.util.Optional.of(new com.example.ecompoc.product.model.Product(
                productId, "Product", "Description", 10.0, 10, "Category")));
        
        // When
        boolean available = deductionService.verifyStockAvailability(productId, requestedQuantity);
        
        // Then
        assertFalse(available);
    }
    
    @Test
    @DisplayName("Should return true when feature is disabled")
    void shouldReturnTrueWhenFeatureDisabled() {
        // Given
        ReflectionTestUtils.setField(deductionService, "stockManagementEnabled", false);
        String productId = "product-1";
        Integer requestedQuantity = 5;
        
        // When
        boolean available = deductionService.verifyStockAvailability(productId, requestedQuantity);
        
        // Then
        assertTrue(available);
        verify(productRepository, never()).findById(anyString());
    }
    
    @Test
    @DisplayName("Should skip deduction when feature is disabled")
    void shouldSkipDeductionWhenFeatureDisabled() {
        // Given
        ReflectionTestUtils.setField(deductionService, "stockManagementEnabled", false);
        String productId = "product-1";
        Integer quantity = 5;
        
        // When
        deductionService.deductStock(productId, quantity);
        
        // Then
        verify(productRepository, never()).updateQuantity(anyString(), anyInt());
    }
}

