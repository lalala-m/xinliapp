package com.example.tongyangyuan.database;

public class NetworkConfig {
    public static final String BASE_URL = "http://10.0.2.2:8080/api";

    public static String getBaseUrl() {
        // Android 模拟器连接宿主机 localhost 的特殊 IP 是 10.0.2.2
        return BASE_URL;
    }
}
