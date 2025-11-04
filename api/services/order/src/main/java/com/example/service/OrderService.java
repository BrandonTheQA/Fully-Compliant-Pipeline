package com.example.service;

import com.example.dto.CreateOrderRequest;
import com.example.dto.OrderResponse;
import com.example.exception.OrderNotFoundException;
import com.example.exception.OrderValidationException;
import com.example.exception.ServiceUnavailableException;
import com.example.model.Order;
import com.example.model.OrderItem;
import com.example.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for order management
 */
@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
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
    
    @PostConstruct
    public void logServiceConfiguration() {
        logger.info("Order Service Configuration:");
        logger.info("  User Service URL: {}", userServiceBaseUrl);
        logger.info("  Product Service URL: {}", productServiceBaseUrl);
    }
    
    /**
     * Create a new order
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Verify user exists
        verifyUserExists(request.getUserId());
        
        // Build order items with product validation
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;
        
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            JsonNode product = verifyProductAvailability(itemRequest.getProductId(), itemRequest.getQuantity());
            
            String productName = product.get("name").asText();
            Double price = product.get("price").asDouble();
            Integer quantity = itemRequest.getQuantity();
            Double subtotal = price * quantity;
            
            OrderItem orderItem = new OrderItem(
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
        String url = userServiceBaseUrl + "/users/" + userId;
        logger.debug("Verifying user exists: userId={}, calling URL: {}", userId, url);
        
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            logger.debug("User verification successful: userId={}, status={}", userId, response.getStatusCode());
        } catch (HttpClientErrorException e) {
            logger.error("User service HTTP error: userId={}, url={}, status={}, message={}", 
                    userId, url, e.getStatusCode(), e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new OrderValidationException("User not found: " + userId);
            }
            throw new ServiceUnavailableException("User service error: " + e.getMessage() + " (Status: " + e.getStatusCode() + ")", e);
        } catch (ResourceAccessException e) {
            logger.error("User service connection error: userId={}, url={}, message={}", 
                    userId, url, e.getMessage());
            throw new ServiceUnavailableException(
                "Cannot connect to user service at " + url + ". Please verify the service is running and accessible. Error: " + e.getMessage(), e);
        } catch (RestClientException e) {
            logger.error("User service REST client error: userId={}, url={}, message={}", 
                    userId, url, e.getMessage());
            throw new ServiceUnavailableException("User service request failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error verifying user: userId={}, url={}, message={}", 
                    userId, url, e.getMessage(), e);
            throw new ServiceUnavailableException("User service is unavailable: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verify product availability and get product details
     */
    private JsonNode verifyProductAvailability(String productId, Integer requestedQuantity) {
        String url = productServiceBaseUrl + "/products/" + productId;
        logger.debug("Verifying product availability: productId={}, quantity={}, calling URL: {}", 
                productId, requestedQuantity, url);
        
        try {
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
            
            logger.debug("Product verification successful: productId={}, availableQuantity={}", 
                    productId, availableQuantity);
            return product;
        } catch (HttpClientErrorException e) {
            logger.error("Product service HTTP error: productId={}, url={}, status={}, message={}", 
                    productId, url, e.getStatusCode(), e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new OrderValidationException("Product not found: " + productId);
            }
            throw new ServiceUnavailableException("Product service error: " + e.getMessage() + " (Status: " + e.getStatusCode() + ")", e);
        } catch (OrderValidationException e) {
            throw e;
        } catch (ResourceAccessException e) {
            logger.error("Product service connection error: productId={}, url={}, message={}", 
                    productId, url, e.getMessage());
            throw new ServiceUnavailableException(
                "Cannot connect to product service at " + url + ". Please verify the service is running and accessible. Error: " + e.getMessage(), e);
        } catch (RestClientException e) {
            logger.error("Product service REST client error: productId={}, url={}, message={}", 
                    productId, url, e.getMessage());
            throw new ServiceUnavailableException("Product service request failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error verifying product: productId={}, url={}, message={}", 
                    productId, url, e.getMessage(), e);
            throw new ServiceUnavailableException("Product service is unavailable: " + e.getMessage(), e);
        }
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

