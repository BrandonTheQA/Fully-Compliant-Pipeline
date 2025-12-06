package com.example.ecompoc.stock.service;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.stock.model.StockNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockEmailService Tests")
class StockEmailServiceTest {
    
    private StockEmailService emailService;
    
    @BeforeEach
    void setUp() {
        emailService = new StockEmailService();
        
        // Set feature toggle to enabled for tests
        ReflectionTestUtils.setField(emailService, "stockManagementEnabled", true);
        ReflectionTestUtils.setField(emailService, "adminEmail", "admin@example.com");
    }
    
    @Test
    @DisplayName("Should send back-in-stock email successfully")
    void shouldSendBackInStockEmailSuccessfully() {
        // Given
        StockNotification notification = new StockNotification();
        notification.setNotificationId("notification-1");
        notification.setEmail("test@example.com");
        notification.setProductId("product-1");
        
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 10, "Category");
        
        // When
        emailService.sendBackInStockEmail(notification, product);
        
        // Then - should not throw exception
        assertDoesNotThrow(() -> emailService.sendBackInStockEmail(notification, product));
    }
    
    @Test
    @DisplayName("Should skip email when feature is disabled")
    void shouldSkipEmailWhenFeatureDisabled() {
        // Given
        ReflectionTestUtils.setField(emailService, "stockManagementEnabled", false);
        StockNotification notification = new StockNotification();
        notification.setEmail("test@example.com");
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 10, "Category");
        
        // When
        emailService.sendBackInStockEmail(notification, product);
        
        // Then - should not throw exception (just logs)
        assertDoesNotThrow(() -> emailService.sendBackInStockEmail(notification, product));
    }
    
    @Test
    @DisplayName("Should skip email when notification email is null")
    void shouldSkipEmailWhenNotificationEmailIsNull() {
        // Given
        StockNotification notification = new StockNotification();
        notification.setEmail(null);
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 10, "Category");
        
        // When
        emailService.sendBackInStockEmail(notification, product);
        
        // Then - should not throw exception
        assertDoesNotThrow(() -> emailService.sendBackInStockEmail(notification, product));
    }
    
    @Test
    @DisplayName("Should send low stock alert email successfully")
    void shouldSendLowStockAlertEmailSuccessfully() {
        // Given
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 5, "Category");
        Integer stockLevel = 5;
        Integer threshold = 10;
        
        // When
        emailService.sendLowStockAlertEmail(product, stockLevel, threshold);
        
        // Then - should not throw exception
        assertDoesNotThrow(() -> emailService.sendLowStockAlertEmail(product, stockLevel, threshold));
    }
    
    @Test
    @DisplayName("Should skip low stock alert when feature is disabled")
    void shouldSkipLowStockAlertWhenFeatureDisabled() {
        // Given
        ReflectionTestUtils.setField(emailService, "stockManagementEnabled", false);
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 5, "Category");
        
        // When
        emailService.sendLowStockAlertEmail(product, 5, 10);
        
        // Then - should not throw exception
        assertDoesNotThrow(() -> emailService.sendLowStockAlertEmail(product, 5, 10));
    }
}

