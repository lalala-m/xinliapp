package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.config.OpenIMProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;

import java.util.Map;

@Service
public class OpenIMService {

    private static final Logger log = LoggerFactory.getLogger(OpenIMService.class);

    private final OpenIMProperties props;
    private final RestTemplate restTemplate;

    public OpenIMService(OpenIMProperties props) {
        this.props = props;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 获取管理员 Token（用于调用 OpenIM 管理接口）
     */
    private String getAdminToken() {
        try {
            String url = props.getApiUrl() + "/auth/user_token";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("secret", props.getSecret());
            body.add("userID", props.getAdminUserId());
            body.add("platform", "1");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, request,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                if ("0".equals(String.valueOf(resp.get("errCode")))) {
                    Object dataObj = resp.get("data");
                    if (dataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) dataObj;
                        return (String) data.get("token");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取 OpenIM 管理员 Token 失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 注册 OpenIM 用户（幂等，已存在则跳过）
     * API: POST {apiUrl}/user/user_register
     */
    public void registerUser(String userId, String nickname, String avatar) {
        try {
            String adminToken = getAdminToken();
            if (adminToken == null) {
                log.warn("无法获取 OpenIM 管理员 Token，跳过注册用户: {}", userId);
                return;
            }

            String url = props.getApiUrl() + "/user/user_register";

            Map<String, Object> userListItem = new java.util.HashMap<>();
            userListItem.put("userID", userId);
            userListItem.put("nickname", nickname);
            userListItem.put("faceURL", avatar);

            Map<String, Object> body = new java.util.HashMap<>();
            body.put("secret", props.getSecret());
            Object[] users = new Object[]{userListItem};
            body.put("users", users);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("token", adminToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, request,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                String errCode = String.valueOf(resp.get("errCode"));
                if ("0".equals(errCode)) {
                    log.info("OpenIM 用户注册成功: {}", userId);
                } else if ("10001".equals(errCode)) {
                    log.debug("OpenIM 用户已存在，跳过: {}", userId);
                } else {
                    log.warn("OpenIM 注册用户失败 [{}]: errCode={}", userId, errCode);
                }
            }
        } catch (Exception e) {
            log.warn("注册 OpenIM 用户失败（不影响业务）[{}]: {}", userId, e.getMessage());
        }
    }

    /**
     * 获取用户 Token（用于客户端连接 OpenIM）
     * API: POST {apiUrl}/auth/user_token
     */
    public String getUserToken(String userId) {
        try {
            String adminToken = getAdminToken();
            if (adminToken == null) {
                log.warn("无法获取 OpenIM 管理员 Token，无法为用户 [{}] 生成 Token", userId);
                return null;
            }

            String url = props.getApiUrl() + "/auth/user_token";

            Map<String, Object> body = new java.util.HashMap<>();
            body.put("secret", props.getSecret());
            body.put("userID", userId);
            body.put("platform", "1");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("token", adminToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, request,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                if ("0".equals(String.valueOf(resp.get("errCode")))) {
                    Object dataObj = resp.get("data");
                    if (dataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) dataObj;
                        return (String) data.get("token");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取 OpenIM Token 失败 [{}]: {}", userId, e.getMessage());
        }
        return null;
    }

    /**
     * 判断 OpenIM 服务是否可用
     */
    public boolean isAvailable() {
        String adminToken = getAdminToken();
        return adminToken != null && !adminToken.isEmpty();
    }
}
