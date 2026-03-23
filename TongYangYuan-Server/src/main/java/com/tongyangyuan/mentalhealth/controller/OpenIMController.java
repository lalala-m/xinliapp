package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.config.OpenIMProperties;
import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import com.tongyangyuan.mentalhealth.service.OpenIMService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * OpenIM 相关接口，供 Android 客户端调用
 *
 * 接口说明：
 * - POST /openim/register    在 OpenIM 服务端注册用户
 * - GET  /openim/token       获取用户连接 OpenIM 所需的 Token
 * - GET  /openim/config      获取 OpenIM 连接配置（SDK 地址等）
 */
@RestController
@RequestMapping("/openim")
public class OpenIMController {

    private final OpenIMService openIMService;
    private final OpenIMProperties openIMProperties;
    private final UserRepository userRepository;

    public OpenIMController(OpenIMService openIMService,
                           OpenIMProperties openIMProperties,
                           UserRepository userRepository) {
        this.openIMService = openIMService;
        this.openIMProperties = openIMProperties;
        this.userRepository = userRepository;
    }

    /**
     * 在 OpenIM 服务端注册用户（幂等）
     * 建议在用户登录后 / 注册后由后端自动调用，此处供手动触发
     */
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> registerUser(@RequestBody RegisterUserRequest request) {
        try {
            String userId = request.getUserId();
            String nickname = request.getNickname() != null ? request.getNickname() : "用户" + userId;
            String avatar = request.getAvatar() != null ? request.getAvatar() : "";

            openIMService.registerUser(userId, nickname, avatar);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("registered", true);

            return ApiResponse.success("用户在OpenIM中注册成功", data);
        } catch (Exception e) {
            return ApiResponse.error("注册OpenIM用户失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户连接 OpenIM 的 Token
     * 客户端每次连接前调用
     */
    @GetMapping("/token/{userId}")
    public ApiResponse<Map<String, Object>> getToken(@PathVariable String userId) {
        try {
            if (!openIMService.isAvailable()) {
                return ApiResponse.error("OpenIM服务未就绪，请稍后重试");
            }

            String token = openIMService.getUserToken(userId);
            if (token == null) {
                return ApiResponse.error("获取Token失败，用户可能未注册");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("token", token);
            // 返回外部可访问的 WebSocket 地址
            data.put("wsUrl", openIMProperties.getExternalWsUrl());
            data.put("apiUrl", openIMProperties.getExternalApiUrl());

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("获取Token失败: " + e.getMessage());
        }
    }

    /**
     * 获取 OpenIM 连接配置（供客户端初始化 SDK）
     */
    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getConfig() {
        try {
            Map<String, Object> data = new HashMap<>();
            // 返回外部可访问的地址
            data.put("apiUrl", openIMProperties.getExternalApiUrl());
            data.put("wsUrl", openIMProperties.getExternalWsUrl());
            data.put("available", openIMService.isAvailable());

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 为当前登录用户自动注册并返回 Token
     * 客户端在登录成功后调用此接口即可完成 OpenIM 初始化
     */
    @PostMapping("/init")
    public ApiResponse<Map<String, Object>> initForUser(@RequestBody InitUserRequest request) {
        try {
            Long userId = request.getUserId();
            if (userId == null) {
                return ApiResponse.error("userId不能为空");
            }

            User user = userRepository.findById(userId)
                    .orElse(null);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }

            String userIdStr = String.valueOf(userId);
            String nickname = user.getNickname() != null ? user.getNickname()
                    : (user.getWxNickname() != null ? user.getWxNickname()
                    : ("用户" + userId));
            String avatar = user.getAvatarUrl() != null ? user.getAvatarUrl()
                    : (user.getWxAvatarUrl() != null ? user.getWxAvatarUrl() : "");

            // 1. 注册用户
            openIMService.registerUser(userIdStr, nickname, avatar);

            // 2. 获取 Token
            String token = openIMService.getUserToken(userIdStr);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userIdStr);
            data.put("token", token);
            // 返回外部可访问的地址
            data.put("wsUrl", openIMProperties.getExternalWsUrl());
            data.put("apiUrl", openIMProperties.getExternalApiUrl());
            data.put("available", openIMService.isAvailable());

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("初始化OpenIM失败: " + e.getMessage());
        }
    }

    public static class RegisterUserRequest {
        private String userId;
        private String nickname;
        private String avatar;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }

    public static class InitUserRequest {
        private Long userId;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}
