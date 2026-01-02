package com.example.ecompoc.returns.repository;

import com.example.ecompoc.returns.model.Return;
import com.example.ecompoc.returns.enums.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for Return entities
 */
@Repository
public interface ReturnRepository extends JpaRepository<Return, String> {
    
    /**
     * Find return by RMA number
     */
    Optional<Return> findByRmaNumber(String rmaNumber);
    
    /**
     * Find all returns for a user
     */
    List<Return> findByUserId(String userId);
    
    /**
     * Find all returns for an order
     */
    List<Return> findByOrderId(String orderId);
    
    /**
     * Find returns by status
     */
    List<Return> findByStatus(ReturnStatus status);
    
    /**
     * Find returns by user and status
     */
    List<Return> findByUserIdAndStatus(String userId, ReturnStatus status);
    
    /**
     * Check if RMA number exists
     */
    boolean existsByRmaNumber(String rmaNumber);
}

