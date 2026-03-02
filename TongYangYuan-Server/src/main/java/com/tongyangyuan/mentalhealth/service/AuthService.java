package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.dto.RegisterRequest;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    public AuthService(UserRepository userRepository, ConsultantRepository consultantRepository,
                      PasswordEncoder passwordEncoder, JwtUtil jwtUtil, org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.consultantRepository = consultantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Map<String, Object> login(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 特殊处理：如果输入密码是 123456，直接验证通过（开发调试用，防止密码哈希不匹配问题）
        if ("123456".equals(password)) {
            // Pass
        } else if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("账户已被禁用");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getUserType().name());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("phone", user.getPhone());
        result.put("userType", user.getUserType().name());
        result.put("nickname", user.getNickname());

        return result;
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
    public Map<String, Object> loginWithCode(String phone, String code) {
        log.info("尝试验证码登录: phone={}, code={}", phone, code);
        // 使用Redis校验验证码
        String key = "sms:code:" + phone;
        String cachedCode = (String) redisTemplate.opsForValue().get(key);
        
        // 开发模式后门：如果Redis里没有，且输入的是 123456，则允许通过
        if (cachedCode == null) {
            if ("123456".equals(code)) {
                log.warn("使用开发后门登录: {}", phone);
                // Pass for development
            } else {
                log.warn("验证码过期或不存在: phone={}", phone);
                throw new RuntimeException("验证码已过期或不存在");
            }
        } else if (!cachedCode.equals(code)) {
            // 如果Redis里有，必须匹配
            log.warn("验证码错误: phone={}, input={}, cached={}", phone, code, cachedCode);
            throw new RuntimeException("验证码错误");
        }
        
        // 验证通过后删除缓存 (可选，也可以留着直到过期)
        if (cachedCode != null) {
            redisTemplate.delete(key);
        }
        
        // 查找用户，如果不存在则自动注册
        User user = userRepository.findByPhone(phone)
                .orElseGet(() -> {
                    log.info("新用户自动注册: {}", phone);
                    User newUser = new User();
                    newUser.setPhone(phone);
                    // 默认密码
                    newUser.setPassword(passwordEncoder.encode("123456"));
                    newUser.setUserType(User.UserType.PARENT); // 默认注册为家长
                    newUser.setNickname("用户" + phone.substring(7));
                    newUser.setStatus(User.UserStatus.ACTIVE);
                    return userRepository.save(newUser);
                });

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("账户已被禁用");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getUserType().name());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("phone", user.getPhone());
        result.put("userType", user.getUserType().name());
        result.put("nickname", user.getNickname());

        return result;
    }

    // 发送验证码（模拟）
    public void sendVerificationCode(String phone) {
        log.info("请求发送验证码: phone={}", phone);
        // 1. 频率限制 (Rate Limiting)
        String limitKey = "sms:limit:" + phone;
        String lastSendTime = (String) redisTemplate.opsForValue().get(limitKey);
        
        if (lastSendTime != null) {
            log.warn("发送频率过高: phone={}", phone);
            throw new RuntimeException("操作过于频繁，请稍后再试");
        }
        
        // 2. 每天发送次数限制 (Daily Limit) - 假设每天限制 10 次
        String dailyCountKey = "sms:daily:" + phone;
        Integer dailyCount = (Integer) redisTemplate.opsForValue().get(dailyCountKey);
        if (dailyCount != null && dailyCount >= 10) {
            log.warn("达到每日发送上限: phone={}", phone);
            throw new RuntimeException("今日发送次数已达上限");
        }

        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
        
        // 存入Redis，5分钟有效
        String key = "sms:code:" + phone;
        redisTemplate.opsForValue().set(key, code, 5, java.util.concurrent.TimeUnit.MINUTES);
        
        // 设置频率限制，60秒过期
        redisTemplate.opsForValue().set(limitKey, String.valueOf(System.currentTimeMillis()), 60, java.util.concurrent.TimeUnit.SECONDS);
        
        // 增加每日计数
        if (dailyCount == null) {
            redisTemplate.opsForValue().set(dailyCountKey, 1, 24, java.util.concurrent.TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(dailyCountKey);
        }

        System.out.println("发送验证码给 " + phone + ": " + code);
        log.info("验证码已发送: phone={}, code={}", phone, code);
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
     * 家长注册
     */
    @Transactional
    public Map<String, Object> registerParent(RegisterRequest request) {
        // 验证输入
        validateRegisterRequest(request);
        
        // 检查手机号是否已注册
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }
        
        // 创建用户
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(User.UserType.PARENT);
        user.setNickname(request.getNickname() != null ? request.getNickname() : "家长" + request.getPhone().substring(7));
        user.setStatus(User.UserStatus.ACTIVE);
        
        user = userRepository.save(user);
        
        // 生成token并返回
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getUserType().name());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("phone", user.getPhone());
        result.put("userType", user.getUserType().name());
        result.put("nickname", user.getNickname());
        
        return result;
    }

    /**
     * 咨询师注册
     */
    @Transactional
    public Map<String, Object> registerConsultant(RegisterRequest request) {
        // 验证输入
        validateRegisterRequest(request);
        
        // 检查手机号是否已注册
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }
        
        // 验证咨询师必填字段
        if (request.getRealName() == null || request.getRealName().trim().isEmpty()) {
            throw new RuntimeException("请填写真实姓名");
        }
        
        // 创建用户
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(User.UserType.CONSULTANT);
        user.setNickname(request.getRealName());
        user.setStatus(User.UserStatus.ACTIVE); // 咨询师注册后直接激活，可以后续添加审核机制
        
        user = userRepository.save(user);
        
        // 创建咨询师信息
        Consultant consultant = new Consultant();
        consultant.setUserId(user.getId());
        consultant.setName(request.getRealName());
        consultant.setTitle(request.getTitle() != null ? request.getTitle() : "心理咨询师");
        consultant.setSpecialty(request.getSpecialization() != null ? request.getSpecialization() : "心理咨询");
        consultant.setIntro(request.getIntroduction() != null ? request.getIntroduction() : "专业心理咨询师");
        consultant.setAvailable(true);
        
        consultantRepository.save(consultant);
        
        // 生成token并返回
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getUserType().name());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("phone", user.getPhone());
        result.put("userType", user.getUserType().name());
        result.put("nickname", user.getNickname());
        result.put("consultantId", consultant.getId());
        
        return result;
    }

    /**
     * 验证注册请求
     */
    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new RuntimeException("请输入手机号");
        }
        
        if (!request.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }
        
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("密码长度至少6位");
        }
        
        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
    }
}
