package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.annotation.RequireAdmin;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.service.ConsultantCertificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/certification")
@RequireAdmin
public class AdminCertificationController {

    @Autowired
    private ConsultantCertificationService certificationService;

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingCertifications() {
        List<Consultant> pendingList = certificationService.getPendingCertifications();
        return ResponseEntity.ok(Map.of("message", "获取成功", "data", pendingList));
    }

    @PostMapping("/{consultantId}/approve")
    public ResponseEntity<?> approveCertification(@PathVariable Long consultantId) {
        try {
            Consultant consultant = certificationService.approveCertification(consultantId);
            return ResponseEntity.ok(Map.of("message", "认证已通过", "data", consultant));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{consultantId}/reject")
    public ResponseEntity<?> rejectCertification(
            @PathVariable Long consultantId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = (body != null) ? body.get("reason") : "未说明";
        try {
            Consultant consultant = certificationService.rejectCertification(consultantId, reason);
            return ResponseEntity.ok(Map.of("message", "认证已驳回", "data", consultant));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
