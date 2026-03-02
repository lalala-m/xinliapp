package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.config.LiveKitProperties;
import io.livekit.server.AccessToken;
import io.livekit.server.VideoGrant;
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

    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getToken(
            @RequestParam String room,
            @RequestParam String identity) {

        AccessToken token = new AccessToken(properties.getApiKey(), properties.getApiSecret());
        token.setName(identity);
        token.setIdentity(identity);
        
        VideoGrant grant = new VideoGrant();
        grant.setRoomJoin(true);
        grant.setRoom(room);
        
        token.addGrant(grant);

        Map<String, String> response = new HashMap<>();
        response.put("token", token.toJwt());
        response.put("serverUrl", properties.getUrl());
        
        return ResponseEntity.ok(response);
    }
}
