package com.example.tongyangyuan.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tongyangyuan.database.dao.ChildProfileDao;
import com.example.tongyangyuan.database.dao.ConsultantDao;
import com.example.tongyangyuan.database.entity.ChildProfileEntity;
import com.example.tongyangyuan.database.entity.ConsultantEntity;
import com.example.tongyangyuan.data.AppointmentRecord;
import com.example.tongyangyuan.data.AppointmentStore;
import com.example.tongyangyuan.data.PreferenceStore;
import com.example.tongyangyuan.consult.Consultant;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataSyncService {
    private static final String TAG = "DataSyncService";
    private static DataSyncService instance;
    private final Context context;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private DataSyncService(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized DataSyncService getInstance(Context context) {
        if (instance == null) {
            instance = new DataSyncService(context);
        }
        return instance;
    }

    public interface SyncCallback {
        void onSuccess(int count);
        void onError(Exception e);
    }

    public void syncConsultants(SyncCallback callback) {
        executorService.execute(() -> {
            try {
                List<ConsultantEntity> consultants = fetchConsultantsFromApi();
                saveConsultantsToLocal(consultants);
                int count = consultants.size();
                Log.d(TAG, "Synced " + count + " consultants successfully");
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                Log.e(TAG, "Failed to sync consultants", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void syncChildProfiles(String userPhone, SyncCallback callback) {
        executorService.execute(() -> {
            try {
                List<ChildProfileEntity> profiles = fetchChildProfilesFromApi(userPhone);
                saveChildProfilesToLocal(profiles, userPhone);
                int count = profiles.size();
                Log.d(TAG, "Synced " + count + " child profiles successfully for user: " + userPhone);
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                Log.e(TAG, "Failed to sync child profiles", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void uploadChildProfile(ChildProfileEntity profile, SyncCallback callback) {
        executorService.execute(() -> {
            try {
                uploadChildProfileToApi(profile);
                saveChildProfileToLocal(profile);
                Log.d(TAG, "Uploaded child profile successfully: " + profile.getName());
                mainHandler.post(() -> callback.onSuccess(1));
            } catch (Exception e) {
                Log.e(TAG, "Failed to upload child profile", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void syncAppointments(SyncCallback callback) {
        executorService.execute(() -> {
            try {
                PreferenceStore preferenceStore = new PreferenceStore(context);
                long userId = preferenceStore.getUserId();
                String token = preferenceStore.getAuthToken();
                
                if (userId <= 0 || token == null || token.isEmpty()) {
                     mainHandler.post(() -> callback.onError(new Exception("Not logged in")));
                     return;
                }
                
                List<AppointmentRecord> appointments = fetchAppointmentsFromApi(String.valueOf(userId), token);
                AppointmentStore appointmentStore = new AppointmentStore(context);
                appointmentStore.updateFromServer(appointments);
                
                int count = appointments.size();
                Log.d(TAG, "Synced " + count + " appointments successfully");
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                Log.e(TAG, "Failed to sync appointments", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private List<ConsultantEntity> fetchConsultantsFromApi() throws Exception {
        String baseUrl = NetworkConfig.getBaseUrl();
        URL url = new URL(baseUrl + "/consultants");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        InputStream is = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        is.close();
        conn.disconnect();
        return parseConsultantsJson(sb.toString());
    }

    private List<String> parseStringList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] items = str.split(",");
        List<String> result = new ArrayList<>();
        for (String item : items) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private void saveConsultantsToLocal(List<ConsultantEntity> consultants) {
        AppDatabase db = AppDatabase.getInstance(context);
        ConsultantDao dao = db.consultantDao();
        // 使用 REPLACE 策略增量更新，保留本地数据不被清空
        for (ConsultantEntity consultant : consultants) {
            ConsultantEntity existing = null;
            if (consultant.getServerId() > 0) {
                existing = dao.getConsultantByServerId(consultant.getServerId());
            }
            if (existing == null && consultant.getUserId() > 0) {
                existing = dao.getConsultantByUserId(consultant.getUserId());
            }
            if (existing != null) {
                consultant.setId(existing.getId());
            }
            dao.insertConsultant(consultant);
        }
    }

    private List<ChildProfileEntity> fetchChildProfilesFromApi(String userPhone) throws Exception {
        // 使用 userId 从服务器获取孩子列表
        long userId = com.example.tongyangyuan.data.PreferenceStore.getInstance(context).getUserId();
        if (userId == -1) {
            Log.w(TAG, "fetchChildProfilesFromApi: userId not found, skipping");
            return new ArrayList<>();
        }
        
        String baseUrl = NetworkConfig.getBaseUrl();
        String token = com.example.tongyangyuan.data.PreferenceStore.getInstance(context).getAuthToken();
        URL url = new URL(baseUrl + "/children/parent/" + userId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            
            JSONObject json = new JSONObject(sb.toString());
            if (json.getInt("code") == 200) {
                JSONArray data = json.getJSONArray("data");
                List<ChildProfileEntity> profiles = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject item = data.getJSONObject(i);
                    ChildProfileEntity entity = new ChildProfileEntity();
                    entity.setId(String.valueOf(item.optLong("id")));
                    entity.setUserPhone("uid-" + userId); // 使用 userId 作为 key
                    entity.setName(item.optString("name"));
                    entity.setGender(item.optString("gender", "BOY"));
                    entity.setBirthDate(item.optString("birthDate"));
                    entity.setEthnicity(item.optString("ethnicity"));
                    entity.setNativePlace(item.optString("nativePlace"));
                    entity.setFamilyRank(item.optString("familyRank"));
                    entity.setBirthPlace(item.optString("birthPlace"));
                    entity.setLanguageEnv(item.optString("languageEnv"));
                    entity.setSchool(item.optString("school"));
                    entity.setHomeAddress(item.optString("homeAddress"));
                    entity.setInterests(item.optString("interests"));
                    entity.setActivities(item.optString("activities"));
                    entity.setBodyStatus(item.optString("bodyStatus"));
                    entity.setBodyStatusDetail(item.optString("bodyStatusDetail"));
                    entity.setMedicalHistoryOther(item.optString("medicalHistoryOther"));
                    entity.setFatherPhone(item.optString("fatherPhone"));
                    entity.setMotherPhone(item.optString("motherPhone"));
                    entity.setGuardianPhone(item.optString("guardianPhone"));
                    
                    // 处理 medicalHistory
                    String historyStr = item.optString("medicalHistory");
                    List<String> historyList = new ArrayList<>();
                    if (historyStr != null && !historyStr.isEmpty()) {
                        try {
                            JSONArray arr = new JSONArray(historyStr);
                            for (int j = 0; j < arr.length(); j++) {
                                historyList.add(arr.getString(j));
                            }
                        } catch (Exception e) {
                            String[] parts = historyStr.split(",");
                            for (String p : parts) {
                                if (!p.trim().isEmpty()) historyList.add(p.trim());
                            }
                        }
                    }
                    entity.setMedicalHistory(historyList);
                    profiles.add(entity);
                }
                return profiles;
            }
        }
        return new ArrayList<>();
    }

    private List<AppointmentRecord> fetchAppointmentsFromApi(String userId, String token) throws Exception {
        String baseUrl = NetworkConfig.getBaseUrl();
        URL url = new URL(baseUrl + "/appointments/parent/" + userId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        
        InputStream is = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        is.close();
        conn.disconnect();
        
        return parseAppointmentsJson(sb.toString());
    }

    private List<AppointmentRecord> parseAppointmentsJson(String json) {
        Gson gson = new Gson();
        ApiResponse<List<ServerAppointment>> resp = gson.fromJson(json,
                new TypeToken<ApiResponse<List<ServerAppointment>>>() {}.getType());
        
        List<AppointmentRecord> result = new ArrayList<>();
        if (resp != null && resp.getCode() == 200 && resp.getData() != null) {
            AppDatabase db = AppDatabase.getInstance(context);
            ConsultantDao consultantDao = db.consultantDao();
            
            for (ServerAppointment sa : resp.getData()) {
                long consultantUserId = sa.getConsultantId() != null ? sa.getConsultantId() : 0L;
                ConsultantEntity ce = consultantUserId > 0
                        ? consultantDao.getConsultantByUserId(consultantUserId)
                        : null;

                Consultant consultant;
                if (ce != null) {
                    consultant = new Consultant(
                            ce.getUserId(),
                            ce.getName(),
                            ce.getTitle(),
                            ce.getSpecialty(),
                            ce.getRating(),
                            ce.getServedCount(),
                            ce.getAvatarColor(),
                            ce.getIdentityTags(),
                            ce.getIntro(),
                            ce.getReviews()
                    );
                } else {
                    consultant = new Consultant(
                            0,
                            "咨询师已下架",
                            "心理咨询师",
                            "",
                            5.0,
                            "0",
                            "#6FA6F8",
                            new ArrayList<>(),
                            "",
                            new ArrayList<>()
                    );
                }
                
                String date = sa.getAppointmentDate();
                
                AppointmentRecord record = new AppointmentRecord(
                        sa.getAppointmentNo(), 
                        consultant,
                        date,
                        sa.getTimeSlot(),
                        sa.getDescription(),
                        System.currentTimeMillis(), 
                        sa.getChildId() != null ? String.valueOf(sa.getChildId()) : "",
                        sa.getChildName()
                );
                record.setServerId(sa.getId());
                record.setStatus(sa.getStatus());
                
                result.add(record);
            }
        }
        return result;
    }

    private void uploadChildProfileToApi(ChildProfileEntity profile) throws Exception {
    }

    private void saveChildProfilesToLocal(List<ChildProfileEntity> profiles, String userPhone) {
        AppDatabase db = AppDatabase.getInstance(context);
        ChildProfileDao dao = db.childProfileDao();
        // 使用 REPLACE 策略增量更新，不删除本地已有数据
        for (ChildProfileEntity profile : profiles) {
            dao.insertChildProfile(profile);
        }
    }

    private void saveChildProfileToLocal(ChildProfileEntity profile) {
        AppDatabase db = AppDatabase.getInstance(context);
        ChildProfileDao dao = db.childProfileDao();
        dao.insertChildProfile(profile);
    }

    private String joinStringList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    public void shutdown() {
        executorService.shutdown();
    }
    public static List<ConsultantEntity> parseConsultantsJson(String json) {
        Gson gson = new Gson();
        ApiResponse<List<ServerConsultant>> resp = gson.fromJson(json,
                new TypeToken<ApiResponse<List<ServerConsultant>>>() {}.getType());
        List<ConsultantEntity> result = new ArrayList<>();
        if (resp != null && resp.getCode() == 200 && resp.getData() != null) {
            for (ServerConsultant sc : resp.getData()) {
                ConsultantEntity entity = new ConsultantEntity();
                entity.setUserId(sc.getUserId() != null ? sc.getUserId() : 0);
                entity.setServerId(sc.getId()); // Store Server ID (Consultant PK)
                entity.setName(sc.getName());
                entity.setTitle(sc.getTitle());
                entity.setSpecialty(sc.getSpecialty());
                try {
                    entity.setRating(sc.getRating() != null ? sc.getRating().doubleValue() : 5.0);
                } catch (Exception e) {
                    entity.setRating(5.0);
                }
                entity.setServedCount(sc.getServedCount() != null ? String.valueOf(sc.getServedCount()) : "0");
                entity.setAvatarColor(sc.getAvatarColor());
                entity.setAvatarUrl(sc.getAvatarUrl());
                entity.setIntro(sc.getIntro());
                entity.setReviews(new ArrayList<>());
                // 根据后端 identityTier 映射为前端 identityTags
                List<String> identityTags = mapTierToIdentityTags(sc.getIdentityTier());
                entity.setIdentityTags(identityTags);
                // 保存擅长的人生阶段
                List<String> stageNames = new ArrayList<>();
                if (sc.getStages() != null) {
                    for (ServerLifeStage stage : sc.getStages()) {
                        if (stage.getName() != null) {
                            stageNames.add(stage.getName());
                        }
                    }
                }
                entity.setStages(stageNames);
                result.add(entity);
            }
        }
        return result;
    }

    /**
     * 将后端 identityTier 映射为前端 identityTags 列表
     * 前端 Consultant.resolveTier() 依赖这些标签来解析 tier
     */
    private static List<String> mapTierToIdentityTags(String tier) {
        List<String> tags = new ArrayList<>();
        if (tier == null || tier.isEmpty()) {
            tags.add("童康源黄V认证");
            return tags;
        }
        switch (tier) {
            case "PLATINUM":
                tags.add("童康源黄V认证");
                break;
            case "GOLD":
                tags.add("童康源指导师（蓝V认证）");
                break;
            case "SILVER":
            case "BRONZE":
                tags.add("童康源内部人员");
                break;
            default:
                // 如果后端已经返回前端映射值（YELLOW_V/BLUE_V/INTERNAL）
                if ("YELLOW_V".equals(tier)) {
                    tags.add("童康源黄V认证");
                } else if ("BLUE_V".equals(tier)) {
                    tags.add("童康源指导师（蓝V认证）");
                } else if ("INTERNAL".equals(tier)) {
                    tags.add("童康源内部人员");
                } else {
                    tags.add("童康源黄V认证");
                }
                break;
        }
        return tags;
    }

    private static class ApiResponse<T> {
        private int code;
        private String message;
        private T data;
        public int getCode() { return code; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }

    private static class ServerConsultant {
        private Long id;
        private Long userId;
        private String name;
        private String title;
        private String specialty;
        private Double rating;
        private Integer servedCount;
        private String avatarColor;
        private String avatarUrl;
        private String intro;
        private String identityTier;
        private List<ServerLifeStage> stages;

        public Long getUserId() {
            return userId;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public String getSpecialty() {
            return specialty;
        }

        public Double getRating() {
            return rating;
        }

        public Integer getServedCount() {
            return servedCount;
        }

        public String getAvatarColor() {
            return avatarColor;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public String getIntro() {
            return intro;
        }

        public String getIdentityTier() {
            return identityTier;
        }

        public List<ServerLifeStage> getStages() {
            return stages;
        }
    }

    private static class ServerLifeStage {
        private Long id;
        private String name;
        private String code;

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getCode() { return code; }
    }
    
    private static class ServerAppointment {
        private Long id;
        private String appointmentNo;
        private Long consultantId;
        private Long parentUserId;
        private Long childId;
        private String childName;
        private String appointmentDate;
        private String timeSlot;
        private String description;
        private String status;
        
        public Long getId() { return id; }
        public String getAppointmentNo() { return appointmentNo; }
        public Long getConsultantId() { return consultantId; }
        public Long getParentUserId() { return parentUserId; }
        public Long getChildId() { return childId; }
        public String getChildName() { return childName; }
        public String getAppointmentDate() { return appointmentDate; }
        public String getTimeSlot() { return timeSlot; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
    }
}
