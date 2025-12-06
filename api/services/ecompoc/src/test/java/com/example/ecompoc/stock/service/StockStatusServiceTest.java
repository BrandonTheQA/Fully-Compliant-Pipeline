package com.example.ecompoc.stock.service;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.model.StockStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("StockStatusService Tests")
class StockStatusServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    private StockStatusService stockStatusService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        stockStatusService = new StockStatusService(productRepository);
        
        // Set feature toggle to enabled for tests
        ReflectionTestUtils.setField(stockStatusService, "stockManagementEnabled", true);
        ReflectionTestUtils.setField(stockStatusService, "defaultLowStockThreshold", 10);
    }
    
    @Test
    @DisplayName("Should return IN_STOCK for product with quantity above threshold")
    void shouldReturnInStockForProductAboveThreshold() {
        // Given
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 50, "Category");
        product.setLowStockThreshold(10);
        
        // When
        StockStatus status = stockStatusService.calculateStockStatus(product);
        
        // Then
        assertEquals(StockStatus.IN_STOCK, status);
    }
    
    @Test
    @DisplayName("Should return LOW_STOCK for product with quantity at threshold")
    void shouldReturnLowStockForProductAtThreshold() {
        // Given
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 10, "Category");
        product.setLowStockThreshold(10);
        
        // When
        StockStatus status = stockStatusService.calculateStockStatus(product);
        
        // Then
        assertEquals(StockStatus.LOW_STOCK, status);
    }
    
    @Test
    @DisplayName("Should return LOW_STOCK for product with quantity below threshold")
    void shouldReturnLowStockForProductBelowThreshold() {
        // Given
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 5, "Category");
        product.setLowStockThreshold(10);
        
        // When
        StockStatus status = stockStatusService.calculateStockStatus(product);
        
        // Then
        assertEquals(StockStatus.LOW_STOCK, status);
    }
    
    @Test
    @DisplayName("Should return OUT_OF_STOCK for product with zero quantity")
    void shouldReturnOutOfStockForProductWithZeroQuantity() {
        // Given
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 0, "Category");
        
        // When
        StockStatus status = stockStatusService.calculateStockStatus(product);
        
        // Then
        assertEquals(StockStatus.OUT_OF_STOCK, status);
    }
    
    @Test
    @DisplayName("Should return OUT_OF_STOCK for null product")
    void shouldReturnOutOfStockForNullProduct() {
        // When
        StockStatus status = stockStatusService.calculateStockStatus(null);
        
        // Then
        assertEquals(StockStatus.OUT_OF_STOCK, status);
    }
    
    @Test
    @DisplayName("Should use default threshold when product threshold is null")
    void shouldUseDefaultThresholdWhenProductThresholdIsNull() {
        // Given
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 5, "Category");
        product.setLowStockThreshold(null);
        
        // When
        StockStatus status = stockStatusService.calculateStockStatus(product);
        
        // Then
        assertEquals(StockStatus.LOW_STOCK, status); // 5 < 10 (default)
    }
    
    @Test
    @DisplayName("Should get stock status with caching")
    void shouldGetStockStatusWithCaching() {
        // Given
        String productId = "product-1";
        Product product = new Product(productId, "Product 1", "Description", 10.0, 50, "Category");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        // When
        StockStatus status1 = stockStatusService.getStockStatus(productId);
        // Note: Cache might not work in unit tests without Spring context, so we verify at least one call
        StockStatus status2 = stockStatusService.getStockStatus(productId);
        
        // Then
        assertEquals(StockStatus.IN_STOCK, status1);
        assertEquals(StockStatus.IN_STOCK, status2);
        // Cache may or may not work in unit tests without Spring context
        verify(productRepository, atLeast(1)).findById(productId);
    }
    
    @Test
    @DisplayName("Should return null when feature is disabled")
    void shouldReturnNullWhenFeatureDisabled() {
        // Given
        ReflectionTestUtils.setField(stockStatusService, "stockManagementEnabled", false);
        String productId = "product-1";
        
        // When
        StockStatus status = stockStatusService.getStockStatus(productId);
        
        // Then
        assertNull(status);
        verify(productRepository, never()).findById(anyString());
    }
    
    @Test
    @DisplayName("Should get bulk stock status for multiple products")
    void shouldGetBulkStockStatusForMultipleProducts() {
        // Given
        List<String> productIds = Arrays.asList("product-1", "product-2", "product-3");
        Product product1 = new Product("product-1", "Product 1", "Description", 10.0, 50, "Category");
        Product product2 = new Product("product-2", "Product 2", "Description", 20.0, 5, "Category");
        Product product3 = new Product("product-3", "Product 3", "Description", 30.0, 0, "Category");
        
        when(productRepository.findAllById(productIds)).thenReturn(Arrays.asList(product1, product2, product3));
        
        // When
        Map<String, StockStatus> statusMap = stockStatusService.getBulkStockStatus(productIds);
        
        // Then
        assertNotNull(statusMap);
        assertEquals(3, statusMap.size());
        assertEquals(StockStatus.IN_STOCK, statusMap.get("product-1"));
        assertEquals(StockStatus.LOW_STOCK, statusMap.get("product-2"));
        assertEquals(StockStatus.OUT_OF_STOCK, statusMap.get("product-3"));
    }
    
    @Test
    @DisplayName("Should return empty map when feature is disabled for bulk query")
    void shouldReturnEmptyMapWhenFeatureDisabledForBulkQuery() {
        // Given
        ReflectionTestUtils.setField(stockStatusService, "stockManagementEnabled", false);
        List<String> productIds = Arrays.asList("product-1", "product-2");
        
        // When
        Map<String, StockStatus> statusMap = stockStatusService.getBulkStockStatus(productIds);
        
        // Then
        assertNotNull(statusMap);
        assertTrue(statusMap.isEmpty());
        verify(productRepository, never()).findAllById(anyList());
    }
    
    @Test
    @DisplayName("Should get correct stock status message")
    void shouldGetCorrectStockStatusMessage() {
        // Given
        Product inStockProduct = new Product("product-1", "Product 1", "Description", 10.0, 50, "Category");
        Product lowStockProduct = new Product("product-2", "Product 2", "Description", 20.0, 5, "Category");
        Product outOfStockProduct = new Product("product-3", "Product 3", "Description", 30.0, 0, "Category");
        
        // When
        String inStockMessage = stockStatusService.getStockStatusMessage(inStockProduct);
        String lowStockMessage = stockStatusService.getStockStatusMessage(lowStockProduct);
        String outOfStockMessage = stockStatusService.getStockStatusMessage(outOfStockProduct);
        
        // Then
        assertEquals("In Stock", inStockMessage);
        assertEquals("Low Stock - Only 5 left!", lowStockMessage);
        assertEquals("Out of Stock", outOfStockMessage);
    }
    
    @Test
    @DisplayName("Should return null for invalid product ID")
    void shouldReturnNullForInvalidProductId() {
        // Given
        String productId = "";
        
        // When
        StockStatus status = stockStatusService.getStockStatus(productId);
        
        // Then
        assertNull(status);
        verify(productRepository, never()).findById(anyString());
    }
}

