package com.example.controller;

import com.example.dto.CreateProductRequest;
import com.example.dto.ProductResponse;
import com.example.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for product management endpoints
 */
@RestController
@RequestMapping("/api")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * GET /api/products - List all products
     */
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    /**
     * GET /api/products/{id} - Get product details by ID
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
        ProductResponse product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }
    
    /**
     * POST /api/products - Add or update a product (admin use)
     */
    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createOrUpdateProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse product = productService.createOrUpdateProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
}

