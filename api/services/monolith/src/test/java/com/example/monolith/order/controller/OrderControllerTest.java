package com.example.monolith.order.controller;

import com.example.monolith.common.exception.GlobalExceptionHandler;
import com.example.monolith.order.dto.CreateOrderRequest;
import com.example.monolith.order.dto.OrderResponse;
import com.example.monolith.order.exception.OrderNotFoundException;
import com.example.monolith.order.exception.OrderValidationException;
import com.example.monolith.order.exception.ServiceUnavailableException;
import com.example.monolith.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for OrderController
 */
@DisplayName("OrderController Tests")
class OrderControllerTest {

    private MockMvc mockMvc;
    private OrderService orderService;
    private ObjectMapper objectMapper;
    private CreateOrderRequest createOrderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        OrderController orderController = new OrderController(orderService);

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        // Setup test data
        CreateOrderRequest.OrderItemRequest itemRequest = 
            new CreateOrderRequest.OrderItemRequest("product-id", 2);
        List<CreateOrderRequest.OrderItemRequest> items = new ArrayList<>();
        items.add(itemRequest);
        createOrderRequest = new CreateOrderRequest("user-id", items);
        
        OrderResponse.OrderItemResponse itemResponse = 
            new OrderResponse.OrderItemResponse("product-id", "Product 1", 2, 10.0, 20.0);
        List<OrderResponse.OrderItemResponse> itemResponses = new ArrayList<>();
        itemResponses.add(itemResponse);
        orderResponse = new OrderResponse("order-id", "user-id", itemResponses, 20.0, "PENDING", "2023-01-01T00:00:00", "2023-01-01T00:00:00");
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() throws Exception {
        // Given
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("order-id"))
                .andExpect(jsonPath("$.userId").value("user-id"))
                .andExpect(jsonPath("$.totalAmount").value(20.0))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].productId").value("product-id"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should get order successfully")
    void shouldGetOrderSuccessfully() throws Exception {
        // Given
        when(orderService.getOrder("order-id")).thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(get("/api/orders/order-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-id"))
                .andExpect(jsonPath("$.userId").value("user-id"))
                .andExpect(jsonPath("$.totalAmount").value(20.0))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).getOrder("order-id");
    }

    @Test
    @DisplayName("Should get user orders successfully")
    void shouldGetUserOrdersSuccessfully() throws Exception {
        // Given
        List<OrderResponse> orders = new ArrayList<>();
        orders.add(orderResponse);
        
        OrderResponse.OrderItemResponse itemResponse2 = 
            new OrderResponse.OrderItemResponse("product-id-2", "Product 2", 1, 15.0, 15.0);
        List<OrderResponse.OrderItemResponse> itemResponses2 = new ArrayList<>();
        itemResponses2.add(itemResponse2);
        OrderResponse orderResponse2 = new OrderResponse("order-id-2", "user-id", itemResponses2, 15.0, "COMPLETED", "2023-01-02T00:00:00", "2023-01-02T00:00:00");
        orders.add(orderResponse2);
        
        when(orderService.getUserOrders("user-id")).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders/user/user-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("order-id"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value("order-id-2"))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));

        verify(orderService).getUserOrders("user-id");
    }

    @Test
    @DisplayName("Should handle OrderNotFoundException")
    void shouldHandleOrderNotFoundException() throws Exception {
        when(orderService.getOrder("non-existent-id"))
                .thenThrow(new OrderNotFoundException("Order not found"));
        mockMvc.perform(get("/api/orders/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found"));
        verify(orderService).getOrder("non-existent-id");
    }

    @Test
    @DisplayName("Should handle OrderValidationException")
    void shouldHandleOrderValidationException() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new OrderValidationException("User not found: user-id"));
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("User not found: user-id"));
        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should handle ServiceUnavailableException")
    void shouldHandleServiceUnavailableException() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new ServiceUnavailableException("User service is unavailable"));
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.message").value("User service is unavailable"));
        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should handle validation errors for create order")
    void shouldHandleValidationErrorsForCreateOrder() throws Exception {
        // Given - invalid request with missing required fields
        CreateOrderRequest invalidRequest = new CreateOrderRequest("", new ArrayList<>());

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void shouldHandleEmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isInternalServerError());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isInternalServerError());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should return empty list when user has no orders")
    void shouldReturnEmptyListWhenUserHasNoOrders() throws Exception {
        // Given
        when(orderService.getUserOrders("user-with-no-orders")).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/orders/user/user-with-no-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(orderService).getUserOrders("user-with-no-orders");
    }
}

