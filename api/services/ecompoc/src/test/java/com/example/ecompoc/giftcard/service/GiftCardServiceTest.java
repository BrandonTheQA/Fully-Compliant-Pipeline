package com.example.ecompoc.giftcard.service;

import com.example.ecompoc.giftcard.model.GiftCard;
import com.example.ecompoc.giftcard.model.GiftCardStatus;
import com.example.ecompoc.giftcard.repository.GiftCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class GiftCardServiceTest {
    
    @Mock
    private GiftCardRepository giftCardRepository;
    
    @InjectMocks
    private GiftCardService giftCardService;
    
    private GiftCard testGiftCard;
    private String testCode;
    
    @BeforeEach
    void setUp() {
        testCode = "ABCD-EFGH-IJKL-MNOP";
        testGiftCard = new GiftCard();
        testGiftCard.setGiftCardId(UUID.randomUUID().toString());
        testGiftCard.setCode(testCode);
        testGiftCard.setAmount(BigDecimal.valueOf(100.00));
        testGiftCard.setBalance(BigDecimal.valueOf(100.00));
        testGiftCard.setStatus(GiftCardStatus.ACTIVE);
        testGiftCard.setExpirationDate(LocalDateTime.now().plusMonths(12));
        testGiftCard.setCreatedAt(LocalDateTime.now());
        testGiftCard.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void testFindById() {
        when(giftCardRepository.findById(anyString())).thenReturn(Optional.of(testGiftCard));
        
        Optional<GiftCard> result = giftCardService.findById(testGiftCard.getGiftCardId());
        
        assertTrue(result.isPresent());
        assertEquals(testGiftCard.getCode(), result.get().getCode());
    }
    
    @Test
    void testFindByCode() {
        when(giftCardRepository.findByCode(testCode)).thenReturn(Optional.of(testGiftCard));
        
        Optional<GiftCard> result = giftCardService.findByCode(testCode);
        
        assertTrue(result.isPresent());
        assertEquals(testCode, result.get().getCode());
    }
    
    @Test
    void testValidateGiftCard_Success() {
        BigDecimal redemptionAmount = BigDecimal.valueOf(50.00);
        
        assertDoesNotThrow(() -> {
            giftCardService.validateGiftCard(testGiftCard, redemptionAmount);
        });
    }
    
    @Test
    void testValidateGiftCard_NullCard() {
        assertThrows(IllegalArgumentException.class, () -> {
            giftCardService.validateGiftCard(null, BigDecimal.valueOf(50.00));
        });
    }
    
    @Test
    void testValidateGiftCard_Expired() {
        testGiftCard.setExpirationDate(LocalDateTime.now().minusDays(1));
        
        assertThrows(IllegalArgumentException.class, () -> {
            giftCardService.validateGiftCard(testGiftCard, BigDecimal.valueOf(50.00));
        });
    }
    
    @Test
    void testValidateGiftCard_InsufficientBalance() {
        BigDecimal redemptionAmount = BigDecimal.valueOf(150.00);
        
        assertThrows(IllegalArgumentException.class, () -> {
            giftCardService.validateGiftCard(testGiftCard, redemptionAmount);
        });
    }
    
    @Test
    void testValidateGiftCard_NotActive() {
        testGiftCard.setStatus(GiftCardStatus.EXPIRED);
        
        assertThrows(IllegalArgumentException.class, () -> {
            giftCardService.validateGiftCard(testGiftCard, BigDecimal.valueOf(50.00));
        });
    }
    
    @Test
    void testIsExpired_NotExpired() {
        assertFalse(giftCardService.isExpired(testGiftCard));
    }
    
    @Test
    void testIsExpired_Expired() {
        testGiftCard.setExpirationDate(LocalDateTime.now().minusDays(1));
        assertTrue(giftCardService.isExpired(testGiftCard));
    }
    
    @Test
    void testCheckExpiration_UpdatesStatus() {
        testGiftCard.setExpirationDate(LocalDateTime.now().minusDays(1));
        when(giftCardRepository.save(any(GiftCard.class))).thenReturn(testGiftCard);
        
        giftCardService.checkExpiration(testGiftCard);
        
        assertEquals(GiftCardStatus.EXPIRED, testGiftCard.getStatus());
        verify(giftCardRepository, times(1)).save(testGiftCard);
    }
    
    @Test
    void testUpdateBalance_Success() {
        BigDecimal redemptionAmount = BigDecimal.valueOf(50.00);
        when(giftCardRepository.findByCodeWithLock(testCode)).thenReturn(Optional.of(testGiftCard));
        when(giftCardRepository.save(any(GiftCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        GiftCard result = giftCardService.updateBalance(testCode, redemptionAmount);
        
        assertEquals(BigDecimal.valueOf(50.00), result.getBalance());
        verify(giftCardRepository, times(1)).save(any(GiftCard.class));
    }
    
    @Test
    void testUpdateBalance_FullyRedeemed() {
        BigDecimal redemptionAmount = BigDecimal.valueOf(100.00);
        when(giftCardRepository.findByCodeWithLock(testCode)).thenReturn(Optional.of(testGiftCard));
        when(giftCardRepository.save(any(GiftCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        GiftCard result = giftCardService.updateBalance(testCode, redemptionAmount);
        
        assertEquals(0, result.getBalance().compareTo(BigDecimal.ZERO));
        assertEquals(GiftCardStatus.REDEEMED, result.getStatus());
    }
    
    @Test
    void testUpdateBalance_InsufficientBalance() {
        BigDecimal redemptionAmount = BigDecimal.valueOf(150.00);
        when(giftCardRepository.findByCodeWithLock(testCode)).thenReturn(Optional.of(testGiftCard));
        
        assertThrows(IllegalArgumentException.class, () -> {
            giftCardService.updateBalance(testCode, redemptionAmount);
        });
    }
}
