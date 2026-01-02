package com.example.ecompoc.returns.service;

import com.example.ecompoc.returns.repository.ReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("RMAGenerator Tests")
class RMAGeneratorTest {

    @Mock
    private ReturnRepository returnRepository;

    @InjectMocks
    private RMAGenerator rmaGenerator;

    @BeforeEach
    void setUp() {
        when(returnRepository.existsByRmaNumber(anyString())).thenReturn(false);
    }

    @Test
    @DisplayName("Should generate unique RMA number with correct format (AC1.7)")
    void shouldGenerateUniqueRMANumberWithCorrectFormat() {
        // Given
        when(returnRepository.existsByRmaNumber(anyString())).thenReturn(false);

        // When
        String rmaNumber = rmaGenerator.generateUniqueRMA();

        // Then
        assertNotNull(rmaNumber);
        assertTrue(rmaNumber.startsWith("RMA-"));
        assertEquals(18, rmaNumber.length()); // RMA-YYYYMMDD-XXXXX = 18 chars
        assertTrue(rmaNumber.matches("^RMA-\\d{8}-\\d{5}$"));
        verify(returnRepository, times(1)).existsByRmaNumber(rmaNumber);
    }

    @Test
    @DisplayName("Should generate RMA number with date prefix (RMA-YYYYMMDD-XXXXX)")
    void shouldGenerateRMANumberWithDatePrefix() {
        // Given
        when(returnRepository.existsByRmaNumber(anyString())).thenReturn(false);

        // When
        String rmaNumber = rmaGenerator.generateUniqueRMA();

        // Then
        assertNotNull(rmaNumber);
        String[] parts = rmaNumber.split("-");
        assertEquals(3, parts.length);
        assertEquals("RMA", parts[0]);
        assertEquals(8, parts[1].length()); // Date part YYYYMMDD
        assertEquals(5, parts[2].length()); // Sequence part XXXXX
    }

    @Test
    @DisplayName("Should validate RMA format - valid format")
    void shouldValidateRMAFormatValid() {
        // Given
        String validRMA = "RMA-20241217-12345";

        // When
        boolean isValid = rmaGenerator.isValidFormat(validRMA);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should validate RMA format - invalid format")
    void shouldValidateRMAFormatInvalid() {
        // Given
        String invalidRMA1 = "RMA-2024-12-17-12345"; // Wrong format
        String invalidRMA2 = "RMA-20241217-123"; // Wrong sequence length
        String invalidRMA3 = "RMA-20241217-123456"; // Wrong sequence length
        String invalidRMA4 = "INVALID-20241217-12345"; // Wrong prefix
        String invalidRMA5 = null;
        String invalidRMA6 = "RMA-20241217-1234"; // Too short

        // When/Then
        assertFalse(rmaGenerator.isValidFormat(invalidRMA1));
        assertFalse(rmaGenerator.isValidFormat(invalidRMA2));
        assertFalse(rmaGenerator.isValidFormat(invalidRMA3));
        assertFalse(rmaGenerator.isValidFormat(invalidRMA4));
        assertFalse(rmaGenerator.isValidFormat(invalidRMA5));
        assertFalse(rmaGenerator.isValidFormat(invalidRMA6));
    }

    @Test
    @DisplayName("Should handle RMA uniqueness with retry logic")
    void shouldHandleRMAUniquenessWithRetryLogic() {
        // Given
        String duplicateRMA = "RMA-20241217-12345";
        String uniqueRMA = "RMA-20241217-67890";
        
        when(returnRepository.existsByRmaNumber(duplicateRMA)).thenReturn(true);
        when(returnRepository.existsByRmaNumber(uniqueRMA)).thenReturn(false);
        when(returnRepository.existsByRmaNumber(anyString())).thenAnswer(invocation -> {
            String rma = invocation.getArgument(0);
            return rma.equals(duplicateRMA);
        });

        // When
        String generatedRMA = rmaGenerator.generateUniqueRMA();

        // Then
        assertNotNull(generatedRMA);
        assertNotEquals(duplicateRMA, generatedRMA);
        assertTrue(rmaGenerator.isValidFormat(generatedRMA));
    }

    @Test
    @DisplayName("Should throw exception after max retries")
    void shouldThrowExceptionAfterMaxRetries() {
        // Given
        when(returnRepository.existsByRmaNumber(anyString())).thenReturn(true); // Always return duplicate

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rmaGenerator.generateUniqueRMA();
        });

        assertTrue(exception.getMessage().contains("Failed to generate unique RMA number"));
        verify(returnRepository, atLeast(10)).existsByRmaNumber(anyString()); // MAX_RETRIES = 10
    }

    @Test
    @DisplayName("Should generate multiple unique RMA numbers")
    void shouldGenerateMultipleUniqueRMANumbers() {
        // Given
        Set<String> generatedRMAs = new HashSet<>();
        when(returnRepository.existsByRmaNumber(anyString())).thenReturn(false);

        // When
        for (int i = 0; i < 10; i++) {
            String rma = rmaGenerator.generateUniqueRMA();
            generatedRMAs.add(rma);
        }

        // Then
        assertEquals(10, generatedRMAs.size()); // All should be unique
        for (String rma : generatedRMAs) {
            assertTrue(rmaGenerator.isValidFormat(rma));
        }
    }

    @Test
    @DisplayName("Should generate RMA with current date")
    void shouldGenerateRMAWithCurrentDate() {
        // Given
        when(returnRepository.existsByRmaNumber(anyString())).thenReturn(false);
        String currentDate = java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        // When
        String rmaNumber = rmaGenerator.generateUniqueRMA();

        // Then
        assertNotNull(rmaNumber);
        String[] parts = rmaNumber.split("-");
        assertEquals(currentDate, parts[1]); // Date part should match current date
    }

    @Test
    @DisplayName("Should generate RMA with 5-digit sequence")
    void shouldGenerateRMAWithFiveDigitSequence() {
        // Given
        when(returnRepository.existsByRmaNumber(anyString())).thenReturn(false);

        // When
        String rmaNumber = rmaGenerator.generateUniqueRMA();

        // Then
        assertNotNull(rmaNumber);
        String[] parts = rmaNumber.split("-");
        assertEquals(5, parts[2].length()); // Sequence should be 5 digits
        assertTrue(parts[2].matches("\\d{5}")); // Should be all digits
    }

    @Test
    @DisplayName("Should validate RMA format with correct length")
    void shouldValidateRMAFormatWithCorrectLength() {
        // Given
        String validRMA = "RMA-20241217-12345"; // Exactly 18 characters

        // When
        boolean isValid = rmaGenerator.isValidFormat(validRMA);

        // Then
        assertTrue(isValid);
        assertEquals(18, validRMA.length());
    }

    @Test
    @DisplayName("Should reject RMA with wrong length")
    void shouldRejectRMAWithWrongLength() {
        // Given
        String tooShort = "RMA-20241217-1234"; // 17 chars
        String tooLong = "RMA-20241217-123456"; // 19 chars

        // When/Then
        assertFalse(rmaGenerator.isValidFormat(tooShort));
        assertFalse(rmaGenerator.isValidFormat(tooLong));
    }

    @Test
    @DisplayName("Should handle empty string for RMA validation")
    void shouldHandleEmptyStringForRMAValidation() {
        // Given
        String emptyRMA = "";

        // When
        boolean isValid = rmaGenerator.isValidFormat(emptyRMA);

        // Then
        assertFalse(isValid);
    }
}

