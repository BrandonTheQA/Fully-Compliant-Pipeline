package com.example.ecompoc.returns.service;

import com.example.ecompoc.returns.dto.ReturnAnalyticsResponse;
import com.example.ecompoc.returns.enums.ReturnReason;
import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.enums.ReturnType;
import com.example.ecompoc.returns.model.Return;
import com.example.ecompoc.returns.model.ReturnItem;
import com.example.ecompoc.returns.repository.ReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnAnalyticsService Tests")
class ReturnAnalyticsServiceTest {

    @Mock
    private ReturnRepository returnRepository;

    @InjectMocks
    private ReturnAnalyticsService returnAnalyticsService;

    private List<Return> testReturns;

    @BeforeEach
    void setUp() {
        testReturns = new ArrayList<>();
    }

    @Test
    @DisplayName("Should calculate overall return metrics (AC10.1)")
    void shouldCalculateOverallReturnMetrics() {
        // Given
        Return return1 = createTestReturn("return-1", ReturnStatus.REFUNDED, 50.0);
        Return return2 = createTestReturn("return-2", ReturnStatus.REFUNDED, 75.0);
        testReturns.addAll(Arrays.asList(return1, return2));

        when(returnRepository.findAll()).thenReturn(testReturns);

        // When
        ReturnAnalyticsResponse response = returnAnalyticsService.getAnalytics();

        // Then
        assertNotNull(response);
        assertEquals(2L, response.getTotalReturns());
        assertEquals(125.0, response.getTotalReturnValue(), 0.01);
    }

    @Test
    @DisplayName("Should calculate return rate by product (AC10.2)")
    void shouldCalculateReturnRateByProduct() {
        // Given
        Return return1 = createTestReturn("return-1", ReturnStatus.REFUNDED, 50.0);
        ReturnItem item1 = new ReturnItem(1L, "product-1", "Product 1", 2, ReturnReason.DEFECTIVE, 25.0);
        return1.addItem(item1);

        Return return2 = createTestReturn("return-2", ReturnStatus.REFUNDED, 75.0);
        ReturnItem item2 = new ReturnItem(2L, "product-1", "Product 1", 1, ReturnReason.CHANGED_MIND, 25.0);
        return2.addItem(item2);

        testReturns.addAll(Arrays.asList(return1, return2));
        when(returnRepository.findAll()).thenReturn(testReturns);

        // When
        ReturnAnalyticsResponse response = returnAnalyticsService.getAnalytics();

        // Then
        assertNotNull(response);
        assertNotNull(response.getReturnRateByProduct());
        assertTrue(response.getReturnRateByProduct().size() > 0);
        
        // Product 1 should have 3 total returns (2 + 1)
        ReturnAnalyticsResponse.ProductReturnRate productRate = response.getReturnRateByProduct().stream()
            .filter(p -> p.getProductId().equals("product-1"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(productRate);
        assertEquals("product-1", productRate.getProductId());
        assertEquals("Product 1", productRate.getProductName());
        assertEquals(3L, productRate.getTotalReturns());
    }

    @Test
    @DisplayName("Should calculate return reason distribution (AC10.4)")
    void shouldCalculateReturnReasonDistribution() {
        // Given
        Return return1 = createTestReturn("return-1", ReturnStatus.REFUNDED, 50.0);
        ReturnItem item1 = new ReturnItem(1L, "product-1", "Product 1", 1, ReturnReason.DEFECTIVE, 50.0);
        return1.addItem(item1);

        Return return2 = createTestReturn("return-2", ReturnStatus.REFUNDED, 75.0);
        ReturnItem item2 = new ReturnItem(2L, "product-2", "Product 2", 1, ReturnReason.CHANGED_MIND, 75.0);
        return2.addItem(item2);

        Return return3 = createTestReturn("return-3", ReturnStatus.REFUNDED, 25.0);
        ReturnItem item3 = new ReturnItem(3L, "product-3", "Product 3", 1, ReturnReason.DEFECTIVE, 25.0);
        return3.addItem(item3);

        testReturns.addAll(Arrays.asList(return1, return2, return3));
        when(returnRepository.findAll()).thenReturn(testReturns);

        // When
        ReturnAnalyticsResponse response = returnAnalyticsService.getAnalytics();

        // Then
        assertNotNull(response);
        assertNotNull(response.getReturnReasonsDistribution());
        assertEquals(2L, response.getReturnReasonsDistribution().get("DEFECTIVE"));
        assertEquals(1L, response.getReturnReasonsDistribution().get("CHANGED_MIND"));
    }

    @Test
    @DisplayName("Should calculate average return processing time (AC10.5)")
    void shouldCalculateAverageReturnProcessingTime() {
        // Given
        Return return1 = createTestReturn("return-1", ReturnStatus.REFUNDED, 50.0);
        return1.setCreatedAt(LocalDateTime.now().minusDays(10));
        return1.setRefundDate(LocalDateTime.now().minusDays(5)); // 5 days processing time

        Return return2 = createTestReturn("return-2", ReturnStatus.REFUNDED, 75.0);
        return2.setCreatedAt(LocalDateTime.now().minusDays(8));
        return2.setRefundDate(LocalDateTime.now().minusDays(3)); // 5 days processing time

        testReturns.addAll(Arrays.asList(return1, return2));
        when(returnRepository.findAll()).thenReturn(testReturns);

        // When
        ReturnAnalyticsResponse response = returnAnalyticsService.getAnalytics();

        // Then
        assertNotNull(response);
        assertTrue(response.getAverageReturnProcessingTime() > 0);
        // Average should be around 5 days
        assertEquals(5.0, response.getAverageReturnProcessingTime(), 1.0);
    }

    @Test
    @DisplayName("Should calculate returns by status (AC10.1)")
    void shouldCalculateReturnsByStatus() {
        // Given
        Return return1 = createTestReturn("return-1", ReturnStatus.PENDING_APPROVAL, 50.0);
        Return return2 = createTestReturn("return-2", ReturnStatus.APPROVED, 75.0);
        Return return3 = createTestReturn("return-3", ReturnStatus.REFUNDED, 25.0);
        Return return4 = createTestReturn("return-4", ReturnStatus.REFUNDED, 100.0);

        testReturns.addAll(Arrays.asList(return1, return2, return3, return4));
        when(returnRepository.findAll()).thenReturn(testReturns);

        // When
        ReturnAnalyticsResponse response = returnAnalyticsService.getAnalytics();

        // Then
        assertNotNull(response);
        assertNotNull(response.getReturnsByStatus());
        assertEquals(1L, response.getReturnsByStatus().get("PENDING_APPROVAL"));
        assertEquals(1L, response.getReturnsByStatus().get("APPROVED"));
        assertEquals(2L, response.getReturnsByStatus().get("REFUNDED"));
    }

    @Test
    @DisplayName("Should calculate monthly return statistics (AC10.12)")
    void shouldCalculateMonthlyReturnStatistics() {
        // Given
        Return return1 = createTestReturn("return-1", ReturnStatus.REFUNDED, 50.0);
        return1.setCreatedAt(LocalDateTime.of(2024, 12, 1, 10, 0));

        Return return2 = createTestReturn("return-2", ReturnStatus.REFUNDED, 75.0);
        return2.setCreatedAt(LocalDateTime.of(2024, 12, 15, 10, 0));

        Return return3 = createTestReturn("return-3", ReturnStatus.REFUNDED, 25.0);
        return3.setCreatedAt(LocalDateTime.of(2024, 11, 20, 10, 0));

        testReturns.addAll(Arrays.asList(return1, return2, return3));
        when(returnRepository.findAll()).thenReturn(testReturns);

        // When
        ReturnAnalyticsResponse response = returnAnalyticsService.getAnalytics();

        // Then
        assertNotNull(response);
        assertNotNull(response.getReturnsByMonth());
        assertTrue(response.getReturnsByMonth().size() > 0);
        
        // Should have entries for December 2024 and November 2024
        boolean hasDecember = response.getReturnsByMonth().stream()
            .anyMatch(m -> m.getYear() == 2024 && m.getMonth().equals("DECEMBER"));
        assertTrue(hasDecember);
    }

    @Test
    @DisplayName("Should handle analytics with no returns (Edge Case 13)")
    void shouldHandleAnalyticsWithNoReturns() {
        // Given
        when(returnRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        ReturnAnalyticsResponse response = returnAnalyticsService.getAnalytics();

        // Then
        assertNotNull(response);
        assertEquals(0L, response.getTotalReturns());
        assertEquals(0.0, response.getTotalReturnValue(), 0.01);
        assertEquals(0.0, response.getAverageReturnProcessingTime(), 0.01);
        assertNotNull(response.getReturnReasonsDistribution());
        assertNotNull(response.getReturnRateByProduct());
        assertNotNull(response.getReturnsByStatus());
    }

    @Test
    @DisplayName("Should calculate financial impact metrics (AC10.7)")
    void shouldCalculateFinancialImpactMetrics() {
        // Given
        Return return1 = createTestReturn("return-1", ReturnStatus.REFUNDED, 50.0);
        Return return2 = createTestReturn("return-2", ReturnStatus.REFUNDED, 75.0);
        Return return3 = createTestReturn("return-3", ReturnStatus.REFUNDED, 25.0);

        testReturns.addAll(Arrays.asList(return1, return2, return3));
        when(returnRepository.findAll()).thenReturn(testReturns);

        // When
        ReturnAnalyticsResponse response = returnAnalyticsService.getAnalytics();

        // Then
        assertNotNull(response);
        assertEquals(150.0, response.getTotalReturnValue(), 0.01);
    }

    @Test
    @DisplayName("Should return zero processing time when no completed returns")
    void shouldReturnZeroProcessingTimeWhenNoCompletedReturns() {
        // Given
        Return return1 = createTestReturn("return-1", ReturnStatus.PENDING_APPROVAL, 50.0);
        Return return2 = createTestReturn("return-2", ReturnStatus.APPROVED, 75.0);

        testReturns.addAll(Arrays.asList(return1, return2));
        when(returnRepository.findAll()).thenReturn(testReturns);

        // When
        ReturnAnalyticsResponse response = returnAnalyticsService.getAnalytics();

        // Then
        assertNotNull(response);
        assertEquals(0.0, response.getAverageReturnProcessingTime(), 0.01);
    }

    @Test
    @DisplayName("Should handle returns with null refund dates")
    void shouldHandleReturnsWithNullRefundDates() {
        // Given
        Return return1 = createTestReturn("return-1", ReturnStatus.REFUNDED, 50.0);
        return1.setCreatedAt(LocalDateTime.now().minusDays(10));
        return1.setRefundDate(null); // No refund date
        return1.setUpdatedAt(LocalDateTime.now().minusDays(5));

        testReturns.add(return1);
        when(returnRepository.findAll()).thenReturn(testReturns);

        // When
        ReturnAnalyticsResponse response = returnAnalyticsService.getAnalytics();

        // Then
        assertNotNull(response);
        // Should use updatedAt if refundDate is null
        assertTrue(response.getAverageReturnProcessingTime() >= 0);
    }

    @Test
    @DisplayName("Should sort product return rates by total returns descending")
    void shouldSortProductReturnRatesByTotalReturnsDescending() {
        // Given
        Return return1 = createTestReturn("return-1", ReturnStatus.REFUNDED, 50.0);
        ReturnItem item1 = new ReturnItem(1L, "product-1", "Product 1", 5, ReturnReason.DEFECTIVE, 10.0);
        return1.addItem(item1);

        Return return2 = createTestReturn("return-2", ReturnStatus.REFUNDED, 75.0);
        ReturnItem item2 = new ReturnItem(2L, "product-2", "Product 2", 2, ReturnReason.CHANGED_MIND, 37.5);
        return2.addItem(item2);

        Return return3 = createTestReturn("return-3", ReturnStatus.REFUNDED, 25.0);
        ReturnItem item3 = new ReturnItem(3L, "product-1", "Product 1", 3, ReturnReason.DEFECTIVE, 8.33);
        return3.addItem(item3);

        testReturns.addAll(Arrays.asList(return1, return2, return3));
        when(returnRepository.findAll()).thenReturn(testReturns);

        // When
        ReturnAnalyticsResponse response = returnAnalyticsService.getAnalytics();

        // Then
        assertNotNull(response);
        assertNotNull(response.getReturnRateByProduct());
        assertTrue(response.getReturnRateByProduct().size() >= 2);
        
        // Product 1 should be first (8 total returns: 5 + 3)
        // Product 2 should be second (2 total returns)
        ReturnAnalyticsResponse.ProductReturnRate first = response.getReturnRateByProduct().get(0);
        assertEquals("product-1", first.getProductId());
        assertEquals(8L, first.getTotalReturns());
    }

    // Helper method to create test return
    private Return createTestReturn(String returnId, ReturnStatus status, Double refundAmount) {
        Return returnEntity = new Return(
            returnId,
            "order-123",
            "user-123",
            "RMA-20241217-12345",
            status,
            ReturnType.REFUND_TO_PAYMENT
        );
        returnEntity.setRefundAmountDecimal(refundAmount != null ? BigDecimal.valueOf(refundAmount) : null);
        returnEntity.setCreatedAt(LocalDateTime.now().minusDays(5));
        returnEntity.setUpdatedAt(LocalDateTime.now());
        if (status == ReturnStatus.REFUNDED) {
            returnEntity.setRefundDate(LocalDateTime.now());
        }
        return returnEntity;
    }
}

