package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.*;
import com.tongyangyuan.mentalhealth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class LearningPackageService {
    
    @Autowired
    private LearningPackageRepository learningPackageRepository;
    
    @Autowired
    private LearningVideoRepository learningVideoRepository;
    
    @Autowired
    private UserLearningProgressRepository progressRepository;
    
    @Autowired
    private UserRecommendedPackageRepository recommendedPackageRepository;
    
    /**
     * 获取所有启用的学习包
     */
    public List<LearningPackage> getAllActivePackages() {
        return learningPackageRepository.findByActiveTrueOrderBySortOrderAsc();
    }
    
    /**
     * 分页获取学习包
     */
    public Page<LearningPackage> getPackages(Pageable pageable) {
        return learningPackageRepository.findByActiveTrue(pageable);
    }
    
    /**
     * 根据分类获取学习包
     */
    public List<LearningPackage> getPackagesByCategory(String category) {
        return learningPackageRepository.findByCategoryAndActiveTrueOrderBySortOrderAsc(category);
    }
    
    /**
     * 获取学习包详情（包含视频列表）
     */
    public Map<String, Object> getPackageDetail(Long packageId) {
        LearningPackage pkg = learningPackageRepository.findById(packageId)
            .orElseThrow(() -> new RuntimeException("学习包不存在"));
        
        List<LearningVideo> videos = learningVideoRepository
            .findByPackageIdAndActiveTrueOrderBySortOrderAsc(packageId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("package", pkg);
        result.put("videos", videos);
        
        return result;
    }
    
    /**
     * 获取用户的推荐学习包
     */
    public List<Map<String, Object>> getRecommendedPackages(Long userId) {
        List<UserRecommendedPackage> recommendations = 
            recommendedPackageRepository.findByUserIdOrderByGmtCreateDesc(userId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserRecommendedPackage rec : recommendations) {
            Optional<LearningPackage> pkgOpt = learningPackageRepository.findById(rec.getPackageId());
            if (pkgOpt.isPresent()) {
                Map<String, Object> item = new HashMap<>();
                item.put("recommendation", rec);
                item.put("package", pkgOpt.get());
                result.add(item);
            }
        }
        
        return result;
    }
    
    /**
     * 开始学习包（标记为已查看和已开始）
     */
    @Transactional
    public void startLearningPackage(Long userId, Long packageId) {
        // 查找推荐记录并更新状态
        List<UserRecommendedPackage> recommendations = 
            recommendedPackageRepository.findByUserIdOrderByGmtCreateDesc(userId);
        
        for (UserRecommendedPackage rec : recommendations) {
            if (rec.getPackageId().equals(packageId)) {
                rec.setViewed(true);
                rec.setStarted(true);
                recommendedPackageRepository.save(rec);
                break;
            }
        }
    }
    
    /**
     * 获取用户在学习包中的学习进度
     */
    public Map<String, Object> getUserPackageProgress(Long userId, Long packageId) {
        // 获取学习包的所有视频
        List<LearningVideo> videos = learningVideoRepository
            .findByPackageIdAndActiveTrueOrderBySortOrderAsc(packageId);
        
        // 获取用户的学习进度
        List<UserLearningProgress> progressList = 
            progressRepository.findByUserIdAndPackageId(userId, packageId);
        
        // 统计完成情况
        long completedCount = progressRepository.countCompletedVideos(userId, packageId);
        long verifiedCount = progressRepository.countVerifiedVideos(userId, packageId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalVideos", videos.size());
        result.put("completedVideos", completedCount);
        result.put("verifiedVideos", verifiedCount);
        result.put("progressList", progressList);
        result.put("canTakeTest", completedCount == videos.size() && verifiedCount == videos.size());
        
        return result;
    }
    
    /**
     * 根据标签推荐学习包
     */
    public List<LearningPackage> findPackagesByTag(String tag) {
        return learningPackageRepository.findByIssueTagContaining(tag);
    }
    
    /**
     * 获取所有分类
     */
    public List<String> getAllCategories() {
        return learningPackageRepository.findAllCategories();
    }
}
