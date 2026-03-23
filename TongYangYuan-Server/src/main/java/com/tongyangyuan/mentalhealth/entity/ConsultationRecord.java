package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_records")
public class ConsultationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "consultant_id", nullable = false)
    private Long consultantId;

    @Column(name = "parent_user_id", nullable = false)
    private Long parentUserId;

    @Column(name = "child_id")
    private Long childId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_type", nullable = false, length = 20)
    private ConsultationType consultationType;

    @Column(name = "duration")
    private Integer duration; // 咨询时长（分钟）

    @Column(columnDefinition = "TEXT")
    private String summary; // 咨询摘要

    @Column(name = "consultant_feedback", columnDefinition = "TEXT")
    private String consultantFeedback; // 咨询师反馈

    @Column(name = "core_issue_tags", length = 500)
    private String coreIssueTags; // 核心困扰标签，逗号分隔

    @Column(precision = 3, scale = 2)
    private BigDecimal rating; // 用户评分

    @Column(name = "user_comment", columnDefinition = "TEXT")
    private String userComment; // 用户评价

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RecordStatus status = RecordStatus.COMPLETED;

    @Column(name = "created_at", updatable = false)
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

    public enum ConsultationType {
        ONLINE, OFFLINE, VIDEO, AUDIO
    }

    public enum RecordStatus {
        COMPLETED, CANCELLED
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(Long consultantId) {
        this.consultantId = consultantId;
    }

    public Long getParentUserId() {
        return parentUserId;
    }

    public void setParentUserId(Long parentUserId) {
        this.parentUserId = parentUserId;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }

    public ConsultationType getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(ConsultationType consultationType) {
        this.consultationType = consultationType;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getConsultantFeedback() {
        return consultantFeedback;
    }

    public void setConsultantFeedback(String consultantFeedback) {
        this.consultantFeedback = consultantFeedback;
    }

    public String getCoreIssueTags() {
        return coreIssueTags;
    }

    public void setCoreIssueTags(String coreIssueTags) {
        this.coreIssueTags = coreIssueTags;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
