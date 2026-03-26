package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.Appointment;
import com.tongyangyuan.mentalhealth.service.AppointmentService;
import org.springframework.web.bind.annotation.*;

import com.tongyangyuan.mentalhealth.service.ConsultantService;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ConsultantService consultantService;

    public AppointmentController(AppointmentService appointmentService, ConsultantService consultantService) {
        this.appointmentService = appointmentService;
        this.consultantService = consultantService;
    }

    @GetMapping("/consultant/{consultantId}")
    public ApiResponse<List<Appointment>> getAppointmentsByConsultant(@PathVariable Long consultantId) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByConsultantId(consultantId);
            return ApiResponse.success(appointments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/parent/{parentUserId}")
    public ApiResponse<List<Map<String, Object>>> getAppointmentsByParent(@PathVariable Long parentUserId) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByParentUserId(parentUserId);
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (Appointment app : appointments) {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("id", app.getId());
                map.put("appointmentNo", app.getAppointmentNo());
                map.put("consultantId", app.getConsultantId());
                map.put("childName", app.getChildName());
                map.put("childAge", app.getChildAge());
                map.put("appointmentDate", app.getAppointmentDate());
                map.put("timeSlot", app.getTimeSlot());
                // status 转 String，避开内部枚举序列化
                map.put("status", app.getStatus() != null ? app.getStatus().name() : null);
                map.put("domain", app.getDomain());
                map.put("description", app.getDescription());
                map.put("isPinned", Boolean.TRUE.equals(app.getIsPinned()));
                map.put("isChatted", Boolean.TRUE.equals(app.getIsChatted()));
                map.put("createdAt", app.getCreatedAt());
                map.put("updatedAt", app.getUpdatedAt());
                // 用名字查consultant，不调用service层，避免懒加载实体序列化问题
                Consultant c = null;
                if (app.getConsultantId() != null) {
                    try {
                        c = consultantService.getConsultantById(app.getConsultantId());
                    } catch (Exception ignored) {}
                }
                map.put("consultantName", c != null ? c.getName() : "未知咨询师");
                result.add(map);
            }
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<Appointment> getAppointmentById(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            return ApiResponse.success(appointment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<Appointment> createAppointment(@RequestBody Appointment appointment) {
        try {
            Appointment created = appointmentService.createAppointment(appointment);
            return ApiResponse.success("预约创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Appointment> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment) {
        try {
            Appointment updated = appointmentService.updateAppointment(id, appointment);
            return ApiResponse.success("预约更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAppointment(@PathVariable Long id) {
        try {
            appointmentService.deleteAppointment(id);
            return ApiResponse.success("预约删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
