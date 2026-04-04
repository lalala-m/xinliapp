package com.tongyangyuan.mentalhealth.config;

import com.tongyangyuan.mentalhealth.service.OpenIMService;
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
 *
 * 支持两种 token：
 * 1. JWT token - 来自 /auth/login 接口，格式为 JJWT
 * 2. OpenIM token - 来自 /openim/init 接口，用于即时通讯信令
 */
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketChannelInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private OpenIMService openIMService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 方法1：从 Authorization header 获取 token
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            // 方法2：如果 header 没有 token，检查 WebSocket 握手时是否已通过 URL token 认证
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // 从 session attributes 获取握手时注入的 userId
                if (accessor.getSessionAttributes() != null
                        && accessor.getSessionAttributes().containsKey("userId")) {
                    Object userId = accessor.getSessionAttributes().get("userId");
                    accessor.setUser(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            userId, null, java.util.Collections.emptyList()));
                    log.debug("STOMP auth success: userId={} (from WebSocket handshake)", userId);
                    return message;
                }
                // 两者都没有，拒绝连接
                log.warn("STOMP CONNECT rejected: no Authorization header and no userId in session");
                throw new org.springframework.messaging.simp.stomp.ConnectionLostException(
                        "Invalid or missing Authorization token");
            }

            String token = authHeader.substring(7);
            Long userId = null;

            // 方式A：尝试作为 JWT token 验证（来自 /auth/login）
            try {
                if (!jwtUtil.isTokenExpired(token)) {
                    userId = jwtUtil.extractUserId(token);
                }
            } catch (Exception e) {
                log.debug("STOMP token is not a valid JWT, trying OpenIM token: {}", e.getMessage());
            }

            // 方式B：如果 JWT 验证失败，尝试作为 OpenIM token 验证
            if (userId == null && openIMService != null) {
                try {
                    userId = openIMService.extractUserIdFromToken(token);
                    log.debug("STOMP auth via OpenIM token: userId={}", userId);
                } catch (Exception e) {
                    log.debug("STOMP token is not a valid OpenIM token either: {}", e.getMessage());
                }
            }

            // 方式C：如果以上都失败，检查 session 中是否有 userId
            if (userId == null && accessor.getSessionAttributes() != null
                    && accessor.getSessionAttributes().containsKey("userId")) {
                Object sid = accessor.getSessionAttributes().get("userId");
                if (sid instanceof Long) {
                    userId = (Long) sid;
                } else if (sid instanceof Integer) {
                    userId = ((Integer) sid).longValue();
                } else if (sid instanceof String) {
                    try { userId = Long.parseLong((String) sid); } catch (Exception ignored) {}
                }
            }

            if (userId != null) {
                accessor.setUser(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userId, null, java.util.Collections.emptyList()));
                log.debug("STOMP auth success: userId={}", userId);
                return message;
            }

            // 未认证的用户，拒绝 CONNECT
            log.warn("STOMP CONNECT rejected: missing or invalid Authorization header (not a valid JWT nor OpenIM token)");
            throw new org.springframework.messaging.simp.stomp.ConnectionLostException(
                    "Invalid or missing Authorization token");
        }

        return message;
    }
}
