package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.ConsultationIssue;
import com.tongyangyuan.mentalhealth.entity.LifeStage;
import com.tongyangyuan.mentalhealth.service.LifeStageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户咨询问题标签 - 公开查询接口
 * 供App端使用，无需管理员权限
 */
@RestController
@RequestMapping("/life-stages")
public class LifeStageController {

    private final LifeStageService lifeStageService;

    public LifeStageController(LifeStageService lifeStageService) {
        this.lifeStageService = lifeStageService;
    }

    /**
     * 获取所有启用的年龄/人生阶段（含问题列表）
     * GET /api/life-stages
     */
    @GetMapping
    public ApiResponse<List<LifeStage>> getAllStages() {
        try {
            List<LifeStage> stages = lifeStageService.getAllEnabledStagesWithIssues();
            return ApiResponse.success(stages);
        } catch (Exception e) {
            return ApiResponse.error("获取阶段列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取某阶段下的所有问题，按分类分组
     * GET /api/life-stages/{code}/issues
     */
    @GetMapping("/{code}/issues")
    public ApiResponse<Map<String, List<ConsultationIssue>>> getIssuesByStageCode(@PathVariable String code) {
        try {
            LifeStage stage = lifeStageService.getStageByCodeWithIssues(code);
            Map<String, List<ConsultationIssue>> grouped = lifeStageService.getIssuesGroupedByCategory(stage.getId());
            return ApiResponse.success(grouped);
        } catch (Exception e) {
            return ApiResponse.error("获取问题列表失败: " + e.getMessage());
        }
    }
}
