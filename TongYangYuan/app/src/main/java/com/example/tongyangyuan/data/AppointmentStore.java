package com.example.tongyangyuan.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.example.tongyangyuan.consult.Consultant;
import com.example.tongyangyuan.data.PreferenceStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppointmentStore {
    private static final String PREF_NAME = "appointment_store";
    private static final String KEY_APPOINTMENTS = "appointments";
    private static final String KEY_APPOINTMENTS_PREFIX = "appointments_";
    
    private final SharedPreferences sharedPreferences;
    private final PreferenceStore preferenceStore;

    public AppointmentStore(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferenceStore = new PreferenceStore(context);
    }

    public void syncFromServer() {
        new Thread(() -> {
            try {
                long userId = preferenceStore.getUserId();
                if (userId <= 0) return;

                String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                java.net.URL url = new java.net.URL(baseUrl + "/appointments/parent/" + userId);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                String token = preferenceStore.getAuthToken();
                if (!TextUtils.isEmpty(token)) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    JSONObject jsonRes = new JSONObject(response.toString());
                    if (jsonRes.optInt("code") == 200) {
                        JSONArray data = jsonRes.getJSONArray("data");
                        List<AppointmentRecord> serverRecords = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            AppointmentRecord record = parseFromServerJson(item);
                            if (record != null) {
                                serverRecords.add(record);
                            }
                        }
                        updateFromServer(serverRecords);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private AppointmentRecord parseFromServerJson(JSONObject obj) {
        try {
            long serverId = obj.getLong("id");
            String appointmentNo = obj.getString("appointmentNo");
            String date = obj.getString("appointmentDate");
            String timeSlot = obj.getString("timeSlot");
            String description = obj.optString("description", "");
            long createTime = System.currentTimeMillis(); // 暂用当前时间，或者解析 createdAt
            String childId = obj.optString("childId", ""); // 注意：Server可能返回的是childId数字
            // 如果childId是数字，转字符串
            if (childId.isEmpty() && obj.has("childId") && !obj.isNull("childId")) {
                childId = String.valueOf(obj.optLong("childId"));
            }
            String childName = obj.optString("childName", "");
            String status = obj.optString("status", "PENDING");
            String domain = obj.optString("domain", ""); // 解析 domain
            
            // 构造Consultant (需要后端返回详细信息，或者再去查询)
            // 假设后端返回了 consultant 对象
            // 如果后端只返回了 consultantId，我们可能无法构建完整的 Consultant 对象
            // 这里为了简单，如果后端没返回 consultant 详情，我们造一个临时的
            Consultant consultant;
            if (obj.has("consultant")) {
                // TODO: 解析后端返回的 consultant 对象
                // 暂时用默认值，实际应完善后端 DTO
                consultant = new Consultant("咨询师", "心理咨询师", "儿童心理", 5.0, "100+", "#6FA6F8", new ArrayList<>(), "简介", new ArrayList<>());
                consultant.setUserId(obj.getLong("consultantId"));
            } else {
                consultant = new Consultant("咨询师", "心理咨询师", "儿童心理", 5.0, "100+", "#6FA6F8", new ArrayList<>(), "简介", new ArrayList<>());
                consultant.setUserId(obj.optLong("consultantId", 1));
            }

            AppointmentRecord record = new AppointmentRecord(
                    appointmentNo, consultant, date, timeSlot, description, createTime, childId, childName);
            record.setServerId(serverId);
            record.setStatus(status);
            record.setDomain(domain);
            return record;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String addAppointment(Consultant consultant, String date, String timeSlot,
                                 String description, String childId, String childName, String domain) {
        String id = UUID.randomUUID().toString();
        long createTime = System.currentTimeMillis();
        AppointmentRecord record = new AppointmentRecord(
                id, consultant, date, timeSlot, description, createTime, childId, childName);
        record.setDomain(domain); // 设置领域
        
        List<AppointmentRecord> records = getAllAppointments();
        records.add(0, record); // 最新的在前面
        
        saveAppointments(records);

        // 同步到服务端
        syncAppointmentToServer(record);

        return id;
    }

    public String addAppointment(Consultant consultant, String date, String timeSlot,
                                 String description, String childId, String childName) {
        return addAppointment(consultant, date, timeSlot, description, childId, childName, null);
    }

    private void syncAppointmentToServer(AppointmentRecord record) {
        new Thread(() -> {
            try {
                String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                java.net.URL url = new java.net.URL(baseUrl + "/appointments");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                
                String token = preferenceStore.getAuthToken();
                if (!TextUtils.isEmpty(token)) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                JSONObject json = new JSONObject();
                json.put("appointmentNo", record.getId());
                // consultant.getUserId() 可能为0，如果为0则尝试用默认值1
                long consultantId = record.getConsultant().getUserId();
                json.put("consultantId", consultantId > 0 ? consultantId : 1); 
                json.put("parentUserId", preferenceStore.getUserId());
                String childId = record.getChildId();
                if (!TextUtils.isEmpty(childId)) {
                    try {
                        json.put("childId", Long.parseLong(childId));
                    } catch (NumberFormatException e) {
                        Log.w("AppointmentStore", "Invalid childId, skip sending: " + childId, e);
                    }
                }
                json.put("childName", record.getChildName());
                json.put("appointmentDate", record.getDate());
                json.put("timeSlot", record.getTimeSlot());
                json.put("description", record.getDescription());
                String domain = record.getDomain();
                if (!TextUtils.isEmpty(domain)) {
                    json.put("domain", domain);
                }
                json.put("status", "PENDING");

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    JSONObject jsonRes = new JSONObject(response.toString());
                    if (jsonRes.optInt("code") == 200) {
                        JSONObject data = jsonRes.getJSONObject("data");
                        long serverId = data.getLong("id");
                        record.setServerId(serverId);
                        
                        // Update local storage
                        List<AppointmentRecord> records = getAllAppointments();
                        boolean updated = false;
                        for (AppointmentRecord r : records) {
                            if (r.getId().equals(record.getId())) {
                                r.setServerId(serverId);
                                updated = true;
                                break;
                            }
                        }
                        if (updated) {
                            saveAppointments(records);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public List<AppointmentRecord> getAllAppointments() {
        String json = readAppointmentsJson();
        List<AppointmentRecord> records = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                AppointmentRecord record = parseFromJson(obj);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return records;
    }

    public AppointmentRecord getAppointmentById(String id) {
        List<AppointmentRecord> records = getAllAppointments();
        for (AppointmentRecord record : records) {
            if (record.getId().equals(id)) {
                return record;
            }
        }
        return null;
    }

    public AppointmentRecord getAppointmentByServerId(long serverId) {
        List<AppointmentRecord> records = getAllAppointments();
        for (AppointmentRecord record : records) {
            if (record.getServerId() == serverId) {
                return record;
            }
        }
        return null;
    }

    public void markConsultationEnded(String appointmentId) {
        if (TextUtils.isEmpty(appointmentId)) {
            return;
        }
        List<AppointmentRecord> records = getAllAppointments();
        boolean changed = false;
        for (AppointmentRecord record : records) {
            if (appointmentId.equals(record.getId())) {
                record.setHasChatted(true);
                String status = record.getStatus();
                if (TextUtils.isEmpty(status) || !"COMPLETED".equalsIgnoreCase(status)) {
                    record.setStatus("COMPLETED");
                }
                changed = true;
                break;
            }
        }
        if (changed) {
            saveAppointments(records);
        }
    }

    public boolean isConsultationEnded(String appointmentId) {
        if (TextUtils.isEmpty(appointmentId)) {
            return false;
        }
        AppointmentRecord record = getAppointmentById(appointmentId);
        if (record == null) {
            try {
                long serverId = Long.parseLong(appointmentId);
                record = getAppointmentByServerId(serverId);
            } catch (NumberFormatException ignored) {
            }
        }
        if (record == null) {
            return false;
        }
        String status = record.getStatus();
        return record.hasChatted()
                || "COMPLETED".equalsIgnoreCase(status)
                || "CANCELLED".equalsIgnoreCase(status);
    }

    public void markAsChatted(String appointmentId) {
        List<AppointmentRecord> records = getAllAppointments();
        for (AppointmentRecord record : records) {
            if (record.getId().equals(appointmentId)) {
                record.setHasChatted(true);
                break;
            }
        }
        saveAppointments(records);
    }

    public void setPinned(String appointmentId, boolean pinned) {
        List<AppointmentRecord> records = getAllAppointments();
        for (AppointmentRecord record : records) {
            if (record.getId().equals(appointmentId)) {
                record.setPinned(pinned);
                break;
            }
        }
        saveAppointments(records);
    }

    public void updateFromServer(List<AppointmentRecord> serverRecords) {
        List<AppointmentRecord> localRecords = getAllAppointments();
        boolean changed = false;

        for (AppointmentRecord serverRecord : serverRecords) {
            boolean exists = false;
            for (int i = 0; i < localRecords.size(); i++) {
                AppointmentRecord local = localRecords.get(i);
                // 匹配规则：Server ID 相同，或者 Local ID (UUID) 等于 Server 的 appointmentNo
                if ((serverRecord.getServerId() > 0 && serverRecord.getServerId() == local.getServerId()) ||
                    (local.getId().equals(serverRecord.getId()))) { // 注意：serverRecord.getId() 在解析时已经设为 appointmentNo
                    
                    // 更新本地记录状态
                    local.setServerId(serverRecord.getServerId());
                    local.setStatus(serverRecord.getStatus()); // 需要在 AppointmentRecord 中添加 setStatus
                    // 可以根据需要更新其他字段
                    exists = true;
                    changed = true;
                    break;
                }
            }
            if (!exists) {
                localRecords.add(serverRecord);
                changed = true;
            }
        }
        
        if (changed) {
            // 按时间倒序排序
            localRecords.sort((a, b) -> Long.compare(b.getCreateTime(), a.getCreateTime()));
            saveAppointments(localRecords);
        }
    }

    public void updateFromServerJsonArray(JSONArray dataArray) {
        if (dataArray == null) {
            return;
        }
        List<AppointmentRecord> serverRecords = new ArrayList<>();
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject item = dataArray.optJSONObject(i);
            if (item == null) {
                continue;
            }
            AppointmentRecord record = parseFromServerJson(item);
            if (record != null) {
                serverRecords.add(record);
            }
        }
        if (!serverRecords.isEmpty()) {
            updateFromServer(serverRecords);
        }
    }

    public void deleteAppointment(String appointmentId) {
        List<AppointmentRecord> records = getAllAppointments();
        AppointmentRecord target = null;
        for (AppointmentRecord record : records) {
            if (record.getId().equals(appointmentId)) {
                target = record;
                break;
            }
        }
        if (target != null) {
            records.remove(target);
            saveAppointments(records);
            
            // 如果已同步到服务器，则尝试从服务器删除
            if (target.getServerId() > 0) {
                deleteAppointmentFromServer(target.getServerId());
            }
        }
    }

    private void deleteAppointmentFromServer(long serverId) {
        new Thread(() -> {
            try {
                String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                java.net.URL url = new java.net.URL(baseUrl + "/appointments/" + serverId);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                
                String token = preferenceStore.getAuthToken();
                if (!TextUtils.isEmpty(token)) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    // Deleted successfully
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void saveAppointments(List<AppointmentRecord> records) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (AppointmentRecord record : records) {
                jsonArray.put(toJson(record));
            }
        sharedPreferences.edit().putString(getAppointmentsKey(), jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject toJson(AppointmentRecord record) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", record.getId());
        obj.put("date", record.getDate());
        obj.put("timeSlot", record.getTimeSlot());
        obj.put("description", record.getDescription());
        obj.put("createTime", record.getCreateTime());
        obj.put("hasChatted", record.hasChatted());
        obj.put("childId", record.getChildId() != null ? record.getChildId() : "");
        obj.put("childName", record.getChildName() != null ? record.getChildName() : "");
        obj.put("pinned", record.isPinned());
        obj.put("serverId", record.getServerId());
        obj.put("status", record.getStatus());
        obj.put("domain", record.getDomain());
        
        // 存储Consultant信息
        Consultant c = record.getConsultant();
        JSONObject consultantObj = new JSONObject();
        consultantObj.put("name", c.getName());
        consultantObj.put("title", c.getTitle());
        consultantObj.put("specialty", c.getSpecialty());
        consultantObj.put("rating", c.getRating());
        consultantObj.put("servedCount", c.getServedCount());
        consultantObj.put("avatarColor", c.getAvatarColor());
        consultantObj.put("intro", c.getIntro());
        consultantObj.put("identityTier", c.getIdentityTier().name());
        JSONArray tagArray = new JSONArray();
        for (String tag : c.getIdentityTags()) {
            tagArray.put(tag);
        }
        consultantObj.put("identityTags", tagArray);
        JSONArray reviewsArray = new JSONArray();
        for (String review : c.getReviews()) {
            reviewsArray.put(review);
        }
        consultantObj.put("reviews", reviewsArray);
        obj.put("consultant", consultantObj);
        
        return obj;
    }

    private AppointmentRecord parseFromJson(JSONObject obj) {
        try {
            String id = obj.getString("id");
            String date = obj.getString("date");
            String timeSlot = obj.getString("timeSlot");
            String description = obj.getString("description");
            long createTime = obj.getLong("createTime");
            boolean hasChatted = obj.optBoolean("hasChatted", false);
            boolean pinned = obj.optBoolean("pinned", false);
            
            JSONObject consultantObj = obj.getJSONObject("consultant");
            String name = consultantObj.getString("name");
            String title = consultantObj.getString("title");
            String specialty = consultantObj.getString("specialty");
            double rating = consultantObj.getDouble("rating");
            String servedCount = consultantObj.getString("servedCount");
            String avatarColor = consultantObj.getString("avatarColor");
            String intro = consultantObj.getString("intro");
            JSONArray reviewsArray = consultantObj.getJSONArray("reviews");
            List<String> reviews = new ArrayList<>();
            for (int i = 0; i < reviewsArray.length(); i++) {
                reviews.add(reviewsArray.getString(i));
            }
            
            JSONArray tagArray = consultantObj.optJSONArray("identityTags");
            List<String> identityTags = new ArrayList<>();
            if (tagArray != null) {
                for (int i = 0; i < tagArray.length(); i++) {
                    identityTags.add(tagArray.getString(i));
                }
            }
            Consultant consultant = new Consultant(name, title, specialty, rating,
                    servedCount, avatarColor, identityTags, intro, reviews);
            
            String childId = obj.optString("childId", "");
            String childName = obj.optString("childName", "");

            AppointmentRecord record = new AppointmentRecord(
                    id, consultant, date, timeSlot, description, createTime, childId, childName);
            record.setHasChatted(hasChatted);
            record.setPinned(pinned);
            record.setServerId(obj.optLong("serverId", -1));
            record.setStatus(obj.optString("status", "PENDING"));
            record.setDomain(obj.optString("domain", ""));
            return record;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    private String readAppointmentsJson() {
        String key = getAppointmentsKey();
        String json = sharedPreferences.getString(key, null);
        if (json == null) {
            json = sharedPreferences.getString(KEY_APPOINTMENTS, null);
            if (json != null) {
                sharedPreferences.edit()
                        .putString(key, json)
                        .remove(KEY_APPOINTMENTS)
                        .apply();
            }
        }
        return json != null ? json : "[]";
    }

    private String getAppointmentsKey() {
        String phone = preferenceStore.getLastLoginPhone();
        if (TextUtils.isEmpty(phone)) {
            phone = "guest";
        }
        return KEY_APPOINTMENTS_PREFIX + phone;
    }
}
