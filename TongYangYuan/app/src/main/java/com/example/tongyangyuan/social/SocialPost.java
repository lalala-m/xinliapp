package com.example.tongyangyuan.social;

public class SocialPost {
    private final String id;
    private final String imageUrl;
    private final String title;
    private final String authorAvatarUrl;
    private final String authorName;
    private final int likeCount;
    private final String consultantName;
    private boolean isLiked;

    public SocialPost(String id, String imageUrl, String title, String authorAvatarUrl, String authorName, int likeCount, String consultantName, boolean isLiked) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.authorAvatarUrl = authorAvatarUrl;
        this.authorName = authorName;
        this.likeCount = likeCount;
        this.consultantName = consultantName;
        this.isLiked = isLiked;
    }

    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public String getConsultantName() {
        return consultantName;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}

