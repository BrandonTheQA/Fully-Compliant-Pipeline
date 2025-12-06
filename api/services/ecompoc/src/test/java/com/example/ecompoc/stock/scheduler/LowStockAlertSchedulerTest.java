package com.example.ecompoc.stock.scheduler;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.model.LowStockAlert;
import com.example.ecompoc.stock.repository.LowStockAlertRepository;
import com.example.ecompoc.stock.service.StockEmailService;
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

@DisplayName("LowStockAlertScheduler Tests")
class LowStockAlertSchedulerTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private LowStockAlertRepository alertRepository;
    
    @Mock
    private StockEmailService emailService;
    
    private LowStockAlertScheduler scheduler;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new LowStockAlertScheduler(
            productRepository, alertRepository, emailService);
        
        // Set feature toggle to enabled for tests
        ReflectionTestUtils.setField(scheduler, "stockManagementEnabled", true);
        ReflectionTestUtils.setField(scheduler, "defaultLowStockThreshold", 10);
    }
    
    @Test
    @DisplayName("Should create and send low stock alert for product below threshold")
    void shouldCreateAndSendLowStockAlertForProductBelowThreshold() {
        // Given
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 5, "Category");
        product.setLowStockThreshold(10);
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        when(alertRepository.findActiveAlertByProductId("product-1")).thenReturn(Optional.empty());
        when(alertRepository.save(any(LowStockAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        scheduler.processLowStockAlerts();
        
        // Then
        // Scheduler saves alert twice: once when creating (PENDING), once after sending email (SENT)
        verify(alertRepository, times(2)).save(any(LowStockAlert.class));
        verify(emailService).sendLowStockAlertEmail(eq(product), eq(5), eq(10));
        
        ArgumentCaptor<LowStockAlert> captor = ArgumentCaptor.forClass(LowStockAlert.class);
        verify(alertRepository, times(2)).save(captor.capture());
        
        // Get the last saved alert (after email is sent, status is SENT)
        List<LowStockAlert> savedAlerts = captor.getAllValues();
        LowStockAlert finalAlert = savedAlerts.get(savedAlerts.size() - 1);
        assertEquals("product-1", finalAlert.getProductId());
        assertEquals(5, finalAlert.getStockLevel());
        assertEquals(10, finalAlert.getThreshold());
        assertEquals("SENT", finalAlert.getStatus());
    }
    
    @Test
    @DisplayName("Should not create duplicate alert for product with existing active alert")
    void shouldNotCreateDuplicateAlertForProductWithExistingActiveAlert() {
        // Given
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 5, "Category");
        LowStockAlert existingAlert = new LowStockAlert();
        existingAlert.setAlertId("alert-1");
        existingAlert.setProductId("product-1");
        existingAlert.setStatus("PENDING");
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        when(alertRepository.findActiveAlertByProductId("product-1")).thenReturn(Optional.of(existingAlert));
        
        // When
        scheduler.processLowStockAlerts();
        
        // Then
        verify(alertRepository, never()).save(any(LowStockAlert.class));
        verify(emailService, never()).sendLowStockAlertEmail(any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Should resolve alert when product is above threshold")
    void shouldResolveAlertWhenProductIsAboveThreshold() {
        // Given
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 20, "Category");
        LowStockAlert activeAlert = new LowStockAlert();
        activeAlert.setAlertId("alert-1");
        activeAlert.setProductId("product-1");
        activeAlert.setStatus("SENT");
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        when(alertRepository.findActiveAlertByProductId("product-1")).thenReturn(Optional.of(activeAlert));
        when(alertRepository.save(any(LowStockAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        scheduler.processLowStockAlerts();
        
        // Then
        verify(alertRepository).save(activeAlert);
        assertEquals("RESOLVED", activeAlert.getStatus());
    }
    
    @Test
    @DisplayName("Should skip processing when feature is disabled")
    void shouldSkipProcessingWhenFeatureDisabled() {
        // Given
        ReflectionTestUtils.setField(scheduler, "stockManagementEnabled", false);
        
        // When
        scheduler.processLowStockAlerts();
        
        // Then
        verify(productRepository, never()).findAll();
        verify(alertRepository, never()).save(any(LowStockAlert.class));
    }
    
    @Test
    @DisplayName("Should skip products with zero quantity")
    void shouldSkipProductsWithZeroQuantity() {
        // Given
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 0, "Category");
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        
        // When
        scheduler.processLowStockAlerts();
        
        // Then
        verify(alertRepository, never()).save(any(LowStockAlert.class));
        verify(emailService, never()).sendLowStockAlertEmail(any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Should use default threshold when product threshold is null")
    void shouldUseDefaultThresholdWhenProductThresholdIsNull() {
        // Given
        Product product = new Product("product-1", "Product 1", "Description", 10.0, 5, "Category");
        product.setLowStockThreshold(null);
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        when(alertRepository.findActiveAlertByProductId("product-1")).thenReturn(Optional.empty());
        when(alertRepository.save(any(LowStockAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        scheduler.processLowStockAlerts();
        
        // Then
        verify(emailService).sendLowStockAlertEmail(eq(product), eq(5), eq(10)); // default threshold
    }
}

