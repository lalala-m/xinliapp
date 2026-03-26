package com.example.tongyangyuan.database;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.tongyangyuan.data.PreferenceStore;

public class NetworkConfig {

    // 模拟器访问本机后端：须先在电脑执行 adb reverse tcp:8080 tcp:8080（模拟器已连接时）
    // 10.0.2.2 在部分 Windows/Hyper-V 环境下会连不上，127.0.0.1 + adb reverse 更稳
    private static final String EMULATOR_BASE_URL = "http://127.0.0.1:8080/api";

    // 真机连接电脑本地后端（手机和电脑需在同一 WiFi）
    private static final String LOCAL_LAN_BASE_URL = "http://172.17.81.135:8080/api";

    // 生产环境 - 远程服务器地址
    private static final String PRODUCTION_BASE_URL = "http://106.120.183.117:8080/api";

    // 环境：0=生产(106) 1=模拟器(127.0.0.1 + adb reverse) 2=真机本地(电脑IP，同一WiFi局域网)
    private static final int ENV_MODE = 1;

    private static String cachedBaseUrl = null;

    public static String getBaseUrl() {
        if (cachedBaseUrl != null) {
            return cachedBaseUrl;
        }
        switch (ENV_MODE) {
            case 0:
                cachedBaseUrl = PRODUCTION_BASE_URL;
                break;
            case 1:
                cachedBaseUrl = EMULATOR_BASE_URL;
                break;
            case 2:
                cachedBaseUrl = LOCAL_LAN_BASE_URL;
                break;
            default:
                cachedBaseUrl = PRODUCTION_BASE_URL;
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
