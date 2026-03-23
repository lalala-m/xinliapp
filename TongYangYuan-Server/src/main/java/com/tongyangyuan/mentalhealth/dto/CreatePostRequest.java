package com.tongyangyuan.mentalhealth.dto;

import lombok.Data;

@Data
public class CreatePostRequest {
    private String title;
    private String content;
    private String imageUrl;
    private Long guidedByConsultantId; // Optional ID of the guiding consultant

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getGuidedByConsultantId() {
        return guidedByConsultantId;
    }

    public void setGuidedByConsultantId(Long guidedByConsultantId) {
        this.guidedByConsultantId = guidedByConsultantId;
    }
}

