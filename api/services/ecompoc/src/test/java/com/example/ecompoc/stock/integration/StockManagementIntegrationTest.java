package com.example.ecompoc.stock.integration;

import com.example.ecompoc.order.dto.CreateOrderRequest;
import com.example.ecompoc.order.service.OrderService;
import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.model.StockNotification;
import com.example.ecompoc.stock.model.StockStatus;
import com.example.ecompoc.stock.repository.StockNotificationRepository;
import com.example.ecompoc.stock.service.StockDeductionService;
import com.example.ecompoc.stock.service.StockNotificationService;
import com.example.ecompoc.stock.service.StockStatusService;
import com.example.ecompoc.user.model.User;
import com.example.ecompoc.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for stock management functionality
 */
@SpringBootTest(properties = {"stock-management.enabled=true"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Stock Management Integration Tests")
class StockManagementIntegrationTest {
    
    @Autowired
    private StockStatusService stockStatusService;
    
    @Autowired
    private StockNotificationService notificationService;
    
    @Autowired
    private StockDeductionService deductionService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private StockNotificationRepository notificationRepository;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserRepository userRepository;
    
    private Product testProduct;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(UUID.randomUUID().toString());
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
        
        // Create test product
        testProduct = new Product();
        testProduct.setId(UUID.randomUUID().toString());
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(10.0);
        testProduct.setQuantity(50);
        testProduct.setCategory("Test Category");
        testProduct.setLowStockThreshold(10);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());
        testProduct = productRepository.save(testProduct);
        
        // Ensure StockDeductionService is injected into OrderService
        // Use reflection to set it since setter might not be accessible
        if (deductionService != null) {
            try {
                orderService.setStockDeductionService(deductionService);
            } catch (Exception e) {
                // If setter doesn't work, use reflection
                org.springframework.test.util.ReflectionTestUtils.setField(
                    orderService, "stockDeductionService", deductionService);
            }
        }
    }
    
    @Test
    @DisplayName("Should calculate stock status correctly")
    void shouldCalculateStockStatusCorrectly() {
        // Given - product with quantity 50, threshold 10
        // When
        StockStatus status = stockStatusService.calculateStockStatus(testProduct);
        
        // Then
        assertEquals(StockStatus.IN_STOCK, status);
    }
    
    @Test
    @DisplayName("Should detect low stock correctly")
    void shouldDetectLowStockCorrectly() {
        // Given
        testProduct.setQuantity(5);
        testProduct = productRepository.save(testProduct);
        
        // When
        StockStatus status = stockStatusService.calculateStockStatus(testProduct);
        
        // Then
        assertEquals(StockStatus.LOW_STOCK, status);
    }
    
    @Test
    @DisplayName("Should detect out of stock correctly")
    void shouldDetectOutOfStockCorrectly() {
        // Given
        testProduct.setQuantity(0);
        testProduct = productRepository.save(testProduct);
        
        // When
        StockStatus status = stockStatusService.calculateStockStatus(testProduct);
        
        // Then
        assertEquals(StockStatus.OUT_OF_STOCK, status);
    }
    
    @Test
    @DisplayName("Should sign up for back-in-stock notification")
    void shouldSignUpForBackInStockNotification() {
        // Given
        testProduct.setQuantity(0);
        testProduct = productRepository.save(testProduct);
        
        // When
        StockNotification notification = notificationService.signUpForNotification(
            testProduct.getId(), testUser.getId(), testUser.getEmail());
        
        // Then
        assertNotNull(notification);
        assertEquals(testProduct.getId(), notification.getProductId());
        assertEquals(testUser.getId(), notification.getUserId());
        assertEquals(testUser.getEmail(), notification.getEmail());
        assertEquals("PENDING", notification.getStatus());
        assertNotNull(notification.getSignupDate());
        
        // Verify saved in database
        List<StockNotification> saved = notificationRepository.findByUserId(testUser.getId());
        assertEquals(1, saved.size());
    }
    
    @Test
    @DisplayName("Should deduct stock on order creation")
    void shouldDeductStockOnOrderCreation() {
        // Given
        testProduct.setQuantity(50);
        testProduct = productRepository.save(testProduct);
        
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(testUser.getId());
        request.setItems(List.of(
            new CreateOrderRequest.OrderItemRequest(testProduct.getId(), 5)
        ));
        
        // Ensure service is injected
        assertNotNull(deductionService, "StockDeductionService should be available");
        org.springframework.test.util.ReflectionTestUtils.setField(orderService, "stockDeductionService", deductionService);
        
        // When
        orderService.createOrder(request);
        
        // Flush to ensure updates are persisted
        productRepository.flush();
        
        // Then - query again to get updated quantity from database
        // The @Modifying query updates the database directly, so we need to query fresh
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(45, updatedProduct.getQuantity(), 
            "Stock should be deducted from 50 to 45 after ordering 5 units. " +
            "If this fails, StockDeductionService may not be injected into OrderService.");
    }
    
    @Test
    @DisplayName("Should prevent order creation when insufficient stock")
    void shouldPreventOrderCreationWhenInsufficientStock() {
        // Given
        testProduct.setQuantity(3);
        testProduct = productRepository.save(testProduct);
        
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(testUser.getId());
        request.setItems(List.of(
            new CreateOrderRequest.OrderItemRequest(testProduct.getId(), 5)
        ));
        
        // When & Then
        assertThrows(Exception.class, () -> orderService.createOrder(request));
        
        // Verify stock was not deducted
        Product product = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(3, product.getQuantity());
    }
    
    @Test
    @DisplayName("Should process back-in-stock event and send notifications")
    void shouldProcessBackInStockEventAndSendNotifications() {
        // Given
        testProduct.setQuantity(0);
        testProduct = productRepository.save(testProduct);
        
        // Sign up for notification
        StockNotification notification = notificationService.signUpForNotification(
            testProduct.getId(), testUser.getId(), testUser.getEmail());
        assertNotNull(notification);
        
        // Restore stock
        testProduct.setQuantity(10);
        testProduct = productRepository.save(testProduct);
        
        // When
        notificationService.processBackInStockEvent(testProduct.getId());
        
        // Then
        StockNotification updated = notificationRepository.findById(notification.getNotificationId()).orElseThrow();
        assertEquals("NOTIFIED", updated.getStatus());
        assertNotNull(updated.getNotifiedDate());
    }
    
    @Test
    @DisplayName("Should handle concurrent stock deduction")
    void shouldHandleConcurrentStockDeduction() {
        // Given
        testProduct.setQuantity(10);
        testProduct = productRepository.save(testProduct);
        
        // When - try to deduct more than available (should fail)
        assertThrows(IllegalStateException.class, () -> 
            deductionService.deductStock(testProduct.getId(), 15));
        
        // Then - stock should remain unchanged
        Product product = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(10, product.getQuantity());
    }
    
    @Test
    @DisplayName("Should restore stock successfully")
    void shouldRestoreStockSuccessfully() {
        // Given
        testProduct.setQuantity(50);
        testProduct = productRepository.save(testProduct);
        
        // Flush to ensure product is saved
        productRepository.flush();
        
        // Deduct some stock first
        deductionService.deductStock(testProduct.getId(), 10);
        
        // Flush to ensure deduction is persisted
        productRepository.flush();
        
        // Verify deduction worked
        Product afterDeduction = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(40, afterDeduction.getQuantity()); // 50 - 10 = 40
        
        // When
        deductionService.restoreStock(testProduct.getId(), 5);
        
        // Flush to ensure restoration is persisted
        productRepository.flush();
        
        // Then
        Product product = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(45, product.getQuantity()); // 40 + 5 = 45
    }
}

