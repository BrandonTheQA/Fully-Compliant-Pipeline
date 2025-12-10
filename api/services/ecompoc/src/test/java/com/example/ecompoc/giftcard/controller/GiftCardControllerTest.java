package com.example.ecompoc.giftcard.controller;

import com.example.ecompoc.giftcard.dto.*;
import com.example.ecompoc.giftcard.model.GiftCard;
import com.example.ecompoc.giftcard.model.GiftCardStatus;
import com.example.ecompoc.giftcard.repository.GiftCardRepository;
import com.example.ecompoc.giftcard.repository.GiftCardTransactionRepository;
import com.example.ecompoc.giftcard.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GiftCardControllerTest {
    
    private MockMvc mockMvc;
    private GiftCardPurchaseService purchaseService;
    private GiftCardRedemptionService redemptionService;
    private GiftCardService giftCardService;
    private GiftCardEmailService emailService;
    private GiftCardRepository giftCardRepository;
    private GiftCardTransactionRepository transactionRepository;
    private GiftCardMapper mapper;
    private ObjectMapper objectMapper;
    
    private GiftCard testGiftCard;
    
    @BeforeEach
    void setUp() {
        purchaseService = mock(GiftCardPurchaseService.class);
        redemptionService = mock(GiftCardRedemptionService.class);
        giftCardService = mock(GiftCardService.class);
        emailService = mock(GiftCardEmailService.class);
        giftCardRepository = mock(GiftCardRepository.class);
        transactionRepository = mock(GiftCardTransactionRepository.class);
        mapper = mock(GiftCardMapper.class);
        objectMapper = new ObjectMapper();
        
        GiftCardController controller = new GiftCardController(
            purchaseService, redemptionService, giftCardService, emailService,
            giftCardRepository, transactionRepository, mapper);
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        
        testGiftCard = new GiftCard();
        testGiftCard.setGiftCardId(UUID.randomUUID().toString());
        testGiftCard.setCode("ABCD-EFGH-IJKL-MNOP");
        testGiftCard.setAmount(BigDecimal.valueOf(100.00));
        testGiftCard.setBalance(BigDecimal.valueOf(100.00));
        testGiftCard.setStatus(GiftCardStatus.ACTIVE);
        testGiftCard.setExpirationDate(LocalDateTime.now().plusMonths(12));
    }
    
    @Test
    void testPurchaseGiftCard() throws Exception {
        PurchaseGiftCardRequest request = new PurchaseGiftCardRequest();
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setQuantity(1);
        request.setPurchaserEmail("purchaser@example.com");
        
        GiftCardResponse response = new GiftCardResponse();
        response.setCode("ABCD-EFGH-IJKL-MNOP");
        response.setAmount(BigDecimal.valueOf(100.00));
        
        when(purchaseService.purchaseGiftCard(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(testGiftCard);
        when(mapper.toResponse(any())).thenReturn(response);
        
        mockMvc.perform(post("/api/gift-cards/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.giftCards").isArray())
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }
    
    @Test
    void testRedeemGiftCard() throws Exception {
        RedeemGiftCardRequest request = new RedeemGiftCardRequest();
        request.setCode("ABCD-EFGH-IJKL-MNOP");
        request.setRedemptionAmount(BigDecimal.valueOf(50.00));
        
        GiftCard updatedCard = new GiftCard();
        updatedCard.setBalance(BigDecimal.valueOf(50.00));
        updatedCard.setCode("ABCD-EFGH-IJKL-MNOP");
        
        GiftCardResponse response = new GiftCardResponse();
        response.setCode("ABCD-EFGH-IJKL-MNOP");
        response.setBalance(BigDecimal.valueOf(50.00));
        
        when(redemptionService.redeemGiftCard(anyString(), any())).thenReturn(updatedCard);
        when(mapper.toResponse(any())).thenReturn(response);
        
        mockMvc.perform(post("/api/gift-cards/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.remainingBalance").value(50.00));
    }
    
    @Test
    void testApplyGiftCard() throws Exception {
        ApplyGiftCardRequest request = new ApplyGiftCardRequest();
        request.setCode("ABCD-EFGH-IJKL-MNOP");
        request.setOrderTotal(BigDecimal.valueOf(150.00));
        
        Map<String, Object> result = new HashMap<>();
        result.put("appliedAmount", BigDecimal.valueOf(100.00));
        result.put("remainingBalance", BigDecimal.ZERO);
        result.put("giftCard", testGiftCard);
        
        GiftCardResponse response = new GiftCardResponse();
        response.setCode("ABCD-EFGH-IJKL-MNOP");
        
        when(redemptionService.applyGiftCardToOrder(anyString(), any(), any())).thenReturn(result);
        when(mapper.toResponse(any())).thenReturn(response);
        
        mockMvc.perform(post("/api/gift-cards/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedAmount").value(100.00))
                .andExpect(jsonPath("$.remainingBalance").value(0));
    }
    
    @Test
    void testCheckBalance() throws Exception {
        when(giftCardService.findByCode(anyString())).thenReturn(Optional.of(testGiftCard));
        doNothing().when(giftCardService).checkExpiration(any());
        
        mockMvc.perform(get("/api/gift-cards/balance/ABCD-EFGH-IJKL-MNOP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ABCD-EFGH-IJKL-MNOP"))
                .andExpect(jsonPath("$.balance").value(100.00));
    }
    
    @Test
    void testGetUserGiftCards() throws Exception {
        String userId = UUID.randomUUID().toString();
        when(giftCardRepository.findByPurchaserId(userId)).thenReturn(Arrays.asList(testGiftCard));
        
        GiftCardResponse response = new GiftCardResponse();
        response.setCode("ABCD-EFGH-IJKL-MNOP");
        when(mapper.toResponseList(any())).thenReturn(Arrays.asList(response));
        
        mockMvc.perform(get("/api/gift-cards/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void testResendEmail() throws Exception {
        String giftCardId = UUID.randomUUID().toString();
        when(giftCardService.findById(giftCardId)).thenReturn(Optional.of(testGiftCard));
        doNothing().when(emailService).resendGiftCardEmail(any());
        
        mockMvc.perform(post("/api/gift-cards/resend/" + giftCardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Gift card email resent successfully"));
    }
}
