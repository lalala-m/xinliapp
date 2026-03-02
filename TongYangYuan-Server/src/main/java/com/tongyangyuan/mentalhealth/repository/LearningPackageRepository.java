package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.LearningPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPackageRepository extends JpaRepository<LearningPackage, Long> {
    
    // 查询所有启用的学习包
    List<LearningPackage> findByActiveTrueOrderBySortOrderAsc();
    
    // 分页查询启用的学习包
    Page<LearningPackage> findByActiveTrue(Pageable pageable);
    
    // 根据分类查询学习包
    List<LearningPackage> findByCategoryAndActiveTrueOrderBySortOrderAsc(String category);
    
    // 根据标签查询学习包（模糊匹配）
    @Query("SELECT lp FROM LearningPackage lp WHERE lp.active = true AND lp.issueTags LIKE %:tag%")
    List<LearningPackage> findByIssueTagContaining(@Param("tag") String tag);
    
    // 查询所有分类
    @Query("SELECT DISTINCT lp.category FROM LearningPackage lp WHERE lp.active = true")
    List<String> findAllCategories();
}
