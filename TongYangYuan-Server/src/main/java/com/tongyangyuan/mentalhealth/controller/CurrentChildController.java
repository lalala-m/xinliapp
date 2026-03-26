package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.Child;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.ChildRepository;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/current-child")
public class CurrentChildController {

    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final JwtUtil jwtUtil;

    public CurrentChildController(UserRepository userRepository,
                                  ChildRepository childRepository,
                                  JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.childRepository = childRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取当前操作孩子
     * GET /user/current-child
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getCurrentChild(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (user.getCurrentChildId() == null) {
                // 如果没有设置当前孩子，返回第一个孩子
                var children = childRepository.findByParentUserId(userId);
                if (children.isEmpty()) {
                    return ApiResponse.success("暂无孩子信息", null);
                }
                Child firstChild = children.get(0);
                // 自动设置第一个孩子为当前孩子
                user.setCurrentChildId(firstChild.getId());
                userRepository.save(user);
                return ApiResponse.success("当前孩子", buildChildInfo(firstChild));
            }

            Child child = childRepository.findById(user.getCurrentChildId()).orElse(null);
            if (child == null) {
                return ApiResponse.success("当前孩子不存在", null);
            }

            return ApiResponse.success("当前孩子", buildChildInfo(child));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 设置当前操作孩子
     * PUT /user/current-child
     */
    @PutMapping
    @Transactional
    public ApiResponse<Map<String, Object>> setCurrentChild(
            @RequestBody SetCurrentChildRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));

            // 验证孩子是否属于该用户
            Child child = childRepository.findById(request.getChildId())
                    .orElseThrow(() -> new RuntimeException("孩子不存在"));

            if (!child.getParentUserId().equals(userId)) {
                return ApiResponse.error("无权操作该孩子信息");
            }

            // 更新用户的当前孩子
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setCurrentChildId(request.getChildId());
            userRepository.save(user);

            return ApiResponse.success("已切换当前孩子", buildChildInfo(child));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户的所有孩子（带当前孩子标识）
     * GET /user/children
     */
    @GetMapping("/all")
    public ApiResponse<Map<String, Object>> getAllChildren(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            var children = childRepository.findByParentUserId(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("children", children.stream().map(this::buildChildInfo).toList());
            result.put("currentChildId", user.getCurrentChildId());

            return ApiResponse.success("孩子列表", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 构建孩子信息（包含计算年龄）
     */
    private Map<String, Object> buildChildInfo(Child child) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", child.getId());
        info.put("name", child.getName());
        info.put("gender", child.getGender());
        info.put("birthDate", child.getBirthDate());
        info.put("age", calculateAge(child.getBirthDate()));
        info.put("school", child.getSchool());
        return info;
    }

    /**
     * 根据出生日期计算年龄
     */
    private int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * 设置当前孩子的请求体
     */
    public static class SetCurrentChildRequest {
        private Long childId;

        public Long getChildId() {
            return childId;
        }

        public void setChildId(Long childId) {
            this.childId = childId;
        }
    }
}
