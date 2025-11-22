package com.example.ecompoc.abandonedcart.repository;

import com.example.ecompoc.abandonedcart.model.AbandonedCartEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for AbandonedCartEmail entities
 */
@Repository
public interface AbandonedCartEmailRepository extends JpaRepository<AbandonedCartEmail, String> {
    
    /**
     * Find all emails for an abandoned cart
     */
    List<AbandonedCartEmail> findByAbandonedCartId(String abandonedCartId);
    
    /**
     * Find email by abandoned cart ID and email type
     */
    Optional<AbandonedCartEmail> findByAbandonedCartIdAndEmailType(String abandonedCartId, String emailType);
}

