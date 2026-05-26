package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.SpecialtyCategory;
import com.tongyangyuan.mentalhealth.entity.SpecialtyTag;
import com.tongyangyuan.mentalhealth.service.SpecialtyCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/specialty-categories")
public class SpecialtyCategoryController {

    private final SpecialtyCategoryService specialtyCategoryService;

    public SpecialtyCategoryController(SpecialtyCategoryService specialtyCategoryService) {
        this.specialtyCategoryService = specialtyCategoryService;
    }

    /**
     * 获取所有分类及标签
     * GET /api/specialty-categories
     */
    @GetMapping
    public ApiResponse<List<SpecialtyCategory>> getAllCategories() {
        try {
            List<SpecialtyCategory> categories = specialtyCategoryService.getAllCategoriesWithTags();
            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error("获取分类失败: " + e.getMessage());
        }
    }

    /**
     * 获取某分类下的所有标签
     * GET /api/specialty-categories/{code}/tags
     */
    @GetMapping("/{code}/tags")
    public ApiResponse<List<SpecialtyTag>> getTagsByCategoryCode(@PathVariable String code) {
        try {
            List<SpecialtyTag> tags = specialtyCategoryService.getTagsByCategoryCode(code);
            return ApiResponse.success(tags);
        } catch (Exception e) {
            return ApiResponse.error("获取标签失败: " + e.getMessage());
        }
    }
}
