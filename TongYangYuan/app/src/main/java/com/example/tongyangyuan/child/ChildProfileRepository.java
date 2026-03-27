package com.example.tongyangyuan.child;

import android.content.Context;

import com.example.tongyangyuan.database.AppDatabase;
import com.example.tongyangyuan.database.DataSyncService;
import com.example.tongyangyuan.database.dao.ChildProfileDao;
import com.example.tongyangyuan.database.entity.ChildProfileEntity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChildProfileRepository {
    private static ChildProfileRepository instance;
    private final Context context;
    private final ExecutorService executorService;

    private ChildProfileRepository(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized ChildProfileRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ChildProfileRepository(context);
        }
        return instance;
    }

    public interface ProfilesCallback {
        void onSuccess(List<ChildProfile> profiles);
        void onError(Exception e);
    }

    public interface SingleProfileCallback {
        void onSuccess(ChildProfile profile);
        void onError(Exception e);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void getProfilesByUser(String userPhone, ProfilesCallback callback) {
        // 如果已登录，优先从服务器获取
        if (com.example.tongyangyuan.data.PreferenceStore.getInstance(context).isLoggedIn()) {
            syncFromServer(userPhone, new SaveCallback() {
                @Override
                public void onSuccess() {
                    // 同步完成后从本地读取（因为syncFromServer已经更新了本地数据库）
                    // 实际应该直接解析服务器返回的数据，这里为了兼容旧逻辑先这样
                    loadFromLocal(userPhone, callback);
                }

                @Override
                public void onError(Exception e) {
                    // 失败则降级到本地
                    loadFromLocal(userPhone, callback);
                }
            });
        } else {
            loadFromLocal(userPhone, callback);
        }
    }

    private void loadFromLocal(String userPhone, ProfilesCallback callback) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                ChildProfileDao dao = db.childProfileDao();
                List<ChildProfileEntity> entities = dao.getChildProfilesByUser(userPhone);

                List<ChildProfile> profiles = new ArrayList<>();
                for (ChildProfileEntity entity : entities) {
                    profiles.add(entityToProfile(entity));
                }

                callback.onSuccess(profiles);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public List<ChildProfile> getProfilesByUserSync(String userPhone) {
        // 同步方法仅读取本地，避免阻塞主线程太久
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            ChildProfileDao dao = db.childProfileDao();
            List<ChildProfileEntity> entities = dao.getChildProfilesByUser(userPhone);

            List<ChildProfile> profiles = new ArrayList<>();
            for (ChildProfileEntity entity : entities) {
                profiles.add(entityToProfile(entity));
            }
            return profiles;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void getProfileById(String id, SingleProfileCallback callback) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                ChildProfileDao dao = db.childProfileDao();
                ChildProfileEntity entity = dao.getChildProfileById(id);

                if (entity != null) {
                    callback.onSuccess(entityToProfile(entity));
                } else {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public ChildProfile getProfileByIdSync(String id) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            ChildProfileDao dao = db.childProfileDao();
            ChildProfileEntity entity = dao.getChildProfileById(id);
            return entity != null ? entityToProfile(entity) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void syncFromServer(String userPhone, SaveCallback callback) {
        executorService.execute(() -> {
            try {
                // 使用 HttpURLConnection 调用 /children/parent/{parentId}
                long userId = com.example.tongyangyuan.data.PreferenceStore.getInstance(context).getUserId();
                if (userId == -1) {
                    // 如果没有UserId，可能还没登录成功，暂时跳过
                    callback.onSuccess();
                    return;
                }

                String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                String token = com.example.tongyangyuan.data.PreferenceStore.getInstance(context).getAuthToken();
                
                java.net.URL url = new java.net.URL(baseUrl + "/children/parent/" + userId);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                
                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    
                    org.json.JSONObject json = new org.json.JSONObject(response.toString());
                    if (json.getInt("code") == 200) {
                        JSONArray data = json.getJSONArray("data");
                        AppDatabase db = AppDatabase.getInstance(context);
                        ChildProfileDao dao = db.childProfileDao();
                        dao.deleteByUser(userPhone);
                        for (int i = 0; i < data.length(); i++) {
                            org.json.JSONObject item = data.getJSONObject(i);
                            ChildProfile profile = new ChildProfile();
                            // 使用服务器返回的 id 作为本地存储的 id（转换为字符串）
                            profile.setId(String.valueOf(item.optLong("id")));
                            profile.setName(item.optString("name"));
                            try {
                                profile.setGender(ChildProfile.Gender.valueOf(item.optString("gender", "BOY")));
                            } catch (Exception e) {
                                profile.setGender(ChildProfile.Gender.BOY);
                            }
                            profile.setBirthDate(item.optString("birthDate"));
                            profile.setEthnicity(item.optString("ethnicity"));
                            profile.setNativePlace(item.optString("nativePlace"));
                            profile.setFamilyRank(item.optString("familyRank"));
                            profile.setBirthPlace(item.optString("birthPlace"));
                            profile.setLanguageEnv(item.optString("languageEnv"));
                            profile.setSchool(item.optString("school"));
                            profile.setHomeAddress(item.optString("homeAddress"));
                            profile.setInterests(item.optString("interests"));
                            profile.setActivities(item.optString("activities"));
                            profile.setBodyStatus(item.optString("bodyStatus"));
                            profile.setBodyStatusDetail(item.optString("bodyStatusDetail"));
                            profile.setMedicalHistoryOther(item.optString("medicalHistoryOther"));
                            profile.setFatherPhone(item.optString("fatherPhone"));
                            profile.setMotherPhone(item.optString("motherPhone"));
                            profile.setGuardianPhone(item.optString("guardianPhone"));

                            String historyStr = item.optString("medicalHistory");
                            List<String> historyList = new ArrayList<>();
                            if (historyStr != null && !historyStr.isEmpty()) {
                                try {
                                    JSONArray arr = new JSONArray(historyStr);
                                    for(int j=0; j<arr.length(); j++) historyList.add(arr.getString(j));
                                } catch (JSONException e) {
                                    String[] parts = historyStr.split(",");
                                    for(String p : parts) if(!p.trim().isEmpty()) historyList.add(p.trim());
                                }
                            }
                            if (historyList.isEmpty()) historyList.add("无");
                            profile.setMedicalHistory(historyList);

                            dao.insert(profileToEntity(profile, userPhone));
                        }
                        com.example.tongyangyuan.data.PreferenceStore
                                .getInstance(context)
                                .setHasChildProfile(data.length() > 0);
                    }
                }
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void saveProfiles(String userPhone, List<ChildProfile> profiles, SaveCallback callback) {
        executorService.execute(() -> {
            try {
                // 1. 保存到本地
                AppDatabase db = AppDatabase.getInstance(context);
                ChildProfileDao dao = db.childProfileDao();
                
                // 删除该用户的所有旧数据，实现全量同步
                dao.deleteByUser(userPhone);
                
                for (ChildProfile profile : profiles) {
                    dao.insert(profileToEntity(profile, userPhone));
                }

                // 2. 上传到服务器 (Batch)
                long userId = com.example.tongyangyuan.data.PreferenceStore.getInstance(context).getUserId();
                if (userId != -1) {
                    String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                    String token = com.example.tongyangyuan.data.PreferenceStore.getInstance(context).getAuthToken();
                    
                    java.net.URL url = new java.net.URL(baseUrl + "/children/batch");
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setDoOutput(true);
                    
                    JSONArray jsonArray = new JSONArray();
                    for (ChildProfile profile : profiles) {
                        org.json.JSONObject obj = profile.toJson();
                        obj.put("parentUserId", userId);
                        // 保留服务器 ID，如果本地没有服务器 ID 则不上传 id 字段
                        String localId = profile.getId();
                        if (localId == null || localId.startsWith("child-") || localId.startsWith("temp-")) {
                            obj.remove("id");
                        }
                        
                        // 处理 medicalHistory (List -> String)
                        JSONArray historyArray = obj.optJSONArray("medicalHistory");
                        if (historyArray != null) {
                            obj.put("medicalHistory", historyArray.toString());
                        }
                        
                        jsonArray.put(obj);
                    }
                    
                    try (java.io.OutputStream os = conn.getOutputStream()) {
                        os.write(jsonArray.toString().getBytes("UTF-8"));
                    }
                    
                    // 读取服务器返回的数据（包含服务器生成的 id）
                    if (conn.getResponseCode() == 200) {
                        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                        StringBuilder serverResponse = new StringBuilder();
                        String respLine;
                        while ((respLine = br.readLine()) != null) {
                            serverResponse.append(respLine);
                        }
                        
                        org.json.JSONObject respJson = new org.json.JSONObject(serverResponse.toString());
                        if (respJson.getInt("code") == 200) {
                            JSONArray savedChildren = respJson.getJSONArray("data");
                            // 更新本地数据库中的 id 为服务器返回的 id
                            for (int i = 0; i < savedChildren.length() && i < profiles.size(); i++) {
                                org.json.JSONObject savedChild = savedChildren.getJSONObject(i);
                                long serverId = savedChild.optLong("id", -1);
                                if (serverId != -1) {
                                    // 更新本地实体的 id
                                    ChildProfileEntity entity = dao.getChildProfileById(profiles.get(i).getId());
                                    if (entity != null) {
                                        entity.setId(String.valueOf(serverId));
                                        dao.updateChildProfile(entity);
                                    }
                                }
                            }
                        }
                    }
                }
                
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    private ChildProfile entityToProfile(ChildProfileEntity entity) {
        ChildProfile profile = new ChildProfile(entity.getId());
        profile.setName(entity.getName());
        try {
            profile.setGender(ChildProfile.Gender.valueOf(entity.getGender()));
        } catch (Exception e) {
            profile.setGender(ChildProfile.Gender.BOY);
        }
        profile.setBirthDate(entity.getBirthDate());
        profile.setEthnicity(entity.getEthnicity());
        profile.setNativePlace(entity.getNativePlace());
        profile.setFamilyRank(entity.getFamilyRank());
        profile.setBirthPlace(entity.getBirthPlace());
        profile.setLanguageEnv(entity.getLanguageEnv());
        profile.setSchool(entity.getSchool());
        profile.setHomeAddress(entity.getHomeAddress());
        profile.setInterests(entity.getInterests());
        profile.setActivities(entity.getActivities());
        profile.setBodyStatus(entity.getBodyStatus());
        profile.setBodyStatusDetail(entity.getBodyStatusDetail());
        profile.setMedicalHistory(entity.getMedicalHistory());
        profile.setMedicalHistoryOther(entity.getMedicalHistoryOther());
        profile.setFatherPhone(entity.getFatherPhone());
        profile.setMotherPhone(entity.getMotherPhone());
        profile.setGuardianPhone(entity.getGuardianPhone());
        return profile;
    }

    private ChildProfileEntity profileToEntity(ChildProfile profile, String userPhone) {
        ChildProfileEntity entity = new ChildProfileEntity();
        entity.setId(profile.getId());
        entity.setUserPhone(userPhone);
        entity.setName(profile.getName());
        entity.setGender(profile.getGender().name());
        entity.setBirthDate(profile.getBirthDate());
        entity.setEthnicity(profile.getEthnicity());
        entity.setNativePlace(profile.getNativePlace());
        entity.setFamilyRank(profile.getFamilyRank());
        entity.setBirthPlace(profile.getBirthPlace());
        entity.setLanguageEnv(profile.getLanguageEnv());
        entity.setSchool(profile.getSchool());
        entity.setHomeAddress(profile.getHomeAddress());
        entity.setInterests(profile.getInterests());
        entity.setActivities(profile.getActivities());
        entity.setBodyStatus(profile.getBodyStatus());
        entity.setBodyStatusDetail(profile.getBodyStatusDetail());
        entity.setMedicalHistory(profile.getMedicalHistory());
        entity.setMedicalHistoryOther(profile.getMedicalHistoryOther());
        entity.setFatherPhone(profile.getFatherPhone());
        entity.setMotherPhone(profile.getMotherPhone());
        entity.setGuardianPhone(profile.getGuardianPhone());
        return entity;
    }

    public String profilesToJson(List<ChildProfile> profiles) {
        try {
            JSONArray array = new JSONArray();
            for (ChildProfile profile : profiles) {
                array.put(profile.toJson());
            }
            return array.toString();
        } catch (JSONException e) {
            return "[]";
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
