package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.ConsultationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationRecordRepository extends JpaRepository<ConsultationRecord, Long> {
    
    // 根据用户ID查询咨询记录
    Page<ConsultationRecord> findByParentUserIdOrderByCreatedAtDesc(Long parentUserId, Pageable pageable);
    
    // 根据咨询师ID查询咨询记录
    Page<ConsultationRecord> findByConsultantIdOrderByCreatedAtDesc(Long consultantId, Pageable pageable);
    
    // 根据预约ID查询咨询记录
    ConsultationRecord findByAppointmentId(Long appointmentId);
    
    // 查询用户的所有咨询记录
    List<ConsultationRecord> findByParentUserIdOrderByCreatedAtDesc(Long parentUserId);
    
    // 查询咨询师的所有咨询记录
    List<ConsultationRecord> findByConsultantIdOrderByCreatedAtDesc(Long consultantId);
    
    // 统计用户的咨询次数
    long countByParentUserId(Long parentUserId);
    
    // 统计咨询师的咨询次数
    long countByConsultantId(Long consultantId);
}
