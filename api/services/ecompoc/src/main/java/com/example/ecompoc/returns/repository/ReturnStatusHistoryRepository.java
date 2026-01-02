package com.example.ecompoc.returns.repository;

import com.example.ecompoc.returns.model.ReturnStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for ReturnStatusHistory entities
 */
@Repository
public interface ReturnStatusHistoryRepository extends JpaRepository<ReturnStatusHistory, Long> {
    
    /**
     * Find all status history for a return, ordered by creation date
     */
    List<ReturnStatusHistory> findByReturnEntity_ReturnIdOrderByCreatedAtAsc(String returnId);
}

