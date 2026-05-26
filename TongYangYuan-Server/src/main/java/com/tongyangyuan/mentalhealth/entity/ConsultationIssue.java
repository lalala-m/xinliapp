package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_issues")
public class ConsultationIssue implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stage_id", nullable = false)
    private Long stageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private LifeStage stage;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "issue_name", nullable = false, length = 100)
    private String issueName;

    @Column(name = "is_custom_input")
    private Boolean isCustomInput = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }

    public LifeStage getStage() { return stage; }
    public void setStage(LifeStage stage) { this.stage = stage; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getIssueName() { return issueName; }
    public void setIssueName(String issueName) { this.issueName = issueName; }

    public Boolean getIsCustomInput() { return isCustomInput; }
    public void setIsCustomInput(Boolean isCustomInput) { this.isCustomInput = isCustomInput; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
