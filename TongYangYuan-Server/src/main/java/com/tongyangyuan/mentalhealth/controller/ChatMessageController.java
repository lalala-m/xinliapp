package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.dto.ChatMessageDTO;
import com.tongyangyuan.mentalhealth.dto.WebRTCSignal;
import com.tongyangyuan.mentalhealth.entity.ChatMessage;
import com.tongyangyuan.mentalhealth.service.ChatMessageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            forwardCallSignalingToWebrtcIfNeeded(saved);

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

    /**
     * 家长端通过 POST /messages 发送 SYSTEM 消息 CALL:action:callType:sessionId；
     * 咨询师 Web（chat.js）来电弹窗订阅的是 /user/queue/webrtc，故此处同步转发。
     */
    private void forwardCallSignalingToWebrtcIfNeeded(ChatMessage saved) {
        if (saved.getMessageType() != ChatMessage.MessageType.SYSTEM) {
            return;
        }
        String content = saved.getContent();
        if (content == null || !content.startsWith("CALL:")) {
            return;
        }
        String[] parts = content.split(":", 4);
        if (parts.length < 4) {
            return;
        }
        String action = parts[1];
        if (!"call".equals(action) && !"accept".equals(action) && !"reject".equals(action) && !"end".equals(action)) {
            return;
        }
        String callTypePart = parts[2].isEmpty() ? "video" : parts[2];
        String sessionId = parts[3];

        WebRTCSignal sig = new WebRTCSignal();
        sig.setType(action);
        sig.setFromUserId(saved.getSenderUserId());
        sig.setToUserId(saved.getReceiverUserId());
        sig.setAppointmentId(saved.getAppointmentId());
        Map<String, Object> data = new HashMap<>();
        data.put("callType", callTypePart);
        data.put("sessionId", sessionId);
        sig.setData(data);

        messagingTemplate.convertAndSendToUser(
                saved.getReceiverUserId().toString(),
                "/queue/webrtc",
                sig
        );
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
