package com.example.tongyangyuan.openim;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * OpenIM 连接配置（从后端 API 动态获取）
 */
public class OpenIMConfig {

    private static final String PREF_NAME = "openim_config";
    private static final String KEY_API_URL = "api_url";
    private static final String KEY_WS_URL = "ws_url";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_AVAILABLE = "available";

    private final SharedPreferences pref;

    public OpenIMConfig(Context context) {
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getApiUrl() {
        return pref.getString(KEY_API_URL, "");
    }

    public void setApiUrl(String apiUrl) {
        pref.edit().putString(KEY_API_URL, apiUrl).apply();
    }

    public String getWsUrl() {
        return pref.getString(KEY_WS_URL, "");
    }

    public void setWsUrl(String wsUrl) {
        pref.edit().putString(KEY_WS_URL, wsUrl).apply();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public void setUserId(String userId) {
        pref.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, "");
    }

    public void setToken(String token) {
        pref.edit().putString(KEY_TOKEN, token).apply();
    }

    public boolean isAvailable() {
        return pref.getBoolean(KEY_AVAILABLE, false);
    }

    public void setAvailable(boolean available) {
        pref.edit().putBoolean(KEY_AVAILABLE, available).apply();
    }

    /**
     * 清除配置（退出登录时调用）
     */
    public void clear() {
        pref.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_TOKEN)
                .putBoolean(KEY_AVAILABLE, false)
                .apply();
    }

    /**
     * 完整保存从后端获取的配置
     */
    public void saveFromResponse(String userId, String token, String wsUrl, String apiUrl) {
        pref.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_TOKEN, token)
                .putString(KEY_WS_URL, wsUrl)
                .putString(KEY_API_URL, apiUrl)
                .putBoolean(KEY_AVAILABLE, true)
                .apply();
    }
}
