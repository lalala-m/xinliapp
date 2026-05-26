package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 咨询聊天记录导出实体
 */
@Entity
@Table(name = "consultation_chat_exports")
public class ConsultationChatExport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "consultation_record_id", nullable = false)
    private Long consultationRecordId;
    
    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;
    
    @Column(name = "chat_content", columnDefinition = "TEXT")
    private String chatContent; // JSON格式的聊天记录
    
    @Column(name = "export_status", length = 20)
    private String exportStatus = "PENDING"; // PENDING, COMPLETED, FAILED
    
    @Column(name = "export_started_at")
    private LocalDateTime exportStartedAt;
    
    @Column(name = "export_completed_at")
    private LocalDateTime exportCompletedAt;
    
    @Column(name = "export_error_message", length = 500)
    private String exportErrorMessage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
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
    
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    
    public String getChatContent() { return chatContent; }
    public void setChatContent(String chatContent) { this.chatContent = chatContent; }
    
    public String getExportStatus() { return exportStatus; }
    public void setExportStatus(String exportStatus) { this.exportStatus = exportStatus; }
    
    public LocalDateTime getExportStartedAt() { return exportStartedAt; }
    public void setExportStartedAt(LocalDateTime exportStartedAt) { this.exportStartedAt = exportStartedAt; }
    
    public LocalDateTime getExportCompletedAt() { return exportCompletedAt; }
    public void setExportCompletedAt(LocalDateTime exportCompletedAt) { this.exportCompletedAt = exportCompletedAt; }
    
    public String getExportErrorMessage() { return exportErrorMessage; }
    public void setExportErrorMessage(String exportErrorMessage) { this.exportErrorMessage = exportErrorMessage; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}