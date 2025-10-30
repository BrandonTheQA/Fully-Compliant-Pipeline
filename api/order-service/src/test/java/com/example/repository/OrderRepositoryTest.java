package com.example.repository;

import com.example.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderRepository Tests")
class OrderRepositoryTest {

    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository = new OrderRepository();
    }

    @Test
    @DisplayName("Should save order successfully")
    void shouldSaveOrderSuccessfully() {
        Order.OrderItem item = new Order.OrderItem("product-1", "Product 1", 2, 10.0);
        List<Order.OrderItem> items = new ArrayList<>();
        items.add(item);
        Order order = new Order("order-id", "user-id", items, 20.0, "PENDING");

        Order savedOrder = orderRepository.save(order);

        assertNotNull(savedOrder);
        assertEquals("order-id", savedOrder.getId());
        assertEquals("user-id", savedOrder.getUserId());
        assertEquals(1, savedOrder.getItems().size());
        assertEquals(20.0, savedOrder.getTotalAmount());
        assertEquals("PENDING", savedOrder.getStatus());
        assertNotNull(savedOrder.getCreatedAt());
    }

    @Test
    @DisplayName("Should find order by id")
    void shouldFindOrderById() {
        Order.OrderItem item = new Order.OrderItem("product-1", "Product 1", 2, 10.0);
        List<Order.OrderItem> items = new ArrayList<>();
        items.add(item);
        Order order = new Order("order-id", "user-id", items, 20.0, "PENDING");
        orderRepository.save(order);

        Optional<Order> foundOrder = orderRepository.findById("order-id");

        assertTrue(foundOrder.isPresent());
        assertEquals("order-id", foundOrder.get().getId());
        assertEquals("user-id", foundOrder.get().getUserId());
        assertEquals(20.0, foundOrder.get().getTotalAmount());
    }

    @Test
    @DisplayName("Should return empty when order not found by id")
    void shouldReturnEmptyWhenOrderNotFoundById() {
        Optional<Order> foundOrder = orderRepository.findById("non-existent-id");
        assertFalse(foundOrder.isPresent());
    }

    @Test
    @DisplayName("Should find orders by user id")
    void shouldFindOrdersByUserId() {
        Order.OrderItem item1 = new Order.OrderItem("product-1", "Product 1", 1, 10.0);
        List<Order.OrderItem> items1 = new ArrayList<>();
        items1.add(item1);
        Order order1 = new Order("order-1", "user-1", items1, 10.0, "PENDING");

        Order.OrderItem item2 = new Order.OrderItem("product-2", "Product 2", 2, 15.0);
        List<Order.OrderItem> items2 = new ArrayList<>();
        items2.add(item2);
        Order order2 = new Order("order-2", "user-1", items2, 30.0, "COMPLETED");

        Order.OrderItem item3 = new Order.OrderItem("product-3", "Product 3", 1, 20.0);
        List<Order.OrderItem> items3 = new ArrayList<>();
        items3.add(item3);
        Order order3 = new Order("order-3", "user-2", items3, 20.0, "PENDING");

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        List<Order> userOrders = orderRepository.findByUserId("user-1");

        assertEquals(2, userOrders.size());
        assertTrue(userOrders.stream().anyMatch(o -> o.getId().equals("order-1")));
        assertTrue(userOrders.stream().anyMatch(o -> o.getId().equals("order-2")));
        assertFalse(userOrders.stream().anyMatch(o -> o.getId().equals("order-3")));
    }

    @Test
    @DisplayName("Should return empty list when no orders for user")
    void shouldReturnEmptyListWhenNoOrdersForUser() {
        List<Order> userOrders = orderRepository.findByUserId("user-with-no-orders");
        assertTrue(userOrders.isEmpty());
    }

    @Test
    @DisplayName("Should count orders correctly")
    void shouldCountOrdersCorrectly() {
        assertEquals(0, orderRepository.count());

        Order.OrderItem item1 = new Order.OrderItem("product-1", "Product 1", 1, 10.0);
        List<Order.OrderItem> items1 = new ArrayList<>();
        items1.add(item1);
        Order order1 = new Order("order-1", "user-1", items1, 10.0, "PENDING");

        orderRepository.save(order1);
        assertEquals(1, orderRepository.count());

        Order.OrderItem item2 = new Order.OrderItem("product-2", "Product 2", 2, 15.0);
        List<Order.OrderItem> items2 = new ArrayList<>();
        items2.add(item2);
        Order order2 = new Order("order-2", "user-2", items2, 30.0, "PENDING");

        orderRepository.save(order2);
        assertEquals(2, orderRepository.count());
    }

    @Test
    @DisplayName("Should update existing order")
    void shouldUpdateExistingOrder() {
        Order.OrderItem item1 = new Order.OrderItem("product-1", "Product 1", 1, 10.0);
        List<Order.OrderItem> items1 = new ArrayList<>();
        items1.add(item1);
        Order originalOrder = new Order("order-id", "user-id", items1, 10.0, "PENDING");
        orderRepository.save(originalOrder);

        Order.OrderItem item2 = new Order.OrderItem("product-2", "Product 2", 2, 15.0);
        List<Order.OrderItem> items2 = new ArrayList<>();
        items2.add(item2);
        Order updatedOrder = new Order("order-id", "user-id", items2, 30.0, "COMPLETED");
        orderRepository.save(updatedOrder);

        Optional<Order> foundOrder = orderRepository.findById("order-id");
        assertTrue(foundOrder.isPresent());
        assertEquals("COMPLETED", foundOrder.get().getStatus());
        assertEquals(30.0, foundOrder.get().getTotalAmount());
        assertEquals(1, orderRepository.count());
    }

    @Test
    @DisplayName("Should handle null order gracefully")
    void shouldHandleNullOrderGracefully() {
        assertThrows(NullPointerException.class, () -> orderRepository.save(null));
    }
}


