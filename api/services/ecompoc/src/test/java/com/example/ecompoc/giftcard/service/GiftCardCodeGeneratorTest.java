package com.example.ecompoc.giftcard.service;

import com.example.ecompoc.giftcard.repository.GiftCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class GiftCardCodeGeneratorTest {
    
    @Mock
    private GiftCardRepository giftCardRepository;
    
    private GiftCardCodeGenerator codeGenerator;
    
    @BeforeEach
    void setUp() {
        codeGenerator = new GiftCardCodeGenerator(giftCardRepository);
    }
    
    @Test
    void testGenerateUniqueCode() {
        when(giftCardRepository.existsByCode(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        
        String code = codeGenerator.generateUniqueCode();
        
        assertNotNull(code);
        assertEquals(19, code.length()); // 16 chars + 3 hyphens
        assertTrue(code.matches("^[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}-[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}-[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}-[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}$"));
        assertFalse(code.contains("0"));
        assertFalse(code.contains("O"));
        assertFalse(code.contains("1"));
        assertFalse(code.contains("I"));
    }
    
    @Test
    void testIsValidFormat_ValidCode() {
        assertTrue(codeGenerator.isValidFormat("ABCD-EFGH-JKLM-NPQR"));
        assertTrue(codeGenerator.isValidFormat("2345-6789-ABCD-EFGH"));
    }
    
    @Test
    void testIsValidFormat_InvalidCode() {
        assertFalse(codeGenerator.isValidFormat("ABCD-EFGH-JKLM")); // Too short
        assertFalse(codeGenerator.isValidFormat("ABCD-EFGH-JKLM-NPQR-STUV")); // Too long
        assertFalse(codeGenerator.isValidFormat("ABCD-EFGH-JKLM-MN0P")); // Contains 0
        assertFalse(codeGenerator.isValidFormat("ABCD-EFGH-IJKL-MNOP")); // Contains I
        assertFalse(codeGenerator.isValidFormat("ABCD-EFGH-JKLO-MNOP")); // Contains O
        assertFalse(codeGenerator.isValidFormat(null));
    }
}
