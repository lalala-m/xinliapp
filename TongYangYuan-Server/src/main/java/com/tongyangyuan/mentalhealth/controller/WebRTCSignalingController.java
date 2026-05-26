package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.WebRTCSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebRTC 信令控制器
 * 处理 STOMP 消息，转发通话信令
 */
@Controller
public class WebRTCSignalingController {

    private static final Logger log = LoggerFactory.getLogger(WebRTCSignalingController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 处理 WebRTC 信令消息
     * 客户端发送消息到 /app/webrtc.signal
     * 服务端转发到目标用户的 /user/queue/webrtc
     */
    @MessageMapping("/webrtc.signal")
    public void handleWebRTCSignal(@Payload WebRTCSignal signal) {
        log.info("[WebRTC Signal] Received: type={}, fromUserId={}, toUserId={}, appointmentId={}",
                signal.getType(), signal.getFromUserId(), signal.getToUserId(), signal.getAppointmentId());

        // 如果有目标用户ID，发送给特定用户
        if (signal.getToUserId() != null) {
            messagingTemplate.convertAndSendToUser(
                    signal.getToUserId().toString(),
                    "/queue/webrtc",
                    signal
            );
            log.info("[WebRTC Signal] Forwarded to user: {}", signal.getToUserId());
        }

        // 同时发送广播（确保所有订阅者都能收到）
        if (signal.getAppointmentId() != null) {
            String broadcastTopic = "/topic/webrtc." + signal.getAppointmentId();
            messagingTemplate.convertAndSend(broadcastTopic, signal);
            log.info("[WebRTC Signal] Broadcast to topic: {}", broadcastTopic);
        }
    }
}
