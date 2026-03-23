package com.tongyangyuan.mentalhealth.websocket;

import com.tongyangyuan.mentalhealth.dto.WebRTCSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebRTCSignalingController {

    private static final Logger log = LoggerFactory.getLogger(WebRTCSignalingController.class);
    private final SimpMessagingTemplate messagingTemplate;

    public WebRTCSignalingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/webrtc.signal")
    public void handleSignal(@Payload WebRTCSignal signal) {
        try {
            log.info("收到WebRTC信令: type={}, from={}, to={}",
                    signal.getType(), signal.getFromUserId(), signal.getToUserId());

            // 转发信令给目标用户
            messagingTemplate.convertAndSendToUser(
                    signal.getToUserId().toString(),
                    "/queue/webrtc",
                    signal
            );

            log.info("WebRTC信令转发成功");
        } catch (Exception e) {
            log.error("处理WebRTC信令失败", e);
        }
    }

    @MessageMapping("/webrtc.call")
    public void initiateCall(@Payload WebRTCSignal signal) {
        try {
            log.info("发起视频通话: from={}, to={}, appointment={}",
                    signal.getFromUserId(), signal.getToUserId(), signal.getAppointmentId());

            signal.setType("call");

            // 通知被叫用户
            messagingTemplate.convertAndSendToUser(
                    signal.getToUserId().toString(),
                    "/queue/webrtc",
                    signal
            );

            log.info("视频通话请求已发送");
        } catch (Exception e) {
            log.error("发起视频通话失败", e);
        }
    }

    @MessageMapping("/webrtc.accept")
    public void acceptCall(@Payload WebRTCSignal signal) {
        try {
            log.info("接受视频通话: from={}, to={}", signal.getFromUserId(), signal.getToUserId());

            signal.setType("accept");

            // 通知呼叫方
            messagingTemplate.convertAndSendToUser(
                    signal.getToUserId().toString(),
                    "/queue/webrtc",
                    signal
            );

            log.info("视频通话接受通知已发送");
        } catch (Exception e) {
            log.error("接受视频通话失败", e);
        }
    }

    @MessageMapping("/webrtc.reject")
    public void rejectCall(@Payload WebRTCSignal signal) {
        try {
            log.info("拒绝视频通话: from={}, to={}", signal.getFromUserId(), signal.getToUserId());

            signal.setType("reject");

            // 通知呼叫方
            messagingTemplate.convertAndSendToUser(
                    signal.getToUserId().toString(),
                    "/queue/webrtc",
                    signal
            );

            log.info("视频通话拒绝通知已发送");
        } catch (Exception e) {
            log.error("拒绝视频通话失败", e);
        }
    }

    @MessageMapping("/webrtc.end")
    public void endCall(@Payload WebRTCSignal signal) {
        try {
            log.info("结束视频通话: from={}, to={}", signal.getFromUserId(), signal.getToUserId());

            signal.setType("end");

            // 通知对方
            messagingTemplate.convertAndSendToUser(
                    signal.getToUserId().toString(),
                    "/queue/webrtc",
                    signal
            );

            log.info("视频通话结束通知已发送");
        } catch (Exception e) {
            log.error("结束视频通话失败", e);
        }
    }
}
