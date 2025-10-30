package com.example.service;

import com.example.dto.CreateOrderRequest;
import com.example.dto.OrderResponse;
import com.example.exception.OrderNotFoundException;
import com.example.exception.OrderValidationException;
import com.example.exception.ServiceUnavailableException;
import com.example.model.Order;
import com.example.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private ObjectMapper objectMapper;

    private OrderService orderService;
    private String userServiceBaseUrl = "http://localhost:8081/api";
    private String productServiceBaseUrl = "http://localhost:8082/api";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService();
        ReflectionTestUtils.setField(orderService, "orderRepository", orderRepository);
        ReflectionTestUtils.setField(orderService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(orderService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(orderService, "userServiceBaseUrl", userServiceBaseUrl);
        ReflectionTestUtils.setField(orderService, "productServiceBaseUrl", productServiceBaseUrl);
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() throws Exception {
        String userId = "user-id";
        String productId = "product-id";

        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(productId, 2);
        List<CreateOrderRequest.OrderItemRequest> items = new ArrayList<>();
        items.add(itemRequest);
        CreateOrderRequest request = new CreateOrderRequest(userId, items);

        when(restTemplate.getForEntity(eq(userServiceBaseUrl + "/users/" + userId), eq(Object.class)))
            .thenReturn(new ResponseEntity<>(new Object(), HttpStatus.OK));

        String productJson = "{\"id\":\"product-id\",\"name\":\"Product 1\",\"price\":10.0,\"quantity\":5}";
        when(restTemplate.getForEntity(eq(productServiceBaseUrl + "/products/" + productId), eq(String.class)))
            .thenReturn(new ResponseEntity<>(productJson, HttpStatus.OK));

        JsonNode productNode = new ObjectMapper().readTree(productJson);
        when(objectMapper.readTree(productJson)).thenReturn(productNode);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId("order-id");
            return order;
        });

        OrderResponse result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals("order-id", result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(20.0, result.getTotalAmount());
        assertEquals("PENDING", result.getStatus());
        assertEquals(1, result.getItems().size());
    }

    @Test
    @DisplayName("Should throw OrderValidationException when user not found")
    void shouldThrowOrderValidationExceptionWhenUserNotFound() {
        String userId = "non-existent-user";
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest("product-id", 1);
        List<CreateOrderRequest.OrderItemRequest> items = new ArrayList<>();
        items.add(itemRequest);
        CreateOrderRequest request = new CreateOrderRequest(userId, items);

        when(restTemplate.getForEntity(eq(userServiceBaseUrl + "/users/" + userId), eq(Object.class)))
            .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThrows(OrderValidationException.class, () -> orderService.createOrder(request));
        verify(restTemplate).getForEntity(eq(userServiceBaseUrl + "/users/" + userId), eq(Object.class));
        verify(restTemplate, never()).getForEntity(contains("/products/"), any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw OrderValidationException when product not found")
    void shouldThrowOrderValidationExceptionWhenProductNotFound() {
        String userId = "user-id";
        String productId = "non-existent-product";

        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(productId, 1);
        List<CreateOrderRequest.OrderItemRequest> items = new ArrayList<>();
        items.add(itemRequest);
        CreateOrderRequest request = new CreateOrderRequest(userId, items);

        when(restTemplate.getForEntity(eq(userServiceBaseUrl + "/users/" + userId), eq(Object.class)))
            .thenReturn(new ResponseEntity<>(new Object(), HttpStatus.OK));

        when(restTemplate.getForEntity(eq(productServiceBaseUrl + "/products/" + productId), eq(String.class)))
            .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThrows(OrderValidationException.class, () -> orderService.createOrder(request));
        verify(restTemplate).getForEntity(eq(userServiceBaseUrl + "/users/" + userId), eq(Object.class));
        verify(restTemplate).getForEntity(eq(productServiceBaseUrl + "/products/" + productId), eq(String.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw OrderValidationException when insufficient quantity")
    void shouldThrowOrderValidationExceptionWhenInsufficientQuantity() throws Exception {
        String userId = "user-id";
        String productId = "product-id";

        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(productId, 10);
        List<CreateOrderRequest.OrderItemRequest> items = new ArrayList<>();
        items.add(itemRequest);
        CreateOrderRequest request = new CreateOrderRequest(userId, items);

        when(restTemplate.getForEntity(eq(userServiceBaseUrl + "/users/" + userId), eq(Object.class)))
            .thenReturn(new ResponseEntity<>(new Object(), HttpStatus.OK));

        String productJson = "{\"id\":\"product-id\",\"name\":\"Product 1\",\"price\":10.0,\"quantity\":5}";
        when(restTemplate.getForEntity(eq(productServiceBaseUrl + "/products/" + productId), eq(String.class)))
            .thenReturn(new ResponseEntity<>(productJson, HttpStatus.OK));

        JsonNode productNode = new ObjectMapper().readTree(productJson);
        when(objectMapper.readTree(productJson)).thenReturn(productNode);

        assertThrows(OrderValidationException.class, () -> orderService.createOrder(request));
        verify(restTemplate).getForEntity(eq(userServiceBaseUrl + "/users/" + userId), eq(Object.class));
        verify(restTemplate).getForEntity(eq(productServiceBaseUrl + "/products/" + productId), eq(String.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw ServiceUnavailableException when user service fails")
    void shouldThrowServiceUnavailableExceptionWhenUserServiceFails() {
        String userId = "user-id";
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest("product-id", 1);
        List<CreateOrderRequest.OrderItemRequest> items = new ArrayList<>();
        items.add(itemRequest);
        CreateOrderRequest request = new CreateOrderRequest(userId, items);

        when(restTemplate.getForEntity(eq(userServiceBaseUrl + "/users/" + userId), eq(Object.class)))
            .thenThrow(new RestClientException("Connection refused"));

        assertThrows(ServiceUnavailableException.class, () -> orderService.createOrder(request));
        verify(restTemplate).getForEntity(eq(userServiceBaseUrl + "/users/" + userId), eq(Object.class));
        verify(restTemplate, never()).getForEntity(contains("/products/"), any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should get order successfully")
    void shouldGetOrderSuccessfully() {
        String orderId = "order-id";
        String userId = "user-id";

        Order.OrderItem orderItem = new Order.OrderItem("product-id", "Product 1", 2, 10.0);
        List<Order.OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        Order order = new Order(orderId, userId, orderItems, 20.0, "PENDING");

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

        OrderResponse result = orderService.getOrder(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(20.0, result.getTotalAmount());
        assertEquals("PENDING", result.getStatus());
        assertEquals(1, result.getItems().size());
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order not found")
    void shouldThrowOrderNotFoundExceptionWhenOrderNotFound() {
        String orderId = "non-existent-order";
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(orderId));
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should get user orders successfully")
    void shouldGetUserOrdersSuccessfully() {
        String userId = "user-id";

        Order.OrderItem orderItem1 = new Order.OrderItem("product-1", "Product 1", 1, 10.0);
        List<Order.OrderItem> orderItems1 = new ArrayList<>();
        orderItems1.add(orderItem1);
        Order order1 = new Order("order-1", userId, orderItems1, 10.0, "PENDING");

        Order.OrderItem orderItem2 = new Order.OrderItem("product-2", "Product 2", 2, 15.0);
        List<Order.OrderItem> orderItems2 = new ArrayList<>();
        orderItems2.add(orderItem2);
        Order order2 = new Order("order-2", userId, orderItems2, 30.0, "COMPLETED");

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);

        when(orderRepository.findByUserId(userId)).thenReturn(orders);

        List<OrderResponse> result = orderService.getUserOrders(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(o -> o.getId().equals("order-1")));
        assertTrue(result.stream().anyMatch(o -> o.getId().equals("order-2")));
        verify(orderRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should return empty list when user has no orders")
    void shouldReturnEmptyListWhenUserHasNoOrders() {
        String userId = "user-with-no-orders";
        when(orderRepository.findByUserId(userId)).thenReturn(new ArrayList<>());
        List<OrderResponse> result = orderService.getUserOrders(userId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should calculate total amount correctly for multiple items")
    void shouldCalculateTotalAmountCorrectlyForMultipleItems() throws Exception {
        String userId = "user-id";

        CreateOrderRequest.OrderItemRequest itemRequest1 = new CreateOrderRequest.OrderItemRequest("product-1", 2);
        CreateOrderRequest.OrderItemRequest itemRequest2 = new CreateOrderRequest.OrderItemRequest("product-2", 3);
        List<CreateOrderRequest.OrderItemRequest> items = new ArrayList<>();
        items.add(itemRequest1);
        items.add(itemRequest2);
        CreateOrderRequest request = new CreateOrderRequest(userId, items);

        when(restTemplate.getForEntity(eq(userServiceBaseUrl + "/users/" + userId), eq(Object.class)))
            .thenReturn(new ResponseEntity<>(new Object(), HttpStatus.OK));

        String product1Json = "{\"id\":\"product-1\",\"name\":\"Product 1\",\"price\":10.0,\"quantity\":5}";
        when(restTemplate.getForEntity(eq(productServiceBaseUrl + "/products/product-1"), eq(String.class)))
            .thenReturn(new ResponseEntity<>(product1Json, HttpStatus.OK));
        JsonNode product1Node = new ObjectMapper().readTree(product1Json);
        when(objectMapper.readTree(product1Json)).thenReturn(product1Node);

        String product2Json = "{\"id\":\"product-2\",\"name\":\"Product 2\",\"price\":15.0,\"quantity\":5}";
        when(restTemplate.getForEntity(eq(productServiceBaseUrl + "/products/product-2"), eq(String.class)))
            .thenReturn(new ResponseEntity<>(product2Json, HttpStatus.OK));
        JsonNode product2Node = new ObjectMapper().readTree(product2Json);
        when(objectMapper.readTree(product2Json)).thenReturn(product2Node);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId("order-id");
            return order;
        });

        OrderResponse result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals(65.0, result.getTotalAmount());
        assertEquals(2, result.getItems().size());
    }
}


