package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.Appointment;
import com.tongyangyuan.mentalhealth.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final com.tongyangyuan.mentalhealth.repository.ConsultantRepository consultantRepository;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    private final ChatMessageService chatMessageService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              com.tongyangyuan.mentalhealth.repository.ConsultantRepository consultantRepository,
                              org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
                              ChatMessageService chatMessageService) {
        this.appointmentRepository = appointmentRepository;
        this.consultantRepository = consultantRepository;
        this.redisTemplate = redisTemplate;
        this.chatMessageService = chatMessageService;
    }

    public List<Appointment> getAppointmentsByConsultantId(Long consultantId) {
        // 尝试修正 consultantId：如果传入的是 userId，则转换为 consultantId
        Long actualConsultantId = resolveConsultantId(consultantId);
        return appointmentRepository.findByConsultantIdOrderByCreatedAtDesc(actualConsultantId);
    }

    public List<Appointment> getAppointmentsByParentUserId(Long parentUserId) {
        log.info("查询用户预约列表: parentUserId={}", parentUserId);
        return appointmentRepository.findByParentUserIdOrderByCreatedAtDesc(parentUserId);
    }

    public List<Appointment> getAppointmentsByConsultantIdAndStatus(Long consultantId, Appointment.AppointmentStatus status) {
        Long actualConsultantId = resolveConsultantId(consultantId);
        return appointmentRepository.findByConsultantIdAndStatusOrderByCreatedAtDesc(actualConsultantId, status);
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
    }

    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        Long consultantId = resolveConsultantId(appointment.getConsultantId());
        log.info("尝试创建预约: consultantId={}, date={}, slot={}", consultantId, appointment.getAppointmentDate(), appointment.getTimeSlot());
        
        // 1. Redis 分布式锁，防止超卖 (锁粒度：咨询师+日期+时间段)
        String lockKey = "lock:appointment:" + consultantId + ":" + appointment.getAppointmentDate() + ":" + appointment.getTimeSlot();
        // 尝试获取锁，10秒过期
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, java.util.concurrent.TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("获取预约锁失败 (超卖拦截): {}", lockKey);
            throw new RuntimeException("该时段正在被其他人预约，请稍后重试");
        }

        try {
            // 2. 验证咨询师是否存在
            com.tongyangyuan.mentalhealth.entity.Consultant consultant = consultantRepository.findById(consultantId)
                    .orElseThrow(() -> new RuntimeException("咨询师不存在"));
            
            appointment.setConsultantId(consultantId);
            appointment.setStatus(Appointment.AppointmentStatus.PENDING);

            // 3. 验证咨询师是否有权限指导该领域
            if (consultant.getIdentityTier() == com.tongyangyuan.mentalhealth.entity.Consultant.IdentityTier.GOLD || 
                consultant.getIdentityTier() == com.tongyangyuan.mentalhealth.entity.Consultant.IdentityTier.SILVER) {
                
                String domain = appointment.getDomain();
                String specialty = consultant.getSpecialty();
                
                if (domain == null || domain.isEmpty()) {
                    if (specialty != null && !specialty.isEmpty()) {
                        throw new RuntimeException("请选择咨询领域");
                    }
                } else if (specialty != null && !specialty.isEmpty()) {
                    boolean matchFound = false;
                    String[] specialties = specialty.split("[,，]");
                    for (String s : specialties) {
                        if (domain.trim().equalsIgnoreCase(s.trim())) {
                            matchFound = true;
                            break;
                        }
                    }
                    
                    if (!matchFound) {
                        throw new RuntimeException("该咨询师暂不提供[" + domain + "]领域的指导服务");
                    }
                }
            }

            // 4. 双重检查：数据库验证时间段是否被占用
            boolean isOccupied = appointmentRepository.existsByConsultantIdAndAppointmentDateAndTimeSlotAndStatusNot(
                    consultantId, appointment.getAppointmentDate(), appointment.getTimeSlot(), Appointment.AppointmentStatus.CANCELLED);
            
            if (isOccupied) {
                log.warn("时段已被占用 (DB Check): {}", lockKey);
                throw new RuntimeException("该时段已被预约");
            }

            // 5. 生成预约编号并保存
            appointment.setAppointmentNo(generateAppointmentNo());
            Appointment saved = appointmentRepository.save(appointment);
            log.info("预约创建成功: id={}, no={}", saved.getId(), saved.getAppointmentNo());
            
            // 清除该用户的预约缓存，确保下次查询是新的
            String cacheKey = "parent_appointments::" + appointment.getParentUserId();
            redisTemplate.delete(cacheKey);
            log.info("清除用户预约缓存: {}", cacheKey);

            // 通过 WebSocket 通知咨询师有新预约
            chatMessageService.notifyConsultantNewAppointment(saved);
            log.info("已通知咨询师有新预约: consultantUserId={}", consultant.getUserId());

            return saved;
            
        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
            log.debug("释放预约锁: {}", lockKey);
        }
    }

    private String generateAppointmentNo() {
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + String.format("%04d", new java.util.Random().nextInt(10000));
    }

    private Long resolveConsultantId(Long id) {
        // 1. 检查是否为有效的 Consultant ID (PK)
        if (consultantRepository.existsById(id)) {
            return id;
        }
        // 2. 检查是否为 Consultant 的 User ID
        return consultantRepository.findByUserId(id)
                .map(com.tongyangyuan.mentalhealth.entity.Consultant::getId)
                .orElse(id); // 如果都找不到，保持原样（可能会报错，但交给数据库约束处理）
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "parent_appointments", key = "#result.parentUserId", condition = "#result != null")
    public Appointment updateAppointment(Long id, Appointment appointmentDetails) {
        Appointment appointment = getAppointmentById(id);

        if (appointmentDetails.getStatus() != null) {
            appointment.setStatus(appointmentDetails.getStatus());
        }
        if (appointmentDetails.getIsPinned() != null) {
            appointment.setIsPinned(appointmentDetails.getIsPinned());
        }
        if (appointmentDetails.getIsChatted() != null) {
            appointment.setIsChatted(appointmentDetails.getIsChatted());
        }

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }
}
