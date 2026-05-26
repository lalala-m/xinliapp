package com.example.tongyangyuan.database;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.tongyangyuan.data.PreferenceStore;

public class NetworkConfig {

    // —— 真机联调必读 ——
    // ENV_MODE=1 时 base 为 127.0.0.1：仅适用于「安卓模拟器 + adb reverse」。
    // 物理真机上 127.0.0.1 是手机本机，永远连不到电脑，聊天会一直「正在连接聊天服务器…」。
    // 真机请：① 改 ENV_MODE=2 并把 LOCAL_LAN_BASE_URL 改成你电脑在局域网的真实 IPv4（ipconfig 查看），
    // 手机与电脑同一 WiFi；或 ② USB 调试时执行 adb reverse tcp:8080 tcp:8080（仍可用模式 1）；
    // 或 ③ 在 Application 启动前通过 initFromPrefs 已写入的 SharedPreferences「base_url」覆盖（见 setServerUrl）。
    // 仅开手机流量(4G/5G)无法访问你家里的 192.168.x.x，除非走公网/隧道。

    // 模拟器访问本机后端：
    // Android Studio 模拟器使用 10.0.2.2 访问宿主机（不需要 adb reverse）
    // 其他模拟器（如 Genymotion）可能需要 10.0.3.2
    // 如果用 adb reverse，则可以用 127.0.0.1
    // 注意：10.0.2.2 在部分 Windows/Hyper-V 环境下可能连不上，此时改用 127.0.0.1 + adb reverse
    // 【重要】使用 127.0.0.1 需要配合 adb reverse tcp:8080 tcp:8080
    // === 原服务器配置（保留）===
    // private static final String EMULATOR_BASE_URL = "http://127.0.0.1:8080/api";
    // ===========================
    // 【本地测试配置】USB连接真机 + adb reverse 时使用 127.0.0.1
    private static final String EMULATOR_BASE_URL = "http://127.0.0.1:8080/api";

    // 真机连接电脑本地后端（手机和电脑需在同一 WiFi）；请改为本机实际局域网 IP，不要用占位 IP
    // === 原服务器配置（保留）===
    // 当前WiFi IP: 172.17.81.135
    // private static final String LOCAL_LAN_BASE_URL = "http://172.17.81.135:8080/api";
    // ===========================
    // 【本地测试配置】如需WiFi局域网调试，请改为本机实际IP（ipconfig查看）
    private static final String LOCAL_LAN_BASE_URL = "http://172.17.81.135:8080/api";

    // 生产环境 - 远程服务器地址
    // === 原服务器配置（保留）===
    // private static final String PRODUCTION_BASE_URL = "http://139.196.5.153:8080/api";
    // ===========================
    // 【本地测试配置】生产环境地址（保留备用）
    private static final String PRODUCTION_BASE_URL = "http://139.196.5.153:8080/api";

    // 环境：0=生产 1=模拟器/本地调试(127.0.0.1+adb reverse) 2=真机局域网(WiFi)
    // === 原配置 ===
    // 上线生产环境，改为模式0
    // ==============
    // 【生产环境配置】模式0：连接远程服务器
    private static final int ENV_MODE = 0;

    private static String cachedBaseUrl = null;

    /**
     * 在 Application.onCreate 最早调用：若用户曾通过 setServerUrl 保存过地址，则优先使用，避免每次改代码重编。
     */
    public static void initFromPrefs(Context context) {
        if (context == null) return;
        String saved = getSavedServerUrl(context);
        if (saved != null && !saved.trim().isEmpty()) {
            cachedBaseUrl = saved.trim();
        }
    }

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

    /**
     * 获取当前环境的主机地址（不含协议、不含端口）。
     * 模拟器：127.0.0.1（需配合 adb reverse）| 真机/局域网：LOCAL_LAN_BASE_URL 的主机部分 | 生产：PRODUCTION_BASE_URL 的主机部分
     */
    public static String getHost() {
        String baseUrl = getBaseUrl();
        // 去掉协议前缀，取 host:port 部分
        String withoutScheme = baseUrl.replaceFirst("https?://", "");
        // 只取主机部分，去掉端口号
        String host = withoutScheme.replaceFirst(":.*", "").replaceFirst("/.*", "");
        return host;
    }
    
    /**
     * 获取当前环境的端口号（从 baseUrl 中提取）
     */
    public static int getPort() {
        String baseUrl = getBaseUrl();
        String withoutScheme = baseUrl.replaceFirst("https?://", "");
        // 提取端口号
        String portStr = withoutScheme.replaceFirst(".*:", "").replaceFirst("/.*", "");
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            return 8080; // 默认端口
        }
    }

    /**
     * 将任意 URL 中的 localhost / 127.0.0.1 替换为当前环境的正确主机地址。
     * 用于 OpenIM / WebRTC 等后端返回含 localhost 的 wsUrl/apiUrl。
     * 模拟器环境：后端返回 127.0.0.1，但模拟器需要用 10.0.2.2 访问宿主机
     */
    public static String resolveHost(String url) {
        if (url == null || url.isEmpty()) return url;
        // 模拟器环境：将 127.0.0.1 替换为 10.0.2.2（Android 模拟器访问宿主机的标准地址）
        if (EMULATOR_BASE_URL.contains("10.0.2.2")) {
            return url.replaceFirst("://localhost", "://10.0.2.2")
                      .replaceFirst("://127\\.0\\.0\\.1", "://10.0.2.2");
        }
        // 其他环境（真机/局域网）：使用当前配置的主机
        String host = getHost();
        return url.replaceFirst("://localhost", "://" + host)
                  .replaceFirst("://127\\.0\\.0\\.1", "://" + host);
    }
}
