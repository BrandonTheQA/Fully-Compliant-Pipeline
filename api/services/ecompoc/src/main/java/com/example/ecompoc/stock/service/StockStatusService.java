package com.example.ecompoc.stock.service;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.model.StockStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for calculating and retrieving stock status
 */
@Service
public class StockStatusService {
    
    private static final Logger logger = LoggerFactory.getLogger(StockStatusService.class);
    
    private final ProductRepository productRepository;
    
    @Value("${stock-management.enabled:true}")
    private boolean stockManagementEnabled;
    
    @Value("${stock-management.low-stock-threshold-default:10}")
    private Integer defaultLowStockThreshold;
    
    public StockStatusService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Calculate stock status for a product
     */
    public StockStatus calculateStockStatus(Product product) {
        if (product == null || product.getQuantity() == null) {
            return StockStatus.OUT_OF_STOCK;
        }
        
        Integer quantity = product.getQuantity();
        if (quantity <= 0) {
            return StockStatus.OUT_OF_STOCK;
        }
        
        Integer threshold = product.getLowStockThreshold() != null 
            ? product.getLowStockThreshold() 
            : defaultLowStockThreshold;
        
        if (quantity <= threshold) {
            return StockStatus.LOW_STOCK;
        }
        
        return StockStatus.IN_STOCK;
    }
    
    /**
     * Get stock status for a product (with caching)
     */
    @Cacheable(value = "stockStatus", key = "#productId", unless = "#result == null")
    public StockStatus getStockStatus(String productId) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled");
            return null;
        }
        
        if (productId == null || productId.isBlank()) {
            logger.warn("Invalid product ID for stock status query: {}", productId);
            return null;
        }
        
        return productRepository.findById(productId)
            .map(this::calculateStockStatus)
            .orElse(null);
    }
    
    /**
     * Get stock status without caching (for real-time verification in cart/checkout)
     */
    public StockStatus getStockStatusRealTime(String productId) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled");
            return null;
        }
        
        if (productId == null || productId.isBlank()) {
            logger.warn("Invalid product ID for stock status query: {}", productId);
            return null;
        }
        
        return productRepository.findById(productId)
            .map(this::calculateStockStatus)
            .orElse(null);
    }
    
    /**
     * Get bulk stock status for multiple products
     */
    public Map<String, StockStatus> getBulkStockStatus(List<String> productIds) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled");
            return Map.of();
        }
        
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        
        List<Product> products = productRepository.findAllById(productIds);
        return products.stream()
            .collect(Collectors.toMap(
                Product::getId,
                this::calculateStockStatus
            ));
    }
    
    /**
     * Get stock status message for display
     */
    public String getStockStatusMessage(Product product) {
        if (product == null || product.getQuantity() == null) {
            return "Out of Stock";
        }
        
        StockStatus status = calculateStockStatus(product);
        switch (status) {
            case IN_STOCK:
                return "In Stock";
            case LOW_STOCK:
                return String.format("Low Stock - Only %d left!", product.getQuantity());
            case OUT_OF_STOCK:
                return "Out of Stock";
            default:
                return "Unknown";
        }
    }
}

