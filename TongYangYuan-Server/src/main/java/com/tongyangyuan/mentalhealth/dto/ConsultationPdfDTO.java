package com.tongyangyuan.mentalhealth.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询记录PDF数据传输对象
 */
public class ConsultationPdfDTO implements Serializable {
    
    private Long recordId;
    private Long appointmentId;
    private String appointmentNo;
    private LocalDateTime consultationDate;
    private Integer duration;
    private String consultationType;
    private String summary;
    private String consultantFeedback;
    private String coreIssueTags;
    private BigDecimal rating;
    private String userComment;
    private String status;
    
    // 咨询师信息
    private String consultantName;
    private String consultantTitle;
    private String consultantSpecialty;
    
    // 家长信息
    private Long parentUserId;
    private String parentNickname;
    private String parentPhone;
    
    // 儿童信息
    private Long childId;
    private String childName;
    private Integer childAge;
    private String childGender;
    
    // 签名信息
    private List<SignatureInfo> signatures;
    
    // 聊天记录
    private List<ChatMessageDTO> chatMessages;
    
    // Getters and Setters
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    
    public String getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(String appointmentNo) { this.appointmentNo = appointmentNo; }
    
    public LocalDateTime getConsultationDate() { return consultationDate; }
    public void setConsultationDate(LocalDateTime consultationDate) { this.consultationDate = consultationDate; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    
    public String getConsultationType() { return consultationType; }
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getConsultantFeedback() { return consultantFeedback; }
    public void setConsultantFeedback(String consultantFeedback) { this.consultantFeedback = consultantFeedback; }
    
    public String getCoreIssueTags() { return coreIssueTags; }
    public void setCoreIssueTags(String coreIssueTags) { this.coreIssueTags = coreIssueTags; }
    
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    
    public String getUserComment() { return userComment; }
    public void setUserComment(String userComment) { this.userComment = userComment; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getConsultantName() { return consultantName; }
    public void setConsultantName(String consultantName) { this.consultantName = consultantName; }
    
    public String getConsultantTitle() { return consultantTitle; }
    public void setConsultantTitle(String consultantTitle) { this.consultantTitle = consultantTitle; }
    
    public String getConsultantSpecialty() { return consultantSpecialty; }
    public void setConsultantSpecialty(String consultantSpecialty) { this.consultantSpecialty = consultantSpecialty; }
    
    public Long getParentUserId() { return parentUserId; }
    public void setParentUserId(Long parentUserId) { this.parentUserId = parentUserId; }
    
    public String getParentNickname() { return parentNickname; }
    public void setParentNickname(String parentNickname) { this.parentNickname = parentNickname; }
    
    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
    
    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }
    
    public Integer getChildAge() { return childAge; }
    public void setChildAge(Integer childAge) { this.childAge = childAge; }
    
    public String getChildGender() { return childGender; }
    public void setChildGender(String childGender) { this.childGender = childGender; }
    
    public List<SignatureInfo> getSignatures() { return signatures; }
    public void setSignatures(List<SignatureInfo> signatures) { this.signatures = signatures; }
    
    public List<ChatMessageDTO> getChatMessages() { return chatMessages; }
    public void setChatMessages(List<ChatMessageDTO> chatMessages) { this.chatMessages = chatMessages; }
    
    /**
     * 签名信息内部类
     */
    public static class SignatureInfo implements Serializable {
        private Long id;
        private String signerType; // CONSULTANT, PARENT
        private String signerName;
        private String signatureData; // Base64编码的图片
        private LocalDateTime signedAt;
        private String ipAddress;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getSignerType() { return signerType; }
        public void setSignerType(String signerType) { this.signerType = signerType; }
        
        public String getSignerName() { return signerName; }
        public void setSignerName(String signerName) { this.signerName = signerName; }
        
        public String getSignatureData() { return signatureData; }
        public void setSignatureData(String signatureData) { this.signatureData = signatureData; }
        
        public LocalDateTime getSignedAt() { return signedAt; }
        public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }
        
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    }
    
    /**
     * 聊天消息DTO
     */
    public static class ChatMessageDTO implements Serializable {
        private Long messageId;
        private String senderType; // CONSULTANT, PARENT
        private String senderName;
        private String content;
        private LocalDateTime timestamp;
        private String messageType; // TEXT, IMAGE, VOICE
        
        public Long getMessageId() { return messageId; }
        public void setMessageId(Long messageId) { this.messageId = messageId; }
        
        public String getSenderType() { return senderType; }
        public void setSenderType(String senderType) { this.senderType = senderType; }
        
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
    }
}