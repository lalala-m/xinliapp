package com.tongyangyuan.mentalhealth.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "openim")
public class OpenIMProperties {

    // Docker 内部地址（服务端内部调用）
    private String apiUrl = "http://openim:10002";
    private String wsUrl = "ws://openim:10001";

    // 对外暴露的公网地址（供手机端连接）
    // 部署时请修改为你的服务器公网IP或域名
    @Value("${openim.external-api-url:http://你的服务器IP:10002}")
    private String externalApiUrl;

    @Value("${openim.external-ws-url:ws://你的服务器IP:10001}")
    private String externalWsUrl;

    private String adminUserId = "openIMAdmin";
    private String secret = "openIMAdmin";

    // 供客户端使用的 SDK URL（默认返回外网地址）
    private String sdkUrl;

    /**
     * 获取供客户端使用的 WebSocket URL
     * 优先使用外部配置，否则使用内部地址
     */
    public String getSdkUrl() {
        if (sdkUrl != null && !sdkUrl.isEmpty()) {
            return sdkUrl;
        }
        // 如果没有单独配置 sdkUrl，返回外部 WebSocket URL
        return externalWsUrl;
    }

    /**
     * 获取供客户端使用的 API URL
     */
    public String getExternalApiUrl() {
        return externalApiUrl;
    }

    /**
     * 获取供客户端使用的 WebSocket URL
     */
    public String getExternalWsUrl() {
        return externalWsUrl;
    }
}
