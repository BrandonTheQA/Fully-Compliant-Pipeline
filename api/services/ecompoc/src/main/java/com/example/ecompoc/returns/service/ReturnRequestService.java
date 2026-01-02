package com.example.ecompoc.returns.service;

import com.example.ecompoc.order.model.Order;
import com.example.ecompoc.order.model.OrderItem;
import com.example.ecompoc.order.repository.OrderRepository;
import com.example.ecompoc.returns.dto.CreateReturnRequest;
import com.example.ecompoc.returns.dto.ReturnResponse;
import com.example.ecompoc.returns.enums.ReturnReason;
import com.example.ecompoc.returns.enums.ReturnStatus;
import com.example.ecompoc.returns.enums.ReturnType;
import com.example.ecompoc.returns.model.Return;
import com.example.ecompoc.returns.model.ReturnItem;
import com.example.ecompoc.returns.model.ReturnStatusHistory;
import com.example.ecompoc.returns.repository.ReturnRepository;
import com.example.ecompoc.returns.repository.ReturnStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling return request submission, validation, and RMA generation
 */
@Service
public class ReturnRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReturnRequestService.class);
    
    private final ReturnRepository returnRepository;
    private final ReturnStatusHistoryRepository statusHistoryRepository;
    private final OrderRepository orderRepository;
    private final RMAGenerator rmaGenerator;
    private final ReturnPolicyService returnPolicyService;
    
    @Autowired
    public ReturnRequestService(ReturnRepository returnRepository,
                               ReturnStatusHistoryRepository statusHistoryRepository,
                               OrderRepository orderRepository,
                               RMAGenerator rmaGenerator,
                               ReturnPolicyService returnPolicyService) {
        this.returnRepository = returnRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.orderRepository = orderRepository;
        this.rmaGenerator = rmaGenerator;
        this.returnPolicyService = returnPolicyService;
    }
    
    /**
     * Create a return request
     */
    @Transactional
    public ReturnResponse createReturnRequest(CreateReturnRequest request) {
        logger.info("Creating return request for orderId={}, userId={}, items={}", 
            request.getOrderId(), request.getUserId(), request.getItems().size());
        
        try {
            // Validate order exists and belongs to user
            logger.debug("Looking up order: {}", request.getOrderId());
            Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> {
                    logger.error("Order not found: {}", request.getOrderId());
                    return new IllegalArgumentException("Order not found: " + request.getOrderId());
                });
            logger.debug("Order found: orderId={}, userId={}, itemsCount={}", 
                order.getId(), order.getUserId(), order.getItems() != null ? order.getItems().size() : 0);
            
            // Force eager loading of lazy collections within transaction
            // Access items to trigger lazy loading
            List<OrderItem> orderItems = order.getItems();
            if (orderItems == null || orderItems.isEmpty()) {
                logger.error("Order has no items: {}", request.getOrderId());
                throw new IllegalArgumentException("Order has no items: " + request.getOrderId());
            }
            // Ensure items are loaded by accessing them
            int itemsCount = orderItems.size();
            logger.debug("Order items loaded: count={}, itemIds={}", 
                itemsCount, orderItems.stream().map(OrderItem::getId).collect(Collectors.toList()));
            
            if (!order.getUserId().equals(request.getUserId())) {
                logger.error("Order userId mismatch: order.userId={}, request.userId={}", 
                    order.getUserId(), request.getUserId());
                throw new IllegalArgumentException("Order does not belong to user: " + request.getUserId());
            }
            
            // Validate return window
            logger.debug("Validating return window for order: {}", order.getId());
            LocalDate deliveryDate = order.getEstimatedDeliveryDate() != null 
                ? order.getEstimatedDeliveryDate().toLocalDate() 
                : order.getCreatedAt().toLocalDate();
            logger.debug("Delivery date: {}, order created: {}", deliveryDate, order.getCreatedAt());
            
            if (!returnPolicyService.isWithinReturnWindow(deliveryDate)) {
                logger.warn("Order outside return window: deliveryDate={}, windowDays={}", 
                    deliveryDate, returnPolicyService.getActivePolicy().getReturnWindowDays());
                throw new IllegalArgumentException(
                    String.format("Order is outside return window. Delivery date: %s, Return window: %d days",
                        deliveryDate.format(DateTimeFormatter.ISO_DATE),
                        returnPolicyService.getActivePolicy().getReturnWindowDays()));
            }
            
            // Validate return items
            logger.debug("Validating return items");
            validateReturnItems(order, request.getItems());
            logger.debug("Return items validated successfully");
            
            // Generate RMA number
            logger.debug("Generating RMA number");
            String rmaNumber = rmaGenerator.generateUniqueRMA();
            logger.debug("Generated RMA: {}", rmaNumber);
            
            // Create return entity
            String returnId = UUID.randomUUID().toString();
            ReturnType returnType = ReturnType.valueOf(request.getReturnType());
            Return returnEntity = new Return(returnId, order.getId(), request.getUserId(), 
                                            rmaNumber, ReturnStatus.PENDING_APPROVAL, returnType);
            logger.debug("Created return entity: returnId={}, rmaNumber={}, type={}", 
                returnId, rmaNumber, returnType);
            
            // Calculate total return amount
            BigDecimal totalReturnAmount = BigDecimal.ZERO;
            
            // Create return items
            logger.debug("Processing {} return items", request.getItems().size());
            for (CreateReturnRequest.ReturnItemRequest itemRequest : request.getItems()) {
                logger.debug("Processing return item: orderItemId={}, quantity={}, reason={}", 
                    itemRequest.getOrderItemId(), itemRequest.getQuantity(), itemRequest.getReturnReason());
                
                OrderItem orderItem = order.getItems().stream()
                    .filter(item -> item.getId().equals(itemRequest.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> {
                        logger.error("Order item not found: orderItemId={}, availableItemIds={}", 
                            itemRequest.getOrderItemId(), 
                            order.getItems().stream().map(OrderItem::getId).collect(Collectors.toList()));
                        return new IllegalArgumentException(
                            "Order item not found: " + itemRequest.getOrderItemId());
                    });
                logger.debug("Found order item: orderItemId={}, productId={}, price={}, quantity={}", 
                    orderItem.getId(), orderItem.getProductId(), orderItem.getPrice(), orderItem.getQuantity());
                
                // Validate quantity
                if (itemRequest.getQuantity() > orderItem.getQuantity()) {
                    logger.error("Return quantity exceeds order quantity: returnQty={}, orderQty={}, itemId={}", 
                        itemRequest.getQuantity(), orderItem.getQuantity(), orderItem.getId());
                    throw new IllegalArgumentException(
                        String.format("Return quantity (%d) exceeds order quantity (%d) for item %s",
                            itemRequest.getQuantity(), orderItem.getQuantity(), orderItem.getProductId()));
                }
                
                // Create return item
                ReturnReason returnReason = ReturnReason.valueOf(itemRequest.getReturnReason());
                ReturnItem returnItem = new ReturnItem(
                    itemRequest.getOrderItemId(),
                    orderItem.getProductId(),
                    orderItem.getProductName(),
                    itemRequest.getQuantity(),
                    returnReason,
                    orderItem.getPrice()
                );
                
                returnItem.setCondition(itemRequest.getCondition());
                returnItem.setComments(itemRequest.getComments());
                
                // Calculate refund amount for this item (original price * quantity)
                BigDecimal itemRefundAmount = orderItem.getPriceDecimal()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                
                // Apply restocking fee if applicable
                BigDecimal restockingFee = returnPolicyService.calculateRestockingFee(itemRefundAmount);
                BigDecimal netRefundAmount = itemRefundAmount.subtract(restockingFee);
                returnItem.setRefundAmountDecimal(netRefundAmount);
                
                logger.debug("Calculated refund for item: original={}, restockingFee={}, net={}", 
                    itemRefundAmount, restockingFee, netRefundAmount);
                
                totalReturnAmount = totalReturnAmount.add(netRefundAmount);
                returnEntity.addItem(returnItem);
            }
            
            returnEntity.setRefundAmountDecimal(totalReturnAmount);
            logger.debug("Total return amount: {}", totalReturnAmount);
            
            // Save return entity
            logger.debug("Saving return entity to database");
            returnEntity = returnRepository.save(returnEntity);
            logger.debug("Return entity saved: returnId={}", returnEntity.getReturnId());
            
            // Create initial status history
            logger.debug("Creating status history entry");
            ReturnStatusHistory statusHistory = new ReturnStatusHistory(
                returnEntity, ReturnStatus.PENDING_APPROVAL, 
                "Return request submitted", request.getUserId());
            statusHistoryRepository.save(statusHistory);
            logger.debug("Status history saved: historyId={}", statusHistory.getHistoryId());
            
            logger.info("Created return request {} with RMA {} for order {}", 
                returnId, rmaNumber, order.getId());
            
            // Force eager loading of lazy collections before mapping to response
            logger.debug("Loading lazy collections for response mapping");
            returnEntity.getItems().size();
            returnEntity.getStatusHistory().size();
            
            // Note: Automatic approval will be triggered by the controller after return creation
            logger.debug("Mapping return entity to response");
            ReturnResponse response = mapToResponse(returnEntity);
            logger.info("Return request created successfully: returnId={}, rmaNumber={}", 
                response.getReturnId(), response.getRmaNumber());
            return response;
            
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating return request: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating return request for orderId={}, userId={}: {}", 
                request.getOrderId(), request.getUserId(), e.getMessage(), e);
            logger.error("Exception class: {}", e.getClass().getName());
            if (e.getCause() != null) {
                logger.error("Caused by: {}", e.getCause().getMessage(), e.getCause());
            }
            throw e;
        }
    }
    
    /**
     * Get return entity by ID (internal use)
     */
    @Transactional(readOnly = true)
    public com.example.ecompoc.returns.model.Return getReturnEntityById(String returnId) {
        Return returnEntity = returnRepository.findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
        
        // Force eager loading of lazy collections within transaction
        returnEntity.getItems().size();
        returnEntity.getStatusHistory().size();
        
        return returnEntity;
    }
    
    /**
     * Get return by RMA number
     */
    @Transactional(readOnly = true)
    public ReturnResponse getReturnByRMA(String rmaNumber) {
        Return returnEntity = returnRepository.findByRmaNumber(rmaNumber)
            .orElseThrow(() -> new IllegalArgumentException("Return not found for RMA: " + rmaNumber));
        
        // Force eager loading of lazy collections within transaction
        returnEntity.getItems().size();
        returnEntity.getStatusHistory().size();
        
        return mapToResponse(returnEntity);
    }
    
    /**
     * Get return by ID
     */
    @Transactional(readOnly = true)
    public ReturnResponse getReturnById(String returnId) {
        Return returnEntity = returnRepository.findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
        
        // Force eager loading of lazy collections within transaction
        returnEntity.getItems().size();
        returnEntity.getStatusHistory().size();
        
        return mapToResponse(returnEntity);
    }
    
    /**
     * Get all returns for a user
     */
    @Transactional(readOnly = true)
    public List<ReturnResponse> getUserReturns(String userId) {
        List<Return> returns = returnRepository.findByUserId(userId);
        
        // Force eager loading of lazy collections within transaction
        for (Return returnEntity : returns) {
            returnEntity.getItems().size();
            returnEntity.getStatusHistory().size();
        }
        
        return returns.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Validate return items
     */
    private void validateReturnItems(Order order, List<CreateReturnRequest.ReturnItemRequest> returnItems) {
        if (returnItems == null || returnItems.isEmpty()) {
            throw new IllegalArgumentException("Return must contain at least one item");
        }
        
        // Check for duplicate order item IDs
        long uniqueItemIds = returnItems.stream()
            .map(CreateReturnRequest.ReturnItemRequest::getOrderItemId)
            .distinct()
            .count();
        
        if (uniqueItemIds != returnItems.size()) {
            throw new IllegalArgumentException("Duplicate order items in return request");
        }
        
        // Validate each item exists in order
        for (CreateReturnRequest.ReturnItemRequest itemRequest : returnItems) {
            boolean itemExists = order.getItems().stream()
                .anyMatch(item -> item.getId().equals(itemRequest.getOrderItemId()));
            
            if (!itemExists) {
                throw new IllegalArgumentException(
                    "Order item not found in order: " + itemRequest.getOrderItemId());
            }
        }
    }
    
    /**
     * Map Return entity to ReturnResponse DTO
     */
    private ReturnResponse mapToResponse(Return returnEntity) {
        ReturnResponse response = new ReturnResponse();
        response.setReturnId(returnEntity.getReturnId());
        response.setOrderId(returnEntity.getOrderId());
        response.setUserId(returnEntity.getUserId());
        response.setRmaNumber(returnEntity.getRmaNumber());
        response.setStatus(returnEntity.getStatus().name());
        response.setReturnType(returnEntity.getReturnType().name());
        response.setRefundAmount(returnEntity.getRefundAmount());
        response.setRefundMethod(returnEntity.getRefundMethod());
        response.setRefundDate(returnEntity.getRefundDate() != null 
            ? returnEntity.getRefundDate().toString() : null);
        response.setReturnTrackingNumber(returnEntity.getReturnTrackingNumber());
        response.setReturnCarrier(returnEntity.getReturnCarrier());
        response.setReturnLabelUrl(returnEntity.getReturnLabelUrl());
        response.setCreatedAt(returnEntity.getCreatedAt().toString());
        response.setUpdatedAt(returnEntity.getUpdatedAt().toString());
        
        // Map items
        List<com.example.ecompoc.returns.dto.ReturnItemResponse> itemResponses = new ArrayList<>();
        for (ReturnItem item : returnEntity.getItems()) {
            com.example.ecompoc.returns.dto.ReturnItemResponse itemResponse = 
                new com.example.ecompoc.returns.dto.ReturnItemResponse();
            itemResponse.setReturnItemId(item.getReturnItemId());
            itemResponse.setOrderItemId(item.getOrderItemId());
            itemResponse.setProductId(item.getProductId());
            itemResponse.setProductName(item.getProductName());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setReturnReason(item.getReturnReason().name());
            itemResponse.setCondition(item.getCondition());
            itemResponse.setComments(item.getComments());
            itemResponse.setOriginalPrice(item.getOriginalPrice());
            itemResponse.setRefundAmount(item.getRefundAmount());
            itemResponses.add(itemResponse);
        }
        response.setItems(itemResponses);
        
        // Map status history
        List<com.example.ecompoc.returns.dto.ReturnStatusHistoryResponse> historyResponses = new ArrayList<>();
        for (ReturnStatusHistory history : returnEntity.getStatusHistory()) {
            com.example.ecompoc.returns.dto.ReturnStatusHistoryResponse historyResponse = 
                new com.example.ecompoc.returns.dto.ReturnStatusHistoryResponse();
            historyResponse.setHistoryId(history.getHistoryId());
            historyResponse.setStatus(history.getStatus().name());
            historyResponse.setNotes(history.getNotes());
            historyResponse.setUpdatedBy(history.getUpdatedBy());
            historyResponse.setCreatedAt(history.getCreatedAt().toString());
            historyResponses.add(historyResponse);
        }
        response.setStatusHistory(historyResponses);
        
        return response;
    }
}

