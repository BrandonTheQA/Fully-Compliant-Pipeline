package com.example.ecompoc.pricealert.controller;

import com.example.ecompoc.common.exception.GlobalExceptionHandler;
import com.example.ecompoc.pricealert.dto.CreatePriceAlertRequest;
import com.example.ecompoc.pricealert.dto.PriceAlertListResponse;
import com.example.ecompoc.pricealert.dto.PriceAlertResponse;
import com.example.ecompoc.pricealert.dto.PriceHistoryResponse;
import com.example.ecompoc.pricealert.dto.UpdatePriceAlertRequest;
import com.example.ecompoc.pricealert.service.PriceAlertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PriceAlertController
 */
@DisplayName("PriceAlertController Tests")
class PriceAlertControllerTest {

    private MockMvc mockMvc;
    private PriceAlertService priceAlertService;
    private ObjectMapper objectMapper;
    private CreatePriceAlertRequest createRequest;
    private PriceAlertResponse alertResponse;
    private UpdatePriceAlertRequest updateRequest;

    @BeforeEach
    void setUp() {
        priceAlertService = mock(PriceAlertService.class);
        PriceAlertController controller = new PriceAlertController(priceAlertService);
        
        // Enable the feature toggle for testing
        ReflectionTestUtils.setField(controller, "priceAlertEnabled", true);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();
        
        // Setup test data
        createRequest = new CreatePriceAlertRequest();
        createRequest.setProductId("product-1");
        createRequest.setEmail("test@example.com");
        createRequest.setTargetPrice(80.0);
        createRequest.setNotificationFrequency("IMMEDIATE");
        
        alertResponse = new PriceAlertResponse();
        alertResponse.setAlertId("alert-1");
        alertResponse.setProductId("product-1");
        alertResponse.setUserEmail("test@example.com");
        alertResponse.setCurrentPrice(100.0);
        alertResponse.setTargetPrice(80.0);
        alertResponse.setStatus("ACTIVE");
        alertResponse.setNotificationFrequency("IMMEDIATE");
        alertResponse.setCreatedAt("2023-01-01T00:00:00");
        
        updateRequest = new UpdatePriceAlertRequest();
        updateRequest.setTargetPrice(75.0);
        updateRequest.setNotificationFrequency("DAILY_DIGEST");
    }

    @Test
    @DisplayName("Should create price alert successfully")
    void shouldCreatePriceAlertSuccessfully() throws Exception {
        // Given
        when(priceAlertService.createPriceAlert(any(CreatePriceAlertRequest.class))).thenReturn(alertResponse);

        // When & Then
        mockMvc.perform(post("/api/v2/price-alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertId").value("alert-1"))
                .andExpect(jsonPath("$.productId").value("product-1"))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.currentPrice").value(100.0))
                .andExpect(jsonPath("$.targetPrice").value(80.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(priceAlertService).createPriceAlert(any(CreatePriceAlertRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when create request fails")
    void shouldReturn400WhenCreateRequestFails() throws Exception {
        // Given
        when(priceAlertService.createPriceAlert(any(CreatePriceAlertRequest.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v2/price-alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(priceAlertService).createPriceAlert(any(CreatePriceAlertRequest.class));
    }

    @Test
    @DisplayName("Should get price alerts by email")
    void shouldGetPriceAlertsByEmail() throws Exception {
        // Given
        PriceAlertListResponse listResponse = new PriceAlertListResponse();
        listResponse.setAlerts(Collections.singletonList(alertResponse));
        when(priceAlertService.getPriceAlerts("test@example.com", null)).thenReturn(listResponse);

        // When & Then
        mockMvc.perform(get("/api/v2/price-alerts")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerts").isArray())
                .andExpect(jsonPath("$.alerts[0].alertId").value("alert-1"));

        verify(priceAlertService).getPriceAlerts("test@example.com", null);
    }

    @Test
    @DisplayName("Should get price alerts by userId")
    void shouldGetPriceAlertsByUserId() throws Exception {
        // Given
        PriceAlertListResponse listResponse = new PriceAlertListResponse();
        listResponse.setAlerts(Collections.singletonList(alertResponse));
        when(priceAlertService.getPriceAlerts(null, "user-1")).thenReturn(listResponse);

        // When & Then
        mockMvc.perform(get("/api/v2/price-alerts")
                .param("userId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerts").isArray());

        verify(priceAlertService).getPriceAlerts(null, "user-1");
    }

    @Test
    @DisplayName("Should get price alert by ID")
    void shouldGetPriceAlertById() throws Exception {
        // Given
        when(priceAlertService.getPriceAlert("alert-1")).thenReturn(alertResponse);

        // When & Then
        mockMvc.perform(get("/api/v2/price-alerts/alert-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value("alert-1"))
                .andExpect(jsonPath("$.productId").value("product-1"))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"));

        verify(priceAlertService).getPriceAlert("alert-1");
    }

    @Test
    @DisplayName("Should return 404 when alert not found")
    void shouldReturn404WhenAlertNotFound() throws Exception {
        // Given
        when(priceAlertService.getPriceAlert("non-existent")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v2/price-alerts/non-existent"))
                .andExpect(status().isNotFound());

        verify(priceAlertService).getPriceAlert("non-existent");
    }

    @Test
    @DisplayName("Should update price alert successfully")
    void shouldUpdatePriceAlertSuccessfully() throws Exception {
        // Given
        alertResponse.setTargetPrice(75.0);
        alertResponse.setNotificationFrequency("DAILY_DIGEST");
        when(priceAlertService.updatePriceAlert(eq("alert-1"), any(UpdatePriceAlertRequest.class)))
            .thenReturn(alertResponse);

        // When & Then
        mockMvc.perform(put("/api/v2/price-alerts/alert-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value("alert-1"))
                .andExpect(jsonPath("$.targetPrice").value(75.0))
                .andExpect(jsonPath("$.notificationFrequency").value("DAILY_DIGEST"));

        verify(priceAlertService).updatePriceAlert(eq("alert-1"), any(UpdatePriceAlertRequest.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent alert")
    void shouldReturn404WhenUpdatingNonExistentAlert() throws Exception {
        // Given
        when(priceAlertService.updatePriceAlert(eq("non-existent"), any(UpdatePriceAlertRequest.class)))
            .thenReturn(null);

        // When & Then
        mockMvc.perform(put("/api/v2/price-alerts/non-existent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(priceAlertService).updatePriceAlert(eq("non-existent"), any(UpdatePriceAlertRequest.class));
    }

    @Test
    @DisplayName("Should delete price alert successfully")
    void shouldDeletePriceAlertSuccessfully() throws Exception {
        // Given
        doNothing().when(priceAlertService).deletePriceAlert("alert-1");

        // When & Then
        mockMvc.perform(delete("/api/v2/price-alerts/alert-1"))
                .andExpect(status().isNoContent());

        verify(priceAlertService).deletePriceAlert("alert-1");
    }

    @Test
    @DisplayName("Should get price history for alert")
    void shouldGetPriceHistoryForAlert() throws Exception {
        // Given
        PriceHistoryResponse historyResponse = new PriceHistoryResponse();
        historyResponse.setPriceHistoryId("history-1");
        historyResponse.setProductId("product-1");
        historyResponse.setPrice(90.0);
        historyResponse.setPreviousPrice(100.0);
        historyResponse.setChangeType("DECREASE");
        historyResponse.setChangePercentage(10.0);
        
        when(priceAlertService.getPriceAlert("alert-1")).thenReturn(alertResponse);
        when(priceAlertService.getPriceHistory("product-1"))
            .thenReturn(Collections.singletonList(historyResponse));

        // When & Then
        mockMvc.perform(get("/api/v2/price-alerts/alert-1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].priceHistoryId").value("history-1"))
                .andExpect(jsonPath("$[0].price").value(90.0))
                .andExpect(jsonPath("$[0].changeType").value("DECREASE"));

        verify(priceAlertService).getPriceAlert("alert-1");
        verify(priceAlertService).getPriceHistory("product-1");
    }

    @Test
    @DisplayName("Should return 404 when getting history for non-existent alert")
    void shouldReturn404WhenGettingHistoryForNonExistentAlert() throws Exception {
        // Given
        when(priceAlertService.getPriceAlert("non-existent")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v2/price-alerts/non-existent/history"))
                .andExpect(status().isNotFound());

        verify(priceAlertService).getPriceAlert("non-existent");
        verify(priceAlertService, never()).getPriceHistory(anyString());
    }

    @Test
    @DisplayName("Should handle validation errors for create request")
    void shouldHandleValidationErrorsForCreateRequest() throws Exception {
        // Given - invalid request with missing required fields
        CreatePriceAlertRequest invalidRequest = new CreatePriceAlertRequest();
        invalidRequest.setEmail("invalid-email"); // Invalid email format

        // When & Then
        mockMvc.perform(post("/api/v2/price-alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(priceAlertService, never()).createPriceAlert(any(CreatePriceAlertRequest.class));
    }
}

