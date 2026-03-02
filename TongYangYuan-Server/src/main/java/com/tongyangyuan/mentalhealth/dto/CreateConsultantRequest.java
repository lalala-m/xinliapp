package com.tongyangyuan.mentalhealth.dto;

import com.tongyangyuan.mentalhealth.entity.Consultant;

public class CreateConsultantRequest {
    private String phone;
    private String name;
    private String title;
    private String specialty;
    private String intro;
    private Consultant.IdentityTier identityTier;

    // Getters and Setters
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public Consultant.IdentityTier getIdentityTier() {
        return identityTier;
    }

    public void setIdentityTier(Consultant.IdentityTier identityTier) {
        this.identityTier = identityTier;
    }
}
