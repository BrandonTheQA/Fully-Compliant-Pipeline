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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for order management
 */
@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /**
     * Create a new order
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Verify user exists
        verifyUserExists(request.getUserId());

        // Build order items with product validation
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        request.getItems().forEach(itemRequest -> {
            Product product = verifyProductAvailability(itemRequest.getProductId(), itemRequest.getQuantity());

            String productName = product.getName();
            Double price = product.getPrice();
            Integer quantity = itemRequest.getQuantity();
            Double subtotal = price * quantity;

            OrderItem orderItem = new OrderItem(
                itemRequest.getProductId(),
                productName,
                quantity,
                price
            );
            orderItems.add(orderItem);
            logger.debug("Prepared order item productId={}, price={}, quantity={}", itemRequest.getProductId(), price, quantity);
        });

        totalAmount = orderItems.stream()
            .mapToDouble(OrderItem::getSubtotal)
            .sum();

        // Create order
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(orderId, request.getUserId(), orderItems, totalAmount, "PENDING");
        Order savedOrder = orderRepository.save(order);

        logger.info("Created order {} for user {} with {} items", orderId, request.getUserId(), orderItems.size());
        return mapToResponse(savedOrder);
    }

    /**
     * Get order by ID
     */
    public OrderResponse getOrder(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        return mapToResponse(order);
    }

    /**
     * Get all orders for a user
     */
    public List<OrderResponse> getUserOrders(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        List<OrderResponse> responses = new ArrayList<>();
        for (Order order : orders) {
            responses.add(mapToResponse(order));
        }

        return responses;
    }

    /**
     * Verify user exists internally
     */
    private void verifyUserExists(String userId) {
        boolean exists = userRepository.existsById(userId);
        if (!exists) {
            logger.warn("User {} not found while creating order", userId);
            throw new OrderValidationException("User not found: " + userId);
        }
    }

    /**
     * Verify product availability and get product details
     */
    private Product verifyProductAvailability(String productId, Integer requestedQuantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new OrderValidationException("Product not found: " + productId));

        Integer availableQuantity = product.getQuantity();
        if (availableQuantity == null || availableQuantity < requestedQuantity) {
            throw new OrderValidationException(
                "Product " + productId + " does not have sufficient quantity. " +
                "Requested: " + requestedQuantity + ", Available: " + availableQuantity
            );
        }

        if (product.getPrice() == null) {
            throw new OrderValidationException("Product " + productId + " has no price");
        }

        return product;
    }

    /**
     * Map Order entity to OrderResponse DTO
     */
    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPrice(),
                item.getSubtotal()
            );
            itemResponses.add(itemResponse);
        }

        return new OrderResponse(
            order.getId(),
            order.getUserId(),
            itemResponses,
            order.getTotalAmount(),
            order.getStatus(),
            order.getCreatedAt() != null ? order.getCreatedAt().toString() : null,
            order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : null
        );
    }
}

