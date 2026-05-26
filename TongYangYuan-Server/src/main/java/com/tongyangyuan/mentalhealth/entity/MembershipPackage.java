package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 会员套餐实体
 */
@Entity
@Table(name = "membership_packages")
public class MembershipPackage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;  // 套餐名称
    
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;  // 套餐代码
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;  // 价格
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;  // 原价
    
    @Column(name = "days", nullable = false)
    private Integer days;  // 有效期天数
    
    @Column(name = "description", length = 200)
    private String description;  // 套餐描述
    
    @Column(name = "features", columnDefinition = "TEXT")
    private String features;  // 功能特性(JSON)
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;  // 排序
    
    @Column(name = "is_active")
    private Boolean isActive = true;  // 是否启用
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
    
    // 套餐代码常量
    public static final String CODE_MONTH = "month";
    public static final String CODE_QUARTER = "quarter";
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
