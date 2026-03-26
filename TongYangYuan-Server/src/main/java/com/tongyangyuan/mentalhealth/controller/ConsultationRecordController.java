package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.ConsultationRecord;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.service.ConsultantService;
import com.tongyangyuan.mentalhealth.service.ConsultationRecordService;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/consultation-records")
public class ConsultationRecordController {
    
    @Autowired
    private ConsultationRecordService consultationRecordService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ConsultantService consultantService;
    
    /**
     * 创建咨询记录（咨询师）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ConsultationRecord>> createRecord(
            @RequestBody ConsultationRecord record,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        // 验证是否为咨询师
        ConsultationRecord created = consultationRecordService.createRecord(record);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PostMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<ConsultationRecord>> createOrUpdateRecordForAppointment(
            @PathVariable Long appointmentId,
            @RequestBody ConsultationRecord record,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Consultant consultant = consultantService.getConsultantByUserId(userId);
        record.setConsultantId(consultant.getId());
        record.setAppointmentId(appointmentId);
        ConsultationRecord created = consultationRecordService.createOrUpdateRecordForAppointment(appointmentId, record);
        return ResponseEntity.ok(ApiResponse.success(created));
    }
    
    /**
     * 获取用户的咨询记录（支持筛选）
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Page<ConsultationRecord>>> getUserRecords(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Long childId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Pageable pageable = PageRequest.of(page, size);
        Page<ConsultationRecord> records = consultationRecordService.getUserRecords(
                userId, childId, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/consultant")
    public ResponseEntity<ApiResponse<Page<ConsultationRecord>>> getConsultantRecords(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Consultant consultant = consultantService.getConsultantByUserId(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ConsultationRecord> records = consultationRecordService.getConsultantRecords(consultant.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(records));
    }
    
    /**
     * 用户评价咨询（支持多维度评分）
     */
    @PostMapping("/{id}/rating")
    public ResponseEntity<ApiResponse<ConsultationRecord>> rateConsultation(
            @PathVariable Long id,
            @RequestBody RatingRequest request) {
        ConsultationRecord updated = consultationRecordService.rateConsultation(
                id, request.getRating(), request.getComment(),
                request.getProfessionalismRating(), request.getCommunicationRating(),
                request.getAttitudeRating(), request.getProblemSolvingRating(), request.getOverallRating());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 评价请求体
     */
    public static class RatingRequest {
        private java.math.BigDecimal rating;
        private String comment;
        private java.math.BigDecimal professionalismRating;
        private java.math.BigDecimal communicationRating;
        private java.math.BigDecimal attitudeRating;
        private java.math.BigDecimal problemSolvingRating;
        private java.math.BigDecimal overallRating;

        public java.math.BigDecimal getRating() { return rating; }
        public void setRating(java.math.BigDecimal rating) { this.rating = rating; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public java.math.BigDecimal getProfessionalismRating() { return professionalismRating; }
        public void setProfessionalismRating(java.math.BigDecimal v) { this.professionalismRating = v; }
        public java.math.BigDecimal getCommunicationRating() { return communicationRating; }
        public void setCommunicationRating(java.math.BigDecimal v) { this.communicationRating = v; }
        public java.math.BigDecimal getAttitudeRating() { return attitudeRating; }
        public void setAttitudeRating(java.math.BigDecimal v) { this.attitudeRating = v; }
        public java.math.BigDecimal getProblemSolvingRating() { return problemSolvingRating; }
        public void setProblemSolvingRating(java.math.BigDecimal v) { this.problemSolvingRating = v; }
        public java.math.BigDecimal getOverallRating() { return overallRating; }
        public void setOverallRating(java.math.BigDecimal v) { this.overallRating = v; }
    }
}
