package com.example.ecompoc.returns.service;

import com.example.ecompoc.order.dto.CreateOrderRequest;
import com.example.ecompoc.order.dto.OrderResponse;
import com.example.ecompoc.order.service.OrderService;
import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.returns.dto.ExchangeRequest;
import com.example.ecompoc.returns.model.Return;
import com.example.ecompoc.returns.repository.ReturnRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling exchange requests, price difference calculations, and new order creation
 */
@Service
public class ExchangeService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExchangeService.class);
    
    private final ReturnRepository returnRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;
    
    @Autowired
    public ExchangeService(ReturnRepository returnRepository,
                          ProductRepository productRepository,
                          OrderService orderService) {
        this.returnRepository = returnRepository;
        this.productRepository = productRepository;
        this.orderService = orderService;
    }
    
    /**
     * Process exchange request
     * Creates a new order for the exchange item and handles price differences
     */
    @Transactional
    public OrderResponse processExchange(String returnId, ExchangeRequest exchangeRequest) {
        Return returnEntity = returnRepository.findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
        
        if (returnEntity.getReturnType() != com.example.ecompoc.returns.enums.ReturnType.EXCHANGE) {
            throw new IllegalStateException(
                "Return is not an exchange. Return type: " + returnEntity.getReturnType());
        }
        
        // Validate exchange product exists
        Product exchangeProduct = productRepository.findById(exchangeRequest.getExchangeProductId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Exchange product not found: " + exchangeRequest.getExchangeProductId()));
        
        // Calculate price difference
        BigDecimal originalReturnAmount = returnEntity.getRefundAmountDecimal() != null 
            ? returnEntity.getRefundAmountDecimal() 
            : BigDecimal.ZERO;
        
        Integer exchangeQuantity = exchangeRequest.getQuantity() != null 
            ? exchangeRequest.getQuantity() 
            : 1;
        
        BigDecimal exchangePrice = exchangeProduct.getPriceDecimal() != null 
            ? exchangeProduct.getPriceDecimal() 
            : BigDecimal.ZERO;
        
        BigDecimal exchangeTotal = exchangePrice.multiply(BigDecimal.valueOf(exchangeQuantity));
        BigDecimal priceDifference = exchangeTotal.subtract(originalReturnAmount);
        
        // Create new order for exchange item
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setUserId(returnEntity.getUserId());
        
        List<CreateOrderRequest.OrderItemRequest> orderItems = new ArrayList<>();
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(
            exchangeRequest.getExchangeProductId(), exchangeQuantity);
        orderItems.add(itemRequest);
        orderRequest.setItems(orderItems);
        
        // If exchange item costs more, the difference will be charged
        // If exchange item costs less, the difference will be refunded
        // For now, we create the order and let the payment system handle the difference
        // In a real implementation, you would:
        // 1. If priceDifference > 0: Charge customer the difference
        // 2. If priceDifference < 0: Refund customer the difference
        // 3. If priceDifference == 0: No additional payment needed
        
        OrderResponse exchangeOrder = orderService.createOrder(orderRequest);
        
        logger.info("Processed exchange for return {}: Original amount: ${}, Exchange amount: ${}, Difference: ${}", 
            returnId, originalReturnAmount.doubleValue(), exchangeTotal.doubleValue(), 
            priceDifference.doubleValue());
        logger.info("Created exchange order {} for return {}", exchangeOrder.getId(), returnId);
        
        // TODO: Handle price difference payment/refund
        // If priceDifference > 0: Process payment for difference
        // If priceDifference < 0: Process refund for difference
        
        return exchangeOrder;
    }
    
    /**
     * Calculate price difference for an exchange
     */
    public Double calculatePriceDifference(String returnId, String exchangeProductId, Integer quantity) {
        Return returnEntity = returnRepository.findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
        
        Product exchangeProduct = productRepository.findById(exchangeProductId)
            .orElseThrow(() -> new IllegalArgumentException("Exchange product not found: " + exchangeProductId));
        
        BigDecimal originalReturnAmount = returnEntity.getRefundAmountDecimal() != null 
            ? returnEntity.getRefundAmountDecimal() 
            : BigDecimal.ZERO;
        
        Integer exchangeQty = quantity != null ? quantity : 1;
        BigDecimal exchangePrice = exchangeProduct.getPriceDecimal() != null 
            ? exchangeProduct.getPriceDecimal() 
            : BigDecimal.ZERO;
        
        BigDecimal exchangeTotal = exchangePrice.multiply(BigDecimal.valueOf(exchangeQty));
        BigDecimal priceDifference = exchangeTotal.subtract(originalReturnAmount);
        
        return priceDifference.doubleValue();
    }
}

