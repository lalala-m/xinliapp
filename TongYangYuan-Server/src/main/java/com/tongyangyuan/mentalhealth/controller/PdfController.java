package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.entity.ConsultationChatExport;
import com.tongyangyuan.mentalhealth.entity.ConsultationPdfDocument;
import com.tongyangyuan.mentalhealth.service.ConsultationChatExportService;
import com.tongyangyuan.mentalhealth.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * PDF控制器 - 处理PDF生成和下载请求
 */
@RestController
@RequestMapping("/api/consultation/pdf")
public class PdfController {
    
    @Autowired
    private PdfService pdfService;
    
    @Autowired
    private ConsultationChatExportService chatExportService;
    
    /**
     * 生成咨询记录PDF
     * POST /api/consultation/pdf/generate/{recordId}
     */
    @PostMapping("/generate/{recordId}")
    public ResponseEntity<?> generatePdf(@PathVariable Long recordId) {
        try {
            ConsultationPdfDocument pdf = pdfService.generateConsultationPdf(recordId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "PDF生成成功");
            result.put("data", Map.of(
                "pdfId", pdf.getId(),
                "fileName", pdf.getPdfFileName(),
                "filePath", pdf.getPdfFilePath(),
                "fileSize", pdf.getPdfFileSize(),
                "generatedAt", pdf.getPdfGeneratedAt()
            ));
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "PDF生成失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 下载PDF文件
     * GET /api/consultation/pdf/download/{recordId}
     */
    @GetMapping("/download/{recordId}")
    public ResponseEntity<?> downloadPdf(@PathVariable Long recordId) {
        try {
            var pdfOpt = pdfService.getLatestPdf(recordId);
            if (pdfOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "PDF文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            ConsultationPdfDocument pdf = pdfOpt.get();
            byte[] pdfBytes = pdfService.getPdfFile(pdf.getPdfFilePath());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", pdf.getPdfFileName());
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "PDF下载失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 预览PDF（内联显示）
     * GET /api/consultation/pdf/preview/{recordId}
     */
    @GetMapping("/preview/{recordId}")
    public ResponseEntity<?> previewPdf(@PathVariable Long recordId) {
        try {
            var pdfOpt = pdfService.getLatestPdf(recordId);
            if (pdfOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "PDF文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            ConsultationPdfDocument pdf = pdfOpt.get();
            byte[] pdfBytes = pdfService.getPdfFile(pdf.getPdfFilePath());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", pdf.getPdfFileName());
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "PDF预览失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 获取PDF信息
     * GET /api/consultation/pdf/info/{recordId}
     */
    @GetMapping("/info/{recordId}")
    public ResponseEntity<?> getPdfInfo(@PathVariable Long recordId) {
        try {
            var pdfOpt = pdfService.getLatestPdf(recordId);
            if (pdfOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "PDF文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            ConsultationPdfDocument pdf = pdfOpt.get();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", Map.of(
                "pdfId", pdf.getId(),
                "fileName", pdf.getPdfFileName(),
                "filePath", pdf.getPdfFilePath(),
                "fileSize", pdf.getPdfFileSize(),
                "pageCount", pdf.getPdfPageCount(),
                "generatedAt", pdf.getPdfGeneratedAt(),
                "isLatest", pdf.getIsLatest()
            ));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取PDF信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 导出聊天记录
     * POST /api/consultation/pdf/export-chat/{recordId}
     * 将chat_messages表中的聊天记录导出到consultation_chat_exports表
     */
    @PostMapping("/export-chat/{recordId}")
    public ResponseEntity<?> exportChat(@PathVariable Long recordId, @RequestParam Long appointmentId) {
        try {
            ConsultationChatExport export = chatExportService.exportChatForConsultation(recordId, appointmentId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "聊天记录导出成功");
            result.put("data", Map.of(
                "exportId", export.getId(),
                "chatMessageCount", countChatMessages(export.getChatContent()),
                "exportStatus", export.getExportStatus(),
                "exportedAt", export.getExportCompletedAt()
            ));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "聊天记录导出失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 获取导出的聊天记录
     * GET /api/consultation/pdf/chat-export/{recordId}
     */
    @GetMapping("/chat-export/{recordId}")
    public ResponseEntity<?> getExportedChat(@PathVariable Long recordId) {
        try {
            var messages = chatExportService.getExportedChatMessages(recordId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", Map.of(
                "recordId", recordId,
                "messageCount", messages.size(),
                "messages", messages
            ));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取聊天记录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    private int countChatMessages(String chatContent) {
        if (chatContent == null || chatContent.isEmpty()) {
            return 0;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var list = mapper.readValue(chatContent, java.util.List.class);
            return list.size();
        } catch (Exception e) {
            return 0;
        }
    }
}
