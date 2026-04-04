package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.config.OpenIMProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * OpenIM Server 通信服务
 *
 * 职责：
 * 1. 注册用户到 OpenIM Server（/user/user_register）
     * 2. 为用户生成 OpenIM Token（/auth/get_user_token）
 * 3. 判断 OpenIM Server 是否可用
 *
 * 实际信令通道走的是 Spring Boot 的 STOMP 端点（ws://host:8080/stomp），
 * 这个类只负责 OpenIM Server 的用户管理，不处理消息路由。
 */
@Service
public class OpenIMService {

    private static final Logger log = LoggerFactory.getLogger(OpenIMService.class);

    /** OpenIM API 请求超时时间（毫秒） */
    private static final int TIMEOUT_MS = 10000;

    /** OpenIM Server 的 admin secret（HS256 签名密钥，从配置读取） */
    private static final String ADMIN_SECRET = "openIM123";  // 后备值，优先使用 props.getSecret()
    private static final String ADMIN_USER_ID = "imAdmin";

    private final OpenIMProperties props;
    private final RestTemplate restTemplate;

    @Autowired
    public OpenIMService(OpenIMProperties props) {
        this.props = props;
        this.restTemplate = createRestTemplateWithTimeout(TIMEOUT_MS);
    }

    /** 获取当前配置的 secret，优先用 props，fallback 到常量 */
    private String getSecret() {
        String s = props.getSecret();
        return (s != null && !s.isEmpty() && !"openIMAdmin".equals(s)) ? s : ADMIN_SECRET;
    }

    private RestTemplate createRestTemplateWithTimeout(int timeoutMs) {
        RestTemplate template = new RestTemplate();
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(java.time.Duration.ofMillis(timeoutMs));
        template.setRequestFactory(factory);
        return template;
    }

    /**
     * 生成 OpenIM 标准的 operationID（用于 API 追踪）
     */
    private String newOperationId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取管理员 Token（内部方法，用于调用 OpenIM 管理接口）
     * API: POST {apiUrl}/auth/get_admin_token
     * OpenIM 要求：JSON body + operationID header
     */
    private String getAdminToken() {
        String apiUrl = props.getApiUrl();
        try {
            String url = apiUrl + "/auth/get_admin_token";

            Map<String, Object> body = new java.util.HashMap<>();
            body.put("secret", getSecret());
            body.put("userID", ADMIN_USER_ID);
            body.put("platformID", 1);  // OpenIM 新版用 platformID

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("operationID", newOperationId());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, request,
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                Integer errCode = (Integer) resp.get("errCode");
                if (errCode != null && errCode == 0) {
                    Object dataObj = resp.get("data");
                    if (dataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) dataObj;
                        String token = (String) data.get("token");
                        log.debug("OpenIM admin token 获取成功，剩余有效时间: {} 秒",
                                data.get("expireTimeSeconds"));
                        return token;
                    }
                } else {
                    log.warn("OpenIM auth 失败: errCode={}, errMsg={}",
                            errCode, resp.get("errMsg"));
                }
            }
        } catch (Exception e) {
            log.warn("获取 OpenIM 管理员 Token 失败（OpenIM Server 可能未启动）: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 注册 OpenIM 用户（幂等，已存在则跳过）
     * API: POST {apiUrl}/user/user_register
     * OpenIM 要求：JSON body + operationID header + admin token header
     */
    public void registerUser(String userId, String nickname, String avatar) {
        String adminToken = getAdminToken();
        if (adminToken == null) {
            log.warn("无法获取 OpenIM 管理员 Token，跳过注册用户: {}", userId);
            return;
        }

        try {
            String apiUrl = props.getApiUrl();
            String url = apiUrl + "/user/user_register";

            Map<String, Object> userItem = new java.util.HashMap<>();
            userItem.put("userID", userId);
            userItem.put("nickname", nickname != null ? nickname : "用户" + userId);
            userItem.put("faceURL", avatar != null ? avatar : "");

            Map<String, Object> body = new java.util.HashMap<>();
            body.put("secret", getSecret());
            body.put("users", new Object[]{userItem});

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("operationID", newOperationId());
            headers.set("token", adminToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, request,
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                Integer errCode = (Integer) resp.get("errCode");
                if (errCode != null && errCode == 0) {
                    log.info("OpenIM 用户注册成功: {}", userId);
                } else if (errCode != null && errCode == 10001) {
                    log.debug("OpenIM 用户已存在，跳过: {}", userId);
                } else {
                    log.warn("OpenIM 注册用户失败 [{}]: errCode={}, errMsg={}",
                            userId, errCode, resp.get("errMsg"));
                }
            }
        } catch (Exception e) {
            log.warn("注册 OpenIM 用户失败（不影响业务）[{}]: {}", userId, e.getMessage());
        }
    }

    /**
     * 获取用户 Token（用于客户端连接）
     * API: POST {apiUrl}/auth/get_user_token
     * OpenIM 要求：JSON body + operationID header + admin token header
     */
    public String getUserToken(String userId) {
        String adminToken = getAdminToken();
        if (adminToken == null) {
            log.warn("无法获取 OpenIM 管理员 Token，无法为用户 [{}] 生成 Token", userId);
            return null;
        }

        try {
            String apiUrl = props.getApiUrl();
            String url = apiUrl + "/auth/get_user_token";

            Map<String, Object> body = new java.util.HashMap<>();
            body.put("secret", getSecret());
            body.put("userID", userId);
            body.put("platformID", 1);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("operationID", newOperationId());
            headers.set("token", adminToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, request,
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                Integer errCode = (Integer) resp.get("errCode");
                if (errCode != null && errCode == 0) {
                    Object dataObj = resp.get("data");
                    if (dataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) dataObj;
                        return (String) data.get("token");
                    }
                } else {
                    log.warn("获取用户 Token 失败 [{}]: errCode={}, errMsg={}",
                            userId, errCode, resp.get("errMsg"));
                }
            }
        } catch (Exception e) {
            log.warn("获取 OpenIM Token 失败 [{}]: {}", userId, e.getMessage());
        }
        return null;
    }

    /**
     * 判断 OpenIM Server 是否可用
     * 通过尝试获取 admin token 来验证连通性
     */
    public boolean isAvailable() {
        String token = getAdminToken();
        return token != null && !token.isEmpty();
    }

    /**
     * 从 OpenIM JWT token 中解析出 userId
     * OpenIM token 使用 HS256 签名，密钥为 ADMIN_SECRET
     */
    public Long extractUserIdFromToken(String token) {
        try {
            byte[] key = getSecret().getBytes(StandardCharsets.UTF_8);
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(key))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object uid = claims.get("userId");
            if (uid == null) {
                uid = claims.getSubject();
            }

            if (uid instanceof Number) {
                return ((Number) uid).longValue();
            }
            if (uid instanceof String) {
                String uidStr = (String) uid;
                if (uidStr.matches("\\d+")) {
                    return Long.parseLong(uidStr);
                }
            }
            String sub = claims.getSubject();
            if (sub != null && sub.matches("\\d+")) {
                return Long.parseLong(sub);
            }
            log.warn("无法从 OpenIM token 中提取数值型 userId: subject={}, userId={}", sub, uid);
            return null;
        } catch (Exception e) {
            log.debug("无法解析 OpenIM token: {}", e.getMessage());
            return null;
        }
    }
}
