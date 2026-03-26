package com.example.tongyangyuan.consult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Consultant implements Serializable {

    public enum IdentityTier {
        INTERNAL(0),
        BLUE_V(1),
        YELLOW_V(2);

        private final int priority;

        IdentityTier(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    private final String name;
    private final String title;
    private final String specialty;
    private final double rating;
    private final String servedCount;
    private final String avatarColor;
    private final String avatarUrl;
    private final String intro;
    private final List<String> reviews;
    private final List<String> identityTags;
    private final IdentityTier identityTier;
    private final String displayIdentityTag;
    // serverId = 后端 consultants.id（咨询师档案主键，用于业务关联如提交预约）
    private long serverId;
    private long userId;

    public Consultant(long userId, String name, String title, String specialty, double rating,
                      String servedCount, String avatarColor, String avatarUrl, List<String> identityTags,
                      String intro, List<String> reviews) {
        this.userId = userId;
        this.name = name;
        this.title = title;
        this.specialty = specialty;
        this.rating = rating;
        this.servedCount = servedCount;
        this.avatarColor = avatarColor;
        this.avatarUrl = avatarUrl;
        this.identityTags = Collections.unmodifiableList(
                identityTags != null ? new ArrayList<>(identityTags) : new ArrayList<>());
        this.identityTier = resolveTier(this.identityTags);
        this.displayIdentityTag = resolveDisplayTag(this.identityTags);
        this.intro = intro;
        this.reviews = Collections.unmodifiableList(
                reviews != null ? new ArrayList<>(reviews) : new ArrayList<>());
    }

    // 兼容旧的构造函数，avatarUrl 传 null
    public Consultant(String name, String title, String specialty, double rating,
                      String servedCount, String avatarColor, List<String> identityTags,
                      String intro, List<String> reviews) {
        this(0, name, title, specialty, rating, servedCount, avatarColor, null, identityTags, intro, reviews);
    }

    // 兼容旧构造函数（无 avatarUrl）
    public Consultant(long userId, String name, String title, String specialty, double rating,
                      String servedCount, String avatarColor, List<String> identityTags,
                      String intro, List<String> reviews) {
        this(userId, name, title, specialty, rating, servedCount, avatarColor, null, identityTags, intro, reviews);
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getSpecialty() {
        return specialty;
    }

    public double getRating() {
        return rating;
    }

    public String getServedCount() {
        return servedCount;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public List<String> getIdentityTags() {
        return identityTags;
    }

    public IdentityTier getIdentityTier() {
        return identityTier;
    }

    public String getDisplayIdentityTag() {
        return displayIdentityTag;
    }

    public String getIntro() {
        return intro;
    }

    public List<String> getReviews() {
        return reviews;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    private IdentityTier resolveTier(List<String> tags) {
        if (tags == null) {
            return IdentityTier.YELLOW_V;
        }
        if (tags.contains("童康源内部人员")) {
            return IdentityTier.INTERNAL;
        }
        if (tags.contains("童康源指导师（蓝V认证）")) {
            return IdentityTier.BLUE_V;
        }
        return IdentityTier.YELLOW_V;
    }

    private String resolveDisplayTag(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        for (String tag : tags) {
            if (tag != null && tag.contains("内部人员")) {
                return tag;
            }
        }
        for (String tag : tags) {
            if (tag != null && tag.contains("蓝V认证")) {
                return tag;
            }
        }
        for (String tag : tags) {
            if (tag != null && tag.contains("黄V认证")) {
                return tag;
            }
        }
        return tags.get(0);
    }
}

