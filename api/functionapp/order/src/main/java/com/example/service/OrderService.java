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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for order management
 */
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${services.user.base-url}")
    private String userServiceBaseUrl;
    
    @Value("${services.product.base-url}")
    private String productServiceBaseUrl;
    
    /**
     * Create a new order
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Verify user exists
        verifyUserExists(request.getUserId());
        
        // Build order items with product validation
        List<Order.OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;
        
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            JsonNode product = verifyProductAvailability(itemRequest.getProductId(), itemRequest.getQuantity());
            
            String productName = product.get("name").asText();
            Double price = product.get("price").asDouble();
            Integer quantity = itemRequest.getQuantity();
            Double subtotal = price * quantity;
            
            Order.OrderItem orderItem = new Order.OrderItem(
                itemRequest.getProductId(),
                productName,
                quantity,
                price
            );
            orderItems.add(orderItem);
            totalAmount += subtotal;
        }
        
        // Create order
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(orderId, request.getUserId(), orderItems, totalAmount, "PENDING");
        Order savedOrder = orderRepository.save(order);
        
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
     * Verify user exists by calling User Service
     */
    private void verifyUserExists(String userId) {
        try {
            String url = userServiceBaseUrl + "/users/" + userId;
            restTemplate.getForEntity(url, Object.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new OrderValidationException("User not found: " + userId);
            }
            throw new ServiceUnavailableException("User service error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ServiceUnavailableException("User service is unavailable: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verify product availability and get product details
     */
    private JsonNode verifyProductAvailability(String productId, Integer requestedQuantity) {
        try {
            String url = productServiceBaseUrl + "/products/" + productId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            JsonNode product = objectMapper.readTree(response.getBody());
            
            // Check if product exists and has sufficient quantity
            Integer availableQuantity = product.get("quantity").asInt();
            if (availableQuantity == null || availableQuantity < requestedQuantity) {
                throw new OrderValidationException(
                    "Product " + productId + " does not have sufficient quantity. " +
                    "Requested: " + requestedQuantity + ", Available: " + availableQuantity
                );
            }
            
            // Validate price exists
            if (!product.has("price") || product.get("price").isNull()) {
                throw new OrderValidationException("Product " + productId + " has no price");
            }
            
            return product;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new OrderValidationException("Product not found: " + productId);
            }
            throw new ServiceUnavailableException("Product service error: " + e.getMessage(), e);
        } catch (OrderValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceUnavailableException("Product service is unavailable: " + e.getMessage(), e);
        }
    }
    
    /**
     * Map Order entity to OrderResponse DTO
     */
    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = new ArrayList<>();
        for (Order.OrderItem item : order.getItems()) {
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
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}

