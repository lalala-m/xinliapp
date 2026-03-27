package com.example.tongyangyuan.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class PreferenceStore {

    private static final String PREF_NAME = "tong_kang_yuan_pref";
    private static final String KEY_FIRST_LAUNCH_DONE = "first_launch_done";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_HAS_CHILD_PROFILE = "has_child_profile";
    private static final String KEY_LAST_LOGIN_PHONE = "last_login_phone";
    private static final String KEY_CHILD_PROFILES = "child_profiles";
    private static final String KEY_IS_PAID = "is_paid_user";
    private static final String KEY_PAID_USER_MAP = "paid_user_map";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_BADGES = "badges";
    private static final String KEY_AVATAR_URL = "avatar_url";
    private static final String KEY_CURRENT_CHILD_ID = "current_child_id";
    private static final String KEY_CURRENT_CHILD_ID_STRING = "current_child_id_string";
    private static final String KEY_CURRENT_CHILD_NAME = "current_child_name";
    // 咨询师相关
    private static final String KEY_CONSULTANT_TOKEN = "consultant_token";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_NICKNAME = "nickname";

    private final SharedPreferences sharedPreferences;
    private static PreferenceStore instance;

    public static synchronized PreferenceStore getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceStore(context.getApplicationContext());
        }
        return instance;
    }

    public PreferenceStore(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFirstLaunch() {
        return !sharedPreferences.getBoolean(KEY_FIRST_LAUNCH_DONE, false);
    }

    public void markFirstLaunchComplete() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH_DONE, true).apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply();
    }

    public boolean hasChildProfile() {
        return sharedPreferences.getBoolean(KEY_HAS_CHILD_PROFILE, false);
    }

    public void setHasChildProfile(boolean hasChildProfile) {
        sharedPreferences.edit().putBoolean(KEY_HAS_CHILD_PROFILE, hasChildProfile).apply();
    }

    public String getLastLoginPhone() {
        return sharedPreferences.getString(KEY_LAST_LOGIN_PHONE, "");
    }

    public void setLastLoginPhone(String phone) {
        sharedPreferences.edit().putString(KEY_LAST_LOGIN_PHONE, phone).apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, "");
    }

    public void setAuthToken(String token) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    public long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }

    public void setUserId(long userId) {
        sharedPreferences.edit().putLong(KEY_USER_ID, userId).apply();
    }

    public String getAvatarUrl() {
        return sharedPreferences.getString(KEY_AVATAR_URL, "");
    }

    public void setAvatarUrl(String avatarUrl) {
        sharedPreferences.edit().putString(KEY_AVATAR_URL, avatarUrl).apply();
    }

    public long getCurrentChildId() {
        return sharedPreferences.getLong(KEY_CURRENT_CHILD_ID, -1);
    }

    public void setCurrentChildId(long childId) {
        SharedPreferences.Editor ed = sharedPreferences.edit().putLong(KEY_CURRENT_CHILD_ID, childId);
        if (childId >= 0) {
            ed.remove(KEY_CURRENT_CHILD_ID_STRING);
        }
        ed.apply();
    }

    /** 当当前孩子 id 非数字（如本地 UUID）时使用；与 {@link #getCurrentChildId()} &lt; 0 配对 */
    public String getCurrentChildIdString() {
        return sharedPreferences.getString(KEY_CURRENT_CHILD_ID_STRING, "");
    }

    public void setCurrentChildIdString(String id) {
        sharedPreferences.edit()
                .putString(KEY_CURRENT_CHILD_ID_STRING, id != null ? id : "")
                .apply();
    }

    public String getCurrentChildName() {
        return sharedPreferences.getString(KEY_CURRENT_CHILD_NAME, "");
    }

    public void setCurrentChildName(String childName) {
        sharedPreferences.edit().putString(KEY_CURRENT_CHILD_NAME, childName).apply();
    }

    public boolean hasBadge(String badgeId) {
        String badges = sharedPreferences.getString(KEY_BADGES, "");
        if (TextUtils.isEmpty(badges) || TextUtils.isEmpty(badgeId)) {
            return false;
        }
        String[] parts = badges.split(",");
        for (String part : parts) {
            if (badgeId.equals(part.trim())) {
                return true;
            }
        }
        return false;
    }

    public void grantBadge(String badgeId) {
        if (TextUtils.isEmpty(badgeId)) {
            return;
        }
        String badges = sharedPreferences.getString(KEY_BADGES, "");
        if (TextUtils.isEmpty(badges)) {
            sharedPreferences.edit().putString(KEY_BADGES, badgeId).apply();
            return;
        }
        if (hasBadge(badgeId)) {
            return;
        }
        sharedPreferences.edit().putString(KEY_BADGES, badges + "," + badgeId).apply();
    }

    public String getChildProfilesJson() {
        return sharedPreferences.getString(KEY_CHILD_PROFILES, "");
    }

    public void setChildProfilesJson(String json) {
        sharedPreferences.edit().putString(KEY_CHILD_PROFILES, json).apply();
    }

    public boolean isPaidUser() {
        String accountKey = getAccountKey();
        try {
            JSONObject map = new JSONObject(sharedPreferences.getString(KEY_PAID_USER_MAP, "{}"));
            if (map.has(accountKey)) {
                return map.optBoolean(accountKey, false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sharedPreferences.getBoolean(KEY_IS_PAID, false);
    }

    public void setPaidUser(boolean paid) {
        String accountKey = getAccountKey();
        try {
            JSONObject map = new JSONObject(sharedPreferences.getString(KEY_PAID_USER_MAP, "{}"));
            map.put(accountKey, paid);
            sharedPreferences.edit()
                    .putString(KEY_PAID_USER_MAP, map.toString())
                    .putBoolean(KEY_IS_PAID, paid) // legacy fallback
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getAccountKey() {
        String phone = getLastLoginPhone();
        if (TextUtils.isEmpty(phone)) {
            return "guest";
        }
        return phone;
    }

    // ========== 咨询师相关 ==========

    public String getConsultantToken() {
        return sharedPreferences.getString(KEY_CONSULTANT_TOKEN, "");
    }

    public void saveConsultantToken(String token) {
        sharedPreferences.edit().putString(KEY_CONSULTANT_TOKEN, token).apply();
    }

    public void clearConsultantToken() {
        sharedPreferences.edit().remove(KEY_CONSULTANT_TOKEN).apply();
    }

    public String getUserType() {
        return sharedPreferences.getString(KEY_USER_TYPE, "");
    }

    public void saveUserType(String userType) {
        sharedPreferences.edit().putString(KEY_USER_TYPE, userType). apply();
    }

    public String getNickname() {
        return sharedPreferences.getString(KEY_NICKNAME, "");
    }

    public void saveNickname(String nickname) {
        sharedPreferences.edit().putString(KEY_NICKNAME, nickname).apply();
    }

    public long getLastLoginUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }

    public void saveLastLoginUserId(long userId) {
        sharedPreferences.edit().putLong(KEY_USER_ID, userId).apply();
    }

    public void saveLastLoginPhone(String phone) {
        sharedPreferences.edit().putString(KEY_LAST_LOGIN_PHONE, phone).apply();
    }

    public void saveAvatarUrl(String url) {
        sharedPreferences.edit().putString(KEY_AVATAR_URL, url).apply();
    }
}
