package com.tongyangyuan.mentalhealth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges")
public class UserBadge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "badge_id", nullable = false, length = 50)
    private String badgeId;
    
    @Column(name = "badge_name", length = 100)
    private String badgeName;
    
    @Column(name = "badge_icon", length = 200)
    private String badgeIcon;
    
    @Column(length = 50)
    private String category;
    
    @Column(name = "earned_at")
    private LocalDateTime earnedAt;
    
    @PrePersist
    protected void onCreate() {
        earnedAt = LocalDateTime.now();
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
    
    public String getBadgeId() {
        return badgeId;
    }
    
    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }
    
    public String getBadgeName() {
        return badgeName;
    }
    
    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }
    
    public String getBadgeIcon() {
        return badgeIcon;
    }
    
    public void setBadgeIcon(String badgeIcon) {
        this.badgeIcon = badgeIcon;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }
    
    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }
}
