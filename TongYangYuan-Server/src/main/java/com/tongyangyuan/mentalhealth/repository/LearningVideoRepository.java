package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.LearningVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningVideoRepository extends JpaRepository<LearningVideo, Long> {
    
    // 查询学习包的所有启用视频
    List<LearningVideo> findByPackageIdAndActiveTrueOrderBySortOrderAsc(Long packageId);
    
    // 查询学习包的所有视频
    List<LearningVideo> findByPackageId(Long packageId);
    
    // 统计学习包的视频数量
    long countByPackageIdAndActiveTrue(Long packageId);
}
