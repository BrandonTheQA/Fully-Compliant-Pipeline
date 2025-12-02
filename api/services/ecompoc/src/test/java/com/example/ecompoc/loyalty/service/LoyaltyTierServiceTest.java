package com.example.ecompoc.loyalty.service;

import com.example.ecompoc.loyalty.model.LoyaltyTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class LoyaltyTierServiceTest {
    
    private LoyaltyTierService tierService;
    
    @BeforeEach
    void setUp() {
        tierService = new LoyaltyTierService();
        // Set test values
        ReflectionTestUtils.setField(tierService, "bronzeThreshold", 0);
        ReflectionTestUtils.setField(tierService, "bronzeMultiplier", 1.0);
        ReflectionTestUtils.setField(tierService, "silverThreshold", 1000);
        ReflectionTestUtils.setField(tierService, "silverMultiplier", 1.25);
        ReflectionTestUtils.setField(tierService, "goldThreshold", 2500);
        ReflectionTestUtils.setField(tierService, "goldMultiplier", 1.5);
        ReflectionTestUtils.setField(tierService, "platinumThreshold", 5000);
        ReflectionTestUtils.setField(tierService, "platinumMultiplier", 2.0);
    }
    
    @Test
    void testCalculateTier_Bronze() {
        assertEquals(LoyaltyTier.BRONZE, tierService.calculateTier(0));
        assertEquals(LoyaltyTier.BRONZE, tierService.calculateTier(500));
        assertEquals(LoyaltyTier.BRONZE, tierService.calculateTier(999));
    }
    
    @Test
    void testCalculateTier_Silver() {
        assertEquals(LoyaltyTier.SILVER, tierService.calculateTier(1000));
        assertEquals(LoyaltyTier.SILVER, tierService.calculateTier(1500));
        assertEquals(LoyaltyTier.SILVER, tierService.calculateTier(2499));
    }
    
    @Test
    void testCalculateTier_Gold() {
        assertEquals(LoyaltyTier.GOLD, tierService.calculateTier(2500));
        assertEquals(LoyaltyTier.GOLD, tierService.calculateTier(3000));
        assertEquals(LoyaltyTier.GOLD, tierService.calculateTier(4999));
    }
    
    @Test
    void testCalculateTier_Platinum() {
        assertEquals(LoyaltyTier.PLATINUM, tierService.calculateTier(5000));
        assertEquals(LoyaltyTier.PLATINUM, tierService.calculateTier(10000));
    }
    
    @Test
    void testGetTierMultiplier() {
        assertEquals(1.0, tierService.getTierMultiplier(LoyaltyTier.BRONZE));
        assertEquals(1.25, tierService.getTierMultiplier(LoyaltyTier.SILVER));
        assertEquals(1.5, tierService.getTierMultiplier(LoyaltyTier.GOLD));
        assertEquals(2.0, tierService.getTierMultiplier(LoyaltyTier.PLATINUM));
    }
    
    @Test
    void testApplyTierMultiplier() {
        assertEquals(100, tierService.applyTierMultiplier(LoyaltyTier.BRONZE, 100));
        assertEquals(125, tierService.applyTierMultiplier(LoyaltyTier.SILVER, 100));
        assertEquals(150, tierService.applyTierMultiplier(LoyaltyTier.GOLD, 100));
        assertEquals(200, tierService.applyTierMultiplier(LoyaltyTier.PLATINUM, 100));
    }
    
    @Test
    void testGetPointsToNextTier() {
        assertEquals(1000, tierService.getPointsToNextTier(0, LoyaltyTier.BRONZE));
        assertEquals(500, tierService.getPointsToNextTier(500, LoyaltyTier.BRONZE));
        assertEquals(1500, tierService.getPointsToNextTier(1000, LoyaltyTier.SILVER));
        assertEquals(0, tierService.getPointsToNextTier(5000, LoyaltyTier.PLATINUM));
    }
}
