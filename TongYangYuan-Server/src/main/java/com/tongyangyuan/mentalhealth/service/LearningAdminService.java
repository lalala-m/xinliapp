package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.LearningPackage;
import com.tongyangyuan.mentalhealth.entity.LearningVideo;
import com.tongyangyuan.mentalhealth.entity.TestQuestion;
import com.tongyangyuan.mentalhealth.repository.LearningPackageRepository;
import com.tongyangyuan.mentalhealth.repository.LearningVideoRepository;
import com.tongyangyuan.mentalhealth.repository.TestQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LearningAdminService {
    
    @Autowired
    private LearningPackageRepository packageRepository;
    
    @Autowired
    private LearningVideoRepository videoRepository;
    
    @Autowired
    private TestQuestionRepository questionRepository;
    
    /**
     * 创建学习包
     */
    @Transactional
    public LearningPackage createPackage(LearningPackage pkg) {
        return packageRepository.save(pkg);
    }
    
    /**
     * 更新学习包
     */
    @Transactional
    public LearningPackage updatePackage(Long id, LearningPackage pkg) {
        LearningPackage existing = packageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("学习包不存在"));
        
        existing.setTitle(pkg.getTitle());
        existing.setCategory(pkg.getCategory());
        existing.setDescription(pkg.getDescription());
        existing.setIssueTags(pkg.getIssueTags());
        existing.setCoverImage(pkg.getCoverImage());
        existing.setActive(pkg.getActive());
        existing.setSortOrder(pkg.getSortOrder());
        
        return packageRepository.save(existing);
    }
    
    /**
     * 删除学习包
     */
    @Transactional
    public void deletePackage(Long id) {
        packageRepository.deleteById(id);
    }
    
    /**
     * 添加视频
     */
    @Transactional
    public LearningVideo addVideo(LearningVideo video) {
        LearningVideo saved = videoRepository.save(video);
        
        // 更新学习包的视频数量和总时长
        updatePackageStats(video.getPackageId());
        
        return saved;
    }
    
    /**
     * 添加测试题
     */
    @Transactional
    public TestQuestion addQuestion(TestQuestion question) {
        return questionRepository.save(question);
    }
    
    /**
     * 批量导入测试题
     */
    @Transactional
    public List<TestQuestion> batchImportQuestions(List<TestQuestion> questions) {
        return questionRepository.saveAll(questions);
    }
    
    /**
     * 获取统计数据
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPackages", packageRepository.count());
        stats.put("totalVideos", videoRepository.count());
        stats.put("totalQuestions", questionRepository.count());
        return stats;
    }
    
    private void updatePackageStats(Long packageId) {
        List<LearningVideo> videos = videoRepository.findByPackageId(packageId);
        
        int videoCount = videos.size();
        int totalDuration = videos.stream()
            .mapToInt(LearningVideo::getDuration)
            .sum() / 60; // 转换为分钟
        
        LearningPackage pkg = packageRepository.findById(packageId)
            .orElseThrow(() -> new RuntimeException("学习包不存在"));
        
        pkg.setVideoCount(videoCount);
        pkg.setTotalDuration(totalDuration);
        packageRepository.save(pkg);
    }
}
