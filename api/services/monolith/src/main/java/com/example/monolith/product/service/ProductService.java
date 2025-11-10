package com.example.monolith.product.service;

import com.example.monolith.product.dto.CreateProductRequest;
import com.example.monolith.product.dto.ProductResponse;
import com.example.monolith.product.exception.ProductNotFoundException;
import com.example.monolith.product.model.Product;
import com.example.monolith.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for product management
 */
@Service
public class ProductService {
    
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
        // Check if product with same name exists
        List<Product> existingProducts = productRepository.findAll();
        Product existingProduct = existingProducts.stream()
                .filter(p -> p.getName().equals(request.getName()))
                .findFirst()
                .orElse(null);
        
        Product product;
        if (existingProduct != null) {
            // Update existing product
            existingProduct.setDescription(request.getDescription());
            existingProduct.setPrice(request.getPrice());
            existingProduct.setQuantity(request.getQuantity());
            existingProduct.setCategory(request.getCategory());
            existingProduct.setUpdatedAt(LocalDateTime.now());
            product = productRepository.save(existingProduct);
        } else {
            // Create new product
            String productId = UUID.randomUUID().toString();
            product = new Product(
                    productId,
                    request.getName(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getQuantity(),
                    request.getCategory()
            );
            product = productRepository.save(product);
        }
        
        return mapToResponse(product);
    }
    
    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToResponse(Product product) {
        String createdAtStr = product.getCreatedAt() != null 
            ? product.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
            : null;
        String updatedAtStr = product.getUpdatedAt() != null 
            ? product.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
            : null;
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory(),
                createdAtStr,
                updatedAtStr
        );
    }
}

