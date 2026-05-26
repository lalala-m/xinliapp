package com.tongyangyuan.mentalhealth.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.tongyangyuan.mentalhealth.dto.ConsultationPdfDTO;
import com.tongyangyuan.mentalhealth.entity.ConsultationChatExport;
import com.tongyangyuan.mentalhealth.entity.ConsultationPdfDocument;
import com.tongyangyuan.mentalhealth.entity.ConsultationRecord;
import com.tongyangyuan.mentalhealth.entity.ConsultationSignature;
import com.tongyangyuan.mentalhealth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * PDF生成服务
 */
@Service
public class PdfService {
    
    @Autowired
    private ConsultationRecordRepository recordRepository;
    
    @Autowired
    private ConsultationSignatureRepository signatureRepository;
    
    @Autowired
    private ConsultationPdfDocumentRepository pdfDocumentRepository;
    
    @Autowired
    private ConsultationChatExportRepository chatExportRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private ConsultantRepository consultantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // PDF存储路径
    private static final String PDF_STORAGE_PATH = "uploads/consultation-pdfs/";
    
    // 日期格式化
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
    
    /**
     * 生成咨询记录PDF
     */
    @Transactional
    public ConsultationPdfDocument generateConsultationPdf(Long recordId) throws Exception {
        ConsultationRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("咨询记录不存在: " + recordId));
        
        ConsultationPdfDTO dto = buildPdfDTO(record);
        
        String fileName = String.format("consultation_%d_%d.pdf", 
                record.getAppointmentId(), 
                System.currentTimeMillis());
        
        Path directory = Paths.get(PDF_STORAGE_PATH);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        
        String filePath = PDF_STORAGE_PATH + fileName;
        generatePdfFile(dto, filePath);
        
        ConsultationPdfDocument pdfDocument = new ConsultationPdfDocument();
        pdfDocument.setConsultationRecordId(recordId);
        pdfDocument.setPdfFileName(fileName);
        pdfDocument.setPdfFilePath(filePath);
        
        File pdfFile = new File(filePath);
        pdfDocument.setPdfFileSize(pdfFile.length());
        pdfDocument.setIsLatest(true);
        
        pdfDocumentRepository.markAllAsNotLatest(recordId);
        pdfDocument = pdfDocumentRepository.save(pdfDocument);
        
        return pdfDocument;
    }
    
    public byte[] getPdfFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("PDF文件不存在: " + filePath);
        }
        return Files.readAllBytes(path);
    }
    
    public Optional<ConsultationPdfDocument> getLatestPdf(Long recordId) {
        return pdfDocumentRepository.findByConsultationRecordIdAndIsLatestTrue(recordId);
    }
    
    private ConsultationPdfDTO buildPdfDTO(ConsultationRecord record) {
        ConsultationPdfDTO dto = new ConsultationPdfDTO();
        
        dto.setRecordId(record.getId());
        dto.setAppointmentId(record.getAppointmentId());
        dto.setConsultationDate(record.getCreatedAt());
        dto.setSummary(record.getSummary());
        dto.setConsultantFeedback(record.getConsultantFeedback());
        dto.setCoreIssueTags(record.getCoreIssueTags());
        dto.setRating(record.getRating());
        dto.setUserComment(record.getUserComment());
        dto.setStatus(record.getStatus() != null ? record.getStatus().name() : null);
        
        if (record.getAppointmentId() != null) {
            dto.setAppointmentNo("AP" + record.getAppointmentId());
            
            var appointmentOpt = appointmentRepository.findById(record.getAppointmentId());
            if (appointmentOpt.isPresent()) {
                var appointment = appointmentOpt.get();
                dto.setConsultationType(appointment.getDomain());
                
                if (appointment.getConsultantId() != null) {
                    var consultantOpt = consultantRepository.findById(appointment.getConsultantId());
                    if (consultantOpt.isPresent()) {
                        var consultant = consultantOpt.get();
                        dto.setConsultantName(consultant.getName());
                        dto.setConsultantTitle(consultant.getTitle());
                        dto.setConsultantSpecialty(consultant.getSpecialty());
                    }
                }
                
                if (appointment.getParentUserId() != null) {
                    var userOpt = userRepository.findById(appointment.getParentUserId());
                    if (userOpt.isPresent()) {
                        var user = userOpt.get();
                        dto.setParentUserId(user.getId());
                        dto.setParentNickname(user.getNickname());
                        dto.setParentPhone(user.getPhone());
                    }
                }
            }
        }
        
        List<ConsultationSignature> signatures = signatureRepository.findByConsultationRecordId(record.getId());
        if (signatures != null && !signatures.isEmpty()) {
            List<ConsultationPdfDTO.SignatureInfo> signatureInfos = signatures.stream()
                    .map(this::convertToSignatureInfo)
                    .toList();
            dto.setSignatures(signatureInfos);
        }
        
        Optional<ConsultationChatExport> chatExport = chatExportRepository.findTopByConsultationRecordIdOrderByCreatedAtDesc(record.getId());
        if (chatExport.isPresent() && chatExport.get().getChatContent() != null) {
            dto.setChatMessages(parseChatMessages(chatExport.get().getChatContent()));
        }
        
        return dto;
    }
    
    private ConsultationPdfDTO.SignatureInfo convertToSignatureInfo(ConsultationSignature signature) {
        ConsultationPdfDTO.SignatureInfo info = new ConsultationPdfDTO.SignatureInfo();
        info.setId(signature.getId());
        info.setSignerType(signature.getSignerType());
        info.setSignerName(signature.getSignerName());
        info.setSignatureData(signature.getSignatureImage());
        info.setSignedAt(signature.getSignedAt());
        info.setIpAddress(signature.getIpAddress());
        return info;
    }
    
    private List<ConsultationPdfDTO.ChatMessageDTO> parseChatMessages(String chatContent) {
        if (chatContent == null || chatContent.isEmpty()) {
            return List.of();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.findAndRegisterModules();
            java.util.List<ConsultationPdfDTO.ChatMessageDTO> messages = mapper.readValue(
                chatContent,
                mapper.getTypeFactory().constructCollectionType(java.util.List.class, ConsultationPdfDTO.ChatMessageDTO.class)
            );
            return messages;
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(PdfService.class).warn("解析聊天记录JSON失败: " + e.getMessage());
            return List.of();
        }
    }
    
    private void generatePdfFile(ConsultationPdfDTO dto, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        addTitle(document, "心理健康咨询服务记录");
        addBasicInfo(document, dto);
        addConsultationContent(document, dto);
        addSignatures(document, dto);
        addFooter(document);
        document.close();
        
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            baos.writeTo(fos);
        }
    }
    
    private void addTitle(Document document, String title) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Paragraph titleParagraph = new Paragraph(title, titleFont);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.setSpacingAfter(20);
        document.add(titleParagraph);
        document.add(new Chunk("\n" + "─".repeat(50) + "\n"));
        document.add(Chunk.NEWLINE);
    }
    
    private void addBasicInfo(Document document, ConsultationPdfDTO dto) throws DocumentException {
        Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
        
        Paragraph sectionTitle = new Paragraph("基本信息", sectionFont);
        sectionTitle.setSpacingBefore(15);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
        
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2f, 1.5f, 2f});
        
        addTableCell(table, "预约编号:", dto.getAppointmentNo() != null ? dto.getAppointmentNo() : "N/A", normalFont);
        addTableCell(table, "咨询日期:", formatDateTime(dto.getConsultationDate()), normalFont);
        addTableCell(table, "咨询时长:", dto.getDuration() != null ? dto.getDuration() + "分钟" : "N/A", normalFont);
        addTableCell(table, "咨询类型:", dto.getConsultationType() != null ? dto.getConsultationType() : "N/A", normalFont);
        
        addTableCell(table, "咨询师:", dto.getConsultantName() != null ? dto.getConsultantName() : "N/A", normalFont);
        addTableCell(table, "职  称:", dto.getConsultantTitle() != null ? dto.getConsultantTitle() : "N/A", normalFont);
        addTableCell(table, "专  业:", dto.getConsultantSpecialty() != null ? dto.getConsultantSpecialty() : "N/A", normalFont);
        addTableCell(table, "评  分:", formatRating(dto.getRating()), normalFont);
        
        addTableCell(table, "家长:", dto.getParentNickname() != null ? dto.getParentNickname() : "N/A", normalFont);
        addTableCell(table, "联系电话:", dto.getParentPhone() != null ? dto.getParentPhone() : "N/A", normalFont);
        addTableCell(table, "儿童姓名:", "N/A", normalFont);
        addTableCell(table, "儿童年龄:", "N/A", normalFont);
        
        document.add(table);
        document.add(Chunk.NEWLINE);
    }
    
    private void addConsultationContent(Document document, ConsultationPdfDTO dto) throws DocumentException {
        Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
        
        Paragraph summaryTitle = new Paragraph("咨询摘要", sectionFont);
        summaryTitle.setSpacingBefore(15);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);
        
        String summary = dto.getSummary() != null ? dto.getSummary() : "暂无";
        document.add(new Paragraph(summary, normalFont));
        
        if (dto.getConsultantFeedback() != null && !dto.getConsultantFeedback().isEmpty()) {
            Paragraph t = new Paragraph("咨询师反馈", sectionFont);
            t.setSpacingBefore(15);
            t.setSpacingAfter(10);
            document.add(t);
            document.add(new Paragraph(dto.getConsultantFeedback(), normalFont));
        }
        
        if (dto.getCoreIssueTags() != null && !dto.getCoreIssueTags().isEmpty()) {
            Paragraph t = new Paragraph("核心问题", sectionFont);
            t.setSpacingBefore(15);
            t.setSpacingAfter(10);
            document.add(t);
            document.add(new Paragraph(dto.getCoreIssueTags(), normalFont));
        }
        
        if (dto.getUserComment() != null && !dto.getUserComment().isEmpty()) {
            Paragraph t = new Paragraph("用户评价", sectionFont);
            t.setSpacingBefore(15);
            t.setSpacingAfter(10);
            document.add(t);
            document.add(new Paragraph("评分: " + formatRating(dto.getRating()), normalFont));
            document.add(new Paragraph(dto.getUserComment(), normalFont));
        }
    }
    
    private void addSignatures(Document document, ConsultationPdfDTO dto) throws DocumentException {
        if (dto.getSignatures() == null || dto.getSignatures().isEmpty()) return;
        
        Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
        Font labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        
        document.newPage();
        document.add(new Paragraph("签名确认", sectionFont));
        document.add(Chunk.NEWLINE);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 1f});
        table.setSpacingBefore(10);
        
        for (ConsultationPdfDTO.SignatureInfo signature : dto.getSignatures()) {
            String signerTypeLabel = "CONSULTANT".equals(signature.getSignerType()) ? "咨询师签名" : "家长签名";
            String signerName = signature.getSignerName() != null ? signature.getSignerName() : "未知";
            
            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(10);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_TOP);
            
            // 签名者名称标签
            cell.addElement(new Paragraph(signerTypeLabel + " - " + signerName, normalFont));
            cell.addElement(new Paragraph(" ", labelFont));
            
            // 嵌入签名图片
            String sigData = signature.getSignatureData();
            if (sigData != null && !sigData.isEmpty()) {
                try {
                    // 移除 Base64 前缀（如 data:image/png;base64,）
                    String base64Data = sigData;
                    if (sigData.contains(",")) {
                        base64Data = sigData.substring(sigData.indexOf(",") + 1);
                    }
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                    com.lowagie.text.Image signatureImage = com.lowagie.text.Image.getInstance(imageBytes);
                    // 限制签名图片尺寸
                    signatureImage.scaleToFit(200, 100);
                    signatureImage.setAlignment(Element.ALIGN_CENTER);
                    cell.addElement(signatureImage);
                } catch (Exception e) {
                    // 图片解析失败时显示文字提示
                    cell.addElement(new Paragraph("[签名图片无法显示]", labelFont));
                }
            } else {
                cell.addElement(new Paragraph("[未签名]", labelFont));
            }
            
            // 签名时间
            if (signature.getSignedAt() != null) {
                cell.addElement(new Paragraph(" ", labelFont));
                cell.addElement(new Paragraph("签名时间: " + formatDateTime(signature.getSignedAt()), labelFont));
            }
            
            table.addCell(cell);
        }
        document.add(table);
    }
    
    private void addFooter(Document document) throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        
        Font footerFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);
        Paragraph footer = new Paragraph("本文件由同阳缘心理健康咨询服务系统自动生成", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        Paragraph generateTime = new Paragraph("生成时间: " + formatDateTime(LocalDateTime.now()), footerFont);
        generateTime.setAlignment(Element.ALIGN_CENTER);
        document.add(generateTime);
    }
    
    private void addTableCell(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATE_FORMATTER);
    }
    
    private String formatRating(BigDecimal rating) {
        if (rating == null) return "N/A";
        return rating.toString() + " / 5.0";
    }
}