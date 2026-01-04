package com.example.ecompoc.product.service;

import com.example.ecompoc.product.dto.CreateProductRequest;
import com.example.ecompoc.product.dto.ProductResponse;
import com.example.ecompoc.product.exception.ProductNotFoundException;
import com.example.ecompoc.product.model.Product;
import com.example.ecompoc.product.repository.ProductRepository;
import com.example.ecompoc.stock.model.StockStatus;
import com.example.ecompoc.stock.service.StockStatusService;
import com.example.ecompoc.pricealert.service.PriceDropDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Service layer for product management
 */
@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private StockStatusService stockStatusService;
    private PriceDropDetectionService priceDropDetectionService;
    
    @Value("${stock-management.enabled:true}")
    private boolean stockManagementEnabled;
    
    @Value("${price-alert.enabled:true}")
    private boolean priceAlertEnabled;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Autowired(required = false)
    public void setStockStatusService(StockStatusService stockStatusService) {
        this.stockStatusService = stockStatusService;
    }
    
    @Autowired(required = false)
    public void setPriceDropDetectionService(PriceDropDetectionService priceDropDetectionService) {
        this.priceDropDetectionService = priceDropDetectionService;
    }
    
    /**
     * Get all products
     */
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get product by ID
     */
    public ProductResponse getProduct(String id) {
        if (id == null || id.isBlank()) {
            throw new ProductNotFoundException("Product not found");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        
        return mapToResponse(product);
    }
    
    /**
     * Create or update a product
     * If product with same name exists, updates it; otherwise creates new one
     */
    public ProductResponse createOrUpdateProduct(CreateProductRequest request) {
        AtomicReference<BigDecimal> oldPriceRef = new AtomicReference<>(null);
        Product product = productRepository.findByName(request.getName())
                .map(existingProduct -> {
                    // Store old price before updating (for price change detection)
                    oldPriceRef.set(existingProduct.getPriceDecimal());
                    // Update existing product
                    existingProduct.setDescription(request.getDescription());
                    existingProduct.setPrice(request.getPrice());
                    existingProduct.setQuantity(request.getQuantity());
                    existingProduct.setCategory(request.getCategory());
                    existingProduct.setUpdatedAt(LocalDateTime.now());
                    return existingProduct;
                })
                .orElseGet(() -> {
                    // Create new product (no old price to track)
                    String productId = UUID.randomUUID().toString();
                    return new Product(
                            productId,
                            request.getName(),
                            request.getDescription(),
                            request.getPrice(),
                            request.getQuantity(),
                            request.getCategory()
                    );
                });
        
        Product savedProduct = productRepository.save(product);
        
        // Price change detection hook (feature-toggle gated)
        BigDecimal oldPrice = oldPriceRef.get();
        if (priceAlertEnabled && priceDropDetectionService != null && oldPrice != null) {
            BigDecimal newPrice = savedProduct.getPriceDecimal();
            if (newPrice != null && !newPrice.equals(oldPrice)) {
                try {
                    priceDropDetectionService.detectPriceChange(savedProduct, oldPrice, newPrice);
                } catch (Exception e) {
                    // Log error but don't fail product update
                    org.slf4j.LoggerFactory.getLogger(ProductService.class)
                        .error("Failed to detect price change for product: {}", savedProduct.getId(), e);
                }
            }
        }
        
        return mapToResponse(savedProduct);
    }
    
    /**
     * Map Product entity to ProductResponse DTO
     * Includes stock status when stock management feature is enabled
     */
    private ProductResponse mapToResponse(Product product) {
        String createdAtStr = product.getCreatedAt() != null 
            ? product.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
            : null;
        String updatedAtStr = product.getUpdatedAt() != null 
            ? product.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
            : null;
        
        ProductResponse response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory(),
                createdAtStr,
                updatedAtStr
        );
        
        // Include stock status when feature is enabled
        if (stockManagementEnabled && stockStatusService != null) {
            StockStatus status = stockStatusService.calculateStockStatus(product);
            if (status != null) {
                response.setStockStatus(status.name());
            }
        }
        
        return response;
    }
}

