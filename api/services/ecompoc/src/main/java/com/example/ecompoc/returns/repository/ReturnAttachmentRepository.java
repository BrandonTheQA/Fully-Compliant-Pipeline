package com.example.ecompoc.returns.repository;

import com.example.ecompoc.returns.model.ReturnAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for ReturnAttachment entities
 */
@Repository
public interface ReturnAttachmentRepository extends JpaRepository<ReturnAttachment, Long> {
    
    /**
     * Find all attachments for a return
     */
    List<ReturnAttachment> findByReturnEntity_ReturnId(String returnId);
}

