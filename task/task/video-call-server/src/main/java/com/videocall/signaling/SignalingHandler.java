package com.videocall.signaling;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

public class SignalingHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> sessions = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        broadcastUserCount();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 广播给其他所有连接
        for (WebSocketSession s : sessions) {
            if (s != session && s.isOpen()) {
                s.sendMessage(new TextMessage(message.getPayload()));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        broadcastUserCount();
    }

    private void broadcastUserCount() {
        try {
            String countMsg = "{\"type\":\"userCount\",\"count\":" + sessions.size() + "}";
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(countMsg));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
