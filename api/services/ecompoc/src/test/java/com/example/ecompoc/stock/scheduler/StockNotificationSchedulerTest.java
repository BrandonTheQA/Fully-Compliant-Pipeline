package com.example.ecompoc.stock.scheduler;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.model.StockNotification;
import com.example.ecompoc.stock.repository.StockNotificationRepository;
import com.example.ecompoc.stock.service.StockNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("StockNotificationScheduler Tests")
class StockNotificationSchedulerTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private StockNotificationRepository notificationRepository;
    
    @Mock
    private StockNotificationService notificationService;
    
    private StockNotificationScheduler scheduler;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new StockNotificationScheduler(
            productRepository, notificationRepository, notificationService);
        
        // Set feature toggle to enabled for tests
        ReflectionTestUtils.setField(scheduler, "stockManagementEnabled", true);
    }
    
    @Test
    @DisplayName("Should process back-in-stock notifications successfully")
    void shouldProcessBackInStockNotificationsSuccessfully() {
        // Given
        StockNotification notification1 = new StockNotification();
        notification1.setNotificationId("notification-1");
        notification1.setProductId("product-1");
        notification1.setStatus("PENDING");
        
        StockNotification notification2 = new StockNotification();
        notification2.setNotificationId("notification-2");
        notification2.setProductId("product-2");
        notification2.setStatus("PENDING");
        
        List<StockNotification> pendingNotifications = Arrays.asList(notification1, notification2);
        
        Product product1 = new Product("product-1", "Product 1", "Description", 10.0, 10, "Category");
        Product product2 = new Product("product-2", "Product 2", "Description", 20.0, 5, "Category");
        
        when(notificationRepository.findByStatus("PENDING")).thenReturn(pendingNotifications);
        // Scheduler filters products to only those with quantity > 0
        // Need to use any() for the Set<String> parameter
        when(productRepository.findAllById(any())).thenReturn(Arrays.asList(product1, product2));
        
        // When
        scheduler.processBackInStockNotifications();
        
        // Then
        verify(notificationRepository).findByStatus("PENDING");
        // Both products have quantity > 0, so both should be processed
        verify(notificationService, times(2)).processBackInStockEvent(anyString());
        verify(notificationService).processBackInStockEvent("product-1");
        verify(notificationService).processBackInStockEvent("product-2");
    }
    
    @Test
    @DisplayName("Should skip processing when no pending notifications")
    void shouldSkipProcessingWhenNoPendingNotifications() {
        // Given
        when(notificationRepository.findByStatus("PENDING")).thenReturn(Arrays.asList());
        
        // When
        scheduler.processBackInStockNotifications();
        
        // Then
        verify(notificationRepository).findByStatus("PENDING");
        verify(notificationService, never()).processBackInStockEvent(anyString());
    }
    
    @Test
    @DisplayName("Should skip processing when feature is disabled")
    void shouldSkipProcessingWhenFeatureDisabled() {
        // Given
        ReflectionTestUtils.setField(scheduler, "stockManagementEnabled", false);
        
        // When
        scheduler.processBackInStockNotifications();
        
        // Then
        verify(notificationRepository, never()).findByStatus(anyString());
        verify(notificationService, never()).processBackInStockEvent(anyString());
    }
    
    @Test
    @DisplayName("Should handle exceptions gracefully")
    void shouldHandleExceptionsGracefully() {
        // Given
        when(notificationRepository.findByStatus("PENDING")).thenThrow(new RuntimeException("Database error"));
        
        // When & Then - should not throw exception
        try {
            scheduler.processBackInStockNotifications();
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }
}

