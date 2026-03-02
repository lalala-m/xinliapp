package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import com.tongyangyuan.mentalhealth.entity.Consultant;
import com.tongyangyuan.mentalhealth.service.ConsultantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultants")
public class ConsultantController {

    private final ConsultantService consultantService;

    public ConsultantController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    @GetMapping
    public ApiResponse<List<Consultant>> getAllConsultants() {
        try {
            List<Consultant> consultants = consultantService.getAllConsultants();
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
}
