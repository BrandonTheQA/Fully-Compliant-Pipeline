package com.example.ecompoc.stock.service;

import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for deducting and restoring stock
 * Uses optimistic locking and atomic SQL updates to handle concurrent access
 */
@Service
public class StockDeductionService {
    
    private static final Logger logger = LoggerFactory.getLogger(StockDeductionService.class);
    
    private final ProductRepository productRepository;
    
    @Value("${stock-management.enabled:true}")
    private boolean stockManagementEnabled;
    
    public StockDeductionService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Deduct stock for a product (transactional, with optimistic locking)
     */
    @Transactional
    public void deductStock(String productId, Integer quantity) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled, skipping stock deduction");
            return;
        }
        
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }
        
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        // Use atomic SQL update to prevent race conditions
        int updatedRows = productRepository.updateQuantity(productId, -quantity);
        
        if (updatedRows == 0) {
            // Product not found or insufficient stock
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
            
            throw new IllegalStateException(
                String.format("Insufficient stock for product %s. Requested: %d, Available: %d",
                    productId, quantity, product.getQuantity()));
        }
        
        logger.info("Deducted {} units from product {} stock", quantity, productId);
    }
    
    /**
     * Restore stock for a product (for order cancellation)
     */
    @Transactional
    public void restoreStock(String productId, Integer quantity) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled, skipping stock restoration");
            return;
        }
        
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }
        
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        int updatedRows = productRepository.updateQuantity(productId, quantity);
        
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        
        logger.info("Restored {} units to product {} stock", quantity, productId);
    }
    
    /**
     * Verify product has sufficient stock
     */
    public boolean verifyStockAvailability(String productId, Integer requestedQuantity) {
        if (!stockManagementEnabled) {
            logger.debug("Stock management feature is disabled, skipping stock verification");
            return true; // Allow if feature disabled
        }
        
        if (productId == null || productId.isBlank() || requestedQuantity == null || requestedQuantity <= 0) {
            return false;
        }
        
        return productRepository.findById(productId)
            .map(product -> product.getQuantity() != null && product.getQuantity() >= requestedQuantity)
            .orElse(false);
    }
}

