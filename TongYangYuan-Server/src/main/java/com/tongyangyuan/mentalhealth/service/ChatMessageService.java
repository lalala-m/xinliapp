package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.Appointment;
import com.tongyangyuan.mentalhealth.entity.ChatMessage;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.repository.ChatMessageRepository;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConsultantRepository consultantRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository,
                             SimpMessagingTemplate messagingTemplate,
                             ConsultantRepository consultantRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.consultantRepository = consultantRepository;
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

    /**
     * 预约被咨询师确认时，通知家长端刷新预约状态
     */
    @Transactional
    public void notifyAppointmentConfirmed(Appointment appointment, Consultant consultant) {
        String consultantName = consultant != null ? consultant.getName() : "咨询师";
        String content = "您的预约已被" + consultantName + "确认，请在预约时间进入咨询。";

        // 发一条系统消息到聊天
        ChatMessage message = new ChatMessage();
        message.setAppointmentId(appointment.getId());
        message.setSenderUserId(consultant.getUserId());
        message.setReceiverUserId(appointment.getParentUserId());
        message.setMessageType(ChatMessage.MessageType.TEXT);
        message.setContent(content);
        message.setIsFromConsultant(true);
        message.setIsRead(false);
        chatMessageRepository.save(message);

        // 通过 WebSocket 推送通知给家长
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "APPOINTMENT_CONFIRMED");
        payload.put("appointmentId", appointment.getId());
        payload.put("consultantName", consultantName);
        payload.put("message", content);
        payload.put("status", "ACCEPTED");
        messagingTemplate.convertAndSendToUser(
                appointment.getParentUserId().toString(),
                "/queue/notifications",
                payload
        );
    }

    /**
     * 预约被咨询师拒绝时，通知家长端
     */
    @Transactional
    public void notifyAppointmentRejected(Appointment appointment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "APPOINTMENT_REJECTED");
        payload.put("appointmentId", appointment.getId());
        payload.put("message", "您的预约已被咨询师拒绝，请选择其他时间或咨询师。");
        payload.put("status", "CANCELLED");
        messagingTemplate.convertAndSendToUser(
                appointment.getParentUserId().toString(),
                "/queue/notifications",
                payload
        );
    }

    /**
     * 新预约创建时，通过 WebSocket 通知咨询师
     */
    public void notifyConsultantNewAppointment(Appointment appointment) {
        // 获取咨询师的 userId（用于 WebSocket 推送）
        Long consultantUserId = consultantRepository.findById(appointment.getConsultantId())
                .map(Consultant::getUserId)
                .orElse(appointment.getConsultantId()); // 降级处理

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_APPOINTMENT");
        payload.put("appointmentId", appointment.getId());
        payload.put("appointmentNo", appointment.getAppointmentNo());
        payload.put("childName", appointment.getChildName());
        payload.put("appointmentDate", appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "");
        payload.put("timeSlot", appointment.getTimeSlot());
        payload.put("domain", appointment.getDomain());
        payload.put("status", "PENDING");
        payload.put("message", "您有新的预约咨询：" + appointment.getChildName() + "，" +
                (appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "") +
                " " + appointment.getTimeSlot());
        messagingTemplate.convertAndSendToUser(
                consultantUserId.toString(),
                "/queue/notifications",
                payload
        );
    }
}
