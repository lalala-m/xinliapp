package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.Appointment;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.entity.User;
import com.tongyangyuan.mentalhealth.repository.AppointmentRepository;
import com.tongyangyuan.mentalhealth.repository.ConsultantRepository;
import com.tongyangyuan.mentalhealth.repository.UserRepository;
import com.tongyangyuan.mentalhealth.service.AppointmentService;
import com.tongyangyuan.mentalhealth.service.ChatMessageService;
import com.tongyangyuan.mentalhealth.service.ConsultantService;
import com.tongyangyuan.mentalhealth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultants")
public class ConsultantController {

    private final ConsultantService consultantService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final ConsultantRepository consultantRepository;
    private final ChatMessageService chatMessageService;

    public ConsultantController(ConsultantService consultantService,
                                UserRepository userRepository,
                                JwtUtil jwtUtil,
                                AppointmentService appointmentService,
                                AppointmentRepository appointmentRepository,
                                ConsultantRepository consultantRepository,
                                ChatMessageService chatMessageService) {
        this.consultantService = consultantService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
        this.consultantRepository = consultantRepository;
        this.chatMessageService = chatMessageService;
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                return jwtUtil.extractUserId(token);
            } catch (Exception ignored) {}
        }
        return null;
    }

    @GetMapping
    public ApiResponse<List<Consultant>> getAllConsultants(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false, defaultValue = "priority") String sort) {
        try {
            List<Consultant> consultants;
            if ("priority".equalsIgnoreCase(sort)) {
                if (domain != null && !domain.isEmpty()) {
                    consultants = consultantService.findByDomainOrderByPriority(domain);
                } else {
                    consultants = consultantService.findAllOrderByPriority();
                }
            } else {
                if (domain != null && !domain.isEmpty()) {
                    consultants = consultantService.getConsultantsByDomain(domain);
                } else {
                    consultants = consultantService.getAllConsultants();
                }
            }
            return ApiResponse.success(consultants);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<Consultant> getConsultantById(@PathVariable Long id) {
        try {
            Consultant consultant = consultantService.getConsultantById(id);
            return ApiResponse.success(consultant);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<Consultant> getConsultantByUserId(@PathVariable Long userId) {
        try {
            Consultant consultant = consultantService.getConsultantByUserId(userId);
            return ApiResponse.success(consultant);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录咨询师的资料（需JWT认证）
     */
    @GetMapping("/me")
    public ApiResponse<Consultant> getMyProfile(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error("请先登录");
        }
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getUserType() != User.UserType.CONSULTANT) {
                return ApiResponse.error("您不是咨询师账号");
            }
            Consultant consultant = consultantService.getConsultantByUserId(userId);
            return ApiResponse.success(consultant);
        } catch (Exception e) {
            return ApiResponse.error("获取资料失败: " + e.getMessage());
        }
    }

    /**
     * 咨询师自我更新资料（需JWT认证，头像双向同步到User表）
     * PUT /consultants/me
     */
    @PutMapping("/me")
    public ApiResponse<Consultant> updateMyProfile(
            HttpServletRequest request,
            @RequestBody Consultant updates) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error("请先登录");
        }
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getUserType() != User.UserType.CONSULTANT) {
                return ApiResponse.error("您不是咨询师账号");
            }
            Consultant consultant = consultantService.getConsultantByUserId(userId);
            // 更新头像：同时写入 User.avatarUrl 和 Consultant.avatarUrl
            if (updates.getAvatarUrl() != null && !updates.getAvatarUrl().isEmpty()) {
                String newAvatar = updates.getAvatarUrl();
                consultant.setAvatarUrl(newAvatar);
                user.setAvatarUrl(newAvatar);
                userRepository.save(user);
            }
            // 更新其他字段（只更新非空字段）
            if (updates.getName() != null) consultant.setName(updates.getName());
            if (updates.getTitle() != null) consultant.setTitle(updates.getTitle());
            if (updates.getSpecialty() != null) consultant.setSpecialty(updates.getSpecialty());
            if (updates.getIntro() != null) consultant.setIntro(updates.getIntro());
            if (updates.getAvatarColor() != null) consultant.setAvatarColor(updates.getAvatarColor());
            if (updates.getAvailable() != null) consultant.setAvailable(updates.getAvailable());

            Consultant saved = consultantService.createOrUpdateConsultant(consultant);
            return ApiResponse.success("资料更新成功", saved);
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<Consultant> createConsultant(@RequestBody Consultant consultant) {
        try {
            Consultant created = consultantService.createOrUpdateConsultant(consultant);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Consultant> updateConsultant(@PathVariable Long id, @RequestBody Consultant consultant) {
        try {
            consultant.setId(id);
            Consultant updated = consultantService.createOrUpdateConsultant(consultant);
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ========== 咨询师预约相关接口 ==========

    /**
     * 获取当前登录咨询师的所有预约（含 WebSocket 通知支持）
     * GET /consultants/me/appointments
     */
    @GetMapping("/me/appointments")
    public ApiResponse<List<Appointment>> getMyAppointments(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error("请先登录");
        }
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getUserType() != User.UserType.CONSULTANT) {
                return ApiResponse.error("您不是咨询师账号");
            }
            Consultant consultant = consultantService.getConsultantByUserId(userId);
            List<Appointment> appointments = appointmentService.getAppointmentsByConsultantId(consultant.getId());
            return ApiResponse.success(appointments);
        } catch (Exception e) {
            return ApiResponse.error("获取预约列表失败: " + e.getMessage());
        }
    }

    /**
     * 咨询师确认预约（只允许本人操作自己的预约）
     * PUT /consultants/me/appointments/{id}/accept
     */
    @PutMapping("/me/appointments/{id}/accept")
    public ApiResponse<Appointment> acceptAppointment(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error("请先登录");
        }
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            // 验证是否属于当前咨询师
            Consultant consultant = consultantService.getConsultantByUserId(userId);
            if (!appointment.getConsultantId().equals(consultant.getId())) {
                return ApiResponse.error("无权操作此预约");
            }
            if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
                return ApiResponse.error("只有待处理的预约才能确认");
            }
            appointment.setStatus(Appointment.AppointmentStatus.ACCEPTED);
            Appointment saved = appointmentRepository.save(appointment);

            // 通过 WebSocket 通知家长预约已确认
            chatMessageService.notifyAppointmentConfirmed(saved, consultant);

            return ApiResponse.success("预约已确认", saved);
        } catch (Exception e) {
            return ApiResponse.error("确认失败: " + e.getMessage());
        }
    }

    /**
     * 咨询师拒绝预约（只允许本人操作自己的预约）
     * PUT /consultants/me/appointments/{id}/reject
     */
    @PutMapping("/me/appointments/{id}/reject")
    public ApiResponse<Appointment> rejectAppointment(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponse.error("请先登录");
        }
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            // 验证是否属于当前咨询师
            Consultant consultant = consultantService.getConsultantByUserId(userId);
            if (!appointment.getConsultantId().equals(consultant.getId())) {
                return ApiResponse.error("无权操作此预约");
            }
            if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED ||
                appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
                return ApiResponse.error("该预约无法被拒绝");
            }
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            Appointment saved = appointmentRepository.save(appointment);

            // 通过 WebSocket 通知家长预约被拒绝
            chatMessageService.notifyAppointmentRejected(saved);

            return ApiResponse.success("已拒绝该预约", saved);
        } catch (Exception e) {
            return ApiResponse.error("操作失败: " + e.getMessage());
        }
    }
}
