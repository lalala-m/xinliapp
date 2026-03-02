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
     * 获取用户的咨询记录
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Page<ConsultationRecord>>> getUserRecords(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Pageable pageable = PageRequest.of(page, size);
        Page<ConsultationRecord> records = consultationRecordService.getUserRecords(userId, pageable);
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
     * 用户评价咨询
     */
    @PutMapping("/{id}/rating")
    public ResponseEntity<ApiResponse<ConsultationRecord>> rateConsultation(
            @PathVariable Long id,
            @RequestParam BigDecimal rating,
            @RequestParam String comment) {
        ConsultationRecord updated = consultationRecordService.rateConsultation(id, rating, comment);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
}
