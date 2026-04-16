package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取用户VIP/支付状态
     * GET /user/payment-status
     */
    @GetMapping("/payment-status")
    public ApiResponse<Map<String, Object>> getPaymentStatus(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Map<String, Object> status = new HashMap<>();
            
            // VIP状态
            boolean isVip = Boolean.TRUE.equals(user.getIsVip());
            LocalDateTime vipExpireTime = user.getVipExpireTime();
            
            // 如果VIP过期，自动更新状态
            if (isVip && vipExpireTime != null && vipExpireTime.isBefore(LocalDateTime.now())) {
                user.setIsVip(false);
                userRepository.save(user);
                isVip = false;
                vipExpireTime = null;
            }
            
            status.put("isPaid", isVip);
            status.put("isVip", isVip);
            status.put("vipExpireTime", vipExpireTime != null ? 
                    vipExpireTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
            
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户信息
     * GET /user/info
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getUserInfo(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Map<String, Object> info = new HashMap<>();
            info.put("userId", user.getId());
            info.put("phone", user.getPhone());
            info.put("nickname", user.getNickname());
            info.put("avatarUrl", user.getAvatarUrl());
            info.put("userType", user.getUserType() != null ? user.getUserType().name() : null);
            info.put("isVip", Boolean.TRUE.equals(user.getIsVip()));
            info.put("vipExpireTime", user.getVipExpireTime());
            info.put("currentChildId", user.getCurrentChildId());

            return ApiResponse.success(info);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户昵称和头像
     * PUT /user/profile
     */
    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 更新昵称
            if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
                user.setNickname(request.getNickname().trim());
            }

            // 更新头像
            if (request.getAvatarUrl() != null) {
                user.setAvatarUrl(request.getAvatarUrl());
            }

            userRepository.save(user);

            Map<String, Object> result = new HashMap<>();
            result.put("nickname", user.getNickname());
            result.put("avatarUrl", user.getAvatarUrl());

            return ApiResponse.success("用户信息已更新", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 设置VIP状态（管理员用或支付回调）
     * PUT /user/vip
     */
    @PutMapping("/vip")
    public ApiResponse<Map<String, Object>> setVipStatus(
            @RequestBody SetVipRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            user.setIsVip(request.getIsVip());
            if (request.getExpireTime() != null) {
                user.setVipExpireTime(LocalDateTime.parse(request.getExpireTime()));
            }
            
            userRepository.save(user);

            Map<String, Object> result = new HashMap<>();
            result.put("isVip", user.getIsVip());
            result.put("vipExpireTime", user.getVipExpireTime());

            return ApiResponse.success("VIP状态已更新", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public static class SetVipRequest {
        private Boolean isVip;
        private String expireTime;

        public Boolean getIsVip() {
            return isVip;
        }

        public void setIsVip(Boolean isVip) {
            this.isVip = isVip;
        }

        public String getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(String expireTime) {
            this.expireTime = expireTime;
        }
    }

    public static class UpdateProfileRequest {
        private String nickname;
        private String avatarUrl;

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
