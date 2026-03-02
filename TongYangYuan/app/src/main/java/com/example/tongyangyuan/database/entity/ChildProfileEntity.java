package com.example.tongyangyuan.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.tongyangyuan.database.converter.StringListConverter;

import java.util.List;

@Entity(tableName = "child_profiles")
@TypeConverters(StringListConverter.class)
public class ChildProfileEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "user_phone")
    private String userPhone;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "gender")
    private String gender;

    @ColumnInfo(name = "birth_date")
    private String birthDate;

    @ColumnInfo(name = "ethnicity")
    private String ethnicity;

    @ColumnInfo(name = "native_place")
    private String nativePlace;

    @ColumnInfo(name = "family_rank")
    private String familyRank;

    @ColumnInfo(name = "birth_place")
    private String birthPlace;

    @ColumnInfo(name = "language_env")
    private String languageEnv;

    @ColumnInfo(name = "school")
    private String school;

    @ColumnInfo(name = "home_address")
    private String homeAddress;

    @ColumnInfo(name = "interests")
    private String interests;

    @ColumnInfo(name = "activities")
    private String activities;

    @ColumnInfo(name = "body_status")
    private String bodyStatus;

    @ColumnInfo(name = "body_status_detail")
    private String bodyStatusDetail;

    @ColumnInfo(name = "medical_history")
    private List<String> medicalHistory;

    @ColumnInfo(name = "medical_history_other")
    private String medicalHistoryOther;

    @ColumnInfo(name = "father_phone")
    private String fatherPhone;

    @ColumnInfo(name = "mother_phone")
    private String motherPhone;

    @ColumnInfo(name = "guardian_phone")
    private String guardianPhone;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getNativePlace() {
        return nativePlace;
    }

    public void setNativePlace(String nativePlace) {
        this.nativePlace = nativePlace;
    }

    public String getFamilyRank() {
        return familyRank;
    }

    public void setFamilyRank(String familyRank) {
        this.familyRank = familyRank;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getLanguageEnv() {
        return languageEnv;
    }

    public void setLanguageEnv(String languageEnv) {
        this.languageEnv = languageEnv;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public String getActivities() {
        return activities;
    }

    public void setActivities(String activities) {
        this.activities = activities;
    }

    public String getBodyStatus() {
        return bodyStatus;
    }

    public void setBodyStatus(String bodyStatus) {
        this.bodyStatus = bodyStatus;
    }

    public String getBodyStatusDetail() {
        return bodyStatusDetail;
    }

    public void setBodyStatusDetail(String bodyStatusDetail) {
        this.bodyStatusDetail = bodyStatusDetail;
    }

    public List<String> getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(List<String> medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getMedicalHistoryOther() {
        return medicalHistoryOther;
    }

    public void setMedicalHistoryOther(String medicalHistoryOther) {
        this.medicalHistoryOther = medicalHistoryOther;
    }

    public String getFatherPhone() {
        return fatherPhone;
    }

    public void setFatherPhone(String fatherPhone) {
        this.fatherPhone = fatherPhone;
    }

    public String getMotherPhone() {
        return motherPhone;
    }

    public void setMotherPhone(String motherPhone) {
        this.motherPhone = motherPhone;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }
}
