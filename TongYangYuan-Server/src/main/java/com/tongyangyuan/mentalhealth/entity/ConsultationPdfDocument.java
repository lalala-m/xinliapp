package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 咨询PDF文档实体
 */
@Entity
@Table(name = "consultation_pdf_documents")
public class ConsultationPdfDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "consultation_record_id", nullable = false)
    private Long consultationRecordId;
    
    @Column(name = "pdf_file_name", nullable = false, length = 255)
    private String pdfFileName;
    
    @Column(name = "pdf_file_path", nullable = false, length = 500)
    private String pdfFilePath;
    
    @Column(name = "pdf_file_size")
    private Long pdfFileSize;
    
    @Column(name = "pdf_page_count")
    private Integer pdfPageCount;
    
    @Column(name = "pdf_generated_at")
    private LocalDateTime pdfGeneratedAt;
    
    @Column(name = "pdf_checksum", length = 64)
    private String pdfChecksum;
    
    @Column(name = "is_latest")
    private Boolean isLatest = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (pdfGeneratedAt == null) {
            pdfGeneratedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getConsultationRecordId() { return consultationRecordId; }
    public void setConsultationRecordId(Long consultationRecordId) { this.consultationRecordId = consultationRecordId; }
    
    public String getPdfFileName() { return pdfFileName; }
    public void setPdfFileName(String pdfFileName) { this.pdfFileName = pdfFileName; }
    
    public String getPdfFilePath() { return pdfFilePath; }
    public void setPdfFilePath(String pdfFilePath) { this.pdfFilePath = pdfFilePath; }
    
    public Long getPdfFileSize() { return pdfFileSize; }
    public void setPdfFileSize(Long pdfFileSize) { this.pdfFileSize = pdfFileSize; }
    
    public Integer getPdfPageCount() { return pdfPageCount; }
    public void setPdfPageCount(Integer pdfPageCount) { this.pdfPageCount = pdfPageCount; }
    
    public LocalDateTime getPdfGeneratedAt() { return pdfGeneratedAt; }
    public void setPdfGeneratedAt(LocalDateTime pdfGeneratedAt) { this.pdfGeneratedAt = pdfGeneratedAt; }
    
    public String getPdfChecksum() { return pdfChecksum; }
    public void setPdfChecksum(String pdfChecksum) { this.pdfChecksum = pdfChecksum; }
    
    public Boolean getIsLatest() { return isLatest; }
    public void setIsLatest(Boolean isLatest) { this.isLatest = isLatest; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}