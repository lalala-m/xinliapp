package com.tongyangyuan.mentalhealth.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.tongyangyuan.mentalhealth.dto.UpdateUserProfileRequest;
import com.tongyangyuan.mentalhealth.entity.*;
import com.tongyangyuan.mentalhealth.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ConsultantRepository consultantRepository;
    private final AppointmentRepository appointmentRepository;
    private final AdminLogRepository adminLogRepository;
    private final ChildRepository childRepository;

    public AdminService(UserRepository userRepository, 
                       ConsultantRepository consultantRepository,
                       AppointmentRepository appointmentRepository,
                       AdminLogRepository adminLogRepository,
                       ChildRepository childRepository) {
        this.userRepository = userRepository;
        this.consultantRepository = consultantRepository;
        this.appointmentRepository = appointmentRepository;
        this.adminLogRepository = adminLogRepository;
        this.childRepository = childRepository;
    }

    // ==================== 用户管理 ====================
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllUsers(User.UserType userType) {
        if (userType != null) {
            return userRepository.findByUserType(userType);
        }
        return userRepository.findAll();
    }

    public List<Map<String, Object>> getParentsWithDetails() {
        List<User> parents = userRepository.findByUserType(User.UserType.PARENT);
        List<Map<String, Object>> result = new ArrayList<>();

        for (User parent : parents) {
            Map<String, Object> map = new HashMap<>();
            // 基本信息
            map.put("id", parent.getId());
            map.put("phone", parent.getPhone());
            map.put("nickname", parent.getNickname());
            map.put("avatarUrl", parent.getAvatarUrl());
            map.put("gmtCreate", parent.getGmtCreate()); // Using gmtCreate instead of createdAt based on User entity
            map.put("lastLoginAt", parent.getLastLoginAt());
            map.put("status", parent.getStatus());
            map.put("userType", parent.getUserType());

            // 孩子信息 (Name, Age/BirthDate, BodyStatus)
            List<Child> children = childRepository.findByParentUserId(parent.getId());
            List<Map<String, String>> childrenInfo = children.stream().map(c -> {
                Map<String, String> cMap = new HashMap<>();
                cMap.put("name", c.getName());
                cMap.put("gender", c.getGender());
                cMap.put("age", c.getBirthDate() != null ? String.valueOf(java.time.Period.between(c.getBirthDate(), LocalDate.now()).getYears()) : "未知");
                cMap.put("bodyStatus", c.getBodyStatus());
                return cMap;
            }).collect(Collectors.toList());
            map.put("children", childrenInfo);

            // 获取最近的预约记录
            List<Appointment> apps = appointmentRepository.findByParentUserIdOrderByCreatedAtDesc(parent.getId());

            // 重点问题 (Key Issues) - From Appointment description or Child body status
            String keyIssues = children.stream()
                    .map(c -> c.getName() + ": " + (c.getBodyStatus() != null ? c.getBodyStatus() : "无"))
                    .collect(Collectors.joining("; "));
            if (keyIssues.isEmpty()) {
                // Try to get from latest appointment
                if (!apps.isEmpty()) {
                    keyIssues = apps.get(0).getDescription();
                }
            }
            map.put("keyIssues", keyIssues);

            // 目前咨询的咨询师 (Current Consultant)
            String currentConsultantName = "无";
            if (!apps.isEmpty()) {
                Appointment latestApp = apps.get(0);
                Long consultantId = latestApp.getConsultantId();
                Consultant consultant = consultantRepository.findById(consultantId).orElse(null);
                if (consultant != null) {
                    currentConsultantName = consultant.getName();
                }
            }
            map.put("currentConsultant", currentConsultantName);

            // 当前咨询师 (Current Consultant) - From latest active appointment
            // Define active as PENDING or CONFIRMED or COMPLETED recently?
            // Let's just take the latest appointment's consultant
            if (!apps.isEmpty()) {
                Long consultantId = apps.get(0).getConsultantId();
                consultantRepository.findById(consultantId).ifPresent(c -> {
                    map.put("currentConsultant", c.getName());
                    map.put("currentConsultantId", c.getId());
                });
            } else {
                map.put("currentConsultant", "无");
            }

            result.add(map);
        }
        return result;
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("该手机号已注册");
        }
        User saved = userRepository.save(user);
        // 如果是咨询师账号，同时创建咨询师档案
        if (saved.getUserType() == User.UserType.CONSULTANT) {
            createConsultantForUser(saved);
        }
        return saved;
    }

    @Transactional
    public User updateUser(Long id, User user) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getNickname() != null) {
            existing.setNickname(user.getNickname());
        }
        if (user.getAvatarUrl() != null) {
            existing.setAvatarUrl(user.getAvatarUrl());
        }
        if (user.getWxAvatarUrl() != null) {
            existing.setWxAvatarUrl(user.getWxAvatarUrl());
        }
        
        User saved = userRepository.save(existing);

        // 同步昵称和头像到咨询师档案
        if (saved.getUserType() == User.UserType.CONSULTANT) {
            consultantRepository.findByUserId(saved.getId()).ifPresent(c -> {
                if (user.getNickname() != null) c.setName(user.getNickname());
                if (saved.getAvatarUrl() != null && !saved.getAvatarUrl().isEmpty()) {
                    c.setAvatarUrl(saved.getAvatarUrl());
                }
                consultantRepository.save(c);
            });
        }

        return saved;
    }

    @Transactional
    public User updateUserStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setStatus(User.UserStatus.valueOf(status.toUpperCase()));
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    /**
     * 更新用户资料（昵称和头像）
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户
     */
    @Transactional
    public User updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新昵称
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            if (request.getNickname().length() > 50) {
                throw new RuntimeException("昵称不能超过50个字符");
            }
            user.setNickname(request.getNickname().trim());
        }
        
        // 更新头像
        if (request.getAvatarUrl() != null) {
            // 空字符串或有效URL都可以
            if (request.getAvatarUrl().isEmpty() || isValidUrl(request.getAvatarUrl())) {
                user.setAvatarUrl(request.getAvatarUrl());
            } else {
                throw new RuntimeException("头像URL格式不正确");
            }
        }
        
        User saved = userRepository.save(user);
        
        // 如果是咨询师，同时同步到咨询师档案
        if (saved.getUserType() == User.UserType.CONSULTANT) {
            consultantRepository.findByUserId(saved.getId()).ifPresent(c -> {
                if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
                    c.setName(request.getNickname().trim());
                }
                if (saved.getAvatarUrl() != null && !saved.getAvatarUrl().isEmpty()) {
                    c.setAvatarUrl(saved.getAvatarUrl());
                }
                consultantRepository.save(c);
            });
        }
        
        return saved;
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) return true;
        return url.matches("^(https?://|/).*");
    }

    // ==================== 咨询师管理 ====================

    public List<Consultant> getAllConsultants() {
        List<Consultant> consultants = consultantRepository.findAll(Sort.by(Sort.Direction.DESC, "gmtCreate"));
        // 优先使用 Consultant 自己的头像，如果没有再从 User 填充
        for (Consultant consultant : consultants) {
            if (consultant.getAvatarUrl() == null || consultant.getAvatarUrl().isEmpty()) {
                userRepository.findById(consultant.getUserId()).ifPresent(user -> {
                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        consultant.setAvatarUrl(user.getAvatarUrl());
                    }
                });
            }
        }
        return consultants;
    }

    public Consultant getConsultantById(Long id) {
        Consultant consultant = consultantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("咨询师不存在"));
        // 优先使用 Consultant 自己的头像，如果没有再从 User 填充
        if (consultant.getAvatarUrl() == null || consultant.getAvatarUrl().isEmpty()) {
            userRepository.findById(consultant.getUserId()).ifPresent(user -> {
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    consultant.setAvatarUrl(user.getAvatarUrl());
                }
            });
        }
        return consultant;
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "consultants", allEntries = true)
    public Consultant createConsultant(com.tongyangyuan.mentalhealth.dto.CreateConsultantRequest request) {
        // 1. Check if user exists or create new user
        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setPhone(request.getPhone());
                    newUser.setNickname(request.getName());
                    newUser.setPassword("123456"); // Default password
                    newUser.setUserType(User.UserType.CONSULTANT);
                    newUser.setStatus(User.UserStatus.ACTIVE);
                    return userRepository.save(newUser);
                });

        // 2. Ensure user is CONSULTANT
        if (user.getUserType() != User.UserType.CONSULTANT) {
            user.setUserType(User.UserType.CONSULTANT);
            user = userRepository.save(user);
        }

        // 3. Create or Update Consultant
        User finalUser = user;
        Consultant consultant = consultantRepository.findByUserId(user.getId())
                .orElse(new Consultant());
        
        consultant.setUserId(finalUser.getId());
        consultant.setName(request.getName());
        consultant.setTitle(request.getTitle());
        consultant.setSpecialty(request.getSpecialty());
        consultant.setIntro(request.getIntro());
        consultant.setIdentityTier(request.getIdentityTier() != null ? request.getIdentityTier() : Consultant.IdentityTier.BRONZE);
        consultant.setAvailable(true);
        // 设置头像
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            consultant.setAvatarUrl(request.getAvatarUrl());
        }

        return consultantRepository.save(consultant);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "consultants", allEntries = true)
    public Consultant updateConsultant(Long id, Consultant consultant) {
        Consultant existing = getConsultantById(id);

        // 手动复制属性，确保 avatarUrl 能被正确更新
        if (consultant.getName() != null) {
            existing.setName(consultant.getName());
        }
        if (consultant.getTitle() != null) {
            existing.setTitle(consultant.getTitle());
        }
        if (consultant.getSpecialty() != null) {
            existing.setSpecialty(consultant.getSpecialty());
        }
        if (consultant.getIntro() != null) {
            existing.setIntro(consultant.getIntro());
        }
        if (consultant.getIdentityTier() != null) {
            existing.setIdentityTier(consultant.getIdentityTier());
        }
        if (consultant.getAvatarUrl() != null) {
            existing.setAvatarUrl(consultant.getAvatarUrl());
            // 同步头像到关联的 User 表
            userRepository.findById(existing.getUserId()).ifPresent(user -> {
                user.setAvatarUrl(consultant.getAvatarUrl());
                userRepository.save(user);
            });
        }
        if (consultant.getAvailable() != null) {
            existing.setAvailable(consultant.getAvailable());
        }

        return consultantRepository.save(existing);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "consultants", allEntries = true)
    public void deleteConsultant(Long id) {
        if (!consultantRepository.existsById(id)) {
            throw new RuntimeException("咨询师不存在");
        }
        consultantRepository.deleteById(id);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "consultants", allEntries = true)
    public Consultant verifyConsultant(Long id) {
        Consultant existing = getConsultantById(id);
        existing.setIsCertificateVerified(true);
        return consultantRepository.save(existing);
    }

    private void createConsultantForUser(User user) {
        consultantRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Consultant consultant = new Consultant();
                    consultant.setUserId(user.getId());
                    String name = user.getNickname() != null && !user.getNickname().isEmpty()
                            ? user.getNickname()
                            : user.getPhone();
                    consultant.setName(name);
                    consultant.setTitle("心理咨询师");
                    consultant.setSpecialty("儿童心理");
                    consultant.setIntro("专业心理咨询师");
                    consultant.setAvailable(true);
                    return consultantRepository.save(consultant);
                });
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "consultants", allEntries = true)
    public Consultant approveConsultant(Long id, String note) {
        Consultant consultant = consultantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("咨询师不存在"));
        
        consultant.setAvailable(true);
        return consultantRepository.save(consultant);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "consultants", allEntries = true)
    public Consultant rejectConsultant(Long id, String note) {
        Consultant consultant = consultantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("咨询师不存在"));
        
        consultant.setAvailable(false);
        return consultantRepository.save(consultant);
    }

    // ==================== 预约管理 ====================
    
    public List<Map<String, Object>> getAllAppointments() {
        List<Appointment> list = appointmentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Map<String, Object>> result = new ArrayList<>();

        for (Appointment app : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("appointmentNo", app.getAppointmentNo());
            map.put("parentUserId", app.getParentUserId());
            map.put("consultantId", app.getConsultantId());
            map.put("childId", app.getChildId());
            map.put("childName", app.getChildName());
            map.put("childAge", app.getChildAge());
            map.put("appointmentDate", app.getAppointmentDate());
            map.put("timeSlot", app.getTimeSlot());
            map.put("description", app.getDescription());
            map.put("domain", app.getDomain());
            map.put("status", app.getStatus());
            map.put("isPinned", app.getIsPinned());
            map.put("isChatted", app.getIsChatted());
            map.put("createdAt", app.getCreatedAt());
            map.put("updatedAt", app.getUpdatedAt());

            java.time.LocalDateTime appointmentTime = null;
            if (app.getAppointmentDate() != null && app.getTimeSlot() != null) {
                try {
                    String[] parts = app.getTimeSlot().split("-");
                    if (parts.length > 0) {
                        String start = parts[0].trim();
                        if (start.length() == 5) {
                            appointmentTime = java.time.LocalDateTime.parse(
                                    app.getAppointmentDate().toString() + "T" + start + ":00");
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            map.put("appointmentTime", appointmentTime);
            map.put("consultationType", app.getDomain());

            User parent = userRepository.findById(app.getParentUserId()).orElse(null);
            if (parent != null) {
                String parentName = parent.getNickname() != null && !parent.getNickname().isEmpty()
                        ? parent.getNickname()
                        : parent.getPhone();
                map.put("parentName", parentName);
                map.put("parentAvatarUrl", parent.getAvatarUrl());
                map.put("parentWxAvatarUrl", parent.getWxAvatarUrl());
            }

            Consultant consultant = consultantRepository.findById(app.getConsultantId()).orElse(null);
            if (consultant != null) {
                map.put("consultantName", consultant.getName());
                map.put("consultantAvatarUrl", consultant.getAvatarUrl());
            }

            result.add(map);
        }

        return result;
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "parent_appointments", key = "#result.parentUserId", condition = "#result != null")
    public Appointment confirmAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        appointment.setStatus(Appointment.AppointmentStatus.ACCEPTED);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "parent_appointments", key = "#result.parentUserId", condition = "#result != null")
    public Appointment cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        return appointmentRepository.save(appointment);
    }

    // ==================== 数据统计 ====================
    
    public Map<String, Object> getOverviewStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户统计
        long totalUsers = userRepository.count();
        long parentCount = userRepository.countByUserType(User.UserType.PARENT);
        long consultantCount = userRepository.countByUserType(User.UserType.CONSULTANT);
        
        // 预约统计
        long totalAppointments = appointmentRepository.count();
        long todayAppointments = appointmentRepository.countByAppointmentDate(LocalDate.now());
        
        stats.put("totalUsers", totalUsers);
        stats.put("totalConsultants", consultantCount); // Match frontend key
        stats.put("parentCount", parentCount);
        stats.put("consultantCount", consultantCount);
        stats.put("totalAppointments", totalAppointments);
        stats.put("todayAppointments", todayAppointments); // Match frontend key
        stats.put("monthlyRevenue", 0.0); // Placeholder
        
        return stats;
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(User.UserStatus.ACTIVE);
        long inactiveUsers = userRepository.countByStatus(User.UserStatus.INACTIVE);
        long bannedUsers = userRepository.countByStatus(User.UserStatus.BANNED);
        
        stats.put("total", totalUsers);
        stats.put("active", activeUsers);
        stats.put("inactive", inactiveUsers);
        stats.put("banned", bannedUsers);
        
        return stats;
    }

    public Map<String, Object> getAppointmentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalAppointments = appointmentRepository.count();
        
        stats.put("total", totalAppointments);
        
        return stats;
    }

    public Map<String, Object> getStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();

        if (endDate.isBefore(startDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);
        LocalDate monthAgo = today.minusDays(29);

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(User.UserStatus.ACTIVE);
        long totalAppointments = appointmentRepository.count();

        result.put("totalUsers", totalUsers);
        result.put("activeUsers", activeUsers);
        result.put("totalAppointments", totalAppointments);
        result.put("totalRevenue", 0.0);

        List<User> allUsers = userRepository.findAll();
        List<Appointment> allAppointments = appointmentRepository.findAll();

        List<Map<String, Object>> userGrowth = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            LocalDate d = cursor;
            long count = allUsers.stream()
                    .filter(u -> u.getGmtCreate() != null && u.getGmtCreate().toLocalDate().equals(d))
                    .count();
            Map<String, Object> item = new HashMap<>();
            item.put("date", d.toString());
            item.put("count", count);
            userGrowth.add(item);
            cursor = cursor.plusDays(1);
        }
        result.put("userGrowth", userGrowth);

        long completed = allAppointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .count();
        long pending = allAppointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.PENDING)
                .count();
        long cancelled = allAppointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                .count();

        Map<String, Object> appointmentStats = new HashMap<>();
        appointmentStats.put("completed", completed);
        appointmentStats.put("pending", pending);
        appointmentStats.put("cancelled", cancelled);
        result.put("appointmentStats", appointmentStats);

        List<Map<String, Object>> revenueData = new ArrayList<>();
        cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", cursor.toString());
            item.put("amount", 0.0);
            revenueData.add(item);
            cursor = cursor.plusDays(1);
        }
        result.put("revenueData", revenueData);

        Map<Long, Long> consultantCounts = allAppointments.stream()
                .filter(a -> a.getConsultantId() != null)
                .collect(Collectors.groupingBy(Appointment::getConsultantId, Collectors.counting()));

        List<Map<String, Object>> consultantRank = new ArrayList<>();
        consultantCounts.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .forEach(e -> {
                    Long consultantId = e.getKey();
                    Consultant consultant = consultantRepository.findById(consultantId).orElse(null);
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", consultant != null ? consultant.getName() : "咨询师" + consultantId);
                    item.put("count", e.getValue());
                    consultantRank.add(item);
                });
        result.put("consultantRank", consultantRank);

        Map<String, Object> summary = new HashMap<>();
        summary.put("today", buildSummarySegment(allUsers, allAppointments, today, today));
        summary.put("week", buildSummarySegment(allUsers, allAppointments, weekAgo, today));
        summary.put("month", buildSummarySegment(allUsers, allAppointments, monthAgo, today));
        summary.put("total", buildSummarySegment(allUsers, allAppointments, null, null));
        result.put("summary", summary);

        return result;
    }

    private Map<String, Object> buildSummarySegment(List<User> users, List<Appointment> appointments,
                                                    LocalDate from, LocalDate to) {
        Map<String, Object> segment = new HashMap<>();

        java.util.function.Predicate<LocalDate> dateFilter = d -> {
            if (from == null || to == null) {
                return true;
            }
            return (d.isEqual(from) || d.isAfter(from)) && (d.isEqual(to) || d.isBefore(to));
        };

        long newUsers = users.stream()
                .filter(u -> u.getGmtCreate() != null)
                .filter(u -> dateFilter.test(u.getGmtCreate().toLocalDate()))
                .count();

        long newAppointments = appointments.stream()
                .filter(a -> a.getCreatedAt() != null)
                .filter(a -> dateFilter.test(a.getCreatedAt().toLocalDate()))
                .count();

        long completed = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .filter(a -> a.getUpdatedAt() != null)
                .filter(a -> dateFilter.test(a.getUpdatedAt().toLocalDate()))
                .count();

        double revenue = 0.0;

        segment.put("newUsers", newUsers);
        segment.put("newAppointments", newAppointments);
        segment.put("completed", completed);
        segment.put("revenue", revenue);

        return segment;
    }

    // ==================== 日志记录 ====================

    public List<AdminLog> getRecentLogs(int limit) {
        return adminLogRepository.findAll(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }
    
    @Transactional
    public void logAction(Long adminUserId, String action, String targetType, Long targetId, String details) {
        AdminLog log = new AdminLog();
        log.setAdminUserId(adminUserId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        
        adminLogRepository.save(log);
    }
}
