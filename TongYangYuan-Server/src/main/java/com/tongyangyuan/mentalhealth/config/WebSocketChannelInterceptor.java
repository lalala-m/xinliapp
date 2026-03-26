package com.tongyangyuan.mentalhealth.config;

import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * STOMP 帧层拦截器
 * 验证 CONNECT 帧中的 Authorization 头（token）
 * WebSocket 握手层（HandshakeInterceptor）始终放行，实际认证在此完成
 */
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketChannelInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (!jwtUtil.isTokenExpired(token)) {
                        Long userId = jwtUtil.extractUserId(token);
                        if (userId != null) {
                            accessor.setUser(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                    userId, null, java.util.Collections.emptyList()));
                            log.debug("STOMP auth success: userId={}", userId);
                            return message;
                        }
                    }
                } catch (Exception e) {
                    log.debug("STOMP token validation failed: {}", e.getMessage());
                }
            }

            // 未认证的用户，拒绝 CONNECT
            log.warn("STOMP CONNECT rejected: missing or invalid Authorization header");
            throw new org.springframework.messaging.simp.stomp.ConnectionLostException(
                    "Invalid or missing Authorization token");
        }

        return message;
    }
}
