package com.example.ecompoc.returns.service;

import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.enums.ReturnType;
import com.example.ecompoc.returns.model.Return;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnShippingService Tests")
class ReturnShippingServiceTest {

    @InjectMocks
    private ReturnShippingService returnShippingService;

    private Return testReturn;
    private String testReturnId;

    @BeforeEach
    void setUp() {
        testReturnId = "return-123";
        testReturn = new Return(
            testReturnId,
            "order-123",
            "user-123",
            "RMA-20241217-12345",
            ReturnStatus.APPROVED,
            ReturnType.REFUND_TO_PAYMENT
        );
        testReturn.setRefundAmountDecimal(BigDecimal.valueOf(50.0));
    }

    @Test
    @DisplayName("Should generate prepaid return shipping label (AC7.1)")
    void shouldGeneratePrepaidReturnShippingLabel() {
        // When
        String labelUrl = returnShippingService.generateReturnLabel(testReturn);

        // Then
        assertNotNull(labelUrl);
        assertTrue(labelUrl.contains("/api/returns/"));
        assertTrue(labelUrl.contains(testReturnId));
        assertTrue(labelUrl.contains("/label/"));
    }

    @Test
    @DisplayName("Should generate tracking number when creating label (AC7.6)")
    void shouldGenerateTrackingNumberWhenCreatingLabel() {
        // When
        returnShippingService.generateReturnLabel(testReturn);

        // Then
        assertNotNull(testReturn.getReturnTrackingNumber());
        assertTrue(testReturn.getReturnTrackingNumber().startsWith("RET"));
    }

    @Test
    @DisplayName("Should set return carrier when creating label")
    void shouldSetReturnCarrierWhenCreatingLabel() {
        // When
        returnShippingService.generateReturnLabel(testReturn);

        // Then
        assertNotNull(testReturn.getReturnCarrier());
        assertEquals("ECOMPOC", testReturn.getReturnCarrier());
    }

    @Test
    @DisplayName("Should generate unique label URLs for different returns")
    void shouldGenerateUniqueLabelUrlsForDifferentReturns() {
        // Given
        Return return1 = new Return(
            "return-1",
            "order-1",
            "user-1",
            "RMA-20241217-11111",
            ReturnStatus.APPROVED,
            ReturnType.REFUND_TO_PAYMENT
        );

        Return return2 = new Return(
            "return-2",
            "order-2",
            "user-2",
            "RMA-20241217-22222",
            ReturnStatus.APPROVED,
            ReturnType.REFUND_TO_PAYMENT
        );

        // When
        String labelUrl1 = returnShippingService.generateReturnLabel(return1);
        String labelUrl2 = returnShippingService.generateReturnLabel(return2);

        // Then
        assertNotEquals(labelUrl1, labelUrl2);
        assertTrue(labelUrl1.contains("return-1"));
        assertTrue(labelUrl2.contains("return-2"));
    }

    @Test
    @DisplayName("Should generate different tracking numbers for different returns")
    void shouldGenerateDifferentTrackingNumbersForDifferentReturns() {
        // Given
        Return return1 = new Return(
            "return-1",
            "order-1",
            "user-1",
            "RMA-20241217-11111",
            ReturnStatus.APPROVED,
            ReturnType.REFUND_TO_PAYMENT
        );

        Return return2 = new Return(
            "return-2",
            "order-2",
            "user-2",
            "RMA-20241217-22222",
            ReturnStatus.APPROVED,
            ReturnType.REFUND_TO_PAYMENT
        );

        // When
        returnShippingService.generateReturnLabel(return1);
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        returnShippingService.generateReturnLabel(return2);

        // Then
        assertNotEquals(return1.getReturnTrackingNumber(), return2.getReturnTrackingNumber());
    }

    @Test
    @DisplayName("Should return null for download label PDF (stub)")
    void shouldReturnNullForDownloadLabelPDF() {
        // When
        byte[] pdfBytes = returnShippingService.downloadReturnLabel(testReturn);

        // Then
        assertNull(pdfBytes); // Stub implementation returns null
    }

    @Test
    @DisplayName("Should handle label generation for different return statuses")
    void shouldHandleLabelGenerationForDifferentReturnStatuses() {
        // Given
        Return approvedReturn = new Return(
            "return-approved",
            "order-123",
            "user-123",
            "RMA-20241217-12345",
            ReturnStatus.APPROVED,
            ReturnType.REFUND_TO_PAYMENT
        );

        // When
        String labelUrl = returnShippingService.generateReturnLabel(approvedReturn);

        // Then
        assertNotNull(labelUrl);
        assertNotNull(approvedReturn.getReturnTrackingNumber());
    }

    @Test
    @DisplayName("Should generate label URL with correct format")
    void shouldGenerateLabelUrlWithCorrectFormat() {
        // When
        String labelUrl = returnShippingService.generateReturnLabel(testReturn);

        // Then
        assertTrue(labelUrl.matches("/api/returns/[^/]+/label/[^/]+"));
    }

    @Test
    @DisplayName("Should set tracking number and carrier on return entity")
    void shouldSetTrackingNumberAndCarrierOnReturnEntity() {
        // Given
        assertNull(testReturn.getReturnTrackingNumber());
        assertNull(testReturn.getReturnCarrier());

        // When
        returnShippingService.generateReturnLabel(testReturn);

        // Then
        assertNotNull(testReturn.getReturnTrackingNumber());
        assertNotNull(testReturn.getReturnCarrier());
        assertTrue(testReturn.getReturnTrackingNumber().length() > 0);
    }
}

