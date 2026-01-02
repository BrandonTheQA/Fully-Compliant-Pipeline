package com.example.ecompoc.returns.repository;

import com.example.ecompoc.returns.model.ReturnPolicyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for ReturnPolicyConfig entities
 */
@Repository
public interface ReturnPolicyConfigRepository extends JpaRepository<ReturnPolicyConfig, Long> {
    
    /**
     * Find the active policy configuration (assuming single active policy)
     */
    Optional<ReturnPolicyConfig> findFirstByOrderByUpdatedAtDesc();
}

