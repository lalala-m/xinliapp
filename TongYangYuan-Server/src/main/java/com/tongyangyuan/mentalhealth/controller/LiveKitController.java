package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.config.LiveKitProperties;
import com.tongyangyuan.mentalhealth.entity.Appointment;
import com.tongyangyuan.mentalhealth.repository.AppointmentRepository;
import io.livekit.server.AccessToken;
import io.livekit.server.CanPublish;
import io.livekit.server.CanSubscribe;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/livekit")
public class LiveKitController {

    @Autowired
    private LiveKitProperties properties;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * 验证预约状态 - 只有 ACCEPTED 状态才能获取 LiveKit Token 进入咨询室
     */
    private ResponseEntity<Map<String, String>> validateAppointmentForConsultation(Long appointmentId, Long userId) {
        if (appointmentId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "预约ID不能为空"));
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "预约不存在"));
        }

        if (appointment.getStatus() != Appointment.AppointmentStatus.ACCEPTED) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "预约尚未被咨询师确认，请等待确认后再进入咨询室",
                           "status", appointment.getStatus().toString()));
        }

        // 验证用户是否有权限使用这个预约
        boolean isParent = appointment.getParentUserId().equals(userId);
        boolean isConsultant = appointment.getConsultantId().equals(userId);

        if (!isParent && !isConsultant) {
            return ResponseEntity.badRequest().body(Map.of("error", "您没有权限使用此预约"));
        }

        return null; // 验证通过
    }

    /**
     * 获取 LiveKit Room Token
     * room: 房间名（建议用 appointmentId）
     * identity: 参与者 ID（建议用 userId）
     * appointmentId: 预约ID（用于验证预约状态）
     * userId: 用户ID（用于验证权限）
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getToken(
            @RequestParam String room,
            @RequestParam String identity,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(required = false) Long userId) {

        // 验证预约状态 - 只有 ACCEPTED 才能获取 Token 进入咨询室
        if (appointmentId != null && userId != null) {
            ResponseEntity<Map<String, String>> validation = validateAppointmentForConsultation(appointmentId, userId);
            if (validation != null) {
                return validation;
            }
        }

        if (properties.getUrl() == null || properties.getUrl().contains("your-livekit")) {
            return ResponseEntity.badRequest().body(Map.of("error", "请在 application.properties 中配置 LiveKit 服务器地址"));
        }

        AccessToken token = new AccessToken(properties.getApiKey(), properties.getApiSecret());
        token.setName(identity);
        token.setIdentity(identity);

        token.addGrants(
                new RoomJoin(true),
                new RoomName(room),
                new CanPublish(true),
                new CanSubscribe(true)
        );

        Map<String, String> response = new HashMap<>();
        response.put("token", token.toJwt());
        response.put("serverUrl", properties.getUrl());

        return ResponseEntity.ok(response);
    }

    /**
     * 获取房间地址（供客户端直接使用）
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("serverUrl", properties.getUrl());
        response.put("apiKey", properties.getApiKey());
        response.put("configured", properties.getUrl() != null && !properties.getUrl().contains("your-livekit"));
        return ResponseEntity.ok(response);
    }
}
