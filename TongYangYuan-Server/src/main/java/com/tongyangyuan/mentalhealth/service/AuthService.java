package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.dto.RegisterRequest;
import com.tongyangyuan.mentalhealth.dto.WeChatLoginRequest;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
import com.tongyangyuan.mentalhealth.repository.LifeStageRepository;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final ConsultantRepository consultantRepository;
    private final com.tongyangyuan.mentalhealth.repository.ConsultantSpecialtyRepository consultantSpecialtyRepository;
    private final LifeStageRepository lifeStageRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;
    private final OpenIMService openIMService;
    private final SmsService smsService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, ConsultantRepository consultantRepository,
                      com.tongyangyuan.mentalhealth.repository.ConsultantSpecialtyRepository consultantSpecialtyRepository,
                      LifeStageRepository lifeStageRepository,
                      PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                      org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate,
                      OpenIMService openIMService, SmsService smsService, EmailService emailService) {
        this.userRepository = userRepository;
        this.consultantRepository = consultantRepository;
        this.consultantSpecialtyRepository = consultantSpecialtyRepository;
        this.lifeStageRepository = lifeStageRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.openIMService = openIMService;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    /**
     * 在 OpenIM 中注册用户（统一方法，幂等）
     */
    private void registerInOpenIM(User user) {
        try {
            String userId = String.valueOf(user.getId());
            String nickname = user.getNickname() != null ? user.getNickname()
                    : (user.getWxNickname() != null ? user.getWxNickname() : "用户" + userId);
            String avatar = user.getAvatarUrl() != null ? user.getAvatarUrl()
                    : (user.getWxAvatarUrl() != null ? user.getWxAvatarUrl() : "");
            openIMService.registerUser(userId, nickname, avatar);
            log.info("用户在 OpenIM 中注册成功: userId={}", userId);
        } catch (Exception e) {
            log.warn("用户在 OpenIM 中注册失败（不影响登录）: userId={}, error={}", user.getId(), e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> login(String account, String password) {
        // 支持手机号、邮箱或用户名登录
        log.info("登录请求: account={}", account);
        try {
            User user;
            if (isEmail(account)) {
                // 邮箱登录
                user = userRepository.findByEmail(account)
                        .orElseThrow(() -> new RuntimeException("用户不存在"));
            } else {
                // 先按手机号查询
                user = userRepository.findByPhone(account)
                        .orElse(null);
                log.info("按手机号查询结果: account={}, user={}", account, user != null ? "找到" : "未找到");
                // 如果找不到，再按用户名（nickname）查询
                if (user == null) {
                    user = userRepository.findByNickname(account)
                            .orElseThrow(() -> new RuntimeException("用户不存在"));
                }
            }

            log.info("找到用户: id={}, phone={}, status={}, type={}", 
                    user.getId(), user.getPhone(), user.getStatus(), user.getUserType());

            log.info("开始验证密码...");
            boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
            log.info("密码验证结果: {}", passwordMatch);
            if (!passwordMatch) {
                throw new RuntimeException("密码错误");
            }

            log.info("检查用户状态...");
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                // 区分审核中和已禁用状态
                if (user.getStatus() == User.UserStatus.INACTIVE && user.getUserType() == User.UserType.CONSULTANT) {
                    throw new RuntimeException("账号正在审核中，请等待管理员审核通过后再登录");
                }
                throw new RuntimeException("账户已被禁用");
            }

            log.info("更新最后登录时间...");
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("注册到OpenIM...");
            registerInOpenIM(user);

            log.info("生成token...");
            String token = jwtUtil.generateToken(user.getId(), 
                    user.getPhone() != null ? user.getPhone() : user.getEmail(), 
                    user.getUserType().name());

            log.info("登录成功: userId={}", user.getId());
            return buildLoginResult(user, token);
        } catch (RuntimeException e) {
            log.error("登录异常: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("登录发生未知异常", e);
            throw new RuntimeException("登录失败: " + e.getMessage(), e);
        }
    }

    @Transactional
    public User register(String phone, String password, User.UserType userType, String nickname) {
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("手机号已被注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setUserType(userType);
        user.setNickname(nickname);
        user.setStatus(User.UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    @Transactional
    public Map<String, Object> loginWithCode(String target, String code) {
        log.info("尝试验证码登录: target={}, code={}", target, code);
        boolean isEmail = isEmail(target);
        String key = isEmail ? "email:code:" + target : "sms:code:" + target;
        String cachedCode = (String) redisTemplate.opsForValue().get(key);

        if (cachedCode == null) {
            log.warn("验证码过期或不存在: target={}", target);
            throw new RuntimeException("验证码已过期或不存在");
        } else if (!cachedCode.equals(code)) {
            log.warn("验证码错误: target={}, input={}, cached={}", target, code, cachedCode);
            throw new RuntimeException("验证码错误");
        }

        // 删除验证码，防止重复使用
        redisTemplate.delete(key);

        User user;
        if (isEmail) {
            // 邮箱登录
            user = userRepository.findByEmail(target)
                    .orElseGet(() -> {
                        log.info("新用户自动注册（邮箱）: {}", target);
                        User newUser = new User();
                        newUser.setEmail(target);
                        newUser.setPhone(generateTempPhone()); // 生成临时手机号
                        newUser.setPassword(passwordEncoder.encode("123456"));
                        newUser.setUserType(User.UserType.PARENT);
                        newUser.setNickname("用户" + target.substring(0, target.indexOf('@')));
                        newUser.setStatus(User.UserStatus.ACTIVE);
                        newUser = userRepository.save(newUser);
                        registerInOpenIM(newUser);
                        return newUser;
                    });
        } else {
            // 手机号登录
            user = userRepository.findByPhone(target)
                    .orElseGet(() -> {
                        log.info("新用户自动注册（手机号）: {}", target);
                        User newUser = new User();
                        newUser.setPhone(target);
                        newUser.setPassword(passwordEncoder.encode("123456"));
                        newUser.setUserType(User.UserType.PARENT);
                        newUser.setNickname("用户" + target.substring(7));
                        newUser.setStatus(User.UserStatus.ACTIVE);
                        newUser = userRepository.save(newUser);
                        registerInOpenIM(newUser);
                        return newUser;
                    });
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            if (user.getStatus() == User.UserStatus.INACTIVE && user.getUserType() == User.UserType.CONSULTANT) {
                throw new RuntimeException("账号正在审核中，请等待管理员审核通过后再登录");
            }
            throw new RuntimeException("账户已被禁用");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), 
                user.getPhone() != null ? user.getPhone() : user.getEmail(), 
                user.getUserType().name());

        Map<String, Object> result = buildLoginResult(user, token);
        result.put("isNewUser", user.getGmtCreate() != null
                && java.time.Duration.between(user.getGmtCreate(), LocalDateTime.now()).getSeconds() < 10);
        return result;
    }

    /**
     * 生成临时手机号（用于邮箱注册用户）
     */
    private String generateTempPhone() {
        return "199" + String.format("%08d", new java.util.Random().nextInt(100000000));
    }

    @Transactional
    public Map<String, Object> loginWithWechat(WeChatLoginRequest request) {
        String openId = request.getOpenId();
        if (openId == null || openId.trim().isEmpty()) {
            throw new RuntimeException("微信授权失败，请重试");
        }
        log.info("微信登录尝试: openId={}", openId);

        User user = userRepository.findByWxOpenId(openId)
                .orElseGet(() -> {
                    log.info("微信新用户自动注册: openId={}", openId);
                    User newUser = new User();
                    newUser.setWxOpenId(openId);
                    if (request.getUnionId() != null) {
                        newUser.setWxUnionId(request.getUnionId());
                    }
                    newUser.setWxNickname(request.getNickname() != null ? request.getNickname() : "微信用户");
                    newUser.setWxAvatarUrl(request.getAvatarUrl());
                    newUser.setUserType(User.UserType.PARENT);
                    newUser.setNickname(request.getNickname() != null ? request.getNickname() : "微信用户");
                    newUser.setAvatarUrl(request.getAvatarUrl());
                    newUser.setPassword(passwordEncoder.encode("123456"));
                    newUser.setStatus(User.UserStatus.ACTIVE);
                    newUser = userRepository.save(newUser);
                    registerInOpenIM(newUser);
                    return newUser;
                });

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            if (user.getStatus() == User.UserStatus.INACTIVE && user.getUserType() == User.UserType.CONSULTANT) {
                throw new RuntimeException("账号正在审核中，请等待管理员审核通过后再登录");
            }
            throw new RuntimeException("账户已被禁用");
        }

        user.setLastLoginAt(LocalDateTime.now());
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setWxNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            user.setWxAvatarUrl(request.getAvatarUrl());
            user.setAvatarUrl(request.getAvatarUrl());
        }
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getUserType().name());
        return buildLoginResult(user, token);
    }

    private Map<String, Object> buildLoginResult(User user, String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("phone", user.getPhone() != null ? user.getPhone() : "");
        result.put("userType", user.getUserType().name());
        result.put("nickname", user.getNickname() != null ? user.getNickname() : "");
        result.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        result.put("wxOpenId", user.getWxOpenId() != null ? user.getWxOpenId() : "");
        result.put("currentChildId", user.getCurrentChildId());
        return result;
    }

    /**
     * 判断是否为邮箱地址
     */
    private boolean isEmail(String str) {
        return str != null && str.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * 发送验证码（支持手机号和邮箱）
     * @param target 手机号或邮箱
     */
    public void sendVerificationCode(String target) {
        log.info("请求发送验证码: target={}", target);
        boolean isEmail = isEmail(target);
        
        // 1. 频率限制 (Rate Limiting)
        String limitKey = isEmail ? "email:limit:" + target : "sms:limit:" + target;
        String lastSendTime = (String) redisTemplate.opsForValue().get(limitKey);
        
        if (lastSendTime != null) {
            log.warn("发送频率过高: target={}", target);
            throw new RuntimeException("操作过于频繁，请稍后再试");
        }
        
        // 2. 每天发送次数限制 (Daily Limit) - 假设每天限制 10 次
        String dailyCountKey = isEmail ? "email:daily:" + target : "sms:daily:" + target;
        Integer dailyCount = (Integer) redisTemplate.opsForValue().get(dailyCountKey);
        if (dailyCount != null && dailyCount >= 10) {
            log.warn("达到每日发送上限: target={}", target);
            throw new RuntimeException("今日发送次数已达上限");
        }

        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
        
        // 存入Redis，5分钟有效
        String key = isEmail ? "email:code:" + target : "sms:code:" + target;
        redisTemplate.opsForValue().set(key, code, 5, java.util.concurrent.TimeUnit.MINUTES);
        
        // 设置频率限制，60秒过期
        redisTemplate.opsForValue().set(limitKey, String.valueOf(System.currentTimeMillis()), 60, java.util.concurrent.TimeUnit.SECONDS);
        
        // 增加每日计数
        if (dailyCount == null) {
            redisTemplate.opsForValue().set(dailyCountKey, 1, 24, java.util.concurrent.TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(dailyCountKey);
        }

        if (isEmail) {
            // 发送邮箱验证码
            boolean emailSent = emailService.sendVerificationCode(target, code);
            if (!emailSent) {
                log.warn("邮件发送失败（可能未配置邮件服务）: email={}", target);
            }
            log.info("邮箱验证码已发送: email={}, code={}", target, code);
        } else {
            // 发送短信验证码
            boolean smsSent = smsService.sendVerificationCode(target, code);
            if (!smsSent) {
                log.warn("短信发送失败（可能处于测试模式或未配置腾讯云密钥）: phone={}", target);
            }
            log.info("短信验证码已发送: phone={}, code={}", target, code);
        }
    }

    @Transactional
    public void resetAllTestPasswords() {
        String[] testPhones = {
            "13800000001", "13800000002", "13800000003", "13800000004", "13800000005",
            "13800000006", "13800000007", "13800000008", "13800000009", "13800000010",
            "13900000001", "13900000002"
        };
        
        String encodedPassword = passwordEncoder.encode("123456");
        
        for (String phone : testPhones) {
            userRepository.findByPhone(phone).ifPresent(user -> {
                user.setPassword(encodedPassword);
                userRepository.save(user);
            });
        }
    }

    /**
     * 家长注册（支持手机号和邮箱）
     */
    @Transactional
    public Map<String, Object> registerParent(RegisterRequest request) {
        // 判断是手机号还是邮箱注册
        boolean isEmail = request.getEmail() != null && !request.getEmail().isEmpty();
        String account = isEmail ? request.getEmail() : request.getPhone();
        
        // 验证输入（含验证码）
        validateRegisterRequest(request);
        validateVerificationCode(account, request.getVerificationCode());
        
        if (isEmail) {
            // 邮箱注册
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已被注册");
            }
        } else {
            // 手机号注册
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("手机号已被注册");
            }
        }
        
        // 创建用户
        User user = new User();
        if (isEmail) {
            user.setEmail(request.getEmail());
            user.setPhone(generateTempPhone()); // 生成临时手机号
            user.setNickname(request.getNickname() != null ? request.getNickname() : "家长" + request.getEmail().substring(0, 3));
        } else {
            user.setPhone(request.getPhone());
            user.setNickname(request.getNickname() != null ? request.getNickname() : "家长" + request.getPhone().substring(7));
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(User.UserType.PARENT);
        user.setStatus(User.UserStatus.ACTIVE);
        
        user = userRepository.save(user);
        registerInOpenIM(user);
        
        // 生成token并返回
        String token = jwtUtil.generateToken(user.getId(), 
                user.getPhone() != null ? user.getPhone() : user.getEmail(), 
                user.getUserType().name());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("phone", user.getPhone() != null ? user.getPhone() : "");
        result.put("email", user.getEmail() != null ? user.getEmail() : "");
        result.put("userType", user.getUserType().name());
        result.put("nickname", user.getNickname());
        
        return result;
    }

    /**
     * 咨询师注册（支持手机号和邮箱）
     */
    @Transactional
    public Map<String, Object> registerConsultant(RegisterRequest request) {
        // 验证人生阶段ID是否存在
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long stageId : request.getTagIds()) {
                if (!lifeStageRepository.existsById(stageId)) {
                    throw new RuntimeException("无效的人生阶段ID: " + stageId);
                }
            }
        }
        // 判断是邮箱注册还是手机号注册
        boolean isEmail = request.getEmail() != null && !request.getEmail().isEmpty();
        
        // 验证输入
        validateRegisterRequest(request);
        
        if (isEmail) {
            // 邮箱注册验证
            validateVerificationCode(request.getEmail(), request.getVerificationCode());
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已被注册");
            }
        } else {
            // 手机号注册验证
            validateVerificationCode(request.getPhone(), request.getVerificationCode());
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("手机号已被注册");
            }
        }
        
        // 验证咨询师必填字段
        if (request.getRealName() == null || request.getRealName().trim().isEmpty()) {
            throw new RuntimeException("请填写真实姓名");
        }
        
        // 验证真实手机号（用于联系）
        String contactPhone = request.getPhone();
        if (contactPhone == null || !contactPhone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("请填写真实的11位联系电话");
        }
        
        // 创建用户（咨询师注册后状态为 INACTIVE，需管理员审核）
        User user = new User();
        if (isEmail) {
            user.setEmail(request.getEmail());
            user.setPhone(contactPhone); // 真实手机号用于联系
        } else {
            user.setPhone(request.getPhone());
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(User.UserType.CONSULTANT);
        user.setNickname(request.getRealName());
        user.setStatus(User.UserStatus.INACTIVE); // 咨询师注册后需审核
        
        user = userRepository.save(user);
        
        // 创建咨询师信息
        Consultant consultant = new Consultant();
        consultant.setUserId(user.getId());
        consultant.setName(request.getRealName());
        consultant.setTitle(request.getTitle() != null ? request.getTitle() : "心理咨询师");
        consultant.setSpecialty(request.getSpecialization() != null ? request.getSpecialization() : "心理咨询");
        consultant.setIntro(request.getIntroduction() != null ? request.getIntroduction() : "专业心理咨询师");
        consultant.setAvailable(false); // 审核通过前不可预约
        
        consultant = consultantRepository.save(consultant);
        
        // 保存咨询师标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                try {
                    com.tongyangyuan.mentalhealth.entity.ConsultantSpecialty cs = 
                        new com.tongyangyuan.mentalhealth.entity.ConsultantSpecialty();
                    cs.setConsultantId(consultant.getId());
                    cs.setTagId(tagId);
                    consultantSpecialtyRepository.save(cs);
                } catch (Exception e) {
                    // 忽略重复或无效标签
                }
            }
        }
        
        registerInOpenIM(user);
        
        // 生成token并返回
        String tokenKey = isEmail ? user.getEmail() : user.getPhone();
        String token = jwtUtil.generateToken(user.getId(), tokenKey, user.getUserType().name());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("userType", user.getUserType().name());
        result.put("nickname", user.getNickname());
        result.put("consultantId", consultant.getId());
        result.put("status", user.getStatus().name());
        
        return result;
    }

    /**
     * 验证注册请求（支持手机号和邮箱）
     */
    private void validateRegisterRequest(RegisterRequest request) {
        boolean isEmail = request.getEmail() != null && !request.getEmail().isEmpty();
        
        if (isEmail) {
            // 邮箱注册验证
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new RuntimeException("邮箱格式不正确");
            }
        } else {
            // 手机号注册验证
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                throw new RuntimeException("请输入手机号");
            }
            if (!request.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new RuntimeException("手机号格式不正确");
            }
        }
        
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("密码长度至少6位");
        }
        
        // 密码复杂度：至少包含字母和数字
        if (!request.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new RuntimeException("密码必须同时包含字母和数字");
        }
        
        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        if (request.getVerificationCode() == null || request.getVerificationCode().trim().isEmpty()) {
            throw new RuntimeException("请输入验证码");
        }
    }
    
    /**
     * 校验验证码（支持手机号和邮箱）
     */
    private void validateVerificationCode(String account, String code) {
        boolean isEmail = account != null && account.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        String key = isEmail ? "email:code:" + account : "sms:code:" + account;
        String cachedCode = (String) redisTemplate.opsForValue().get(key);
        
        if (cachedCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        
        if (!cachedCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        
        // 校验成功后删除验证码，防止重复使用
        redisTemplate.delete(key);
    }
}
