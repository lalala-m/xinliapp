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
public class ConsultationRecordService {
    
    @Autowired
    private ConsultationRecordRepository consultationRecordRepository;
    
    @Autowired
    private UserRecommendedPackageRepository recommendedPackageRepository;
    
    @Autowired
    private LearningPackageRepository learningPackageRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    /**
     * 创建咨询记录
     */
    @Transactional
    public ConsultationRecord createRecord(ConsultationRecord record) {
        ConsultationRecord saved = consultationRecordRepository.save(record);
        
        // 如果有困扰标签，触发学习包推荐
        if (record.getCoreIssueTags() != null && !record.getCoreIssueTags().isEmpty()) {
            recommendLearningPackages(saved);
        }
        
        return saved;
    }

    @Transactional
    public ConsultationRecord createOrUpdateRecordForAppointment(Long appointmentId, ConsultationRecord record) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("预约不存在"));

        ConsultationRecord target = consultationRecordRepository.findByAppointmentId(appointmentId);
        if (target == null) {
            target = new ConsultationRecord();
        }

        target.setAppointmentId(appointmentId);
        target.setConsultantId(appointment.getConsultantId());
        target.setParentUserId(appointment.getParentUserId());
        target.setChildId(appointment.getChildId());

        ConsultationRecord.ConsultationType consultationType = record.getConsultationType() != null
            ? record.getConsultationType()
            : ConsultationRecord.ConsultationType.ONLINE;
        target.setConsultationType(consultationType);

        if (record.getDuration() != null) {
            target.setDuration(record.getDuration());
        }
        if (record.getSummary() != null) {
            target.setSummary(record.getSummary());
        }
        if (record.getConsultantFeedback() != null) {
            target.setConsultantFeedback(record.getConsultantFeedback());
        }
        if (record.getCoreIssueTags() != null) {
            target.setCoreIssueTags(record.getCoreIssueTags());
        }
        if (record.getStatus() != null) {
            target.setStatus(record.getStatus());
        }

        ConsultationRecord saved = consultationRecordRepository.save(target);

        if (saved.getCoreIssueTags() != null && !saved.getCoreIssueTags().isEmpty()) {
            recommendLearningPackages(saved);
        }

        return saved;
    }
    
    /**
     * 推荐学习包
     */
    private void recommendLearningPackages(ConsultationRecord record) {
        String[] tags = record.getCoreIssueTags().split(",");
        
        for (String tag : tags) {
            List<LearningPackage> packages = learningPackageRepository
                .findByIssueTagContaining(tag.trim());
            
            for (LearningPackage pkg : packages) {
                // 检查是否已推荐过
                Optional<UserRecommendedPackage> existing = recommendedPackageRepository
                    .findByUserIdAndPackageIdAndConsultationRecordId(
                        record.getParentUserId(), pkg.getId(), record.getId());
                
                if (existing.isEmpty()) {
                    UserRecommendedPackage recommendation = new UserRecommendedPackage();
                    recommendation.setUserId(record.getParentUserId());
                    recommendation.setPackageId(pkg.getId());
                    recommendation.setConsultationRecordId(record.getId());
                    recommendation.setReason(String.format(
                        "针对您的%s困扰，推荐以下学习内容", tag.trim()));
                    
                    recommendedPackageRepository.save(recommendation);
                }
            }
        }
    }
    
    /**
     * 获取用户的咨询记录
     */
    public Page<ConsultationRecord> getUserRecords(Long userId, Pageable pageable) {
        return consultationRecordRepository.findByParentUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取用户的咨询记录（支持按孩子和时间筛选）
     */
    public Page<ConsultationRecord> getUserRecords(Long userId, Long childId, 
            String startDate, String endDate, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<ConsultationRecord> spec = 
                (root, query, cb) -> cb.equal(root.get("parentUserId"), userId);
        
        if (childId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("childId"), childId));
        }
        
        if (startDate != null && !startDate.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                    cb.greaterThanOrEqualTo(root.get("createdAt"), 
                            java.time.LocalDateTime.parse(startDate + "T00:00:00")));
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                    cb.lessThanOrEqualTo(root.get("createdAt"), 
                            java.time.LocalDateTime.parse(endDate + "T23:59:59")));
        }
        
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
        org.springframework.data.domain.Pageable sortedPageable = 
                org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), 
                        pageable.getPageSize(), sort);
        
        return consultationRecordRepository.findAll(spec, sortedPageable);
    }
    
    /**
     * 获取咨询师的咨询记录
     */
    public Page<ConsultationRecord> getConsultantRecords(Long consultantId, Pageable pageable) {
        return consultationRecordRepository.findByConsultantIdOrderByCreatedAtDesc(consultantId, pageable);
    }
    
    /**
     * 获取咨询记录详情
     */
    public ConsultationRecord getRecordDetail(Long id) {
        return consultationRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("咨询记录不存在"));
    }
    
    /**
     * 用户评价咨询
     */
    @Transactional
    public ConsultationRecord rateConsultation(Long recordId, java.math.BigDecimal rating, String comment) {
        ConsultationRecord record = consultationRecordRepository.findById(recordId)
            .orElseThrow(() -> new RuntimeException("咨询记录不存在"));
        
        record.setRating(rating);
        record.setUserComment(comment);
        
        return consultationRecordRepository.save(record);
    }

    /**
     * 用户评价咨询（多维度）
     */
    @Transactional
    public ConsultationRecord rateConsultation(Long recordId, java.math.BigDecimal overallRating, String comment,
            java.math.BigDecimal professionalismRating, java.math.BigDecimal communicationRating,
            java.math.BigDecimal attitudeRating, java.math.BigDecimal problemSolvingRating,
            java.math.BigDecimal overallRating2) {
        ConsultationRecord record = consultationRecordRepository.findById(recordId)
            .orElseThrow(() -> new RuntimeException("咨询记录不存在"));
        
        // 兼容两种调用方式
        if (overallRating != null) {
            record.setRating(overallRating);
        }
        record.setUserComment(comment);
        
        return consultationRecordRepository.save(record);
    }
    
    /**
     * 根据预约ID获取咨询记录
     */
    public ConsultationRecord getRecordByAppointmentId(Long appointmentId) {
        return consultationRecordRepository.findByAppointmentId(appointmentId);
    }
    
    /**
     * 统计用户的咨询次数
     */
    public long countUserConsultations(Long userId) {
        return consultationRecordRepository.countByParentUserId(userId);
    }
    
    /**
     * 统计咨询师的咨询次数
     */
    public long countConsultantConsultations(Long consultantId) {
        return consultationRecordRepository.countByConsultantId(consultantId);
    }
}
