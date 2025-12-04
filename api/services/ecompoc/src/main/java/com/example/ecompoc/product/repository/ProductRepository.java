package com.example.ecompoc.product.repository;

import com.example.ecompoc.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for Product entities
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
    /**
     * Check if product exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Find product by name
     */
    Optional<Product> findByName(String name);
}

