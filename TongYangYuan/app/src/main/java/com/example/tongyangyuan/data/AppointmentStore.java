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
    /** 同步失败的预约 ID 集合，供外部（如 Activity）读取并弹 Toast */
    private static final String KEY_SYNC_ERROR_IDS = "sync_error_ids";
    /** 本次同步发现的孤儿记录 ID 集合（本地有 serverId=-1 且后端也没有） */
    private static final String KEY_ORPHAN_IDS = "orphan_ids";
    
    private final SharedPreferences sharedPreferences;
    private final PreferenceStore preferenceStore;
    private final android.os.Handler syncHandler;
    private Runnable syncRunnable;
    private static final long SYNC_INTERVAL_MS = 30_000; // 30秒轮询一次

    public AppointmentStore(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferenceStore = new PreferenceStore(context);
        syncHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    }

    /** 启动后台定时同步（每30秒拉取一次后端数据） */
    public void startPeriodicSync(Context context) {
        stopPeriodicSync();
        syncRunnable = new Runnable() {
            @Override
            public void run() {
                syncFromServer();
                syncHandler.postDelayed(this, SYNC_INTERVAL_MS);
            }
        };
        syncHandler.postDelayed(syncRunnable, SYNC_INTERVAL_MS);
    }

    /** 停止定时同步 */
    public void stopPeriodicSync() {
        if (syncRunnable != null) {
            syncHandler.removeCallbacks(syncRunnable);
            syncRunnable = null;
        }
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

                int httpCode = conn.getResponseCode();
                if (httpCode == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
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
                        // updateFromServer 会自动识别并清理孤儿记录
                        int cleaned = updateFromServer(serverRecords);
                        Log.d("AppointmentStore", "同步完成，清理孤儿记录 " + cleaned + " 条");
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("AppointmentStore", "syncFromServer 异常", e);
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
                // 【关键修复】consultants表主键是 consultants.id，不是 users.id
                // 用 consultant.serverId（= consultants.id），若为0才降级
                long consultantId = record.getConsultant() != null ? record.getConsultant().getServerId() : 0;
                if (consultantId <= 0) {
                    consultantId = record.getConsultant() != null ? record.getConsultant().getUserId() : 1;
                }
                json.put("consultantId", consultantId);
                Log.d("AppointmentStore", "syncAppointmentToServer: consultantId=" + consultantId
                        + " (userId=" + (record.getConsultant() != null ? record.getConsultant().getUserId() : -1) + ")");
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
                if (responseCode == 200 || responseCode == 201) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    JSONObject jsonRes = new JSONObject(response.toString());
                    if (jsonRes.optInt("code") == 200) {
                        long serverId = 0;
                        try { serverId = jsonRes.getJSONObject("data").getLong("id"); } catch (Exception ignored) {}
                        record.setServerId(serverId);
                        List<AppointmentRecord> records = getAllAppointments();
                        for (AppointmentRecord r : records) {
                            if (r.getId().equals(record.getId())) {
                                r.setServerId(serverId);
                                break;
                            }
                        }
                        saveAppointments(records);
                        clearSyncError(record.getId());
                        Log.d("AppointmentStore", "预约同步成功，serverId=" + serverId);
                    }
                } else {
                    // 读取错误信息
                    StringBuilder errBody = new StringBuilder();
                    java.io.BufferedReader errBr = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getErrorStream()));
                    String line;
                    while ((line = errBr.readLine()) != null) errBody.append(line);
                    errBr.close();
                    String errMsg = responseCode + ":" + errBody;
                    Log.e("AppointmentStore", "同步失败 " + errMsg);
                    markSyncError(record.getId());
                }
            } catch (Exception e) {
                Log.e("AppointmentStore", "同步异常", e);
                markSyncError(record.getId());
            }
        }).start();
    }

    private void markSyncError(String localId) {
        if (TextUtils.isEmpty(localId)) return;
        SharedPreferences sp = sharedPreferences;
        String ids = sp.getString(KEY_SYNC_ERROR_IDS, "");
        if (!ids.contains(localId)) {
            sp.edit().putString(KEY_SYNC_ERROR_IDS, ids.isEmpty() ? localId : ids + "," + localId).apply();
        }
    }

    private void clearSyncError(String localId) {
        if (TextUtils.isEmpty(localId)) return;
        SharedPreferences sp = sharedPreferences;
        String ids = sp.getString(KEY_SYNC_ERROR_IDS, "");
        if (ids.contains(localId)) {
            String[] arr = ids.split(",");
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                if (!s.equals(localId) && !s.isEmpty()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(s);
                }
            }
            sp.edit().putString(KEY_SYNC_ERROR_IDS, sb.toString()).apply();
        }
    }

    /** 返回有过同步错误的本地预约 ID 列表，供调用方弹 Toast */
    public String[] getSyncErrorIds() {
        String ids = sharedPreferences.getString(KEY_SYNC_ERROR_IDS, "");
        return ids.isEmpty() ? new String[0] : ids.split(",");
    }

    /** 清除所有同步错误标记 */
    public void clearAllSyncErrors() {
        sharedPreferences.edit().remove(KEY_SYNC_ERROR_IDS).apply();
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

    /**
     * 与后端数据合并，返回清理的孤儿记录数量。
     * 孤儿记录定义：本地有记录但 serverId=-1（从未同步成功），且后端也返回了数据（说明这条确实没进去）
     */
    public int updateFromServer(List<AppointmentRecord> serverRecords) {
        List<AppointmentRecord> localRecords = getAllAppointments();
        boolean changed = false;

        // 收集后端已知的 appointmentNo（用于判断孤儿）
        java.util.Set<String> serverAptNos = new java.util.HashSet<>();
        for (AppointmentRecord sr : serverRecords) {
            serverAptNos.add(sr.getId()); // getId() 在这里等于 appointmentNo
        }

        // 收集孤儿 ID
        java.util.List<String> orphanIds = new java.util.ArrayList<>();

        // 遍历本地记录，匹配 & 更新
        for (AppointmentRecord serverRecord : serverRecords) {
            boolean exists = false;
            for (int i = 0; i < localRecords.size(); i++) {
                AppointmentRecord local = localRecords.get(i);
                if ((serverRecord.getServerId() > 0 && serverRecord.getServerId() == local.getServerId()) ||
                    (local.getId().equals(serverRecord.getId()))) {
                    local.setServerId(serverRecord.getServerId());
                    local.setStatus(serverRecord.getStatus());
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

        // 清理孤儿：本地有、后端没有、且 serverId = -1（从未成功入库）
        java.util.Iterator<AppointmentRecord> it = localRecords.iterator();
        while (it.hasNext()) {
            AppointmentRecord local = it.next();
            if (local.getServerId() <= 0 && !serverAptNos.contains(local.getId())) {
                orphanIds.add(local.getId());
                it.remove();
                changed = true;
                Log.d("AppointmentStore", "识别孤儿记录: " + local.getId()
                        + " (consultant=" + (local.getConsultant() != null ? local.getConsultant().getName() : "?") + ")");
            }
        }

        if (changed) {
            localRecords.sort((a, b) -> Long.compare(b.getCreateTime(), a.getCreateTime()));
            saveAppointments(localRecords);
        }

        // 记录孤儿 ID 供 JS 层弹 Toast
        if (!orphanIds.isEmpty()) {
            SharedPreferences sp = sharedPreferences;
            String existing = sp.getString(KEY_ORPHAN_IDS, "");
            java.util.Set<String> all = new java.util.HashSet<>();
            for (String id : existing.split(",")) {
                if (!id.isEmpty()) all.add(id);
            }
            all.addAll(orphanIds);
            StringBuilder sb = new StringBuilder();
            for (String id : all) {
                if (sb.length() > 0) sb.append(",");
                sb.append(id);
            }
            sp.edit().putString(KEY_ORPHAN_IDS, sb.toString()).apply();
        }

        return orphanIds.size();
    }

    /** 返回孤儿预约 ID 列表（从未入库，JSON 数组字符串） */
    public String getOrphanIds() {
        return sharedPreferences.getString(KEY_ORPHAN_IDS, "");
    }

    /** 清除孤儿标记 */
    public void clearOrphanIds() {
        sharedPreferences.edit().remove(KEY_ORPHAN_IDS).apply();
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
