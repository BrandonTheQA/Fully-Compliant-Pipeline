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
        // Validate order exists and belongs to user
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + request.getOrderId()));
        
        if (!order.getUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("Order does not belong to user: " + request.getUserId());
        }
        
        // Validate return window
        LocalDate deliveryDate = order.getEstimatedDeliveryDate() != null 
            ? order.getEstimatedDeliveryDate().toLocalDate() 
            : order.getCreatedAt().toLocalDate();
        
        if (!returnPolicyService.isWithinReturnWindow(deliveryDate)) {
            throw new IllegalArgumentException(
                String.format("Order is outside return window. Delivery date: %s, Return window: %d days",
                    deliveryDate.format(DateTimeFormatter.ISO_DATE),
                    returnPolicyService.getActivePolicy().getReturnWindowDays()));
        }
        
        // Validate return items
        validateReturnItems(order, request.getItems());
        
        // Generate RMA number
        String rmaNumber = rmaGenerator.generateUniqueRMA();
        
        // Create return entity
        String returnId = UUID.randomUUID().toString();
        ReturnType returnType = ReturnType.valueOf(request.getReturnType());
        Return returnEntity = new Return(returnId, order.getId(), request.getUserId(), 
                                        rmaNumber, ReturnStatus.PENDING_APPROVAL, returnType);
        
        // Calculate total return amount
        BigDecimal totalReturnAmount = BigDecimal.ZERO;
        
        // Create return items
        for (CreateReturnRequest.ReturnItemRequest itemRequest : request.getItems()) {
            OrderItem orderItem = order.getItems().stream()
                .filter(item -> item.getId().equals(itemRequest.getOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "Order item not found: " + itemRequest.getOrderItemId()));
            
            // Validate quantity
            if (itemRequest.getQuantity() > orderItem.getQuantity()) {
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
            
            totalReturnAmount = totalReturnAmount.add(netRefundAmount);
            returnEntity.addItem(returnItem);
        }
        
        returnEntity.setRefundAmountDecimal(totalReturnAmount);
        returnEntity = returnRepository.save(returnEntity);
        
        // Create initial status history
        ReturnStatusHistory statusHistory = new ReturnStatusHistory(
            returnEntity, ReturnStatus.PENDING_APPROVAL, 
            "Return request submitted", request.getUserId());
        statusHistoryRepository.save(statusHistory);
        
        logger.info("Created return request {} with RMA {} for order {}", 
            returnId, rmaNumber, order.getId());
        
        // Note: Automatic approval will be triggered by the controller after return creation
        return mapToResponse(returnEntity);
    }
    
    /**
     * Get return entity by ID (internal use)
     */
    public com.example.ecompoc.returns.model.Return getReturnEntityById(String returnId) {
        return returnRepository.findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
    }
    
    /**
     * Get return by RMA number
     */
    public ReturnResponse getReturnByRMA(String rmaNumber) {
        Return returnEntity = returnRepository.findByRmaNumber(rmaNumber)
            .orElseThrow(() -> new IllegalArgumentException("Return not found for RMA: " + rmaNumber));
        
        return mapToResponse(returnEntity);
    }
    
    /**
     * Get return by ID
     */
    public ReturnResponse getReturnById(String returnId) {
        Return returnEntity = returnRepository.findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
        
        return mapToResponse(returnEntity);
    }
    
    /**
     * Get all returns for a user
     */
    public List<ReturnResponse> getUserReturns(String userId) {
        List<Return> returns = returnRepository.findByUserId(userId);
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

