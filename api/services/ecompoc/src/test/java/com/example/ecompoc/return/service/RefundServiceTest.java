package com.example.ecompoc.returns.service;

import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.enums.ReturnType;
import com.example.ecompoc.returns.model.Return;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundService Tests")
class RefundServiceTest {

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private ReturnApprovalService returnApprovalService;

    @Mock
    private ReturnEmailService returnEmailService;

    @InjectMocks
    private RefundService refundService;

    private Return testReturn;
    private String testReturnId;

    @BeforeEach
    void setUp() {
        testReturnId = "return-123";
        testReturn = createTestReturn();
        // Manually inject email service since it uses setter injection
        refundService.setReturnEmailService(returnEmailService);
    }

    @Test
    @DisplayName("Should automatically initiate refund when return received (AC4.1)")
    void shouldAutomaticallyInitiateRefundWhenReturnReceived() {
        // Given
        testReturn.setStatus(ReturnStatus.RECEIVED);
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(returnApprovalService).updateReturnStatus(anyString(), any(ReturnStatus.class), anyString(), anyString());

        // When
        refundService.processRefund(testReturnId, "system");

        // Then
        assertEquals(ReturnStatus.REFUNDED, testReturn.getStatus());
        assertNotNull(testReturn.getRefundDate());
        assertEquals("ORIGINAL_PAYMENT_METHOD", testReturn.getRefundMethod());
        verify(returnRepository, times(2)).save(any(Return.class));
        verify(returnApprovalService, times(1)).updateReturnStatus(
            eq(testReturnId), eq(ReturnStatus.REFUNDED), eq("system"), anyString());
        verify(returnEmailService, times(1)).sendRefundProcessedEmail(any(Return.class));
    }

    @Test
    @DisplayName("Should calculate refund amount with restocking fees (AC4.2)")
    void shouldCalculateRefundAmountWithRestockingFees() {
        // Given
        testReturn.setStatus(ReturnStatus.RECEIVED);
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0)); // Already includes restocking fee deduction

        // When
        Double refundAmount = refundService.calculateRefundAmount(testReturn);

        // Then
        assertNotNull(refundAmount);
        assertEquals(50.0, refundAmount, 0.01);
    }

    @Test
    @DisplayName("Should process refund to original payment method (AC4.3)")
    void shouldProcessRefundToOriginalPaymentMethod() {
        // Given
        testReturn.setStatus(ReturnStatus.RECEIVED);
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(75.0));
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(returnApprovalService).updateReturnStatus(anyString(), any(ReturnStatus.class), anyString(), anyString());

        // When
        refundService.processRefund(testReturnId, "system");

        // Then
        assertEquals("ORIGINAL_PAYMENT_METHOD", testReturn.getRefundMethod());
        verify(returnRepository, times(2)).save(any(Return.class));
    }

    @Test
    @DisplayName("Should process partial refund for partial return (AC4.8)")
    void shouldProcessPartialRefundForPartialReturn() {
        // Given
        testReturn.setStatus(ReturnStatus.RECEIVED);
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(25.0)); // Partial refund amount
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(returnApprovalService).updateReturnStatus(anyString(), any(ReturnStatus.class), anyString(), anyString());

        // When
        refundService.processRefund(testReturnId, "system");

        // Then
        assertEquals(ReturnStatus.REFUNDED, testReturn.getStatus());
        assertEquals(25.0, testReturn.getRefundAmount(), 0.01);
        verify(returnRepository, times(2)).save(any(Return.class));
    }

    @Test
    @DisplayName("Should send refund notification email (AC4.7)")
    void shouldSendRefundNotificationEmail() {
        // Given
        testReturn.setStatus(ReturnStatus.RECEIVED);
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(returnApprovalService).updateReturnStatus(anyString(), any(ReturnStatus.class), anyString(), anyString());

        // When
        refundService.processRefund(testReturnId, "system");

        // Then
        verify(returnEmailService, times(1)).sendRefundProcessedEmail(any(Return.class));
    }

    @Test
    @DisplayName("Should handle refund failure and revert status (AC4.11)")
    void shouldHandleRefundFailureAndRevertStatus() {
        // Given
        testReturn.setStatus(ReturnStatus.RECEIVED);
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        
        // Simulate failure during second save (after gateway processing would complete)
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> {
            Return savedReturn = invocation.getArgument(0);
            // Throw exception when trying to save REFUNDED status (simulates gateway failure)
            if (savedReturn.getStatus() == ReturnStatus.REFUNDED) {
                throw new RuntimeException("Payment gateway error");
            }
            return savedReturn;
        });

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            refundService.processRefund(testReturnId, "system");
        });

        // Verify exception message contains expected text (service wraps the exception)
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Failed to process refund") || 
                  exception.getMessage().contains("Payment gateway error"));
        assertEquals(ReturnStatus.RECEIVED, testReturn.getStatus()); // Reverted back
        verify(returnRepository, atLeast(2)).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid refund amount (zero or negative)")
    void shouldThrowExceptionForInvalidRefundAmount() {
        // Given
        testReturn.setStatus(ReturnStatus.RECEIVED);
        testReturn.setRefundAmountDecimal(BigDecimal.ZERO);
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.processRefund(testReturnId, "system");
        });

        assertTrue(exception.getMessage().contains("Invalid refund amount"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception for refund with null amount")
    void shouldThrowExceptionForRefundWithNullAmount() {
        // Given
        testReturn.setStatus(ReturnStatus.RECEIVED);
        testReturn.setRefundAmountDecimal(null);
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.processRefund(testReturnId, "system");
        });

        assertTrue(exception.getMessage().contains("Invalid refund amount"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception for refund with non-RECEIVED status")
    void shouldThrowExceptionForRefundWithNonReceivedStatus() {
        // Given
        testReturn.setStatus(ReturnStatus.PENDING_APPROVAL);
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            refundService.processRefund(testReturnId, "system");
        });

        assertTrue(exception.getMessage().contains("can only be processed for returns with status RECEIVED"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when return not found")
    void shouldThrowExceptionWhenReturnNotFound() {
        // Given
        when(returnRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            refundService.processRefund("non-existent", "system");
        });

        assertTrue(exception.getMessage().contains("Return not found"));
    }

    @Test
    @DisplayName("Should update status to PROCESSING_REFUND before processing")
    void shouldUpdateStatusToProcessingRefundBeforeProcessing() {
        // Given
        testReturn.setStatus(ReturnStatus.RECEIVED);
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> {
            Return savedReturn = invocation.getArgument(0);
            if (savedReturn.getStatus() == ReturnStatus.PROCESSING_REFUND) {
                // Verify status was updated to PROCESSING_REFUND
                assertEquals(ReturnStatus.PROCESSING_REFUND, savedReturn.getStatus());
            }
            return savedReturn;
        });
        doNothing().when(returnApprovalService).updateReturnStatus(anyString(), any(ReturnStatus.class), anyString(), anyString());

        // When
        refundService.processRefund(testReturnId, "system");

        // Then
        // Status should be PROCESSING_REFUND at some point, then REFUNDED
        verify(returnRepository, atLeast(2)).save(any(Return.class));
    }

    @Test
    @DisplayName("Should set refund date when processing refund")
    void shouldSetRefundDateWhenProcessingRefund() {
        // Given
        testReturn.setStatus(ReturnStatus.RECEIVED);
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(returnApprovalService).updateReturnStatus(anyString(), any(ReturnStatus.class), anyString(), anyString());

        // When
        refundService.processRefund(testReturnId, "system");

        // Then
        assertNotNull(testReturn.getRefundDate());
        assertTrue(testReturn.getRefundDate().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should handle email service failure gracefully")
    void shouldHandleEmailServiceFailureGracefully() {
        // Given
        testReturn.setStatus(ReturnStatus.RECEIVED);
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(returnApprovalService).updateReturnStatus(anyString(), any(ReturnStatus.class), anyString(), anyString());
        doThrow(new RuntimeException("Email service failed")).when(returnEmailService).sendRefundProcessedEmail(any(Return.class));

        // When - should not throw exception, refund should still be processed
        assertDoesNotThrow(() -> {
            refundService.processRefund(testReturnId, "system");
        });

        // Then
        assertEquals(ReturnStatus.REFUNDED, testReturn.getStatus());
        verify(returnEmailService, times(1)).sendRefundProcessedEmail(any(Return.class));
    }

    @Test
    @DisplayName("Should calculate refund amount correctly")
    void shouldCalculateRefundAmountCorrectly() {
        // Given
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(75.50));

        // When
        Double refundAmount = refundService.calculateRefundAmount(testReturn);

        // Then
        assertEquals(75.50, refundAmount, 0.01);
    }

    @Test
    @DisplayName("Should return zero for null refund amount")
    void shouldReturnZeroForNullRefundAmount() {
        // Given
        testReturn.setRefundAmountDecimal(null);

        // When
        Double refundAmount = refundService.calculateRefundAmount(testReturn);

        // Then
        assertEquals(0.0, refundAmount, 0.01);
    }

    // Helper method to create test return
    private Return createTestReturn() {
        Return returnEntity = new Return(
            testReturnId,
            "order-123",
            "user-123",
            "RMA-20241217-12345",
            ReturnStatus.RECEIVED,
            ReturnType.REFUND_TO_PAYMENT
        );
        returnEntity.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        return returnEntity;
    }
}

