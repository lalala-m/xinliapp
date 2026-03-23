package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultants")
public class Consultant implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String specialty;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_tier")
    private IdentityTier identityTier = IdentityTier.BRONZE;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("5.00");

    @Column(name = "served_count")
    private Integer servedCount = 0;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(name = "avatar_color", length = 20)
    private String avatarColor = "#6FA6F8";

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "is_available")
    private Boolean available = true;

    @Column(name = "gmt_create", updatable = false)
    private LocalDateTime gmtCreate;

    @Column(name = "gmt_modified")
    private LocalDateTime gmtModified;

    @Column(name = "is_deleted")
    private Integer deleted = 0;

    // --- 新增字段：黄V认证相关 ---
    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO; // 保证金金额

    @Column(name = "is_deposit_paid")
    private Boolean isDepositPaid = false; // 保证金是否已缴纳

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl; // 证书图片URL

    @Column(name = "is_certificate_verified")
    private Boolean isCertificateVerified = false; // 证书是否已审核通过

    // --- 身份类型映射 ---
    // IDENTITY_TIER 映射:
    // PLATINUM -> 内部人员
    // GOLD -> 蓝V (童康源指导师)
    // SILVER -> 黄V (家庭教育/心理健康/情感指导师)
    // BRONZE -> 普通/待认证

    @PrePersist
    protected void onCreate() {
        gmtCreate = LocalDateTime.now();
        gmtModified = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        gmtModified = LocalDateTime.now();
    }

    public enum IdentityTier {
        BRONZE, SILVER, GOLD, PLATINUM
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public IdentityTier getIdentityTier() {
        return identityTier;
    }

    public void setIdentityTier(IdentityTier identityTier) {
        this.identityTier = identityTier;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getServedCount() {
        return servedCount;
    }

    public void setServedCount(Integer servedCount) {
        this.servedCount = servedCount;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
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

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public Boolean getIsDepositPaid() {
        return isDepositPaid;
    }

    public void setIsDepositPaid(Boolean isDepositPaid) {
        this.isDepositPaid = isDepositPaid;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public Boolean getIsCertificateVerified() {
        return isCertificateVerified;
    }

    public void setIsCertificateVerified(Boolean isCertificateVerified) {
        this.isCertificateVerified = isCertificateVerified;
    }
}
