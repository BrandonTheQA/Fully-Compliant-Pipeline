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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class GiftCardPurchaseServiceTest {
    
    @Mock
    private GiftCardRepository giftCardRepository;
    
    @Mock
    private GiftCardTransactionRepository transactionRepository;
    
    @Mock
    private GiftCardCodeGenerator codeGenerator;
    
    @Mock
    private GiftCardEmailService emailService;
    
    @InjectMocks
    private GiftCardPurchaseService purchaseService;
    
    private String testCode;
    private String testPurchaserEmail;
    
    @BeforeEach
    void setUp() {
        testCode = "ABCD-EFGH-IJKL-MNOP";
        testPurchaserEmail = "purchaser@example.com";
        
        ReflectionTestUtils.setField(purchaseService, "minAmount", BigDecimal.valueOf(10));
        ReflectionTestUtils.setField(purchaseService, "maxAmount", BigDecimal.valueOf(1000));
        ReflectionTestUtils.setField(purchaseService, "expirationMonths", 12);
        
        when(codeGenerator.generateUniqueCode()).thenReturn(testCode);
        when(giftCardRepository.save(any(GiftCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendPurchaseConfirmationEmail(any());
        doNothing().when(emailService).sendGiftCardEmail(any());
    }
    
    @Test
    void testPurchaseGiftCard_Success() {
        BigDecimal amount = BigDecimal.valueOf(100.00);
        String purchaserId = UUID.randomUUID().toString();
        String recipientEmail = "recipient@example.com";
        
        GiftCard result = purchaseService.purchaseGiftCard(
            amount, purchaserId, testPurchaserEmail, recipientEmail, "Recipient Name",
            "Happy Birthday!", "birthday", null);
        
        assertNotNull(result);
        assertEquals(testCode, result.getCode());
        assertEquals(amount, result.getAmount());
        assertEquals(amount, result.getBalance());
        assertEquals(GiftCardStatus.ACTIVE, result.getStatus());
        assertEquals(purchaserId, result.getPurchaserId());
        assertEquals(testPurchaserEmail, result.getPurchaserEmail());
        assertEquals(recipientEmail, result.getRecipientEmail());
        
        verify(giftCardRepository, times(1)).save(any(GiftCard.class));
        verify(transactionRepository, times(1)).save(any());
        verify(emailService, times(1)).sendPurchaseConfirmationEmail(any());
        verify(emailService, times(1)).sendGiftCardEmail(any());
    }
    
    @Test
    void testPurchaseGiftCard_GuestPurchase() {
        BigDecimal amount = BigDecimal.valueOf(50.00);
        
        GiftCard result = purchaseService.purchaseGiftCard(
            amount, null, testPurchaserEmail, null, null, null, null, null);
        
        assertNotNull(result);
        assertEquals(testCode, result.getCode());
        assertNull(result.getPurchaserId());
        assertNull(result.getRecipientEmail());
    }
    
    @Test
    void testPurchaseGiftCard_AmountBelowMinimum() {
        BigDecimal amount = BigDecimal.valueOf(5.00);
        
        assertThrows(IllegalArgumentException.class, () -> {
            purchaseService.purchaseGiftCard(amount, null, testPurchaserEmail, null, null, null, null, null);
        });
    }
    
    @Test
    void testPurchaseGiftCard_AmountAboveMaximum() {
        BigDecimal amount = BigDecimal.valueOf(2000.00);
        
        assertThrows(IllegalArgumentException.class, () -> {
            purchaseService.purchaseGiftCard(amount, null, testPurchaserEmail, null, null, null, null, null);
        });
    }
    
    @Test
    void testPurchaseMultipleGiftCards_Success() {
        BigDecimal amount = BigDecimal.valueOf(50.00);
        int quantity = 3;
        
        List<GiftCard> results = purchaseService.purchaseMultipleGiftCards(
            amount, quantity, null, testPurchaserEmail, null, null, null, null, null);
        
        assertEquals(quantity, results.size());
        verify(giftCardRepository, times(quantity)).save(any(GiftCard.class));
        verify(transactionRepository, times(quantity)).save(any());
    }
    
    @Test
    void testPurchaseMultipleGiftCards_InvalidQuantity() {
        BigDecimal amount = BigDecimal.valueOf(50.00);
        
        assertThrows(IllegalArgumentException.class, () -> {
            purchaseService.purchaseMultipleGiftCards(amount, 0, null, testPurchaserEmail, null, null, null, null, null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            purchaseService.purchaseMultipleGiftCards(amount, 15, null, testPurchaserEmail, null, null, null, null, null);
        });
    }
    
    @Test
    void testPurchaseGiftCard_ScheduledDelivery() {
        BigDecimal amount = BigDecimal.valueOf(100.00);
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(7);
        
        GiftCard result = purchaseService.purchaseGiftCard(
            amount, null, testPurchaserEmail, "recipient@example.com", null, null, null, scheduledDate);
        
        assertEquals(scheduledDate, result.getScheduledDeliveryDate());
        verify(emailService, times(0)).sendGiftCardEmail(any()); // Should not send immediately
    }
}
