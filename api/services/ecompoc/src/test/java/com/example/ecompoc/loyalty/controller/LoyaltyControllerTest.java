package com.example.ecompoc.loyalty.controller;

import com.example.ecompoc.loyalty.dto.LoyaltyAccountResponse;
import com.example.ecompoc.loyalty.dto.LoyaltyDashboardResponse;
import com.example.ecompoc.loyalty.service.LoyaltyPointsService;
import com.example.ecompoc.loyalty.service.LoyaltyReferralService;
import com.example.ecompoc.loyalty.service.LoyaltyService;
import com.example.ecompoc.loyalty.service.LoyaltyTierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoyaltyControllerTest {
    
    private MockMvc mockMvc;
    private LoyaltyService loyaltyService;
    private LoyaltyPointsService pointsService;
    private LoyaltyTierService tierService;
    private LoyaltyReferralService referralService;
    
    @BeforeEach
    void setUp() {
        loyaltyService = mock(LoyaltyService.class);
        pointsService = mock(LoyaltyPointsService.class);
        tierService = mock(LoyaltyTierService.class);
        referralService = mock(LoyaltyReferralService.class);
        
        LoyaltyController controller = new LoyaltyController(
            loyaltyService, pointsService, tierService, referralService);
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }
    
    @Test
    void testGetBalance() throws Exception {
        LoyaltyAccountResponse response = new LoyaltyAccountResponse();
        response.setUserId("user123");
        response.setCurrentPoints(1000);
        response.setCurrentTier("BRONZE");
        
        when(loyaltyService.getAccount(anyString())).thenReturn(response);
        
        mockMvc.perform(get("/api/loyalty/balance")
                .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.currentPoints").value(1000));
    }
    
    @Test
    void testGetDashboard() throws Exception {
        LoyaltyDashboardResponse response = new LoyaltyDashboardResponse();
        LoyaltyAccountResponse account = new LoyaltyAccountResponse();
        account.setCurrentPoints(1000);
        account.setCurrentTier("BRONZE");
        response.setAccount(account);
        
        when(loyaltyService.getDashboard(anyString())).thenReturn(response);
        
        mockMvc.perform(get("/api/loyalty/dashboard")
                .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.account.currentPoints").value(1000));
    }
}
