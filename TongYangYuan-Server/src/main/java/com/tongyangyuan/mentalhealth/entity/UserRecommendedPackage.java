package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_recommended_packages")
public class UserRecommendedPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "consultation_record_id")
    private Long consultationRecordId;

    @Column(columnDefinition = "TEXT")
    private String reason; // 推荐理由

    @Column(name = "is_viewed")
    private Boolean viewed = false;

    @Column(name = "is_started")
    private Boolean started = false;

    @Column(name = "gmt_create", updatable = false)
    private LocalDateTime gmtCreate;

    @Column(name = "gmt_modified")
    private LocalDateTime gmtModified;

    @Column(name = "is_deleted")
    private Integer deleted = 0;

    @PrePersist
    protected void onCreate() {
        gmtCreate = LocalDateTime.now();
        gmtModified = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        gmtModified = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Long getConsultationRecordId() {
        return consultationRecordId;
    }

    public void setConsultationRecordId(Long consultationRecordId) {
        this.consultationRecordId = consultationRecordId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getViewed() {
        return viewed;
    }

    public void setViewed(Boolean viewed) {
        this.viewed = viewed;
    }

    public Boolean getStarted() {
        return started;
    }

    public void setStarted(Boolean started) {
        this.started = started;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
