package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.annotation.RequireAdmin;
import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.ConsultationIssue;
import com.tongyangyuan.mentalhealth.entity.LifeStage;
import com.tongyangyuan.mentalhealth.service.LifeStageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户咨询问题标签 - 管理员管理接口
 */
@RestController
@RequestMapping("/admin/life-stages")
@RequireAdmin
public class AdminLifeStageController {

    private final LifeStageService lifeStageService;

    public AdminLifeStageController(LifeStageService lifeStageService) {
        this.lifeStageService = lifeStageService;
    }

    // ========== 阶段管理 ==========

    /**
     * 获取所有阶段（管理用，含禁用）
     */
    @GetMapping
    public ApiResponse<List<LifeStage>> getAllStages() {
        try {
            List<LifeStage> stages = lifeStageService.getAllStages();
            return ApiResponse.success("获取阶段列表成功", stages);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建阶段
     */
    @PostMapping
    public ApiResponse<LifeStage> createStage(@RequestBody LifeStage stage) {
        try {
            LifeStage created = lifeStageService.createStage(stage);
            return ApiResponse.success("创建阶段成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新阶段
     */
    @PutMapping("/{id}")
    public ApiResponse<LifeStage> updateStage(@PathVariable Long id, @RequestBody LifeStage stage) {
        try {
            LifeStage updated = lifeStageService.updateStage(id, stage);
            return ApiResponse.success("更新阶段成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除阶段（级联删除问题）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStage(@PathVariable Long id) {
        try {
            lifeStageService.deleteStage(id);
            return ApiResponse.success("删除阶段成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ========== 问题管理 ==========

    /**
     * 获取某阶段下的所有问题
     */
    @GetMapping("/{stageId}/issues")
    public ApiResponse<List<ConsultationIssue>> getIssuesByStageId(@PathVariable Long stageId) {
        try {
            List<ConsultationIssue> issues = lifeStageService.getIssuesByStageId(stageId);
            return ApiResponse.success("获取问题列表成功", issues);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建问题
     */
    @PostMapping("/issues")
    public ApiResponse<ConsultationIssue> createIssue(@RequestBody ConsultationIssue issue) {
        try {
            ConsultationIssue created = lifeStageService.createIssue(issue);
            return ApiResponse.success("创建问题成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新问题
     */
    @PutMapping("/issues/{id}")
    public ApiResponse<ConsultationIssue> updateIssue(@PathVariable Long id, @RequestBody ConsultationIssue issue) {
        try {
            ConsultationIssue updated = lifeStageService.updateIssue(id, issue);
            return ApiResponse.success("更新问题成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除问题
     */
    @DeleteMapping("/issues/{id}")
    public ApiResponse<Void> deleteIssue(@PathVariable Long id) {
        try {
            lifeStageService.deleteIssue(id);
            return ApiResponse.success("删除问题成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
