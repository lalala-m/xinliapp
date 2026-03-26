package com.tongyangyuan.mentalhealth.interceptor;

import com.tongyangyuan.mentalhealth.annotation.RequireAdmin;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 管理员权限拦截器
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 检查方法或类上是否有@RequireAdmin注解
        RequireAdmin methodAnnotation = handlerMethod.getMethodAnnotation(RequireAdmin.class);
        RequireAdmin classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireAdmin.class);
        
        // 如果没有@RequireAdmin注解，直接放行
        if (methodAnnotation == null && classAnnotation == null) {
            return true;
        }

        // 获取token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"未登录\",\"message\":\"请先登录\"}");
            return false;
        }

        String token = authHeader.substring(7);
        
        try {
            // 验证token并获取用户ID
            String phone = jwtUtil.extractPhone(token);
            if (phone == null || !jwtUtil.validateToken(token, phone)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"登录已过期\",\"message\":\"请重新登录\"}");
                return false;
            }

            // 获取用户
            User user = userRepository.findByPhone(phone).orElse(null);

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"用户不存在\",\"message\":\"用户不存在\"}");
                return false;
            }

            // 检查是否是管理员或咨询师
            if (user.getUserType() != User.UserType.ADMIN && user.getUserType() != User.UserType.CONSULTANT) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"权限不足\",\"message\":\"需要管理员或咨询师权限\"}");
                return false;
            }

            // 将用户信息存入request attribute，供后续使用
            request.setAttribute("currentUser", user);
            request.setAttribute("userId", user.getId());

            return true;

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"认证失败\",\"message\":\"" + e.getMessage() + "\"}");
            return false;
        }
    }
}
