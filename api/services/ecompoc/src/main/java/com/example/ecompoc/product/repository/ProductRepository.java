package com.example.ecompoc.product.repository;

import com.example.ecompoc.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    /**
     * Atomically update product quantity (for stock deduction/restoration)
     * Only updates if quantity >= requested change (prevents negative stock)
     * Returns number of rows updated (0 if insufficient stock or product not found)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.quantity = p.quantity + :change WHERE p.id = :productId AND p.quantity + :change >= 0")
    int updateQuantity(@Param("productId") String productId, @Param("change") Integer change);
}

