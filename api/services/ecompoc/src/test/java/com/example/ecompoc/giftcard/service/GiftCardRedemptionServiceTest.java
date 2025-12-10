package com.example.ecompoc.giftcard.service;

import com.example.ecompoc.giftcard.model.GiftCard;
import com.example.ecompoc.giftcard.model.GiftCardStatus;
import com.example.ecompoc.giftcard.repository.GiftCardRepository;
import com.example.ecompoc.giftcard.repository.GiftCardTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class GiftCardRedemptionServiceTest {
    
    @Mock
    private GiftCardRepository giftCardRepository;
    
    @Mock
    private GiftCardTransactionRepository transactionRepository;
    
    @Mock
    private GiftCardService giftCardService;
    
    @InjectMocks
    private GiftCardRedemptionService redemptionService;
    
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
        
        when(giftCardRepository.findByCodeWithLock(testCode)).thenReturn(Optional.of(testGiftCard));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(giftCardService).validateGiftCard(any(), any());
        doNothing().when(giftCardService).checkExpiration(any());
    }
    
    @Test
    void testRedeemGiftCard_Success() {
        BigDecimal redemptionAmount = BigDecimal.valueOf(50.00);
        GiftCard updatedCard = new GiftCard();
        updatedCard.setBalance(BigDecimal.valueOf(50.00));
        updatedCard.setCode(testCode);
        
        when(giftCardService.updateBalance(testCode, redemptionAmount)).thenReturn(updatedCard);
        
        GiftCard result = redemptionService.redeemGiftCard(testCode, redemptionAmount);
        
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(50.00), result.getBalance());
        verify(giftCardService, times(1)).validateGiftCard(testGiftCard, redemptionAmount);
        verify(giftCardService, times(1)).updateBalance(testCode, redemptionAmount);
        verify(transactionRepository, times(1)).save(any());
    }
    
    @Test
    void testRedeemGiftCard_NotFound() {
        when(giftCardRepository.findByCodeWithLock(testCode)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> {
            redemptionService.redeemGiftCard(testCode, BigDecimal.valueOf(50.00));
        });
    }
    
    @Test
    void testApplyGiftCardToOrder_Success() {
        BigDecimal orderTotal = BigDecimal.valueOf(150.00);
        BigDecimal appliedAmount = BigDecimal.valueOf(100.00);
        GiftCard updatedCard = new GiftCard();
        updatedCard.setBalance(BigDecimal.ZERO);
        updatedCard.setCode(testCode);
        
        when(giftCardService.updateBalance(testCode, appliedAmount)).thenReturn(updatedCard);
        
        Map<String, Object> result = redemptionService.applyGiftCardToOrder(testCode, orderTotal, null);
        
        assertNotNull(result);
        assertEquals(appliedAmount, result.get("appliedAmount"));
        assertEquals(BigDecimal.ZERO, result.get("remainingBalance"));
        assertNotNull(result.get("giftCard"));
        verify(giftCardService, times(1)).checkExpiration(testGiftCard);
        verify(giftCardService, times(1)).updateBalance(testCode, appliedAmount);
    }
    
    @Test
    void testApplyGiftCardToOrder_PartialRedemption() {
        BigDecimal orderTotal = BigDecimal.valueOf(50.00);
        BigDecimal appliedAmount = BigDecimal.valueOf(50.00);
        GiftCard updatedCard = new GiftCard();
        updatedCard.setBalance(BigDecimal.valueOf(50.00));
        updatedCard.setCode(testCode);
        
        when(giftCardService.updateBalance(testCode, appliedAmount)).thenReturn(updatedCard);
        
        Map<String, Object> result = redemptionService.applyGiftCardToOrder(testCode, orderTotal, null);
        
        assertEquals(appliedAmount, result.get("appliedAmount"));
        assertEquals(BigDecimal.valueOf(50.00), result.get("remainingBalance"));
    }
    
    @Test
    void testApplyGiftCardToOrder_InsufficientBalance() {
        BigDecimal orderTotal = BigDecimal.valueOf(200.00);
        BigDecimal appliedAmount = BigDecimal.valueOf(100.00);
        GiftCard updatedCard = new GiftCard();
        updatedCard.setBalance(BigDecimal.ZERO);
        updatedCard.setCode(testCode);
        
        when(giftCardService.updateBalance(testCode, appliedAmount)).thenReturn(updatedCard);
        
        Map<String, Object> result = redemptionService.applyGiftCardToOrder(testCode, orderTotal, null);
        
        assertEquals(appliedAmount, result.get("appliedAmount")); // Should apply full balance
    }
    
    @Test
    void testApplyMultipleGiftCards_Success() {
        String code1 = "ABCD-EFGH-IJKL-MNOP";
        String code2 = "WXYZ-1234-5678-9ABC";
        
        GiftCard card1 = new GiftCard();
        card1.setCode(code1);
        card1.setBalance(BigDecimal.valueOf(50.00));
        card1.setStatus(GiftCardStatus.ACTIVE);
        card1.setExpirationDate(LocalDateTime.now().plusMonths(12));
        
        GiftCard card2 = new GiftCard();
        card2.setCode(code2);
        card2.setBalance(BigDecimal.valueOf(30.00));
        card2.setStatus(GiftCardStatus.ACTIVE);
        card2.setExpirationDate(LocalDateTime.now().plusMonths(12));
        
        when(giftCardRepository.findByCodeWithLock(code1)).thenReturn(Optional.of(card1));
        when(giftCardRepository.findByCodeWithLock(code2)).thenReturn(Optional.of(card2));
        
        GiftCard updatedCard1 = new GiftCard();
        updatedCard1.setBalance(BigDecimal.ZERO);
        updatedCard1.setCode(code1);
        
        GiftCard updatedCard2 = new GiftCard();
        updatedCard2.setBalance(BigDecimal.ZERO);
        updatedCard2.setCode(code2);
        
        when(giftCardService.updateBalance(code1, BigDecimal.valueOf(50.00))).thenReturn(updatedCard1);
        when(giftCardService.updateBalance(code2, BigDecimal.valueOf(30.00))).thenReturn(updatedCard2);
        
        List<String> codes = Arrays.asList(code1, code2);
        BigDecimal orderTotal = BigDecimal.valueOf(100.00);
        
        List<Map<String, Object>> results = redemptionService.applyMultipleGiftCards(codes, orderTotal, null);
        
        assertEquals(2, results.size());
        verify(giftCardService, times(2)).updateBalance(anyString(), any());
    }
}
