package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultant_specialties",
       uniqueConstraints = @UniqueConstraint(columnNames = {"consultant_id", "tag_id"}, name = "uk_consultant_tag"))
public class ConsultantSpecialty implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consultant_id", nullable = false)
    private Long consultantId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultant_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Consultant consultant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private SpecialtyTag tag;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConsultantId() { return consultantId; }
    public void setConsultantId(Long consultantId) { this.consultantId = consultantId; }

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Consultant getConsultant() { return consultant; }
    public void setConsultant(Consultant consultant) { this.consultant = consultant; }

    public SpecialtyTag getTag() { return tag; }
    public void setTag(SpecialtyTag tag) { this.tag = tag; }
}
