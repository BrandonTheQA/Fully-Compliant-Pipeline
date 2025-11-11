package com.example.ecompoc.product.repository;

import com.example.ecompoc.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for Product entities
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
    /**
     * Check if product exists by name
     */
    boolean existsByName(String name);
}

