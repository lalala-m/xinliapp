package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.annotation.RequireAdmin;
import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.AdminLog;
import com.tongyangyuan.mentalhealth.entity.Appointment;
import com.tongyangyuan.mentalhealth.entity.ChatMessage;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.entity.LearningPackage;
import com.tongyangyuan.mentalhealth.entity.LearningVideo;
import com.tongyangyuan.mentalhealth.entity.TestQuestion;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
import com.tongyangyuan.mentalhealth.service.AdminService;
import com.tongyangyuan.mentalhealth.service.ChatMessageService;
import com.tongyangyuan.mentalhealth.service.LearningAdminService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequireAdmin
public class AdminController {

    private final AdminService adminService;
    private final LearningAdminService learningAdminService;
    private final ChatMessageService chatMessageService;
    private final ConsultantRepository consultantRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AdminController(
            AdminService adminService,
            LearningAdminService learningAdminService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            ChatMessageService chatMessageService,
            ConsultantRepository consultantRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.adminService = adminService;
        this.learningAdminService = learningAdminService;
        this.passwordEncoder = passwordEncoder;
        this.chatMessageService = chatMessageService;
        this.consultantRepository = consultantRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ==================== 用户管理 ====================
    
    /**
     * 获取所有用户
     */
    @GetMapping("/users")
    public ApiResponse<Object> getAllUsers(@RequestParam(required = false) User.UserType userType) {
        try {
            if (userType == User.UserType.PARENT) {
                return ApiResponse.success("获取家长列表成功", adminService.getParentsWithDetails());
            } else if (userType != null) {
                return ApiResponse.success("获取用户列表成功", adminService.getAllUsers(userType));
            } else {
                return ApiResponse.success("获取用户列表成功", adminService.getAllUsers());
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建新用户
     */
    @PostMapping("/users")
    public ApiResponse<User> createUser(@RequestBody User user) {
        try {
            // 设置默认密码（如果未提供）
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword("123456");
            }
            
            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // 设置默认状态
            if (user.getStatus() == null) {
                user.setStatus(User.UserStatus.ACTIVE);
            }
            
            User created = adminService.createUser(user);
            return ApiResponse.success("创建用户成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/users/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updated = adminService.updateUser(id, user);
            return ApiResponse.success("更新用户成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/users/{id}/status")
    public ApiResponse<User> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            User updated = adminService.updateUserStatus(id, status);
            return ApiResponse.success("更新用户状态成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return ApiResponse.success("删除用户成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }


    // ==================== 咨询师管理 ====================

    /**
     * 获取所有咨询师
     */
    @GetMapping("/consultants")
    public ApiResponse<List<Consultant>> getAllConsultants() {
        try {
            List<Consultant> consultants = adminService.getAllConsultants();
            return ApiResponse.success("获取咨询师列表成功", consultants);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建咨询师
     */
    @PostMapping("/consultants")
    public ApiResponse<Consultant> createConsultant(@RequestBody com.tongyangyuan.mentalhealth.dto.CreateConsultantRequest request) {
        try {
            Consultant created = adminService.createConsultant(request);
            return ApiResponse.success("创建咨询师成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取咨询师详情
     */
    @GetMapping("/consultants/{id}")
    public ApiResponse<Consultant> getConsultantById(@PathVariable Long id) {
        try {
            Consultant consultant = adminService.getConsultantById(id);
            return ApiResponse.success("获取咨询师详情成功", consultant);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新咨询师信息
     */
    @PutMapping("/consultants/{id}")
    public ApiResponse<Consultant> updateConsultant(@PathVariable Long id, @RequestBody Consultant consultant) {
        try {
            Consultant updated = adminService.updateConsultant(id, consultant);
            return ApiResponse.success("更新咨询师信息成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除咨询师
     */
    @DeleteMapping("/consultants/{id}")
    public ApiResponse<Void> deleteConsultant(@PathVariable Long id) {
        try {
            adminService.deleteConsultant(id);
            return ApiResponse.success("删除咨询师成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 认证咨询师
     */
    @PutMapping("/consultants/{id}/verify")
    public ApiResponse<Consultant> verifyConsultant(@PathVariable Long id) {
        try {
            Consultant verified = adminService.verifyConsultant(id);
            return ApiResponse.success("认证咨询师成功", verified);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ==================== 预约管理 ====================
    
    /**
     * 获取所有预约
     */
    @GetMapping("/appointments")
    public ApiResponse<List<Map<String, Object>>> getAllAppointments() {
        try {
            List<Map<String, Object>> appointments = adminService.getAllAppointments();
            return ApiResponse.success("获取预约列表成功", appointments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 确认预约
     */
    @PutMapping("/appointments/{id}/confirm")
    public ApiResponse<Appointment> confirmAppointment(@PathVariable Long id) {
        try {
            Appointment appointment = adminService.confirmAppointment(id);

            Long consultantId = appointment.getConsultantId();
            Long parentUserId = appointment.getParentUserId();

            if (consultantId != null && parentUserId != null) {
                Consultant consultant = consultantRepository.findById(consultantId)
                        .orElse(null);
                Long consultantUserId = consultant != null ? consultant.getUserId() : null;

                if (consultantUserId != null) {
                    ChatMessage message = new ChatMessage();
                    message.setAppointmentId(appointment.getId());
                    message.setSenderUserId(consultantUserId);
                    message.setReceiverUserId(parentUserId);
                    message.setMessageType(ChatMessage.MessageType.TEXT);
                    String consultantName = consultant != null ? consultant.getName() : "咨询师";
                    message.setContent("您好，我是" + consultantName + "，很高兴为您服务。");
                    message.setMediaUrl(null);
                    message.setIsFromConsultant(true);
                    message.setIsRead(false);

                    ChatMessage saved = chatMessageService.saveMessage(message);

                    com.tongyangyuan.mentalhealth.dto.ChatMessageDTO dto =
                            new com.tongyangyuan.mentalhealth.dto.ChatMessageDTO();
                    dto.setId(saved.getId());
                    dto.setAppointmentId(saved.getAppointmentId());
                    dto.setSenderUserId(saved.getSenderUserId());
                    dto.setReceiverUserId(saved.getReceiverUserId());
                    dto.setMessageType(saved.getMessageType().name());
                    dto.setContent(saved.getContent());
                    dto.setMediaUrl(saved.getMediaUrl());
                    dto.setIsFromConsultant(saved.getIsFromConsultant());
                    dto.setIsRead(saved.getIsRead());
                    if (saved.getCreatedAt() != null) {
                        dto.setTimestamp(saved.getCreatedAt()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli());
                    }

                    messagingTemplate.convertAndSendToUser(
                            saved.getReceiverUserId().toString(),
                            "/queue/messages",
                            dto
                    );

                    messagingTemplate.convertAndSendToUser(
                            saved.getSenderUserId().toString(),
                            "/queue/messages",
                            dto
                    );
                }
            }

            return ApiResponse.success("确认预约成功", appointment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 取消预约
     */
    @PutMapping("/appointments/{id}/cancel")
    public ApiResponse<Appointment> cancelAppointment(@PathVariable Long id) {
        try {
            Appointment appointment = adminService.cancelAppointment(id);
            return ApiResponse.success("取消预约成功", appointment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ==================== 数据统计 ====================
    
    /**
     * 获取总览数据
     */
    @GetMapping("/statistics/overview")
    public ApiResponse<Map<String, Object>> getOverviewStatistics() {
        try {
            Map<String, Object> stats = adminService.getOverviewStatistics();
            return ApiResponse.success("获取统计数据成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/statistics/users")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> stats = adminService.getUserStatistics();
            return ApiResponse.success("获取用户统计成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取预约统计
     */
    @GetMapping("/statistics/appointments")
    public ApiResponse<Map<String, Object>> getAppointmentStatistics() {
        try {
            Map<String, Object> stats = adminService.getAppointmentStatistics();
            return ApiResponse.success("获取预约统计成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取综合统计（用于数据统计页）
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            Map<String, Object> stats = adminService.getStatistics(start, end);
            return ApiResponse.success("获取统计数据成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ==================== 日志管理 ====================

    /**
     * 获取最近日志
     */
    @GetMapping("/logs")
    public ApiResponse<List<AdminLog>> getRecentLogs(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<AdminLog> logs = adminService.getRecentLogs(limit);
            return ApiResponse.success("获取日志成功", logs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ==================== 学习系统管理 ====================

    /**
     * 创建学习包
     */
    @PostMapping("/learning/packages")
    public ApiResponse<LearningPackage> createPackage(@RequestBody LearningPackage pkg) {
        try {
            LearningPackage created = learningAdminService.createPackage(pkg);
            return ApiResponse.success("创建学习包成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新学习包
     */
    @PutMapping("/learning/packages/{id}")
    public ApiResponse<LearningPackage> updatePackage(@PathVariable Long id, @RequestBody LearningPackage pkg) {
        try {
            LearningPackage updated = learningAdminService.updatePackage(id, pkg);
            return ApiResponse.success("更新学习包成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除学习包
     */
    @DeleteMapping("/learning/packages/{id}")
    public ApiResponse<String> deletePackage(@PathVariable Long id) {
        try {
            learningAdminService.deletePackage(id);
            return ApiResponse.success("删除学习包成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 添加视频
     */
    @PostMapping("/learning/videos")
    public ApiResponse<LearningVideo> addVideo(@RequestBody LearningVideo video) {
        try {
            LearningVideo saved = learningAdminService.addVideo(video);
            return ApiResponse.success("添加视频成功", saved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 添加测试题
     */
    @PostMapping("/learning/questions")
    public ApiResponse<TestQuestion> addQuestion(@RequestBody TestQuestion question) {
        try {
            TestQuestion saved = learningAdminService.addQuestion(question);
            return ApiResponse.success("添加测试题成功", saved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 批量导入测试题
     */
    @PostMapping("/learning/questions/batch")
    public ApiResponse<List<TestQuestion>> batchImportQuestions(@RequestBody List<TestQuestion> questions) {
        try {
            List<TestQuestion> saved = learningAdminService.batchImportQuestions(questions);
            return ApiResponse.success("批量导入测试题成功", saved);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取学习系统统计
     */
    @GetMapping("/learning/statistics")
    public ApiResponse<Map<String, Object>> getLearningStatistics() {
        try {
            Map<String, Object> stats = learningAdminService.getStatistics();
            return ApiResponse.success("获取学习系统统计成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
