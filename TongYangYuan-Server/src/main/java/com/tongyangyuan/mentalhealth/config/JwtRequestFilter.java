package com.tongyangyuan.mentalhealth.config;

import com.tongyangyuan.mentalhealth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        // 跳过视频通话信令端点（WebSocket连接不需要JWT认证）
        if (path != null && path.startsWith("/api/video-signaling")) {
            chain.doFilter(request, response);
            return;
        }
        // 也跳过不带context-path的路径（Spring Security处理的是不带context-path的）
        if (path != null && path.startsWith("/video-signaling")) {
            chain.doFilter(request, response);
            return;
        }
        // 跳过通话配置端点
        if (path != null && path.startsWith("/api/call/config")) {
            chain.doFilter(request, response);
            return;
        }
        // 跳过所有已放行的端点（避免Spring Security返回403）
        if (path != null && (
                path.startsWith("/api/call/") ||
                path.startsWith("/auth/") ||
                path.startsWith("/consultants/") ||
                path.startsWith("/appointments/") ||
                path.startsWith("/messages/") ||
                path.startsWith("/uploads/") ||
                path.startsWith("/upload/") ||
                path.startsWith("/home/") ||
                path.startsWith("/user/info") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/ws_test") ||
                path.startsWith("/websocket_test") ||
                path.startsWith("/api/consultation/pdf/")
        )) {
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        String phone = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                phone = jwtUtil.extractPhone(jwt);
            } catch (Exception e) {
                logger.error("Failed to extract phone from token", e);
            }
        }

        if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt, phone)) {
                Claims claims = jwtUtil.extractAllClaims(jwt);
                String userType = (String) claims.get("userType");
                Long userId = claims.get("userId", Long.class);

                // Create a simple user details object
                UserDetails userDetails = new User(phone, "", new ArrayList<>());

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}
