package com.example.ecompoc.returns.service;

import com.example.ecompoc.returns.model.ReturnPolicyConfig;
import com.example.ecompoc.returns.repository.ReturnPolicyConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnPolicyService Tests")
class ReturnPolicyServiceTest {

    @Mock
    private ReturnPolicyConfigRepository policyConfigRepository;

    @InjectMocks
    private ReturnPolicyService returnPolicyService;

    private ReturnPolicyConfig testPolicy;

    @BeforeEach
    void setUp() {
        testPolicy = new ReturnPolicyConfig(30, 10.0, 50.0, 100.0);
    }

    @Test
    @DisplayName("Should get active return policy (AC5.1)")
    void shouldGetActiveReturnPolicy() {
        // Given
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));

        // When
        ReturnPolicyConfig policy = returnPolicyService.getActivePolicy();

        // Then
        assertNotNull(policy);
        assertEquals(30, policy.getReturnWindowDays());
        assertEquals(10.0, policy.getRestockingFeePercentage(), 0.01);
        assertEquals(50.0, policy.getFreeReturnThreshold(), 0.01);
        assertEquals(100.0, policy.getAutoApproveThreshold(), 0.01);
        verify(policyConfigRepository, times(1)).findFirstByOrderByUpdatedAtDesc();
    }

    @Test
    @DisplayName("Should create default policy when none exists")
    void shouldCreateDefaultPolicyWhenNoneExists() {
        // Given
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.empty());
        when(policyConfigRepository.save(any(ReturnPolicyConfig.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReturnPolicyConfig policy = returnPolicyService.getActivePolicy();

        // Then
        assertNotNull(policy);
        assertEquals(30, policy.getReturnWindowDays());
        assertEquals(0.0, policy.getRestockingFeePercentage(), 0.01);
        assertEquals(0.0, policy.getFreeReturnThreshold(), 0.01);
        assertEquals(100.0, policy.getAutoApproveThreshold(), 0.01);
        verify(policyConfigRepository, times(1)).save(any(ReturnPolicyConfig.class));
    }

    @Test
    @DisplayName("Should update return policy configuration (AC5.7)")
    void shouldUpdateReturnPolicyConfiguration() {
        // Given
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));
        when(policyConfigRepository.save(any(ReturnPolicyConfig.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReturnPolicyConfig updatedPolicy = returnPolicyService.updatePolicy(
            45, // New return window
            null, // Keep restocking fee
            null, // Keep free return threshold
            150.0 // New auto-approve threshold
        );

        // Then
        assertNotNull(updatedPolicy);
        assertEquals(45, updatedPolicy.getReturnWindowDays());
        assertEquals(10.0, updatedPolicy.getRestockingFeePercentage(), 0.01); // Unchanged
        assertEquals(50.0, updatedPolicy.getFreeReturnThreshold(), 0.01); // Unchanged
        assertEquals(150.0, updatedPolicy.getAutoApproveThreshold(), 0.01);
        verify(policyConfigRepository, times(1)).save(any(ReturnPolicyConfig.class));
    }

    @Test
    @DisplayName("Should validate return window - within window (AC1.8)")
    void shouldValidateReturnWindowWithinWindow() {
        // Given
        LocalDate deliveryDate = LocalDate.now().minusDays(10); // 10 days ago
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));

        // When
        boolean isWithinWindow = returnPolicyService.isWithinReturnWindow(deliveryDate);

        // Then
        assertTrue(isWithinWindow);
    }

    @Test
    @DisplayName("Should validate return window - outside window")
    void shouldValidateReturnWindowOutsideWindow() {
        // Given
        LocalDate deliveryDate = LocalDate.now().minusDays(35); // 35 days ago, outside 30-day window
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));

        // When
        boolean isWithinWindow = returnPolicyService.isWithinReturnWindow(deliveryDate);

        // Then
        assertFalse(isWithinWindow);
    }

    @Test
    @DisplayName("Should validate return window - exactly at boundary")
    void shouldValidateReturnWindowAtBoundary() {
        // Given
        LocalDate deliveryDate = LocalDate.now().minusDays(30); // Exactly 30 days ago
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));

        // When
        boolean isWithinWindow = returnPolicyService.isWithinReturnWindow(deliveryDate);

        // Then
        assertTrue(isWithinWindow); // Should be within window (inclusive)
    }

    @Test
    @DisplayName("Should return false for null delivery date")
    void shouldReturnFalseForNullDeliveryDate() {
        // When
        boolean isWithinWindow = returnPolicyService.isWithinReturnWindow(null);

        // Then
        assertFalse(isWithinWindow);
    }

    @Test
    @DisplayName("Should calculate restocking fee (AC4.2)")
    void shouldCalculateRestockingFee() {
        // Given
        BigDecimal returnAmount = BigDecimal.valueOf(100.0);
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));

        // When
        BigDecimal restockingFee = returnPolicyService.calculateRestockingFee(returnAmount);

        // Then
        assertNotNull(restockingFee);
        assertEquals(10.0, restockingFee.doubleValue(), 0.01); // 10% of 100 = 10
    }

    @Test
    @DisplayName("Should return zero restocking fee when percentage is zero")
    void shouldReturnZeroRestockingFeeWhenPercentageIsZero() {
        // Given
        ReturnPolicyConfig zeroFeePolicy = new ReturnPolicyConfig(30, 0.0, 50.0, 100.0);
        BigDecimal returnAmount = BigDecimal.valueOf(100.0);
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(zeroFeePolicy));

        // When
        BigDecimal restockingFee = returnPolicyService.calculateRestockingFee(returnAmount);

        // Then
        assertEquals(0.0, restockingFee.doubleValue(), 0.01);
    }

    @Test
    @DisplayName("Should return zero for null return amount")
    void shouldReturnZeroForNullReturnAmount() {
        // When
        BigDecimal restockingFee = returnPolicyService.calculateRestockingFee(null);

        // Then
        assertEquals(BigDecimal.ZERO, restockingFee);
    }

    @Test
    @DisplayName("Should return zero for zero return amount")
    void shouldReturnZeroForZeroReturnAmount() {
        // Given
        BigDecimal returnAmount = BigDecimal.ZERO;

        // When
        BigDecimal restockingFee = returnPolicyService.calculateRestockingFee(returnAmount);

        // Then
        assertEquals(BigDecimal.ZERO, restockingFee);
    }

    @Test
    @DisplayName("Should check if qualifies for free return shipping (AC7.1)")
    void shouldCheckIfQualifiesForFreeReturnShipping() {
        // Given
        BigDecimal orderTotal = BigDecimal.valueOf(75.0); // Above 50.0 threshold
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));

        // When
        boolean qualifies = returnPolicyService.qualifiesForFreeReturn(orderTotal);

        // Then
        assertTrue(qualifies);
    }

    @Test
    @DisplayName("Should not qualify for free return shipping below threshold")
    void shouldNotQualifyForFreeReturnShippingBelowThreshold() {
        // Given
        BigDecimal orderTotal = BigDecimal.valueOf(25.0); // Below 50.0 threshold
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));

        // When
        boolean qualifies = returnPolicyService.qualifiesForFreeReturn(orderTotal);

        // Then
        assertFalse(qualifies);
    }

    @Test
    @DisplayName("Should return false for null order total")
    void shouldReturnFalseForNullOrderTotal() {
        // When
        boolean qualifies = returnPolicyService.qualifiesForFreeReturn(null);

        // Then
        assertFalse(qualifies);
    }

    @Test
    @DisplayName("Should check if should auto-approve based on value (AC3.1)")
    void shouldCheckIfShouldAutoApproveBasedOnValue() {
        // Given
        BigDecimal returnAmount = BigDecimal.valueOf(75.0); // Below 100.0 threshold
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));

        // When
        boolean shouldAutoApprove = returnPolicyService.shouldAutoApprove(returnAmount);

        // Then
        assertTrue(shouldAutoApprove);
    }

    @Test
    @DisplayName("Should not auto-approve above threshold")
    void shouldNotAutoApproveAboveThreshold() {
        // Given
        BigDecimal returnAmount = BigDecimal.valueOf(150.0); // Above 100.0 threshold
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));

        // When
        boolean shouldAutoApprove = returnPolicyService.shouldAutoApprove(returnAmount);

        // Then
        assertFalse(shouldAutoApprove);
    }

    @Test
    @DisplayName("Should auto-approve at threshold")
    void shouldAutoApproveAtThreshold() {
        // Given
        BigDecimal returnAmount = BigDecimal.valueOf(100.0); // Exactly at threshold
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));

        // When
        boolean shouldAutoApprove = returnPolicyService.shouldAutoApprove(returnAmount);

        // Then
        assertTrue(shouldAutoApprove); // Should be <= threshold
    }

    @Test
    @DisplayName("Should return false for null return amount in auto-approve check")
    void shouldReturnFalseForNullReturnAmountInAutoApproveCheck() {
        // When
        boolean shouldAutoApprove = returnPolicyService.shouldAutoApprove(null);

        // Then
        assertFalse(shouldAutoApprove);
    }

    @Test
    @DisplayName("Should update all policy fields")
    void shouldUpdateAllPolicyFields() {
        // Given
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));
        when(policyConfigRepository.save(any(ReturnPolicyConfig.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReturnPolicyConfig updatedPolicy = returnPolicyService.updatePolicy(
            45, 15.0, 75.0, 150.0
        );

        // Then
        assertEquals(45, updatedPolicy.getReturnWindowDays());
        assertEquals(15.0, updatedPolicy.getRestockingFeePercentage(), 0.01);
        assertEquals(75.0, updatedPolicy.getFreeReturnThreshold(), 0.01);
        assertEquals(150.0, updatedPolicy.getAutoApproveThreshold(), 0.01);
    }

    @Test
    @DisplayName("Should handle null values in policy update")
    void shouldHandleNullValuesInPolicyUpdate() {
        // Given
        when(policyConfigRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testPolicy));
        when(policyConfigRepository.save(any(ReturnPolicyConfig.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReturnPolicyConfig updatedPolicy = returnPolicyService.updatePolicy(
            null, null, null, null
        );

        // Then
        // All values should remain unchanged
        assertEquals(30, updatedPolicy.getReturnWindowDays());
        assertEquals(10.0, updatedPolicy.getRestockingFeePercentage(), 0.01);
        assertEquals(50.0, updatedPolicy.getFreeReturnThreshold(), 0.01);
        assertEquals(100.0, updatedPolicy.getAutoApproveThreshold(), 0.01);
    }
}

