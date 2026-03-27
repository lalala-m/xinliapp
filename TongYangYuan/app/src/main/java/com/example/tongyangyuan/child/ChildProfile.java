package com.example.tongyangyuan.child;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChildProfile {

    public enum Gender {
        BOY,
        GIRL
    }

    private String id;
    private String name = "";
    private Gender gender = Gender.BOY;
    private String birthDate = "";
    private String ethnicity = "";
    private String nativePlace = "";
    private String familyRank = "";
    private String birthPlace = "";
    private String languageEnv = "";
    private String school = "";
    private String homeAddress = "";
    private String interests = "";
    private String activities = "";
    private String bodyStatus = "";
    private String bodyStatusDetail = "";
    private final List<String> medicalHistory = new ArrayList<>();
    private String medicalHistoryOther = "";
    private String fatherPhone = "";
    private String motherPhone = "";
    private String guardianPhone = "";

    public ChildProfile() {
        this(UUID.randomUUID().toString());
        medicalHistory.add("无");
    }

    public ChildProfile(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
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

    public void setMedicalHistory(List<String> history) {
        medicalHistory.clear();
        if (history != null) {
            medicalHistory.addAll(history);
        }
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

    public boolean isCompleted() {
        boolean basicFilled = !name.isEmpty()
                && !birthDate.isEmpty()
                && !ethnicity.isEmpty()
                && !nativePlace.isEmpty()
                && !familyRank.isEmpty()
                && !birthPlace.isEmpty()
                && !languageEnv.isEmpty()
                && !school.isEmpty()
                && !homeAddress.isEmpty()
                && !interests.isEmpty()
                && !activities.isEmpty()
                && !bodyStatus.isEmpty()
                && !medicalHistory.isEmpty()
                && isPhoneValid(fatherPhone)
                && isPhoneValid(motherPhone)
                && isPhoneValid(guardianPhone);
        if (!basicFilled) {
            return false;
        }
        if (requiresBodyStatusDetail(bodyStatus) && bodyStatusDetail.isEmpty()) {
            return false;
        }
        if (medicalHistory.contains("其他") && medicalHistoryOther.isEmpty()) {
            return false;
        }
        return true;
    }

    private boolean requiresBodyStatusDetail(String status) {
        return "较差".equals(status) || "很差".equals(status);
    }

    private boolean isPhoneValid(String phone) {
        return phone != null && phone.length() == 11;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("name", name);
        object.put("gender", gender.name());
        object.put("birthDate", birthDate);
        object.put("ethnicity", ethnicity);
        object.put("nativePlace", nativePlace);
        object.put("familyRank", familyRank);
        object.put("birthPlace", birthPlace);
        object.put("languageEnv", languageEnv);
        object.put("school", school);
        object.put("homeAddress", homeAddress);
        object.put("interests", interests);
        object.put("activities", activities);
        object.put("bodyStatus", bodyStatus);
        object.put("bodyStatusDetail", bodyStatusDetail);
        JSONArray historyArray = new JSONArray();
        for (String entry : medicalHistory) {
            historyArray.put(entry);
        }
        object.put("medicalHistory", historyArray);
        object.put("medicalHistoryOther", medicalHistoryOther);
        object.put("fatherPhone", fatherPhone);
        object.put("motherPhone", motherPhone);
        object.put("guardianPhone", guardianPhone);
        return object;
    }

    public static ChildProfile fromJson(JSONObject object) throws JSONException {
        ChildProfile profile = new ChildProfile(object.optString("id", UUID.randomUUID().toString()));
        profile.setName(object.optString("name", ""));
        try {
            profile.setGender(Gender.valueOf(object.optString("gender", Gender.BOY.name())));
        } catch (IllegalArgumentException e) {
            profile.setGender(Gender.BOY);
        }
        profile.setBirthDate(object.optString("birthDate", ""));
        profile.setEthnicity(object.optString("ethnicity", ""));
        profile.setNativePlace(object.optString("nativePlace", ""));
        profile.setFamilyRank(object.optString("familyRank", ""));
        profile.setBirthPlace(object.optString("birthPlace", ""));
        profile.setLanguageEnv(object.optString("languageEnv", ""));
        profile.setSchool(object.optString("school", ""));
        profile.setHomeAddress(object.optString("homeAddress", ""));
        profile.setInterests(object.optString("interests", ""));
        profile.setActivities(object.optString("activities", ""));
        profile.setBodyStatus(object.optString("bodyStatus", ""));
        profile.setBodyStatusDetail(object.optString("bodyStatusDetail", ""));
        JSONArray historyArray = object.optJSONArray("medicalHistory");
        if (historyArray != null) {
            List<String> history = new ArrayList<>();
            for (int i = 0; i < historyArray.length(); i++) {
                history.add(historyArray.optString(i, ""));
            }
            profile.setMedicalHistory(history);
        }
        profile.setMedicalHistoryOther(object.optString("medicalHistoryOther", ""));
        profile.setFatherPhone(object.optString("fatherPhone", ""));
        profile.setMotherPhone(object.optString("motherPhone", ""));
        profile.setGuardianPhone(object.optString("guardianPhone", ""));
        return profile;
    }
}

