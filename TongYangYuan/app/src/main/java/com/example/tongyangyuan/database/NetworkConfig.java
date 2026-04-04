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

    // 模拟器访问本机后端：须先在电脑执行 adb reverse tcp:8080 tcp:8080（模拟器已连接时）
    // 10.0.2.2 在部分 Windows/Hyper-V 环境下会连不上，127.0.0.1 + adb reverse 更稳
    // LiveKit：后端返回 ws://127.0.0.1:7880 时在模拟器上保持不改，须执行 adb reverse tcp:7880 tcp:7880；
    // 并确保本机已启动 livekit（如 docker）。勿依赖 10.0.2.2:7880，与 adb reverse 冲突。
    private static final String EMULATOR_BASE_URL = "http://127.0.0.1:8080/api";

    // 真机连接电脑本地后端（手机和电脑需在同一 WiFi）；请改为本机实际局域网 IP，不要用占位 IP
    private static final String LOCAL_LAN_BASE_URL = "http://192.168.56.1:8080/api";

    // 生产环境 - 远程服务器地址
    private static final String PRODUCTION_BASE_URL = "http://106.120.183.117:8080/api";

    // 环境：0=生产(106) 1=模拟器(127.0.0.1 + adb reverse) 2=真机本地(电脑IP，同一WiFi局域网)
    private static final int ENV_MODE = 1;

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
     * 获取当前环境的主机地址（不含协议）。
     * 模拟器：127.0.0.1（需配合 adb reverse）| 真机/局域网：LOCAL_LAN_BASE_URL 的主机部分 | 生产：PRODUCTION_BASE_URL 的主机部分
     */
    public static String getHost() {
        String baseUrl = getBaseUrl();
        // 去掉协议前缀，取 host:port 部分
        String withoutScheme = baseUrl.replaceFirst("https?://", "");
        return withoutScheme.replaceFirst("/.*", "");
    }

    /**
     * 将任意 URL 中的 localhost / 127.0.0.1 替换为当前环境的正确主机地址。
     * 用于 OpenIM / LiveKit 等后端返回含 localhost 的 wsUrl/apiUrl，
     * 在模拟器上需映射到 127.0.0.1，真机/局域网保持不变。
     */
    public static String resolveHost(String url) {
        if (url == null || url.isEmpty()) return url;
        String host = getHost();
        // 替换 localhost 或 127.0.0.1
        return url.replaceFirst("://localhost", "://" + host)
                  .replaceFirst("://127\\.0\\.0\\.1", "://" + host);
    }
}
