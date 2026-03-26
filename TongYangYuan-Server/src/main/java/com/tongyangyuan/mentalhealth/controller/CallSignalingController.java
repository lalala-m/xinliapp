package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.config.LiveKitProperties;
import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.Appointment;
import com.tongyangyuan.mentalhealth.repository.AppointmentRepository;
import io.livekit.server.AccessToken;
import io.livekit.server.CanPublish;
import io.livekit.server.CanSubscribe;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 音视频通话信令服务
 * 
 * 支持两种模式：
 * 1. LiveKit 模式 - 需要配置 LiveKit 服务器
 * 2. 模拟模式 - 用于测试，不实际连接媒体服务器
 */
@RestController
@RequestMapping("/api/call")
public class CallSignalingController {

    @Autowired(required = false)
    private LiveKitProperties liveKitProperties;

    @Autowired
    private AppointmentRepository appointmentRepository;

    // 模拟通话会话存储（实际生产环境应使用 Redis）
    private static final Map<String, CallSession> callSessions = new HashMap<>();

    /**
     * 验证预约状态 - 只有 ACCEPTED 状态才能进入咨询室
     */
    private ApiResponse<?> validateAppointmentForConsultation(Long appointmentId, Long userId, boolean isCaller) {
        if (appointmentId == null) {
            return ApiResponse.error(400, "预约ID不能为空");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return ApiResponse.error(404, "预约不存在");
        }

        if (appointment.getStatus() != Appointment.AppointmentStatus.ACCEPTED) {
            return ApiResponse.error(403, "预约尚未被咨询师确认，请等待确认后再进入咨询室");
        }

        // 验证用户是否有权限使用这个预约
        boolean isParent = appointment.getParentUserId().equals(userId);
        boolean isConsultant = appointment.getConsultantId().equals(userId);

        if (!isParent && !isConsultant) {
            return ApiResponse.error(403, "您没有权限使用此预约");
        }

        return null; // 验证通过
    }

    /**
     * 创建通话会话（发起通话）
     * POST /api/call/session
     */
    @PostMapping("/session")
    public ApiResponse<Map<String, Object>> createCallSession(
            @RequestBody CreateSessionRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {

        try {
            // 验证预约状态 - 只有 ACCEPTED 才能进入咨询室
            if (request.getAppointmentId() != null && request.getCallerId() != null) {
                ApiResponse<?> validation = validateAppointmentForConsultation(
                        request.getAppointmentId(), request.getCallerId(), true);
                if (validation != null) {
                    return (ApiResponse<Map<String, Object>>) validation;
                }
            }

            String sessionId = "call_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            
            CallSession session = new CallSession();
            session.setSessionId(sessionId);
            session.setCallerId(request.getCallerId());
            session.setCallerName(request.getCallerName());
            session.setCalleeId(request.getCalleeId());
            session.setCalleeName(request.getCalleeName());
            session.setCallType(request.getCallType());
            session.setAppointmentId(request.getAppointmentId());
            session.setStatus("PENDING");
            session.setCreateTime(System.currentTimeMillis());

            callSessions.put(sessionId, session);

            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            result.put("callerId", session.getCallerId());
            result.put("callerName", session.getCallerName());
            result.put("callType", session.getCallType());
            result.put("status", session.getStatus());

            return ApiResponse.success("通话会话已创建", result);
        } catch (Exception e) {
            return ApiResponse.error("创建通话会话失败: " + e.getMessage());
        }
    }

    /**
     * 获取通话会话状态
     * GET /api/call/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public ApiResponse<CallSession> getSession(@PathVariable String sessionId) {
        CallSession session = callSessions.get(sessionId);
        if (session == null) {
            return ApiResponse.error("通话会话不存在");
        }
        return ApiResponse.success(session);
    }

    /**
     * 获取用户的待处理通话
     * GET /api/call/pending?userId={userId}
     */
    @GetMapping("/pending")
    public ApiResponse<Map<String, Object>> getPendingCall(
            @RequestParam Long userId,
            @RequestHeader(value = "Authorization", required = false) String token) {

        for (CallSession session : callSessions.values()) {
            if (session.getCalleeId().equals(userId) && "PENDING".equals(session.getStatus())) {
                Map<String, Object> result = new HashMap<>();
                result.put("sessionId", session.getSessionId());
                result.put("callerId", session.getCallerId());
                result.put("callerName", session.getCallerName());
                result.put("callType", session.getCallType());
                result.put("appointmentId", session.getAppointmentId());
                return ApiResponse.success(result);
            }
        }
        return ApiResponse.success("无待接听来电", null);
    }

    /**
     * 接听通话
     * POST /api/call/{sessionId}/accept
     */
    @PostMapping("/{sessionId}/accept")
    public ApiResponse<Map<String, Object>> acceptCall(
            @PathVariable String sessionId,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String token) {

        CallSession session = callSessions.get(sessionId);
        if (session == null) {
            return ApiResponse.error("通话会话不存在");
        }

        // 验证预约状态 - 只有 ACCEPTED 才能进入咨询室
        if (session.getAppointmentId() != null && userId != null) {
            ApiResponse<?> validation = validateAppointmentForConsultation(
                    session.getAppointmentId(), userId, false);
            if (validation != null) {
                return (ApiResponse<Map<String, Object>>) validation;
            }
        }

        if (!"PENDING".equals(session.getStatus())) {
            return ApiResponse.error("通话状态不是待接听: " + session.getStatus());
        }

        session.setStatus("CONNECTED");
        session.setConnectTime(System.currentTimeMillis());

        // 获取 LiveKit Token
        Map<String, Object> livekitInfo = getLiveKitToken(session.getAppointmentId(), session.getCalleeId());


        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("status", "CONNECTED");
        result.put("livekitToken", livekitInfo.get("token"));
        result.put("livekitServerUrl", livekitInfo.get("serverUrl"));
        result.put("roomName", livekitInfo.get("roomName"));

        return ApiResponse.success("已接听通话", result);
    }

    /**
     * 拒绝通话
     * POST /api/call/{sessionId}/reject
     */
    @PostMapping("/{sessionId}/reject")
    public ApiResponse<Map<String, Object>> rejectCall(@PathVariable String sessionId) {
        CallSession session = callSessions.get(sessionId);
        if (session == null) {
            return ApiResponse.error("通话会话不存在");
        }

        session.setStatus("REJECTED");
        session.setEndTime(System.currentTimeMillis());

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("status", "REJECTED");

        return ApiResponse.success("已拒绝通话", result);
    }

    /**
     * 结束通话
     * POST /api/call/{sessionId}/end
     */
    @PostMapping("/{sessionId}/end")
    public ApiResponse<Map<String, Object>> endCall(@PathVariable String sessionId) {
        CallSession session = callSessions.get(sessionId);
        if (session == null) {
            return ApiResponse.error("通话会话不存在");
        }

        session.setStatus("ENDED");
        session.setEndTime(System.currentTimeMillis());

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("status", "ENDED");
        long connectTime = session.getConnectTime();
        if (connectTime > 0) {
            long duration = (session.getEndTime() - connectTime) / 1000;
            result.put("duration", duration);
        }

        return ApiResponse.success("通话已结束", result);
    }

    /**
     * 获取 LiveKit Token
     */
    private Map<String, Object> getLiveKitToken(Long appointmentId, Long identity) {
        Map<String, Object> result = new HashMap<>();
        String roomName = "apt_" + appointmentId;
        String identityStr = String.valueOf(identity);

        if (liveKitProperties != null && 
            liveKitProperties.getUrl() != null && 
            !liveKitProperties.getUrl().contains("your-livekit")) {
            try {
                AccessToken token = new AccessToken(liveKitProperties.getApiKey(), liveKitProperties.getApiSecret());
                token.setName(identityStr);
                token.setIdentity(identityStr);
                token.addGrants(
                        new RoomJoin(true),
                        new RoomName(roomName),
                        new CanPublish(true),
                        new CanSubscribe(true)
                );
                result.put("token", token.toJwt());
                result.put("serverUrl", liveKitProperties.getUrl());
            } catch (Exception e) {
                result.put("token", null);
                result.put("serverUrl", null);
                result.put("error", e.getMessage());
            }
        } else {
            result.put("token", null);
            result.put("serverUrl", null);
            result.put("simulate", true);
        }

        result.put("roomName", roomName);
        return result;
    }

    /**
     * 获取 LiveKit 配置
     * GET /api/call/config
     */
    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getConfig() {
        Map<String, Object> result = new HashMap<>();

        if (liveKitProperties != null && 
            liveKitProperties.getUrl() != null && 
            !liveKitProperties.getUrl().contains("your-livekit")) {
            result.put("livekitConfigured", true);
            result.put("serverUrl", liveKitProperties.getUrl());
        } else {
            result.put("livekitConfigured", false);
            result.put("message", "LiveKit 未配置，将使用模拟通话模式");
        }

        return ApiResponse.success(result);
    }

    // ==================== 内部类 ====================

    public static class CallSession {
        private String sessionId;
        private Long callerId;
        private String callerName;
        private Long calleeId;
        private String calleeName;
        private String callType; // "video" 或 "audio"
        private Long appointmentId;
        private String status; // PENDING, CONNECTED, REJECTED, ENDED
        private long createTime;
        private long connectTime;
        private long endTime;

        // Getters and Setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public Long getCallerId() { return callerId; }
        public void setCallerId(Long callerId) { this.callerId = callerId; }
        public String getCallerName() { return callerName; }
        public void setCallerName(String callerName) { this.callerName = callerName; }
        public Long getCalleeId() { return calleeId; }
        public void setCalleeId(Long calleeId) { this.calleeId = calleeId; }
        public String getCalleeName() { return calleeName; }
        public void setCalleeName(String calleeName) { this.calleeName = calleeName; }
        public String getCallType() { return callType; }
        public void setCallType(String callType) { this.callType = callType; }
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getCreateTime() { return createTime; }
        public void setCreateTime(long createTime) { this.createTime = createTime; }
        public long getConnectTime() { return connectTime; }
        public void setConnectTime(long connectTime) { this.connectTime = connectTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
    }

    public static class CreateSessionRequest {
        private Long callerId;
        private String callerName;
        private Long calleeId;
        private String calleeName;
        private String callType;
        private Long appointmentId;

        public Long getCallerId() { return callerId; }
        public void setCallerId(Long callerId) { this.callerId = callerId; }
        public String getCallerName() { return callerName; }
        public void setCallerName(String callerName) { this.callerName = callerName; }
        public Long getCalleeId() { return calleeId; }
        public void setCalleeId(Long calleeId) { this.calleeId = calleeId; }
        public String getCalleeName() { return calleeName; }
        public void setCalleeName(String calleeName) { this.calleeName = calleeName; }
        public String getCallType() { return callType; }
        public void setCallType(String callType) { this.callType = callType; }
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    }
}
