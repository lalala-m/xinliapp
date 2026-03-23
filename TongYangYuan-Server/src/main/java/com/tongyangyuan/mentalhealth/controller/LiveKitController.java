package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.config.LiveKitProperties;
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
@RequestMapping("/api/livekit")
public class LiveKitController {

    @Autowired
    private LiveKitProperties properties;

    /**
     * 获取 LiveKit Room Token
     * room: 房间名（建议用 appointmentId）
     * identity: 参与者 ID（建议用 userId）
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getToken(
            @RequestParam String room,
            @RequestParam String identity) {

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
