package com.example.ecompoc.stock.service;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.model.StockNotification;
import com.example.ecompoc.stock.repository.StockNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("StockNotificationService Tests")
class StockNotificationServiceTest {
    
    @Mock
    private StockNotificationRepository notificationRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private StockEmailService emailService;
    
    private StockNotificationService notificationService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notificationService = new StockNotificationService(
            notificationRepository, productRepository, emailService);
        
        // Set feature toggle to enabled for tests
        ReflectionTestUtils.setField(notificationService, "stockManagementEnabled", true);
    }
    
    @Test
    @DisplayName("Should sign up for notification successfully")
    void shouldSignUpForNotificationSuccessfully() {
        // Given
        String productId = "product-1";
        String userId = "user-1";
        String email = "test@example.com";
        Product product = new Product(productId, "Product 1", "Description", 10.0, 0, "Category");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(notificationRepository.findByProductIdAndUserId(productId, userId)).thenReturn(Optional.empty());
        when(notificationRepository.save(any(StockNotification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        StockNotification result = notificationService.signUpForNotification(productId, userId, email);
        
        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(userId, result.getUserId());
        assertEquals(email, result.getEmail());
        assertEquals("PENDING", result.getStatus());
        assertNotNull(result.getSignupDate());
        
        verify(notificationRepository).save(any(StockNotification.class));
    }
    
    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        String productId = "non-existent";
        String userId = "user-1";
        String email = "test@example.com";
        
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.signUpForNotification(productId, userId, email));
        
        verify(notificationRepository, never()).save(any(StockNotification.class));
    }
    
    @Test
    @DisplayName("Should return existing notification if already subscribed")
    void shouldReturnExistingNotificationIfAlreadySubscribed() {
        // Given
        String productId = "product-1";
        String userId = "user-1";
        String email = "test@example.com";
        Product product = new Product(productId, "Product 1", "Description", 10.0, 0, "Category");
        StockNotification existing = new StockNotification();
        existing.setNotificationId("notification-1");
        existing.setProductId(productId);
        existing.setUserId(userId);
        existing.setStatus("PENDING");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(notificationRepository.findByProductIdAndUserId(productId, userId)).thenReturn(Optional.of(existing));
        
        // When
        StockNotification result = notificationService.signUpForNotification(productId, userId, email);
        
        // Then
        assertNotNull(result);
        assertEquals("notification-1", result.getNotificationId());
        verify(notificationRepository, never()).save(any(StockNotification.class));
    }
    
    @Test
    @DisplayName("Should resubscribe if previously unsubscribed")
    void shouldResubscribeIfPreviouslyUnsubscribed() {
        // Given
        String productId = "product-1";
        String userId = "user-1";
        String email = "test@example.com";
        Product product = new Product(productId, "Product 1", "Description", 10.0, 0, "Category");
        StockNotification unsubscribed = new StockNotification();
        unsubscribed.setNotificationId("notification-1");
        unsubscribed.setProductId(productId);
        unsubscribed.setUserId(userId);
        unsubscribed.setStatus("UNSUBSCRIBED");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(notificationRepository.findByProductIdAndUserId(productId, userId)).thenReturn(Optional.of(unsubscribed));
        when(notificationRepository.save(any(StockNotification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        StockNotification result = notificationService.signUpForNotification(productId, userId, email);
        
        // Then
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(notificationRepository).save(unsubscribed);
    }
    
    @Test
    @DisplayName("Should unsubscribe from notification successfully")
    void shouldUnsubscribeFromNotificationSuccessfully() {
        // Given
        String notificationId = "notification-1";
        StockNotification notification = new StockNotification();
        notification.setNotificationId(notificationId);
        notification.setStatus("PENDING");
        
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(StockNotification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        notificationService.unsubscribe(notificationId);
        
        // Then
        assertEquals("UNSUBSCRIBED", notification.getStatus());
        verify(notificationRepository).save(notification);
    }
    
    @Test
    @DisplayName("Should throw exception when notification not found for unsubscribe")
    void shouldThrowExceptionWhenNotificationNotFoundForUnsubscribe() {
        // Given
        String notificationId = "non-existent";
        
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.unsubscribe(notificationId));
    }
    
    @Test
    @DisplayName("Should get user notifications successfully")
    void shouldGetUserNotificationsSuccessfully() {
        // Given
        String userId = "user-1";
        StockNotification notification1 = new StockNotification();
        notification1.setNotificationId("notification-1");
        notification1.setProductId("product-1");
        notification1.setUserId(userId);
        StockNotification notification2 = new StockNotification();
        notification2.setNotificationId("notification-2");
        notification2.setProductId("product-2");
        notification2.setUserId(userId);
        
        when(notificationRepository.findByUserId(userId)).thenReturn(Arrays.asList(notification1, notification2));
        
        // When
        List<StockNotification> result = notificationService.getUserNotifications(userId);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationRepository).findByUserId(userId);
    }
    
    @Test
    @DisplayName("Should return empty list when feature is disabled")
    void shouldReturnEmptyListWhenFeatureDisabled() {
        // Given
        ReflectionTestUtils.setField(notificationService, "stockManagementEnabled", false);
        String userId = "user-1";
        
        // When
        List<StockNotification> result = notificationService.getUserNotifications(userId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationRepository, never()).findByUserId(anyString());
    }
    
    @Test
    @DisplayName("Should process back-in-stock event and send notifications")
    void shouldProcessBackInStockEventAndSendNotifications() {
        // Given
        String productId = "product-1";
        Product product = new Product(productId, "Product 1", "Description", 10.0, 10, "Category");
        StockNotification notification1 = new StockNotification();
        notification1.setNotificationId("notification-1");
        notification1.setProductId(productId);
        notification1.setEmail("test1@example.com");
        notification1.setStatus("PENDING");
        StockNotification notification2 = new StockNotification();
        notification2.setNotificationId("notification-2");
        notification2.setProductId(productId);
        notification2.setEmail("test2@example.com");
        notification2.setStatus("PENDING");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(notificationRepository.findPendingNotificationsByProductId(productId))
            .thenReturn(Arrays.asList(notification1, notification2));
        when(notificationRepository.save(any(StockNotification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        notificationService.processBackInStockEvent(productId);
        
        // Then
        verify(emailService, times(2)).sendBackInStockEmail(any(StockNotification.class), eq(product));
        verify(notificationRepository, times(2)).save(any(StockNotification.class));
        
        ArgumentCaptor<StockNotification> captor = ArgumentCaptor.forClass(StockNotification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        
        List<StockNotification> savedNotifications = captor.getAllValues();
        assertEquals("NOTIFIED", savedNotifications.get(0).getStatus());
        assertEquals("NOTIFIED", savedNotifications.get(1).getStatus());
    }
    
    @Test
    @DisplayName("Should handle null email in notification signup")
    void shouldThrowExceptionForNullEmail() {
        // Given
        String productId = "product-1";
        String userId = "user-1";
        String email = null;
        Product product = new Product(productId, "Product 1", "Description", 10.0, 0, "Category");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.signUpForNotification(productId, userId, email));
    }
}

