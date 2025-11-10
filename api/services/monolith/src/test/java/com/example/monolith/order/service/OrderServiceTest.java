package com.example.monolith.order.service;

import com.example.monolith.order.dto.CreateOrderRequest;
import com.example.monolith.order.dto.OrderResponse;
import com.example.monolith.order.exception.OrderNotFoundException;
import com.example.monolith.order.exception.OrderValidationException;
import com.example.monolith.order.model.Order;
import com.example.monolith.order.model.OrderItem;
import com.example.monolith.order.repository.OrderRepository;
import com.example.monolith.product.model.Product;
import com.example.monolith.product.repository.ProductRepository;
import com.example.monolith.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(orderRepository, userRepository, productRepository);
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() {
        // Given
        String userId = "user-id";
        String productId = "product-id";
        CreateOrderRequest request = new CreateOrderRequest(userId,
            List.of(new CreateOrderRequest.OrderItemRequest(productId, 2)));

        when(userRepository.existsById(userId)).thenReturn(true);

        Product product = new Product(productId, "Product 1", "Description", 10.0, 5, "Category");
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(20.0, response.getTotalAmount());
        assertEquals(1, response.getItems().size());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(userId, savedOrder.getUserId());
        assertEquals(20.0, savedOrder.getTotalAmount());
        assertEquals("PENDING", savedOrder.getStatus());
        assertEquals(1, savedOrder.getItems().size());
    }

    @Test
    @DisplayName("Should throw OrderValidationException when user not found")
    void shouldThrowWhenUserNotFound() {
        String userId = "missing-user";
        CreateOrderRequest request = new CreateOrderRequest(userId,
            List.of(new CreateOrderRequest.OrderItemRequest("product-id", 1)));

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(OrderValidationException.class, () -> orderService.createOrder(request));

        verify(productRepository, never()).findById(any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw OrderValidationException when product not found")
    void shouldThrowWhenProductNotFound() {
        String userId = "user-id";
        String productId = "missing-product";
        CreateOrderRequest request = new CreateOrderRequest(userId,
            List.of(new CreateOrderRequest.OrderItemRequest(productId, 1)));

        when(userRepository.existsById(userId)).thenReturn(true);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(OrderValidationException.class, () -> orderService.createOrder(request));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw OrderValidationException when insufficient quantity")
    void shouldThrowWhenInsufficientQuantity() {
        String userId = "user-id";
        String productId = "product-id";
        CreateOrderRequest request = new CreateOrderRequest(userId,
            List.of(new CreateOrderRequest.OrderItemRequest(productId, 10)));

        when(userRepository.existsById(userId)).thenReturn(true);

        Product product = new Product(productId, "Product 1", "Description", 10.0, 5, "Category");
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(OrderValidationException.class, () -> orderService.createOrder(request));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should get order successfully")
    void shouldGetOrderSuccessfully() {
        String orderId = "order-id";
        Order order = new Order(orderId, "user-id",
            new ArrayList<>(List.of(new OrderItem("product-id", "Product 1", 1, 10.0))),
            10.0, "PENDING");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrder(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.getId());
        assertEquals("user-id", response.getUserId());
        assertEquals(10.0, response.getTotalAmount());
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order missing")
    void shouldThrowWhenOrderMissing() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder("missing"));
    }

    @Test
    @DisplayName("Should get user orders successfully")
    void shouldGetUserOrdersSuccessfully() {
        String userId = "user-id";
        Order order1 = new Order("order-1", userId,
            new ArrayList<>(List.of(new OrderItem("product-1", "Product 1", 1, 10.0))),
            10.0, "PENDING");
        Order order2 = new Order("order-2", userId,
            new ArrayList<>(List.of(new OrderItem("product-2", "Product 2", 2, 15.0))),
            30.0, "COMPLETED");

        when(orderRepository.findByUserId(userId)).thenReturn(Arrays.asList(order1, order2));

        List<OrderResponse> responses = orderService.getUserOrders(userId);

        assertEquals(2, responses.size());
        assertTrue(responses.stream().anyMatch(r -> r.getId().equals("order-1")));
        assertTrue(responses.stream().anyMatch(r -> r.getId().equals("order-2")));
    }

    @Test
    @DisplayName("Should return empty list when user has no orders")
    void shouldReturnEmptyListWhenNoOrders() {
        when(orderRepository.findByUserId("user-id")).thenReturn(List.of());
        List<OrderResponse> responses = orderService.getUserOrders("user-id");
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}

