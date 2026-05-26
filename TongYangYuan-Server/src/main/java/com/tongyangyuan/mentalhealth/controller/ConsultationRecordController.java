package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.ConsultationRecord;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.entity.ConsultationSignature;
import com.tongyangyuan.mentalhealth.entity.ChatMessage;
import com.tongyangyuan.mentalhealth.service.ChatMessageService;
import com.tongyangyuan.mentalhealth.service.ConsultantService;
import com.tongyangyuan.mentalhealth.service.ConsultationRecordService;
import com.tongyangyuan.mentalhealth.service.ConsultationSignatureService;

import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/consultation-records")
public class ConsultationRecordController {
    
    @Autowired
    private ConsultationRecordService consultationRecordService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ConsultantService consultantService;

    @Autowired
    private ConsultationSignatureService consultationSignatureService;
    
    @Autowired
    private ChatMessageService chatMessageService;

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
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            // 🔧 修复：支持家长用户创建记录，从预约中获取咨询师ID
            try {
                Consultant consultant = consultantService.getConsultantByUserId(userId);
                record.setConsultantId(consultant.getId());
            } catch (RuntimeException e) {
                // 不是咨询师（可能是家长），从预约中获取咨询师ID
                System.out.println("[ConsultationRecord] User is not consultant, getting consultant from appointment");
            }
            record.setAppointmentId(appointmentId);
            ConsultationRecord created = consultationRecordService.createOrUpdateRecordForAppointment(appointmentId, record);
            return ResponseEntity.ok(ApiResponse.success(created));
        } catch (RuntimeException e) {
            // 返回友好错误
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
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

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecordWithSignaturesByAppointment(
            @PathVariable Long appointmentId) {
        ConsultationRecord record = consultationRecordService.getRecordByAppointmentId(appointmentId);
        if (record == null) {
            return ResponseEntity.ok(ApiResponse.error("咨询记录不存在"));
        }
        // 查询关联的签名记录
        List<ConsultationSignature> signatures = consultationSignatureService.getSignatures(record.getId());
        // 🔧 新增：查询聊天记录
        List<ChatMessage> chatMessages = chatMessageService.getMessagesByAppointmentId(appointmentId);
        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("signatures", signatures);
        result.put("chatMessages", chatMessages);
        return ResponseEntity.ok(ApiResponse.success(result));
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
