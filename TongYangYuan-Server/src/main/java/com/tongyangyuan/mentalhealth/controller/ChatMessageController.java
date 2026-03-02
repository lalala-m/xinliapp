package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.dto.ChatMessageDTO;
import com.tongyangyuan.mentalhealth.entity.ChatMessage;
import com.tongyangyuan.mentalhealth.service.ChatMessageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageController(ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/appointment/{appointmentId}")
    public ApiResponse<List<ChatMessage>> getMessagesByAppointment(@PathVariable Long appointmentId) {
        try {
            List<ChatMessage> messages = chatMessageService.getMessagesByAppointmentId(appointmentId);
            return ApiResponse.success(messages);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/unread/{userId}")
    public ApiResponse<List<ChatMessage>> getUnreadMessages(@PathVariable Long userId) {
        try {
            List<ChatMessage> messages = chatMessageService.getUnreadMessages(userId);
            return ApiResponse.success(messages);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        try {
            ChatMessage saved = chatMessageService.saveMessage(message);
            
            ChatMessageDTO dto = convertToDTO(saved);

            // 推送给接收者
            messagingTemplate.convertAndSendToUser(
                saved.getReceiverUserId().toString(),
                "/queue/messages",
                dto
            );
            
            // 推送给发送者（用于多端同步）
            messagingTemplate.convertAndSendToUser(
                saved.getSenderUserId().toString(),
                "/queue/messages",
                dto
            );
            
            return ApiResponse.success("消息发送成功", saved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    private ChatMessageDTO convertToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setAppointmentId(message.getAppointmentId());
        dto.setSenderUserId(message.getSenderUserId());
        dto.setReceiverUserId(message.getReceiverUserId());
        dto.setMessageType(message.getMessageType().name());
        dto.setContent(message.getContent());
        dto.setMediaUrl(message.getMediaUrl());
        dto.setIsFromConsultant(message.getIsFromConsultant());
        dto.setIsRead(message.getIsRead());
        if (message.getCreatedAt() != null) {
            dto.setTimestamp(message.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        return dto;
    }

    @PutMapping("/{messageId}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long messageId) {
        try {
            chatMessageService.markAsRead(messageId);
            return ApiResponse.success("标记已读成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/appointment/{appointmentId}/read/{userId}")
    public ApiResponse<Void> markAllAsRead(@PathVariable Long appointmentId, @PathVariable Long userId) {
        try {
            chatMessageService.markAllAsRead(appointmentId, userId);
            return ApiResponse.success("全部标记已读成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
