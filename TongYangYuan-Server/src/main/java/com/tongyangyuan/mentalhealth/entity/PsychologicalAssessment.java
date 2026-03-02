package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "psychological_assessments")
public class PsychologicalAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "test_record_id", nullable = false)
    private Long testRecordId;

    @Column(name = "consultation_record_id")
    private Long consultationRecordId;

    @Column(name = "knowledge_mastery", columnDefinition = "TEXT")
    private String knowledgeMastery; // JSON格式

    @Column(name = "psychological_state", columnDefinition = "TEXT")
    private String psychologicalState;

    @Column(name = "improvement_analysis", columnDefinition = "TEXT")
    private String improvementAnalysis;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "weak_points", columnDefinition = "TEXT")
    private String weakPoints;

    @Column(name = "report_data", columnDefinition = "TEXT")
    private String reportData; // 完整报告数据（JSON格式）

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

    public Long getTestRecordId() {
        return testRecordId;
    }

    public void setTestRecordId(Long testRecordId) {
        this.testRecordId = testRecordId;
    }

    public Long getConsultationRecordId() {
        return consultationRecordId;
    }

    public void setConsultationRecordId(Long consultationRecordId) {
        this.consultationRecordId = consultationRecordId;
    }

    public String getKnowledgeMastery() {
        return knowledgeMastery;
    }

    public void setKnowledgeMastery(String knowledgeMastery) {
        this.knowledgeMastery = knowledgeMastery;
    }

    public String getPsychologicalState() {
        return psychologicalState;
    }

    public void setPsychologicalState(String psychologicalState) {
        this.psychologicalState = psychologicalState;
    }

    public String getImprovementAnalysis() {
        return improvementAnalysis;
    }

    public void setImprovementAnalysis(String improvementAnalysis) {
        this.improvementAnalysis = improvementAnalysis;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getWeakPoints() {
        return weakPoints;
    }

    public void setWeakPoints(String weakPoints) {
        this.weakPoints = weakPoints;
    }

    public String getReportData() {
        return reportData;
    }

    public void setReportData(String reportData) {
        this.reportData = reportData;
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
