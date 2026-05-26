package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.MembershipRecord;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.MembershipRecordRepository;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final MembershipRecordRepository membershipRecordRepository;

    public UserController(UserRepository userRepository, JwtUtil jwtUtil,
                          MembershipRecordRepository membershipRecordRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.membershipRecordRepository = membershipRecordRepository;
    }

    /**
     * 从 membership_records 表查询用户真实会员状态（优先）
     * 同时同步到 users 表保持数据一致性
     */
    private MembershipRecord getActiveMembership(Long userId) {
        Optional<MembershipRecord> recordOpt = membershipRecordRepository
                .findTopByUserIdAndStatusOrderByEndTimeDesc(userId, MembershipRecord.STATUS_ACTIVE);
        if (recordOpt.isPresent()) {
            MembershipRecord record = recordOpt.get();
            if (record.isActive()) {
                return record;
            }
        }
        return null;
    }

    /**
     * 获取用户VIP/支付状态
     * GET /user/payment-status
     * 优先从 membership_records 表查询，同时兼容 users 表
     */
    @GetMapping("/payment-status")
    public ApiResponse<Map<String, Object>> getPaymentStatus(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Map<String, Object> status = new HashMap<>();
            
            // 优先从 membership_records 查询真实会员状态
            MembershipRecord activeRecord = getActiveMembership(userId);
            
            boolean isVip;
            LocalDateTime vipExpireTime;
            
            if (activeRecord != null) {
                // membership_records 有有效记录，以它为准
                isVip = true;
                vipExpireTime = activeRecord.getEndTime();
                
                // 同步更新 users 表（修复数据不一致）
                if (!Boolean.TRUE.equals(user.getIsVip()) || 
                    user.getVipExpireTime() == null ||
                    !user.getVipExpireTime().equals(vipExpireTime)) {
                    user.setIsVip(true);
                    user.setVipExpireTime(vipExpireTime);
                    userRepository.save(user);
                }
            } else {
                // membership_records 无有效记录，回退到 users 表
                isVip = Boolean.TRUE.equals(user.getIsVip());
                vipExpireTime = user.getVipExpireTime();
                
                // 如果 users 表显示VIP但已过期，自动清理
                if (isVip && vipExpireTime != null && vipExpireTime.isBefore(LocalDateTime.now())) {
                    user.setIsVip(false);
                    user.setVipExpireTime(null);
                    userRepository.save(user);
                    isVip = false;
                    vipExpireTime = null;
                }
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
     * 优先从 membership_records 查询会员状态
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getUserInfo(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 优先从 membership_records 查询真实会员状态
            MembershipRecord activeRecord = getActiveMembership(userId);
            boolean isVip;
            LocalDateTime vipExpireTime;
            
            if (activeRecord != null) {
                isVip = true;
                vipExpireTime = activeRecord.getEndTime();
                // 同步 users 表
                if (!Boolean.TRUE.equals(user.getIsVip())) {
                    user.setIsVip(true);
                    user.setVipExpireTime(vipExpireTime);
                    userRepository.save(user);
                }
            } else {
                isVip = Boolean.TRUE.equals(user.getIsVip());
                vipExpireTime = user.getVipExpireTime();
                // 清理过期状态
                if (isVip && vipExpireTime != null && vipExpireTime.isBefore(LocalDateTime.now())) {
                    user.setIsVip(false);
                    user.setVipExpireTime(null);
                    userRepository.save(user);
                    isVip = false;
                    vipExpireTime = null;
                }
            }

            Map<String, Object> info = new HashMap<>();
            info.put("userId", user.getId());
            info.put("phone", user.getPhone());
            info.put("nickname", user.getNickname());
            info.put("avatarUrl", user.getAvatarUrl());
            info.put("userType", user.getUserType() != null ? user.getUserType().name() : null);
            info.put("isVip", isVip);
            info.put("isPaid", isVip);
            info.put("vipExpireTime", vipExpireTime);
            info.put("currentChildId", user.getCurrentChildId());

            return ApiResponse.success(info);
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
}
