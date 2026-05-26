package com.tongyangyuan.mentalhealth.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 视频通话信令处理器
 * 基于原生WebSocket，简化信令流程
 * 支持房间模式：通过URL参数 roomId 区分不同通话房间
 *
 * 信令流程：
 * 1. call   - 发起通话请求
 * 2. offer  - 发送SDP offer
 * 3. answer - 发送SDP answer
 * 4. candidate - 交换ICE候选
 * 5. end    - 结束通话
 *
 * 🔧 关键设计：按 userId 去重，同一用户的重复连接会自动替换旧连接
 * 这解决了"同一用户从聊天页面跳转到视频通话页面时，旧连接未关闭导致房间人数异常"的问题
 */
public class VideoCallSignalingHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(VideoCallSignalingHandler.class);

    // roomId -> userId -> WebSocketSession 映射
    // 使用 userId 作为 key（而不是 sessionId），确保同一用户只有一个连接
    private final Map<String, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String roomId = getRoomId(session);
        String userId = getUserId(session);
        String sessionId = session.getId();

        Map<String, WebSocketSession> room = rooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());

        // 🔧 修复：同一用户新连接替换旧连接
        WebSocketSession oldSession = room.get(userId);
        if (oldSession != null && oldSession.isOpen() && !oldSession.getId().equals(sessionId)) {
            log.info("Video signaling: replacing old session for user {} in room {} (old={}, new={})",
                    userId, roomId, oldSession.getId(), sessionId);
            try {
                oldSession.close(CloseStatus.NORMAL.withReason("Replaced by new connection"));
            } catch (IOException e) {
                log.warn("Failed to close old session: {}", e.getMessage());
            }
        }

        // 清理已关闭的连接（防御性清理）
        room.entrySet().removeIf(entry -> !entry.getValue().isOpen());

        room.put(userId, session);

        int count = room.size();
        log.info("Video signaling: user {} (session {}) joined room {}, current users: {}",
                userId, sessionId, roomId, count);

        // 广播房间人数
        broadcastToRoom(roomId, createMessage("userCount", "count", count));

        // 当房间至少有2人时，通知所有人对方已加入
        if (count >= 2) {
            broadcastToRoom(roomId, "{\"type\":\"peerJoined\"}");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String roomId = getRoomId(session);
        String userId = getUserId(session);
        String payload = message.getPayload();

        // 忽略心跳消息
        if (payload.contains("\"type\":\"ping\"")) {
            return;
        }

        // 🔧 处理 cleanup 消息：客户端请求清理该用户的旧连接
        if (payload.contains("\"type\":\"cleanup\"")) {
            log.info("Video signaling: received cleanup from user {} in room {}", userId, roomId);
            Map<String, WebSocketSession> room = rooms.get(roomId);
            if (room != null) {
                WebSocketSession current = room.get(userId);
                if (current != null && current.isOpen() && !current.getId().equals(session.getId())) {
                    try {
                        current.close(CloseStatus.NORMAL.withReason("Cleaned up by new connection"));
                    } catch (IOException e) {
                        log.warn("Failed to close session during cleanup: {}", e.getMessage());
                    }
                    room.remove(userId);
                }
            }
            return; // 不转发 cleanup 消息
        }

        log.info("Video signaling: message from user {} (session {}) in room {}: {}",
                userId, session.getId(), roomId, payload);
        log.info("Video signaling: current rooms={}, room {} has {} users",
                rooms.size(), roomId,
                rooms.get(roomId) != null ? rooms.get(roomId).size() : 0);

        // 广播给房间内的其他用户
        Map<String, WebSocketSession> room = rooms.get(roomId);
        if (room != null) {
            int sentCount = 0;
            log.info("Video signaling: room {} users: {}", roomId, room.keySet());
            for (Map.Entry<String, WebSocketSession> entry : room.entrySet()) {
                String otherUserId = entry.getKey();
                WebSocketSession s = entry.getValue();
                log.info("Video signaling: checking user {} (session {}), isOpen={}, isSender={}",
                        otherUserId, s.getId(), s.isOpen(), s == session);
                if (s != session && s.isOpen()) {
                    try {
                        s.sendMessage(new TextMessage(payload));
                        sentCount++;
                        log.info("Video signaling: message forwarded to user {} (session {}) in room {}",
                                otherUserId, s.getId(), roomId);
                    } catch (IOException e) {
                        log.error("Failed to send message to user {} (session {})", otherUserId, s.getId(), e);
                    }
                }
            }
            log.info("Video signaling: message forwarded to {} peers in room {}", sentCount, roomId);

            // 如果房间人数异常（>2），打印警告
            if (room.size() > 2) {
                log.warn("Video signaling: room {} has {} users (expected max 2), users: {}",
                        roomId, room.size(), room.keySet());
            }
        } else {
            log.warn("Video signaling: room {} not found", roomId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomId = getRoomId(session);
        String userId = getUserId(session);
        String sessionId = session.getId();

        Map<String, WebSocketSession> room = rooms.get(roomId);
        if (room != null) {
            WebSocketSession current = room.get(userId);
            // 🔧 修复：只有当前存储的 session 匹配时才移除
            // 防止"旧连接关闭回调"覆盖"新连接"
            if (current != null && current.getId().equals(sessionId)) {
                room.remove(userId);
                log.info("Video signaling: user {} (session {}) left room {}", userId, sessionId, roomId);
            } else if (current != null) {
                log.info("Video signaling: ignoring close of old session {} for user {} (current session is {})",
                        sessionId, userId, current.getId());
            }

            // 同时清理其他已关闭的连接
            room.entrySet().removeIf(entry -> !entry.getValue().isOpen());

            int count = room.size();
            log.info("Video signaling: room {} current users: {}", roomId, count);

            // 广播用户离开消息，让客户端知道有人离开了
            try {
                broadcastToRoomSafe(roomId, "{\"type\":\"peerLeft\"}");
                // 延迟广播房间人数，确保 peerLeft 消息先到达
                Thread.sleep(100);
                broadcastToRoomSafe(roomId, createMessage("userCount", "count", count));
            } catch (Exception e) {
                log.warn("Failed to broadcast after connection closed: {}", e.getMessage());
            }

            // 如果房间空了，清理
            if (count == 0) {
                rooms.remove(roomId);
            }
        }
    }

    /**
     * 从URL参数获取 roomId
     */
    private String getRoomId(WebSocketSession session) {
        URI uri = session.getUri();
        String query = uri != null ? uri.getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length >= 2 && "roomId".equals(kv[0])) {
                    return kv[1];
                }
            }
        }
        return "default";
    }

    /**
     * 🔧 新增：从URL参数获取 userId
     * 客户端需要在 WebSocket URL 中添加 userId 参数，例如：
     * ws://host:8080/api/video-signaling?roomId=123&userId=456
     */
    private String getUserId(WebSocketSession session) {
        URI uri = session.getUri();
        String query = uri != null ? uri.getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length >= 2 && "userId".equals(kv[0])) {
                    return kv[1];
                }
            }
        }
        // fallback：使用 sessionId 作为 userId（向后兼容）
        return session.getId();
    }

    private void broadcastToRoom(String roomId, String message) {
        Map<String, WebSocketSession> room = rooms.get(roomId);
        if (room != null) {
            for (WebSocketSession s : room.values()) {
                if (s.isOpen()) {
                    try {
                        s.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        log.error("Failed to broadcast to session {}", s.getId(), e);
                    }
                }
            }
        }
    }

    private void broadcastToRoomSafe(String roomId, String message) {
        Map<String, WebSocketSession> room = rooms.get(roomId);
        if (room != null) {
            for (WebSocketSession s : room.values()) {
                if (s.isOpen()) {
                    try {
                        synchronized (s) {
                            if (s.isOpen()) {
                                s.sendMessage(new TextMessage(message));
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to broadcast to session {}: {}", s.getId(), e.getMessage());
                    }
                }
            }
        }
    }

    private String createMessage(String type, String dataKey, Object dataValue) {
        return String.format("{\"type\":\"%s\",\"%s\":%s}", type, dataKey,
                dataValue instanceof Number ? dataValue : "\"" + dataValue + "\"");
    }
}
