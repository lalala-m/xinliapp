package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.Child;
import com.tongyangyuan.mentalhealth.repository.ChildRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/children")
public class ChildController {

    private final ChildRepository childRepository;
    private final com.tongyangyuan.mentalhealth.util.JwtUtil jwtUtil;

    public ChildController(ChildRepository childRepository, com.tongyangyuan.mentalhealth.util.JwtUtil jwtUtil) {
        this.childRepository = childRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/parent/{parentUserId}")
    public ApiResponse<List<Child>> getChildrenByParent(@PathVariable Long parentUserId) {
        try {
            List<Child> children = childRepository.findByParentUserId(parentUserId);
            return ApiResponse.success(children);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<Child> createChild(@RequestBody Child child) {
        try {
            Child saved = childRepository.save(child);
            return ApiResponse.success("添加成功", saved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/batch")
    @org.springframework.transaction.annotation.Transactional
    public ApiResponse<List<Child>> createChildren(@RequestBody List<Child> children, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long parentId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                parentId = jwtUtil.extractUserId(token);
            }

            if (parentId == null) {
                if (children != null && !children.isEmpty()) {
                    parentId = children.get(0).getParentUserId();
                }
            }

            if (parentId == null) {
                return ApiResponse.error("无法确定家长身份");
            }

            if (children != null && !children.isEmpty()) {
                for (Child child : children) {
                    child.setParentUserId(parentId);
                }
                // 区分新增和更新：如果有 id 则更新，否则新增
                List<Child> saved = childRepository.saveAll(children);
                return ApiResponse.success("批量保存成功", saved);
            }
            return ApiResponse.success("未提供孩子信息", java.util.Collections.emptyList());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Child> updateChild(@PathVariable Long id, @RequestBody Child child) {
        try {
            child.setId(id);
            Child saved = childRepository.save(child);
            return ApiResponse.success("更新成功", saved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteChild(@PathVariable Long id) {
        try {
            childRepository.deleteById(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
