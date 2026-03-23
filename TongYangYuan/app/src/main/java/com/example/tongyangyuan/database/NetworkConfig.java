package com.example.tongyangyuan.database;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.tongyangyuan.data.PreferenceStore;

public class NetworkConfig {

    // 开发环境（模拟器连接本地服务）
    private static final String LOCAL_BASE_URL = "http://10.0.2.2:8080/api";

    // 生产环境 - 服务器地址
    private static final String PRODUCTION_BASE_URL = "http://106.120.183.117:8080/api";

    // 当前环境配置 - 开发环境用 LOCAL，真机测试/生产用 PRODUCTION
    private static final boolean USE_PRODUCTION = true;

    private static String cachedBaseUrl = null;

    public static String getBaseUrl() {
        if (cachedBaseUrl != null) {
            return cachedBaseUrl;
        }

        // 根据环境选择
        if (USE_PRODUCTION) {
            cachedBaseUrl = PRODUCTION_BASE_URL;
        } else {
            cachedBaseUrl = LOCAL_BASE_URL;
        }

        return cachedBaseUrl;
    }

    /**
     * 动态设置服务器地址（支持运行时切换）
     */
    public static void setServerUrl(Context context, String baseUrl) {
        cachedBaseUrl = baseUrl;
        // 保存到 SharedPreferences 持久化
        context.getSharedPreferences("server_config", Context.MODE_PRIVATE)
                .edit()
                .putString("base_url", baseUrl)
                .apply();
    }

    /**
     * 获取保存的服务器地址（如果有）
     */
    public static String getSavedServerUrl(Context context) {
        return context.getSharedPreferences("server_config", Context.MODE_PRIVATE)
                .getString("base_url", null);
    }

    /**
     * 从本地存储获取认证 Token
     */
    public static String getAuthToken(Context context) {
        try {
            return new PreferenceStore(context).getAuthToken();
        } catch (Exception e) {
            return "";
        }
    }
}
