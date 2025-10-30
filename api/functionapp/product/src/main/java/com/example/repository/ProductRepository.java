package com.example.repository;

import com.example.model.Product;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * In-memory repository for Product entities
 */
@Repository
public class ProductRepository {
    
    private final Map<String, Product> products = new HashMap<>();
    
    /**
     * Save a product
     */
    public Product save(Product product) {
        products.put(product.getId(), product);
        return product;
    }
    
    /**
     * Find product by ID
     */
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }
    
    /**
     * Find all products
     */
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }
    
    /**
     * Check if product exists by ID
     */
    public boolean existsById(String id) {
        return products.containsKey(id);
    }
    
    /**
     * Check if product exists by name
     */
    public boolean existsByName(String name) {
        return products.values().stream()
                .anyMatch(product -> product.getName().equals(name));
    }
    
    /**
     * Get total product count
     */
    public long count() {
        return products.size();
    }
    
    /**
     * Delete product by ID
     */
    public void deleteById(String id) {
        products.remove(id);
    }
}

