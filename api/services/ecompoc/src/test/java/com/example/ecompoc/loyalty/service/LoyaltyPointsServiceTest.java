package com.example.ecompoc.loyalty.service;

import com.example.ecompoc.loyalty.model.*;
import com.example.ecompoc.loyalty.repository.LoyaltyAccountRepository;
import com.example.ecompoc.loyalty.repository.LoyaltyTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoyaltyPointsServiceTest {
    
    @Mock
    private LoyaltyAccountRepository accountRepository;
    
    @Mock
    private LoyaltyTransactionRepository transactionRepository;
    
    @Mock
    private LoyaltyTierService tierService;
    
    @Mock
    private LoyaltyExpirationService expirationService;
    
    @Mock
    private ApplicationContext applicationContext;
    
    @InjectMocks
    private LoyaltyPointsService pointsService;
    
    private LoyaltyAccount testAccount;
    private String testUserId;
    private String testAccountId;
    
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        testAccountId = UUID.randomUUID().toString();
        
        testAccount = new LoyaltyAccount(testAccountId, testUserId, "REF123", EnrollmentSource.AUTO);
        testAccount.setCurrentPoints(1000);
        testAccount.setLifetimePointsEarned(1000);
        testAccount.setCurrentTier(LoyaltyTier.BRONZE);
        
        // Set test configuration values
        ReflectionTestUtils.setField(pointsService, "purchaseRate", 1);
        ReflectionTestUtils.setField(pointsService, "reviewPoints", 50);
        ReflectionTestUtils.setField(pointsService, "referralPoints", 100);
        ReflectionTestUtils.setField(pointsService, "welcomePoints", 100);
        ReflectionTestUtils.setField(pointsService, "redemptionRate", 100);
        ReflectionTestUtils.setField(pointsService, "redemptionMinimum", 500);
        ReflectionTestUtils.setField(pointsService, "maxRedemptionPercentage", 50);
        
        when(accountRepository.findByUserId(testUserId)).thenReturn(Optional.of(testAccount));
        when(tierService.applyTierMultiplier(any(), anyInt())).thenAnswer(invocation -> {
            LoyaltyTier tier = invocation.getArgument(0);
            Integer basePoints = invocation.getArgument(1);
            if (tier == LoyaltyTier.BRONZE) return basePoints;
            if (tier == LoyaltyTier.SILVER) return (int)(basePoints * 1.25);
            if (tier == LoyaltyTier.GOLD) return (int)(basePoints * 1.5);
            return (int)(basePoints * 2.0);
        });
    }
    
    @Test
    void testAwardPurchasePoints() {
        String orderId = UUID.randomUUID().toString();
        Double orderAmount = 100.0;
        
        when(transactionRepository.findByAccountIdAndActivityTypeAndRelatedOrderId(
            any(), any(), any())).thenReturn(Optional.empty());
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(expirationService).setExpirationDate(any());
        
        pointsService.awardPurchasePoints(testUserId, orderId, orderAmount);
        
        verify(transactionRepository, times(1)).save(any(LoyaltyTransaction.class));
        verify(accountRepository, times(1)).save(any(LoyaltyAccount.class));
    }
    
    @Test
    void testAwardWelcomePoints() {
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(expirationService).setExpirationDate(any());
        
        pointsService.awardWelcomePoints(testUserId);
        
        verify(transactionRepository, times(1)).save(any(LoyaltyTransaction.class));
        verify(accountRepository, times(1)).save(any(LoyaltyAccount.class));
    }
    
    @Test
    void testRedeemPoints_Success() {
        Integer pointsToRedeem = 500;
        Double orderTotal = 100.0;
        String orderId = UUID.randomUUID().toString();
        
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        LoyaltyPointsService.RedeemResult result = pointsService.redeemPoints(
            testUserId, pointsToRedeem, orderId, orderTotal);
        
        assertEquals(500, result.getPointsRedeemed());
        assertEquals(5.0, result.getDiscountAmount());
        verify(transactionRepository, times(1)).save(any(LoyaltyTransaction.class));
        verify(accountRepository, times(1)).save(any(LoyaltyAccount.class));
    }
    
    @Test
    void testRedeemPoints_InsufficientPoints() {
        Integer pointsToRedeem = 2000; // More than available
        Double orderTotal = 100.0;
        
        assertThrows(IllegalArgumentException.class, () -> {
            pointsService.redeemPoints(testUserId, pointsToRedeem, null, orderTotal);
        });
    }
    
    @Test
    void testRedeemPoints_BelowMinimum() {
        Integer pointsToRedeem = 200; // Below minimum
        Double orderTotal = 100.0;
        
        assertThrows(IllegalArgumentException.class, () -> {
            pointsService.validateRedemption(testUserId, pointsToRedeem, orderTotal);
        });
    }
    
    @Test
    void testRedeemPoints_MaxRedemptionLimit() {
        Integer pointsToRedeem = 6000; // Would be $60, but max is 50% of $100 = $50
        Double orderTotal = 100.0;
        String orderId = UUID.randomUUID().toString();
        
        // Set account with enough points
        testAccount.setCurrentPoints(10000);
        when(accountRepository.findByUserId(testUserId)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Should cap at $50 (5000 points) automatically
        LoyaltyPointsService.RedeemResult result = pointsService.redeemPoints(
            testUserId, pointsToRedeem, orderId, orderTotal);
        
        assertEquals(5000, result.getPointsRedeemed()); // Capped at max
        assertEquals(50.0, result.getDiscountAmount());
        verify(transactionRepository, times(1)).save(any(LoyaltyTransaction.class));
        verify(accountRepository, times(1)).save(any(LoyaltyAccount.class));
    }
}
