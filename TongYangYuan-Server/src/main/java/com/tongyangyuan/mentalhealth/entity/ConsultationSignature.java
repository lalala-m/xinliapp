package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 咨询签名实体
 */
@Entity
@Table(name = "consultation_signatures")
public class ConsultationSignature {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "consultation_record_id", nullable = false)
    private Long consultationRecordId;
    
    @Column(name = "signer_type", nullable = false, length = 20)
    private String signerType; // CONSULTANT, PARENT
    
    @Column(name = "signer_user_id", nullable = false)
    private Long signerId;
    
    @Column(name = "signer_name", length = 100)
    private String signerName;
    
    @Column(name = "signature_data", columnDefinition = "TEXT")
    private String signatureImage; // Base64编码的签名图片
    
    @Column(name = "verification_media_url", length = 500)
    private String verificationMediaUrl; // 验证照片URL
    
    @Column(name = "signed_at")
    private LocalDateTime signedAt;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "client_info", length = 500)
    private String clientInfo; // 客户端信息
    
    @Column(name = "signature_position", length = 50)
    private String signaturePosition = "BOTTOM_RIGHT"; // 签名在PDF中的位置
    
    @Column(name = "page_number")
    private Integer pageNumber = 1; // 签名所在的页码
    
    @Column(name = "pdf_incorporated")
    private Boolean pdfIncorporated = false; // 签名是否已合并到PDF
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (signedAt == null) {
            signedAt = LocalDateTime.now();
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
    
    public String getSignerType() { return signerType; }
    public void setSignerType(String signerType) { this.signerType = signerType; }
    
    public Long getSignerId() { return signerId; }
    public void setSignerId(Long signerId) { this.signerId = signerId; }
    
    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }
    
    public String getSignatureImage() { return signatureImage; }
    public void setSignatureImage(String signatureImage) { this.signatureImage = signatureImage; }
    
    public String getVerificationMediaUrl() { return verificationMediaUrl; }
    public void setVerificationMediaUrl(String verificationMediaUrl) { this.verificationMediaUrl = verificationMediaUrl; }
    
    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getClientInfo() { return clientInfo; }
    public void setClientInfo(String clientInfo) { this.clientInfo = clientInfo; }
    
    public String getSignaturePosition() { return signaturePosition; }
    public void setSignaturePosition(String signaturePosition) { this.signaturePosition = signaturePosition; }
    
    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    
    public Boolean getPdfIncorporated() { return pdfIncorporated; }
    public void setPdfIncorporated(Boolean pdfIncorporated) { this.pdfIncorporated = pdfIncorporated; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}