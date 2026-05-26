package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.ConsultationChatExport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 咨询聊天记录导出仓储
 */
@Repository
public interface ConsultationChatExportRepository extends JpaRepository<ConsultationChatExport, Long> {
    
    /**
     * 根据咨询记录ID查找最新的导出记录
     */
    Optional<ConsultationChatExport> findTopByConsultationRecordIdOrderByCreatedAtDesc(Long consultationRecordId);
    
    /**
     * 根据预约ID查找导出记录
     */
    Optional<ConsultationChatExport> findByAppointmentId(Long appointmentId);
    
    /**
     * 根据咨询记录ID查找所有导出记录
     */
    java.util.List<ConsultationChatExport> findByConsultationRecordId(Long consultationRecordId);
}