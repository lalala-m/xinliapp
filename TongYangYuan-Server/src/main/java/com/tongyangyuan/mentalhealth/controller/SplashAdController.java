package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.SplashAd;
import com.tongyangyuan.mentalhealth.service.SplashAdService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/splash-ads")
public class SplashAdController {

    private final SplashAdService splashAdService;

    public SplashAdController(SplashAdService splashAdService) {
        this.splashAdService = splashAdService;
    }

    /**
     * 获取当前有效的开屏广告（客户端调用，无需认证）
     */
    @GetMapping("/active")
    public ApiResponse<List<SplashAd>> getActiveAds() {
        try {
            List<SplashAd> ads = splashAdService.getActiveAds();
            return ApiResponse.success("获取成功", ads);
        } catch (Exception e) {
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有广告（管理后台用）
     */
    @GetMapping
    public ApiResponse<List<SplashAd>> getAllAds() {
        try {
            List<SplashAd> ads = splashAdService.getAllAds();
            return ApiResponse.success("获取成功", ads);
        } catch (Exception e) {
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建广告
     */
    @PostMapping
    public ApiResponse<SplashAd> createAd(@RequestBody SplashAd ad) {
        try {
            SplashAd created = splashAdService.createAd(ad);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新广告
     */
    @PutMapping("/{id}")
    public ApiResponse<SplashAd> updateAd(@PathVariable Long id, @RequestBody SplashAd ad) {
        try {
            SplashAd updated = splashAdService.updateAd(id, ad);
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除广告
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAd(@PathVariable Long id) {
        try {
            splashAdService.deleteAd(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 记录广告展示
     */
    @PostMapping("/{id}/show")
    public ApiResponse<Void> recordShow(@PathVariable Long id) {
        try {
            splashAdService.recordShow(id);
            return ApiResponse.success("记录成功", null);
        } catch (Exception e) {
            return ApiResponse.error("记录失败: " + e.getMessage());
        }
    }

    /**
     * 记录广告点击
     */
    @PostMapping("/{id}/click")
    public ApiResponse<Void> recordClick(@PathVariable Long id) {
        try {
            splashAdService.recordClick(id);
            return ApiResponse.success("记录成功", null);
        } catch (Exception e) {
            return ApiResponse.error("记录失败: " + e.getMessage());
        }
    }
}
