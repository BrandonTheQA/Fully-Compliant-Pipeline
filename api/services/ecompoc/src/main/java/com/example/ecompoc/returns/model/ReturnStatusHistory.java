package com.example.ecompoc.returns.model;

import com.example.ecompoc.returns.enums.ReturnStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Return status history entity to track all status changes
 */
@Entity
@Table(name = "return_status_history")
public class ReturnStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private Return returnEntity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(50)")
    private ReturnStatus status;
    
    @Column(name = "notes", columnDefinition = "NVARCHAR(1000)")
    private String notes;
    
    @Column(name = "updated_by", columnDefinition = "NVARCHAR(255)")
    private String updatedBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public ReturnStatusHistory() {}
    
    public ReturnStatusHistory(Return returnEntity, ReturnStatus status, String notes, String updatedBy) {
        this.returnEntity = returnEntity;
        this.status = status;
        this.notes = notes;
        this.updatedBy = updatedBy;
        this.createdAt = LocalDateTime.now();
    }
    
    public Long getHistoryId() {
        return historyId;
    }
    
    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }
    
    public Return getReturnEntity() {
        return returnEntity;
    }
    
    public void setReturnEntity(Return returnEntity) {
        this.returnEntity = returnEntity;
    }
    
    public ReturnStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReturnStatus status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

