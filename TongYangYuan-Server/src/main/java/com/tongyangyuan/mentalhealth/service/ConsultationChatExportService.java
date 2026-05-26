package com.tongyangyuan.mentalhealth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tongyangyuan.mentalhealth.dto.ConsultationPdfDTO;
import com.tongyangyuan.mentalhealth.entity.ChatMessage;
import com.tongyangyuan.mentalhealth.entity.ConsultationChatExport;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 咨询聊天记录导出服务
 * 将聊天记录从chat_messages表导出到consultation_chat_exports表
 */
@Service
public class ConsultationChatExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConsultationChatExportService.class);
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private ConsultationChatExportRepository chatExportRepository;
    
    @Autowired
    private ConsultantRepository consultantRepository;
    
    @Autowired
    private ConsultationRecordRepository recordRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 导出聊天记录到consultation_chat_exports表
     * @param consultationRecordId 咨询记录ID
     * @param appointmentId 预约ID
     * @return 导出的聊天记录
     */
    @Transactional
    public ConsultationChatExport exportChatForConsultation(Long consultationRecordId, Long appointmentId) {
        logger.info("开始导出聊天记录: consultationRecordId={}, appointmentId={}", consultationRecordId, appointmentId);
        
        // 获取聊天记录
        List<ChatMessage> messages = chatMessageRepository.findByAppointmentIdOrderByCreatedAtAsc(appointmentId);
        logger.info("获取到 {} 条聊天记录", messages.size());
        
        // 转换为DTO列表
        List<ConsultationPdfDTO.ChatMessageDTO> chatMessageDTOs = new ArrayList<>();
        for (ChatMessage msg : messages) {
            ConsultationPdfDTO.ChatMessageDTO dto = new ConsultationPdfDTO.ChatMessageDTO();
            dto.setMessageId(msg.getId());
            dto.setSenderType(msg.getIsFromConsultant() ? "CONSULTANT" : "PARENT");
            dto.setSenderName(getSenderName(msg));
            dto.setContent(msg.getContent());
            dto.setTimestamp(msg.getCreatedAt());
            dto.setMessageType(mapMessageType(msg.getMessageType()));
            chatMessageDTOs.add(dto);
        }
        
        // 序列化为JSON
        String chatContentJson;
        try {
            objectMapper.findAndRegisterModules();
            chatContentJson = objectMapper.writeValueAsString(chatMessageDTOs);
        } catch (Exception e) {
            logger.error("序列化聊天记录失败", e);
            chatContentJson = "[]";
        }
        
        // 创建或更新导出记录
        Optional<ConsultationChatExport> existingExport = chatExportRepository.findByAppointmentId(appointmentId);
        ConsultationChatExport export;
        
        if (existingExport.isPresent()) {
            export = existingExport.get();
            export.setChatContent(chatContentJson);
            export.setExportStatus("COMPLETED");
            export.setExportCompletedAt(LocalDateTime.now());
            export.setExportErrorMessage(null);
            export.setUpdatedAt(LocalDateTime.now());
            logger.info("更新已有导出记录: id={}", export.getId());
        } else {
            export = new ConsultationChatExport();
            export.setConsultationRecordId(consultationRecordId);
            export.setAppointmentId(appointmentId);
            export.setChatContent(chatContentJson);
            export.setExportStatus("COMPLETED");
            export.setExportStartedAt(LocalDateTime.now());
            export.setExportCompletedAt(LocalDateTime.now());
            logger.info("创建新导出记录");
        }
        
        return chatExportRepository.save(export);
    }
    
    /**
     * 获取发送者名称
     */
    private String getSenderName(ChatMessage msg) {
        if (msg.getIsFromConsultant() != null && msg.getIsFromConsultant()) {
            // 尝试从咨询师表获取名称
            Optional<Consultant> consultant = consultantRepository.findByUserId(msg.getSenderUserId());
            if (consultant.isPresent()) {
                return consultant.get().getName();
            }
            return "咨询师";
        }
        return "家长";
    }
    
    /**
     * 映射消息类型
     */
    private String mapMessageType(ChatMessage.MessageType type) {
        if (type == null) return "TEXT";
        return switch (type) {
            case TEXT -> "TEXT";
            case IMAGE -> "IMAGE";
            case AUDIO -> "VOICE";
            case VIDEO -> "VIDEO";
            case SYSTEM -> "SYSTEM";
        };
    }
    
    /**
     * 获取导出的聊天记录
     */
    public Optional<ConsultationChatExport> getExportedChat(Long consultationRecordId) {
        return chatExportRepository.findTopByConsultationRecordIdOrderByCreatedAtDesc(consultationRecordId);
    }
    
    /**
     * 获取导出的聊天记录JSON
     */
    public List<ConsultationPdfDTO.ChatMessageDTO> getExportedChatMessages(Long consultationRecordId) {
        Optional<ConsultationChatExport> export = getExportedChat(consultationRecordId);
        if (export.isEmpty() || export.get().getChatContent() == null) {
            return List.of();
        }
        
        try {
            objectMapper.findAndRegisterModules();
            return objectMapper.readValue(
                export.get().getChatContent(),
                objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, ConsultationPdfDTO.ChatMessageDTO.class)
            );
        } catch (Exception e) {
            logger.error("解析导出聊天记录失败", e);
            return List.of();
        }
    }
}