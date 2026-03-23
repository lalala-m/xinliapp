package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.PsychologicalAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsychologicalAssessmentRepository extends JpaRepository<PsychologicalAssessment, Long> {
    
    // 分页查询用户的评估报告
    Page<PsychologicalAssessment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 查询用户的所有评估报告
    List<PsychologicalAssessment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 根据测试记录ID查询评估报告
    Optional<PsychologicalAssessment> findByTestRecordId(Long testRecordId);
}
