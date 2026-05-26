package com.tongyangyuan.mentalhealth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.tongyangyuan.mentalhealth.websocket.VideoCallSignalingHandler;

/**
 * 视频通话信令服务器配置
 * 使用原生WebSocket（非STOMP），简化信令流程
 * 与STOMP聊天信令分开，避免WebView兼容性问题
 */
@Configuration
@EnableWebSocket
public class VideoCallSignalingConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new VideoCallSignalingHandler(), "/api/video-signaling", "/video-signaling")
                .setAllowedOrigins("*");
    }
}
