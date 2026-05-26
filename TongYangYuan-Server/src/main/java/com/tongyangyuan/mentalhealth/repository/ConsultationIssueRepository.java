package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.ConsultationIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationIssueRepository extends JpaRepository<ConsultationIssue, Long> {

    List<ConsultationIssue> findByStageIdAndIsEnabledTrueOrderBySortOrderAsc(Long stageId);

    List<ConsultationIssue> findByStageIdOrderBySortOrderAsc(Long stageId);

    List<ConsultationIssue> findByCategoryNameAndStageIdOrderBySortOrderAsc(String categoryName, Long stageId);

    @Modifying
    @Query("DELETE FROM ConsultationIssue ci WHERE ci.stageId = :stageId")
    void deleteByStageId(@Param("stageId") Long stageId);

    long countByStageId(Long stageId);
}
