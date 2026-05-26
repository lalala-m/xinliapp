package com.tongyangyuan.mentalhealth.service;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 腾讯云短信服务
 */
@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${tencent.sms.secret-id:}")
    private String secretId;

    @Value("${tencent.sms.secret-key:}")
    private String secretKey;

    @Value("${tencent.sms.app-id:}")
    private String appId;

    @Value("${tencent.sms.sign-name:}")
    private String signName;

    @Value("${tencent.sms.template-id:}")
    private String templateId;

    @Value("${app.test-mode:true}")
    private boolean testMode;

    private SmsClient client;

    /**
     * 初始化腾讯云短信客户端
     */
    private SmsClient getClient() {
        if (client == null) {
            if (!isConfigured()) {
                throw new RuntimeException("腾讯云短信配置未设置，请在 application.properties 中配置 tencent.sms.secret-id 和 tencent.sms.secret-key");
            }
            Credential cred = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            client = new SmsClient(cred, "ap-guangzhou", clientProfile);
        }
        return client;
    }

    /**
     * 发送验证码短信
     *
     * @param phone 手机号（如 13800138000）
     * @param code  验证码
     * @return 是否发送成功
     */
    public boolean sendVerificationCode(String phone, String code) {
        // 测试模式或未配置腾讯云密钥时，只打印日志不真正发送
        if (testMode || !isConfigured()) {
            log.info("【测试模式】验证码短信: phone={}, code={}", phone, code);
            return true;
        }

        try {
            SmsClient smsClient = getClient();

            SendSmsRequest req = new SendSmsRequest();
            // 手机号需要加 +86 前缀
            String[] phoneNumbers = {"+86" + phone};
            req.setPhoneNumberSet(phoneNumbers);
            req.setSmsSdkAppId(appId);
            req.setSignName(signName);
            req.setTemplateId(templateId);
            // 模板参数，对应模板中的 {1} 占位符
            String[] templateParams = {code};
            req.setTemplateParamSet(templateParams);

            SendSmsResponse response = smsClient.SendSms(req);

            if (response.getSendStatusSet() != null && response.getSendStatusSet().length > 0) {
                String sendStatus = response.getSendStatusSet()[0].getCode();
                String sendMessage = response.getSendStatusSet()[0].getMessage();
                if ("Ok".equals(sendStatus)) {
                    log.info("短信发送成功: phone={}, requestId={}", phone, response.getRequestId());
                    return true;
                } else {
                    log.error("短信发送失败: phone={}, code={}, message={}", phone, sendStatus, sendMessage);
                    return false;
                }
            } else {
                log.error("短信发送失败: phone={}, 无返回状态", phone);
                return false;
            }
        } catch (TencentCloudSDKException e) {
            log.error("短信发送异常: phone={}, error={}", phone, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("短信发送异常: phone={}", phone, e);
            return false;
        }
    }

    /**
     * 检查是否已配置腾讯云短信
     */
    public boolean isConfigured() {
        return secretId != null && !secretId.isEmpty()
                && !secretId.equals("你的SecretId")
                && secretKey != null && !secretKey.isEmpty()
                && !secretKey.equals("你的SecretKey")
                && appId != null && !appId.isEmpty()
                && !appId.equals("你的SmsSdkAppId")
                && signName != null && !signName.isEmpty()
                && !signName.equals("你的签名名称")
                && templateId != null && !templateId.isEmpty()
                && !templateId.equals("你的模板ID");
    }
}
