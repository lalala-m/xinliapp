package com.tongyangyuan.mentalhealth.websocket;

import com.tongyangyuan.mentalhealth.dto.ChatMessageDTO;
import com.tongyangyuan.mentalhealth.entity.ChatMessage;
import com.tongyangyuan.mentalhealth.entity.UserOnlineStatus;
import com.tongyangyuan.mentalhealth.repository.UserOnlineStatusRepository;
import com.tongyangyuan.mentalhealth.service.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;

@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final UserOnlineStatusRepository userOnlineStatusRepository;
    private final StringRedisTemplate redisTemplate;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, 
                                   ChatMessageService chatMessageService, 
                                   UserOnlineStatusRepository userOnlineStatusRepository,
                                   StringRedisTemplate redisTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageService = chatMessageService;
        this.userOnlineStatusRepository = userOnlineStatusRepository;
        this.redisTemplate = redisTemplate;
    }

    @MessageMapping("/chat.online")
    public void userOnline(@Payload Long userId, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = headerAccessor.getSessionId();
            log.info("用户上线 (Redis+DB): userId={}, sessionId={}", userId, sessionId);

            // 1. 使用 Redis 记录在线状态 (TTL 5分钟，客户端需定时心跳续期)
            String onlineKey = "user:online:" + userId;
            redisTemplate.opsForValue().set(onlineKey, "ONLINE", 5, TimeUnit.MINUTES);
            log.debug("Redis在线状态已设置: {}", onlineKey);
            
            // 2. 同时也更新数据库 (持久化最后上线时间)
            UserOnlineStatus status = userOnlineStatusRepository.findByUserId(userId)
                    .orElse(new UserOnlineStatus());

            status.setUserId(userId);
            status.setIsOnline(true);
            status.setSocketId(sessionId);
            status.setLastSeenAt(LocalDateTime.now());
            userOnlineStatusRepository.save(status);

            // 广播用户在线状态 (TODO: 使用 Redis Pub/Sub 跨服务器广播)
            messagingTemplate.convertAndSend("/topic/online-status", status);
        } catch (Exception e) {
            log.error("处理用户上线失败", e);
        }
    }

    @MessageMapping("/chat.offline")
    public void userOffline(@Payload Long userId) {
        try {
            log.info("用户下线 (Redis+DB): userId={}", userId);
            
            // 1. 清除 Redis 在线状态
            String onlineKey = "user:online:" + userId;
            Boolean deleted = redisTemplate.delete(onlineKey);
            log.debug("Redis在线状态已清除: {}, result={}", onlineKey, deleted);

            // 2. 更新数据库
            UserOnlineStatus status = userOnlineStatusRepository.findByUserId(userId)
                    .orElse(new UserOnlineStatus());
            status.setUserId(userId);
            status.setIsOnline(false);
            status.setSocketId(null);
            userOnlineStatusRepository.save(status);

            // 广播用户下线
            messagingTemplate.convertAndSend("/topic/online-status", status);
        } catch (Exception e) {
            log.error("处理用户下线失败", e);
        }
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO messageDTO, SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("收到消息: {}", messageDTO);

            // 保存消息到数据库
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setAppointmentId(messageDTO.getAppointmentId());
            chatMessage.setSenderUserId(messageDTO.getSenderUserId());
            chatMessage.setReceiverUserId(messageDTO.getReceiverUserId());
            chatMessage.setMessageType(ChatMessage.MessageType.valueOf(messageDTO.getMessageType()));
            chatMessage.setContent(messageDTO.getContent());
            chatMessage.setMediaUrl(messageDTO.getMediaUrl());
            chatMessage.setIsFromConsultant(messageDTO.getIsFromConsultant());
            chatMessage.setIsRead(false);

            ChatMessage savedMessage = chatMessageService.saveMessage(chatMessage);

            // 转换为DTO
            ChatMessageDTO responseDTO = convertToDTO(savedMessage);

            // 发送给接收者 (单机版)
            messagingTemplate.convertAndSendToUser(
                    messageDTO.getReceiverUserId().toString(),
                    "/queue/messages",
                    responseDTO
            );
            
            // TODO: 如果接收者不在当前服务器，使用 Redis Pub/Sub 广播消息到集群
            // redisTemplate.convertAndSend("chat-messages", responseDTO);

            // 也发送给发送者（确认消息已发送）
            messagingTemplate.convertAndSendToUser(
                    messageDTO.getSenderUserId().toString(),
                    "/queue/messages",
                    responseDTO
            );

            log.info("消息发送成功: {}", savedMessage.getId());
        } catch (Exception e) {
            log.error("发送消息失败", e);
        }
    }

    @MessageMapping("/chat.typing")
    public void userTyping(@Payload ChatMessageDTO messageDTO) {
        try {
            // 通知对方用户正在输入
            messagingTemplate.convertAndSendToUser(
                    messageDTO.getReceiverUserId().toString(),
                    "/queue/typing",
                    messageDTO.getSenderUserId()
            );
        } catch (Exception e) {
            log.error("处理输入状态失败", e);
        }
    }

    private ChatMessageDTO convertToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setAppointmentId(message.getAppointmentId());
        dto.setSenderUserId(message.getSenderUserId());
        dto.setReceiverUserId(message.getReceiverUserId());
        dto.setMessageType(message.getMessageType().name());
        dto.setContent(message.getContent());
        dto.setMediaUrl(message.getMediaUrl());
        dto.setIsFromConsultant(message.getIsFromConsultant());
        dto.setTimestamp(message.getCreatedAt() != null ? message.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : System.currentTimeMillis());
        return dto;
    }
}
