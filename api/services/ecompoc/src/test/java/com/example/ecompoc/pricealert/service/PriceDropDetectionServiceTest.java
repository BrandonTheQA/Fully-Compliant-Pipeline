package com.example.ecompoc.pricealert.service;

import com.example.ecompoc.pricealert.model.PriceAlert;
import com.example.ecompoc.pricealert.model.PriceHistory;
import com.example.ecompoc.pricealert.repository.PriceAlertRepository;
import com.example.ecompoc.pricealert.repository.PriceHistoryRepository;
import com.example.ecompoc.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PriceDropDetectionService
 */
@DisplayName("PriceDropDetectionService Tests")
class PriceDropDetectionServiceTest {

    @Mock
    private PriceAlertRepository priceAlertRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private PriceAlertEmailService emailService;

    @InjectMocks
    private PriceDropDetectionService priceDropDetectionService;

    private Product testProduct;
    private PriceAlert testAlert;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Enable the feature toggle for testing
        ReflectionTestUtils.setField(priceDropDetectionService, "priceAlertEnabled", true);
        
        // Inject the email service mock
        ReflectionTestUtils.setField(priceDropDetectionService, "emailService", emailService);
        
        // Setup test product
        testProduct = new Product();
        testProduct.setId("product-1");
        testProduct.setName("Test Product");
        testProduct.setPrice(100.0);
        testProduct.setPriceDecimal(BigDecimal.valueOf(100.0));
        
        // Setup test alert
        testAlert = new PriceAlert();
        testAlert.setAlertId("alert-1");
        testAlert.setProductId("product-1");
        testAlert.setUserEmail("test@example.com");
        testAlert.setCurrentPrice(BigDecimal.valueOf(100.0));
        testAlert.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Should record price decrease in history")
    void shouldRecordPriceDecreaseInHistory() {
        // Given
        BigDecimal oldPrice = BigDecimal.valueOf(100.0);
        BigDecimal newPrice = BigDecimal.valueOf(90.0);

        when(priceHistoryRepository.save(any(PriceHistory.class))).thenAnswer(invocation -> {
            PriceHistory history = invocation.getArgument(0);
            history.setPriceHistoryId("history-1");
            return history;
        });

        // When
        priceDropDetectionService.detectPriceChange(testProduct, oldPrice, newPrice);

        // Then
        verify(priceHistoryRepository).save(any(PriceHistory.class));
    }

    @Test
    @DisplayName("Should record price increase in history")
    void shouldRecordPriceIncreaseInHistory() {
        // Given
        BigDecimal oldPrice = BigDecimal.valueOf(90.0);
        BigDecimal newPrice = BigDecimal.valueOf(100.0);

        when(priceHistoryRepository.save(any(PriceHistory.class))).thenAnswer(invocation -> {
            PriceHistory history = invocation.getArgument(0);
            history.setPriceHistoryId("history-1");
            return history;
        });

        // When
        priceDropDetectionService.detectPriceChange(testProduct, oldPrice, newPrice);

        // Then
        verify(priceHistoryRepository).save(any(PriceHistory.class));
    }

    @Test
    @DisplayName("Should not record history when oldPrice is null")
    void shouldNotRecordHistoryWhenOldPriceIsNull() {
        // Given
        BigDecimal newPrice = BigDecimal.valueOf(100.0);

        // When
        priceDropDetectionService.detectPriceChange(testProduct, null, newPrice);

        // Then
        verify(priceHistoryRepository, never()).save(any(PriceHistory.class));
    }

    @Test
    @DisplayName("Should evaluate alerts for product with price decrease")
    void shouldEvaluateAlertsForProductWithPriceDecrease() {
        // Given
        BigDecimal oldPrice = BigDecimal.valueOf(100.0);
        BigDecimal newPrice = BigDecimal.valueOf(90.0);
        
        PriceHistory latestHistory = new PriceHistory();
        latestHistory.setPrice(newPrice);
        latestHistory.setPreviousPrice(oldPrice);

        when(priceAlertRepository.findActiveAlertsForProduct("product-1"))
            .thenReturn(Collections.singletonList(testAlert));
        when(priceHistoryRepository.findLatestByProductId("product-1"))
            .thenReturn(Optional.of(latestHistory));

        // When
        priceDropDetectionService.evaluateAlertsForProduct("product-1");

        // Then
        verify(priceAlertRepository).findActiveAlertsForProduct("product-1");
        verify(priceHistoryRepository).findLatestByProductId("product-1");
    }

    @Test
    @DisplayName("Should trigger alert when price drops below target")
    void shouldTriggerAlertWhenPriceDropsBelowTarget() {
        // Given
        testAlert.setTargetPrice(BigDecimal.valueOf(95.0));
        BigDecimal currentPrice = BigDecimal.valueOf(90.0);
        BigDecimal previousPrice = BigDecimal.valueOf(100.0);

        when(priceAlertRepository.save(any(PriceAlert.class))).thenReturn(testAlert);

        // When
        priceDropDetectionService.triggerAlert(testAlert, currentPrice, previousPrice);

        // Then
        verify(priceAlertRepository).save(any(PriceAlert.class));
        verify(emailService).sendPriceDropEmail(eq(testAlert), eq(currentPrice), eq(previousPrice));
        assertEquals("TRIGGERED", testAlert.getStatus());
    }

    @Test
    @DisplayName("Should trigger alert when price drops by minimum percentage")
    void shouldTriggerAlertWhenPriceDropsByMinimumPercentage() {
        // Given
        testAlert.setTargetPrice(null); // No target price
        BigDecimal currentPrice = BigDecimal.valueOf(94.0); // 6% drop
        BigDecimal previousPrice = BigDecimal.valueOf(100.0);

        when(priceAlertRepository.save(any(PriceAlert.class))).thenReturn(testAlert);

        // When
        priceDropDetectionService.triggerAlert(testAlert, currentPrice, previousPrice);

        // Then
        verify(priceAlertRepository).save(any(PriceAlert.class));
        verify(emailService).sendPriceDropEmail(eq(testAlert), eq(currentPrice), eq(previousPrice));
    }
}

