package com.example.ecompoc.abandonedcart.service;

import com.example.ecompoc.abandonedcart.dto.AbandonedCartRequest;
import com.example.ecompoc.abandonedcart.dto.AbandonedCartResponse;
import com.example.ecompoc.abandonedcart.model.AbandonedCart;
import com.example.ecompoc.abandonedcart.repository.AbandonedCartRepository;
import com.example.ecompoc.order.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing abandoned carts
 */
@Service
public class AbandonedCartService {
    
    private static final Logger logger = LoggerFactory.getLogger(AbandonedCartService.class);
    
    private final AbandonedCartRepository abandonedCartRepository;
    private final OrderRepository orderRepository;
    private final AbandonedCartDiscountService discountService;
    private final ObjectMapper objectMapper;
    
    @Value("${abandoned-cart.enabled:false}")
    private boolean abandonedCartEnabled;
    
    public AbandonedCartService(AbandonedCartRepository abandonedCartRepository,
                               OrderRepository orderRepository,
                               AbandonedCartDiscountService discountService,
                               ObjectMapper objectMapper) {
        this.abandonedCartRepository = abandonedCartRepository;
        this.orderRepository = orderRepository;
        this.discountService = discountService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Save an abandoned cart
     */
    @Transactional
    public AbandonedCartResponse saveAbandonedCart(AbandonedCartRequest request) {
        if (!abandonedCartEnabled) {
            logger.debug("Abandoned cart feature is disabled");
            return null;
        }
        
        // Validate request
        if (request == null || request.getCartItems() == null || request.getCartItems().isEmpty()) {
            logger.warn("Invalid abandoned cart request: empty cart items");
            return null;
        }
        
        // Check if user is returning customer (2+ orders)
        boolean isReturningCustomer = false;
        if (request.getUserId() != null) {
            List<com.example.ecompoc.order.model.Order> userOrders = orderRepository.findByUserId(request.getUserId());
            isReturningCustomer = userOrders.size() >= 2;
        }
        
        // Calculate discount
        BigDecimal cartTotal = BigDecimal.valueOf(request.getCartTotal());
        AbandonedCartDiscountService.DiscountInfo discountInfo = discountService.calculateDiscount(
            cartTotal, request.getShippingRegion(), isReturningCustomer);
        
        // Create abandoned cart entity
        AbandonedCart abandonedCart = new AbandonedCart();
        abandonedCart.setId(UUID.randomUUID().toString());
        abandonedCart.setUserId(request.getUserId());
        abandonedCart.setEmail(request.getEmail());
        
        // Serialize cart items to JSON
        try {
            String cartItemsJson = objectMapper.writeValueAsString(request.getCartItems());
            abandonedCart.setCartItems(cartItemsJson);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize cart items", e);
            throw new RuntimeException("Failed to save abandoned cart", e);
        }
        
        abandonedCart.setCartTotal(cartTotal);
        abandonedCart.setShippingRegion(request.getShippingRegion());
        
        // Set discount information
        if (discountInfo != null) {
            abandonedCart.setDiscountCode(discountService.generateDiscountCode());
            abandonedCart.setDiscountType(discountInfo.getType());
            abandonedCart.setDiscountValue(discountInfo.getValue());
        }
        
        abandonedCart.setStatus("ABANDONED");
        LocalDateTime now = LocalDateTime.now();
        abandonedCart.setAbandonedAt(now);
        abandonedCart.setExpiresAt(now.plusHours(48)); // 48 hour expiration
        abandonedCart.setCreatedAt(now);
        abandonedCart.setUpdatedAt(now);
        
        // Save to database
        AbandonedCart savedCart = abandonedCartRepository.save(abandonedCart);
        
        logger.info("Saved abandoned cart: id={}, userId={}, cartTotal={}", 
            savedCart.getId(), savedCart.getUserId(), savedCart.getCartTotal());
        
        return mapToResponse(savedCart);
    }
    
    /**
     * Get abandoned cart by ID
     */
    public AbandonedCartResponse getAbandonedCart(String id) {
        if (!abandonedCartEnabled) {
            logger.debug("Abandoned cart feature is disabled");
            return null;
        }
        
        return abandonedCartRepository.findById(id)
            .map(this::mapToResponse)
            .orElse(null);
    }
    
    /**
     * Restore abandoned cart and apply discount
     */
    @Transactional
    public AbandonedCartResponse restoreCart(String id) {
        if (!abandonedCartEnabled) {
            logger.debug("Abandoned cart feature is disabled");
            return null;
        }
        
        AbandonedCart abandonedCart = abandonedCartRepository.findById(id)
            .orElse(null);
        
        if (abandonedCart == null) {
            logger.warn("Abandoned cart not found: id={}", id);
            return null;
        }
        
        // Check if cart is already recovered or expired
        if (!"ABANDONED".equals(abandonedCart.getStatus())) {
            logger.warn("Abandoned cart is not in ABANDONED status: id={}, status={}", 
                id, abandonedCart.getStatus());
            return null;
        }
        
        // Check if discount code is expired
        if (abandonedCart.getExpiresAt() != null && 
            LocalDateTime.now().isAfter(abandonedCart.getExpiresAt())) {
            logger.warn("Abandoned cart discount expired: id={}", id);
            abandonedCart.setStatus("EXPIRED");
            abandonedCart.setUpdatedAt(LocalDateTime.now());
            abandonedCartRepository.save(abandonedCart);
            return null;
        }
        
        // Mark as recovered
        abandonedCart.setStatus("RECOVERED");
        abandonedCart.setUpdatedAt(LocalDateTime.now());
        abandonedCartRepository.save(abandonedCart);
        
        logger.info("Restored abandoned cart: id={}", id);
        
        return mapToResponse(abandonedCart);
    }
    
    /**
     * Check if user is a returning customer (2+ orders)
     */
    public boolean isReturningCustomer(String userId) {
        if (userId == null) {
            return false;
        }
        List<com.example.ecompoc.order.model.Order> userOrders = orderRepository.findByUserId(userId);
        return userOrders.size() >= 2;
    }
    
    /**
     * Map entity to response DTO
     */
    private AbandonedCartResponse mapToResponse(AbandonedCart abandonedCart) {
        AbandonedCartResponse response = new AbandonedCartResponse();
        response.setId(abandonedCart.getId());
        response.setUserId(abandonedCart.getUserId());
        response.setEmail(abandonedCart.getEmail());
        
        // Deserialize cart items
        try {
            if (abandonedCart.getCartItems() != null) {
                List<AbandonedCartResponse.CartItemResponse> cartItems = 
                    objectMapper.readValue(abandonedCart.getCartItems(), 
                        objectMapper.getTypeFactory().constructCollectionType(
                            List.class, AbandonedCartResponse.CartItemResponse.class));
                response.setCartItems(cartItems);
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize cart items", e);
        }
        
        response.setCartTotal(abandonedCart.getCartTotal() != null ? 
            abandonedCart.getCartTotal().doubleValue() : null);
        response.setShippingRegion(abandonedCart.getShippingRegion());
        response.setDiscountCode(abandonedCart.getDiscountCode());
        response.setDiscountType(abandonedCart.getDiscountType());
        response.setDiscountValue(abandonedCart.getDiscountValue() != null ? 
            abandonedCart.getDiscountValue().doubleValue() : null);
        response.setStatus(abandonedCart.getStatus());
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        response.setAbandonedAt(abandonedCart.getAbandonedAt().format(formatter));
        if (abandonedCart.getExpiresAt() != null) {
            response.setExpiresAt(abandonedCart.getExpiresAt().format(formatter));
        }
        response.setCreatedAt(abandonedCart.getCreatedAt().format(formatter));
        response.setUpdatedAt(abandonedCart.getUpdatedAt().format(formatter));
        
        return response;
    }
}

