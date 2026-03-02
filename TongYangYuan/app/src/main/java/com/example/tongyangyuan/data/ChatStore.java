package com.example.tongyangyuan.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatStore {
    private static final String PREF_NAME = "chat_store";
    private static final String KEY_CHAT_PREFIX = "chat_";
    
    private final SharedPreferences sharedPreferences;

    public ChatStore(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveMessage(String appointmentId, ChatMessageRecord message) {
        List<ChatMessageRecord> messages = getMessages(appointmentId);
        messages.add(message);
        saveMessages(appointmentId, messages);
    }

    public void saveMessages(String appointmentId, List<ChatMessageRecord> messages) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (ChatMessageRecord message : messages) {
                jsonArray.put(toJson(message));
            }
            String key = KEY_CHAT_PREFIX + appointmentId;
            sharedPreferences.edit().putString(key, jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<ChatMessageRecord> getMessages(String appointmentId) {
        String key = KEY_CHAT_PREFIX + appointmentId;
        String json = sharedPreferences.getString(key, "[]");
        List<ChatMessageRecord> messages = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                ChatMessageRecord message = parseFromJson(obj);
                if (message != null) {
                    messages.add(message);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messages;
    }

    private JSONObject toJson(ChatMessageRecord message) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("fromConsultant", message.isFromConsultant());
        obj.put("type", message.getType());
        obj.put("content", message.getContent());
        obj.put("mediaUriString", message.getMediaUri() != null ? message.getMediaUri().toString() : null);
        obj.put("timestamp", message.getTimestamp());
        return obj;
    }

    private ChatMessageRecord parseFromJson(JSONObject obj) {
        try {
            boolean fromConsultant = obj.getBoolean("fromConsultant");
            String type = obj.getString("type");
            String content = obj.getString("content");
            String mediaUriString = obj.optString("mediaUriString", null);
            Uri mediaUri = mediaUriString != null && !mediaUriString.isEmpty() ? Uri.parse(mediaUriString) : null;
            long timestamp = obj.optLong("timestamp", System.currentTimeMillis());
            
            return new ChatMessageRecord(fromConsultant, type, content, mediaUri, timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

