package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.ConsultationPdfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 咨询PDF文档仓储
 */
@Repository
public interface ConsultationPdfDocumentRepository extends JpaRepository<ConsultationPdfDocument, Long> {
    
    /**
     * 根据咨询记录ID查找最新的PDF文档
     */
    Optional<ConsultationPdfDocument> findByConsultationRecordIdAndIsLatestTrue(Long consultationRecordId);
    
    /**
     * 根据咨询记录ID查找所有PDF文档
     */
    List<ConsultationPdfDocument> findByConsultationRecordId(Long consultationRecordId);
    
    /**
     * 标记指定咨询记录的所有PDF为非最新
     */
    @Modifying
    @Query("UPDATE ConsultationPdfDocument p SET p.isLatest = false WHERE p.consultationRecordId = :recordId")
    void markAllAsNotLatest(@Param("recordId") Long recordId);
}