package com.example.ecompoc.returns.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Return attachment entity for photos and documents
 */
@Entity
@Table(name = "return_attachments")
public class ReturnAttachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long attachmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private Return returnEntity;
    
    @Column(name = "file_path", nullable = false, columnDefinition = "NVARCHAR(500)")
    private String filePath;
    
    @Column(name = "file_type", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String fileType;
    
    @Column(name = "file_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String fileName;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public ReturnAttachment() {}
    
    public ReturnAttachment(String filePath, String fileType, String fileName) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileName = fileName;
        this.createdAt = LocalDateTime.now();
    }
    
    public Long getAttachmentId() {
        return attachmentId;
    }
    
    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }
    
    public Return getReturnEntity() {
        return returnEntity;
    }
    
    public void setReturnEntity(Return returnEntity) {
        this.returnEntity = returnEntity;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

