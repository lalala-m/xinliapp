package com.tongyangyuan.mentalhealth.dto;

/**
 * 管理员更新用户资料请求
 */
public class UpdateUserProfileRequest {
    private String nickname;
    private String avatarUrl;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
