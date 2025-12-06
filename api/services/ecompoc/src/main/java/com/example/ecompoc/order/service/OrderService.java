package com.example.ecompoc.order.service;

import com.example.ecompoc.order.dto.CreateOrderRequest;
import com.example.ecompoc.order.dto.OrderResponse;
import com.example.ecompoc.order.dto.OrderStatusHistoryResponse;
import com.example.ecompoc.order.dto.OrderTrackingResponse;
import com.example.ecompoc.order.enums.OrderStatus;
import com.example.ecompoc.order.exception.OrderNotFoundException;
import com.example.ecompoc.order.exception.OrderValidationException;
import com.example.ecompoc.order.model.Order;
import com.example.ecompoc.order.model.OrderItem;
import com.example.ecompoc.order.model.OrderStatusHistory;
import com.example.ecompoc.order.notification.service.OrderNotificationService;
import com.example.ecompoc.order.repository.OrderRepository;
import com.example.ecompoc.order.repository.OrderStatusHistoryRepository;
import com.example.ecompoc.order.service.OrderTrackingStreamService;
import com.example.ecompoc.loyalty.service.LoyaltyPointsService;
import com.example.ecompoc.loyalty.service.LoyaltyReferralService;
import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.service.StockDeductionService;
import com.example.ecompoc.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final DeliveryDateCalculatorService deliveryDateCalculatorService;
    private final TrackingNumberService trackingNumberService;
    private final OrderNotificationService notificationService;
    private final OrderTrackingStreamService streamService;
    private LoyaltyPointsService loyaltyPointsService;
    private LoyaltyReferralService loyaltyReferralService;
    private StockDeductionService stockDeductionService;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        OrderStatusHistoryRepository statusHistoryRepository,
                        DeliveryDateCalculatorService deliveryDateCalculatorService,
                        TrackingNumberService trackingNumberService,
                        OrderNotificationService notificationService,
                        OrderTrackingStreamService streamService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.deliveryDateCalculatorService = deliveryDateCalculatorService;
        this.trackingNumberService = trackingNumberService;
        this.notificationService = notificationService;
        this.streamService = streamService;
    }
    
    @Autowired(required = false)
    public void setLoyaltyPointsService(LoyaltyPointsService loyaltyPointsService) {
        this.loyaltyPointsService = loyaltyPointsService;
    }
    
    @Autowired(required = false)
    public void setLoyaltyReferralService(LoyaltyReferralService loyaltyReferralService) {
        this.loyaltyReferralService = loyaltyReferralService;
    }
    
    @Autowired(required = false)
    public void setStockDeductionService(StockDeductionService stockDeductionService) {
        this.stockDeductionService = stockDeductionService;
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

        // Create order first (we'll apply discount and redeem points after)
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(orderId, request.getUserId(), orderItems, totalAmount, OrderStatus.PENDING.name());
        
        // Calculate estimated delivery date
        LocalDateTime estimatedDelivery = deliveryDateCalculatorService.calculateEstimatedDelivery(order);
        order.setEstimatedDeliveryDate(estimatedDelivery);
        
        Order savedOrder = orderRepository.save(order);
        
        // Create initial status history entry
        createStatusHistoryEntry(savedOrder, OrderStatus.PENDING.name(), null, "Order created");

        // Deduct stock for all order items (if stock management enabled)
        if (stockDeductionService != null) {
            for (OrderItem item : orderItems) {
                try {
                    stockDeductionService.deductStock(item.getProductId(), item.getQuantity());
                    logger.debug("Deducted {} units from product {} stock", item.getQuantity(), item.getProductId());
                } catch (Exception e) {
                    logger.error("Failed to deduct stock for product {}: {}", item.getProductId(), e.getMessage());
                    // Rollback order creation if stock deduction fails
                    throw new OrderValidationException(
                        "Failed to deduct stock for product " + item.getProductId() + ": " + e.getMessage());
                }
            }
        }

        // Handle points redemption if applicable (after order is created so we have orderId)
        if (request.getPointsToRedeem() != null && request.getPointsToRedeem() > 0 && loyaltyPointsService != null) {
            try {
                LoyaltyPointsService.RedeemResult redeemResult = loyaltyPointsService.redeemPoints(
                    request.getUserId(), request.getPointsToRedeem(), orderId, totalAmount);
                double discountAmount = redeemResult.getDiscountAmount();
                // Update order total with discount
                savedOrder.setTotalAmount(Math.max(0, totalAmount - discountAmount));
                savedOrder = orderRepository.save(savedOrder);
                logger.info("Applied {} points redemption: ${} discount, new total: ${}", 
                    request.getPointsToRedeem(), discountAmount, savedOrder.getTotalAmount());
            } catch (Exception e) {
                logger.warn("Failed to redeem points for order: {}", e.getMessage());
                // Continue with order creation without redemption
            }
        }

        // Award loyalty points for purchase (after order is created and status is COMPLETED)
        // Note: Points are awarded when order status changes to COMPLETED, not at creation
        // This will be handled in updateOrderStatus when status changes to COMPLETED

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
     * Update order status
     */
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus, String location, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        String oldStatus = order.getStatus();
        order.setStatus(newStatus.name());
        order.setUpdatedAt(LocalDateTime.now());
        
        // If status is SHIPPED, generate tracking number if not already set
        if (newStatus == OrderStatus.SHIPPED && order.getTrackingNumber() == null) {
            String trackingNumber = trackingNumberService.generateTrackingNumber(order);
            order.setTrackingNumber(trackingNumber);
            order.setCarrierName("ECOMPOC"); // Default carrier for POC
        }
        
        // Update estimated delivery date if needed
        if (order.getEstimatedDeliveryDate() == null) {
            LocalDateTime estimatedDelivery = deliveryDateCalculatorService.calculateEstimatedDelivery(order);
            order.setEstimatedDeliveryDate(estimatedDelivery);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Create status history entry
        createStatusHistoryEntry(savedOrder, newStatus.name(), location, notes);
        
        // Trigger notification for status change
        try {
            OrderStatus oldStatusEnum = OrderStatus.valueOf(oldStatus);
            notificationService.sendStatusChangeNotification(savedOrder, oldStatusEnum, newStatus);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid old status '{}' for order {}, skipping notification", oldStatus, orderId);
        }
        
        // Award loyalty points when order is confirmed (completed/paid)
        if (newStatus == OrderStatus.CONFIRMED && loyaltyPointsService != null) {
            try {
                loyaltyPointsService.awardPurchasePoints(savedOrder.getUserId(), orderId, savedOrder.getTotalAmount());
                logger.info("Awarded loyalty points for completed order {}", orderId);
                
                // Process referral completion if this is the user's first completed order
                if (loyaltyReferralService != null) {
                    try {
                        // Check if this is user's first confirmed order
                        // Count confirmed orders excluding the current one
                        List<Order> userOrders = orderRepository.findByUserId(savedOrder.getUserId());
                        long previousConfirmedOrders = userOrders.stream()
                            .filter(o -> !o.getId().equals(orderId) && OrderStatus.CONFIRMED.name().equals(o.getStatus()))
                            .count();
                        
                        // If this is the first confirmed order, process referral
                        if (previousConfirmedOrders == 0) {
                            loyaltyReferralService.processReferralCompletion(savedOrder.getUserId(), orderId);
                            logger.info("Processed referral completion for user {} on order {}", savedOrder.getUserId(), orderId);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to process referral completion for order {}: {}", orderId, e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to award loyalty points for order {}: {}", orderId, e.getMessage());
                // Don't fail order status update if loyalty points fail
            }
        }
        
        // Reverse loyalty points if order is cancelled
        if (newStatus == OrderStatus.CANCELLED && loyaltyPointsService != null) {
            try {
                loyaltyPointsService.reversePoints(savedOrder.getUserId(), orderId);
                logger.info("Reversed loyalty points for cancelled order {}", orderId);
            } catch (Exception e) {
                logger.warn("Failed to reverse loyalty points for cancelled order {}: {}", orderId, e.getMessage());
            }
        }
        
        // Broadcast status update to SSE subscribers
        streamService.broadcastStatusUpdate(orderId, newStatus.name(), 
            "Order status updated to " + newStatus.name() + (notes != null ? ": " + notes : ""));
        
        logger.info("Updated order {} status from {} to {}", orderId, oldStatus, newStatus.name());
        return mapToResponse(savedOrder);
    }
    
    /**
     * Get order status history
     */
    public List<OrderStatusHistory> getOrderStatusHistory(String orderId) {
        // Verify order exists
        orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        return statusHistoryRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }
    
    /**
     * Get order status history as response DTOs
     */
    public List<OrderStatusHistoryResponse> getOrderStatusHistoryResponse(String orderId) {
        List<OrderStatusHistory> history = getOrderStatusHistory(orderId);
        List<OrderStatusHistoryResponse> responses = new ArrayList<>();
        
        for (OrderStatusHistory entry : history) {
            OrderStatusHistoryResponse response = new OrderStatusHistoryResponse(
                entry.getId(),
                entry.getStatus(),
                entry.getLocation(),
                entry.getNotes(),
                entry.getCreatedAt() != null ? entry.getCreatedAt().toString() : null
            );
            responses.add(response);
        }
        
        return responses;
    }
    
    /**
     * Get order tracking information
     */
    public OrderTrackingResponse getOrderTracking(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        List<OrderStatusHistoryResponse> statusHistory = getOrderStatusHistoryResponse(orderId);
        
        return new OrderTrackingResponse(
            order.getId(),
            order.getStatus(),
            order.getTrackingNumber(),
            order.getCarrierName(),
            order.getEstimatedDeliveryDate() != null ? order.getEstimatedDeliveryDate().toString() : null,
            order.getShippingAddress(),
            order.getShippingMethod(),
            order.getCurrentLocation(),
            statusHistory
        );
    }
    
    /**
     * Calculate estimated delivery date for an order
     */
    public LocalDateTime calculateEstimatedDeliveryDate(Order order) {
        return deliveryDateCalculatorService.calculateEstimatedDelivery(order);
    }
    
    /**
     * Generate tracking number for an order
     */
    public String generateTrackingNumber(Order order) {
        return trackingNumberService.generateTrackingNumber(order);
    }
    
    /**
     * Create a status history entry
     */
    private void createStatusHistoryEntry(Order order, String status, String location, String notes) {
        OrderStatusHistory history = new OrderStatusHistory(
            UUID.randomUUID().toString(),
            order,
            status,
            location,
            notes
        );
        statusHistoryRepository.save(history);
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
     * Uses stock-aware validation when stock management feature is enabled
     */
    private Product verifyProductAvailability(String productId, Integer requestedQuantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new OrderValidationException("Product not found: " + productId));

        // Use stock-aware validation if stock management is enabled
        if (stockDeductionService != null) {
            boolean hasStock = stockDeductionService.verifyStockAvailability(productId, requestedQuantity);
            if (!hasStock) {
                Integer availableQuantity = product.getQuantity();
                throw new OrderValidationException(
                    "Product " + productId + " does not have sufficient stock. " +
                    "Requested: " + requestedQuantity + ", Available: " + 
                    (availableQuantity != null ? availableQuantity : 0)
                );
            }
        } else {
            // Fallback to original validation if stock management is disabled
            Integer availableQuantity = product.getQuantity();
            if (availableQuantity == null || availableQuantity < requestedQuantity) {
                throw new OrderValidationException(
                    "Product " + productId + " does not have sufficient quantity. " +
                    "Requested: " + requestedQuantity + ", Available: " + availableQuantity
                );
            }
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

