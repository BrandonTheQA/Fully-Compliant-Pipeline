package com.example.ecompoc.returns.service;

import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.enums.ReturnType;
import com.example.ecompoc.returns.model.Return;
import com.example.ecompoc.returns.model.ReturnPolicyConfig;
import com.example.ecompoc.returns.model.ReturnStatusHistory;
import com.example.ecompoc.returns.repository.ReturnRepository;
import com.example.ecompoc.returns.repository.ReturnStatusHistoryRepository;
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
@DisplayName("ReturnApprovalService Tests")
class ReturnApprovalServiceTest {

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private ReturnStatusHistoryRepository statusHistoryRepository;

    @Mock
    private ReturnPolicyService returnPolicyService;

    @Mock
    private ReturnShippingService returnShippingService;

    @Mock
    private ReturnEmailService returnEmailService;

    @InjectMocks
    private ReturnApprovalService returnApprovalService;

    private Return testReturn;
    private ReturnPolicyConfig testPolicy;
    private String testReturnId;

    @BeforeEach
    void setUp() {
        testReturnId = "return-123";
        testReturn = createTestReturn();
        testPolicy = new ReturnPolicyConfig(30, 10.0, 50.0, 100.0);
        // Manually inject email service since it uses setter injection
        returnApprovalService.setReturnEmailService(returnEmailService);
    }

    @Test
    @DisplayName("Should automatically approve standard returns (AC3.1)")
    void shouldAutomaticallyApproveStandardReturns() {
        // Given
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(75.0)); // Below auto-approve threshold
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnPolicyService.shouldAutoApprove(any(BigDecimal.class))).thenReturn(true);
        when(returnPolicyService.qualifiesForFreeReturn(any(BigDecimal.class))).thenReturn(true);
        when(returnShippingService.generateReturnLabel(any(Return.class))).thenReturn("http://label.url");
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        returnApprovalService.processAutomaticApproval(testReturn);

        // Then
        verify(returnRepository, times(2)).save(any(Return.class));
        verify(returnEmailService, times(1)).sendApprovalEmail(any(Return.class));
    }

    @Test
    @DisplayName("Should route high-value items to manual review (AC3.3)")
    void shouldRouteHighValueItemsToManualReview() {
        // Given
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(500.0)); // Above auto-approve threshold
        when(returnPolicyService.shouldAutoApprove(any(BigDecimal.class))).thenReturn(false);

        // When
        returnApprovalService.processAutomaticApproval(testReturn);

        // Then
        verify(returnRepository, never()).save(any(Return.class));
        verify(returnEmailService, never()).sendApprovalEmail(any(Return.class));
        // Status should remain PENDING_APPROVAL
        assertEquals(ReturnStatus.PENDING_APPROVAL, testReturn.getStatus());
    }

    @Test
    @DisplayName("Should manually approve return (AC3.9)")
    void shouldManuallyApproveReturn() {
        // Given
        testReturn.setStatus(ReturnStatus.PENDING_APPROVAL);
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnPolicyService.qualifiesForFreeReturn(any(BigDecimal.class))).thenReturn(true);
        when(returnShippingService.generateReturnLabel(any(Return.class))).thenReturn("http://label.url");
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        returnApprovalService.approveReturn(testReturnId, "admin-user", "Approved after review");

        // Then
        assertEquals(ReturnStatus.APPROVED, testReturn.getStatus());
        verify(returnRepository, times(2)).save(any(Return.class));
        verify(statusHistoryRepository, times(1)).save(any(ReturnStatusHistory.class));
        verify(returnEmailService, times(1)).sendApprovalEmail(any(Return.class));
    }

    @Test
    @DisplayName("Should manually reject return (AC3.9)")
    void shouldManuallyRejectReturn() {
        // Given
        testReturn.setStatus(ReturnStatus.PENDING_APPROVAL);
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        returnApprovalService.rejectReturn(testReturnId, "admin-user", "Item outside return window");

        // Then
        assertEquals(ReturnStatus.REJECTED, testReturn.getStatus());
        verify(returnRepository, times(1)).save(any(Return.class));
        verify(statusHistoryRepository, times(1)).save(any(ReturnStatusHistory.class));
        verify(returnEmailService, times(1)).sendRejectionEmail(any(Return.class), anyString());
    }

    @Test
    @DisplayName("Should update return status manually")
    void shouldUpdateReturnStatusManually() {
        // Given
        testReturn.setStatus(ReturnStatus.APPROVED);
        ReturnStatus oldStatus = testReturn.getStatus();
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        returnApprovalService.updateReturnStatus(testReturnId, ReturnStatus.IN_TRANSIT, "admin-user", "Status updated");

        // Then
        assertEquals(ReturnStatus.IN_TRANSIT, testReturn.getStatus());
        verify(returnRepository, times(1)).save(any(Return.class));
        verify(statusHistoryRepository, times(1)).save(any(ReturnStatusHistory.class));
        verify(returnEmailService, times(1)).sendStatusUpdateEmail(any(Return.class), eq(oldStatus), eq(ReturnStatus.IN_TRANSIT));
    }

    @Test
    @DisplayName("Should mark return as received (AC4.1)")
    void shouldMarkReturnAsReceived() {
        // Given
        testReturn.setStatus(ReturnStatus.IN_TRANSIT);
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        returnApprovalService.markReturnReceived(testReturnId, "admin-user", "Return received");

        // Then
        assertEquals(ReturnStatus.RECEIVED, testReturn.getStatus());
        verify(returnRepository, times(1)).save(any(Return.class));
        verify(statusHistoryRepository, times(1)).save(any(ReturnStatusHistory.class));
        verify(returnEmailService, times(1)).sendReceivedEmail(any(Return.class));
    }

    @Test
    @DisplayName("Should generate return label when approved (AC3.2)")
    void shouldGenerateReturnLabelWhenApproved() {
        // Given
        testReturn.setStatus(ReturnStatus.PENDING_APPROVAL);
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(75.0));
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnPolicyService.qualifiesForFreeReturn(any(BigDecimal.class))).thenReturn(true);
        when(returnShippingService.generateReturnLabel(any(Return.class))).thenReturn("http://label.url");
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        returnApprovalService.approveReturn(testReturnId, "admin-user", "Approved");

        // Then
        assertEquals("http://label.url", testReturn.getReturnLabelUrl());
        verify(returnShippingService, times(1)).generateReturnLabel(any(Return.class));
    }

    @Test
    @DisplayName("Should not generate label if not qualified for free return")
    void shouldNotGenerateLabelIfNotQualifiedForFreeReturn() {
        // Given
        testReturn.setStatus(ReturnStatus.PENDING_APPROVAL);
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnPolicyService.qualifiesForFreeReturn(any(BigDecimal.class))).thenReturn(false);
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        returnApprovalService.approveReturn(testReturnId, "admin-user", "Approved");

        // Then
        verify(returnShippingService, never()).generateReturnLabel(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when approving non-pending return")
    void shouldThrowExceptionWhenApprovingNonPendingReturn() {
        // Given
        testReturn.setStatus(ReturnStatus.APPROVED);
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            returnApprovalService.approveReturn(testReturnId, "admin-user", "Notes");
        });

        assertTrue(exception.getMessage().contains("cannot be approved"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when rejecting non-pending return")
    void shouldThrowExceptionWhenRejectingNonPendingReturn() {
        // Given
        testReturn.setStatus(ReturnStatus.APPROVED);
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            returnApprovalService.rejectReturn(testReturnId, "admin-user", "Reason");
        });

        assertTrue(exception.getMessage().contains("cannot be rejected"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when marking non-approved/in-transit return as received")
    void shouldThrowExceptionWhenMarkingInvalidStatusAsReceived() {
        // Given
        testReturn.setStatus(ReturnStatus.PENDING_APPROVAL);
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            returnApprovalService.markReturnReceived(testReturnId, "admin-user", "Notes");
        });

        assertTrue(exception.getMessage().contains("cannot be marked as received"));
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    @DisplayName("Should throw exception when return not found")
    void shouldThrowExceptionWhenReturnNotFound() {
        // Given
        when(returnRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            returnApprovalService.approveReturn("non-existent", "admin-user", "Notes");
        });

        assertTrue(exception.getMessage().contains("Return not found"));
    }

    @Test
    @DisplayName("Should handle label generation failure gracefully")
    void shouldHandleLabelGenerationFailureGracefully() {
        // Given
        testReturn.setStatus(ReturnStatus.PENDING_APPROVAL);
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnPolicyService.qualifiesForFreeReturn(any(BigDecimal.class))).thenReturn(true);
        when(returnShippingService.generateReturnLabel(any(Return.class)))
            .thenThrow(new RuntimeException("Label generation failed"));
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When - should not throw exception
        assertDoesNotThrow(() -> {
            returnApprovalService.approveReturn(testReturnId, "admin-user", "Approved");
        });

        // Then
        assertEquals(ReturnStatus.APPROVED, testReturn.getStatus());
        verify(returnEmailService, times(1)).sendApprovalEmail(any(Return.class));
    }

    @Test
    @DisplayName("Should handle email service failure gracefully")
    void shouldHandleEmailServiceFailureGracefully() {
        // Given
        testReturn.setStatus(ReturnStatus.PENDING_APPROVAL);
        when(returnRepository.findById(testReturnId)).thenReturn(Optional.of(testReturn));
        when(returnPolicyService.qualifiesForFreeReturn(any(BigDecimal.class))).thenReturn(false);
        when(returnRepository.save(any(Return.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any(ReturnStatusHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Email service failed")).when(returnEmailService).sendApprovalEmail(any(Return.class));

        // When - should not throw exception
        assertDoesNotThrow(() -> {
            returnApprovalService.approveReturn(testReturnId, "admin-user", "Approved");
        });

        // Then
        assertEquals(ReturnStatus.APPROVED, testReturn.getStatus());
        verify(returnEmailService, times(1)).sendApprovalEmail(any(Return.class));
    }

    @Test
    @DisplayName("Should skip automatic approval if return already processed")
    void shouldSkipAutomaticApprovalIfReturnAlreadyProcessed() {
        // Given
        testReturn.setStatus(ReturnStatus.APPROVED);

        // When
        returnApprovalService.processAutomaticApproval(testReturn);

        // Then
        verify(returnPolicyService, never()).shouldAutoApprove(any(BigDecimal.class));
        verify(returnRepository, never()).save(any(Return.class));
    }

    // Helper method to create test return
    private Return createTestReturn() {
        Return returnEntity = new Return(
            testReturnId,
            "order-123",
            "user-123",
            "RMA-20241217-12345",
            ReturnStatus.PENDING_APPROVAL,
            ReturnType.REFUND_TO_PAYMENT
        );
        returnEntity.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
        return returnEntity;
    }
}

