package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.ChatMessage;
import com.tongyangyuan.mentalhealth.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public List<ChatMessage> getMessagesByAppointmentId(Long appointmentId) {
        return chatMessageRepository.findByAppointmentIdOrderByCreatedAtAsc(appointmentId);
    }

    public List<ChatMessage> getUnreadMessages(Long receiverUserId) {
        return chatMessageRepository.findByReceiverUserIdAndIsReadFalse(receiverUserId);
    }

    @Transactional
    public ChatMessage saveMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    @Transactional
    public void markAsRead(Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        message.setIsRead(true);
        chatMessageRepository.save(message);
    }

    @Transactional
    public void markAllAsRead(Long appointmentId, Long receiverUserId) {
        List<ChatMessage> messages = chatMessageRepository.findByAppointmentIdOrderByCreatedAtAsc(appointmentId);
        messages.stream()
                .filter(msg -> msg.getReceiverUserId().equals(receiverUserId) && !msg.getIsRead())
                .forEach(msg -> {
                    msg.setIsRead(true);
                    chatMessageRepository.save(msg);
                });
    }
}
