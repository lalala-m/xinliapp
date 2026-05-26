package com.tongyangyuan.mentalhealth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务
 * 用于发送验证码邮件
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送验证码邮件
     *
     * @param toEmail 收件人邮箱
     * @param code    验证码
     * @return 是否发送成功
     */
    public boolean sendVerificationCode(String toEmail, String code) {
        if (!isConfigured()) {
            log.warn("邮件服务未配置，无法发送邮件到: {}", toEmail);
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("童康源 - 验证码");
            helper.setText(buildEmailContent(code), true);

            mailSender.send(message);
            log.info("验证码邮件发送成功: email={}", toEmail);
            return true;

        } catch (MessagingException e) {
            log.error("验证码邮件发送失败: email={}, error={}", toEmail, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("验证码邮件发送异常: email={}", toEmail, e);
            return false;
        }
    }

    /**
     * 检查邮件服务是否已配置
     */
    public boolean isConfigured() {
        return fromEmail != null && !fromEmail.isEmpty()
                && mailHost != null && !mailHost.isEmpty();
    }

    /**
     * 构建邮件内容（HTML格式）
     */
    private String buildEmailContent(String code) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f5f7fa; margin: 0; padding: 20px; }\n" +
                "        .container { max-width: 480px; margin: 0 auto; background: white; border-radius: 16px; padding: 40px 32px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }\n" +
                "        .logo { text-align: center; margin-bottom: 28px; }\n" +
                "        .logo h1 { color: #6FA6F8; font-size: 24px; margin: 0; }\n" +
                "        .title { font-size: 20px; font-weight: 600; color: #2C3345; margin-bottom: 12px; text-align: center; }\n" +
                "        .desc { font-size: 14px; color: #627089; margin-bottom: 28px; text-align: center; line-height: 1.6; }\n" +
                "        .code-box { background: linear-gradient(135deg, #EEF3FE 0%, #F4F7FF 100%); border-radius: 12px; padding: 24px; text-align: center; margin-bottom: 24px; }\n" +
                "        .code { font-size: 36px; font-weight: 700; color: #6FA6F8; letter-spacing: 8px; font-family: 'Courier New', monospace; }\n" +
                "        .hint { font-size: 13px; color: #9BA5BF; text-align: center; margin-bottom: 20px; }\n" +
                "        .footer { border-top: 1px solid #E8ECF2; padding-top: 20px; text-align: center; }\n" +
                "        .footer p { font-size: 12px; color: #9BA5BF; margin: 4px 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"logo\">\n" +
                "            <h1>童康源</h1>\n" +
                "        </div>\n" +
                "        <div class=\"title\">验证码</div>\n" +
                "        <div class=\"desc\">您正在进行登录/注册操作，请输入以下验证码完成验证。</div>\n" +
                "        <div class=\"code-box\">\n" +
                "            <div class=\"code\">" + code + "</div>\n" +
                "        </div>\n" +
                "        <div class=\"hint\">验证码5分钟内有效，请勿泄露给他人。</div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>如非本人操作，请忽略此邮件。</p>\n" +
                "            <p>童康源家庭教育</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
