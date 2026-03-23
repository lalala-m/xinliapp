package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {
    
    // 获取学习包的测试题
    List<TestQuestion> findByPackageIdAndActiveTrueOrderBySortOrderAsc(Long packageId);
}
