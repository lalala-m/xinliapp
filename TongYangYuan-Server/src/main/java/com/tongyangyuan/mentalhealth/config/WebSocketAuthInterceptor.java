package com.tongyangyuan.mentalhealth.config;

import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 * - 有 Token：验证通过后放行，将 userId 注入 WebSocket Session
 * - 无 Token：也允许连接（匿名 WebSocket），实际认证由 STOMP 帧头处理
 */
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
    private final JwtUtil jwtUtil;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 始终允许握手。无 Token 时匿名连接，由后续 STOMP 帧头中的 Authorization 头完成用户认证。
        // 即使有 Token 也正常验证并注入 userId。
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null && !token.isEmpty()) {
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                try {
                    if (!jwtUtil.isTokenExpired(token)) {
                        Long userId = jwtUtil.extractUserId(token);
                        if (userId != null) {
                            attributes.put("userId", userId);
                            log.debug("WebSocket handshake: userId={} authenticated", userId);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    log.debug("WebSocket token validation failed: {}", e.getMessage());
                }
            }
        }

        // 无 Token 或验证失败，仍允许匿名连接（ STOMP 层会要求认证）
        log.debug("WebSocket handshake: anonymous connection (no token)");
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
