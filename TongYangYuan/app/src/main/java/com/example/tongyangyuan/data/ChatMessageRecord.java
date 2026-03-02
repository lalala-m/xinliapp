package com.example.tongyangyuan.data;

import android.net.Uri;

import java.io.Serializable;

public class ChatMessageRecord implements Serializable {
    private final boolean fromConsultant;
    private final String type; // TEXT, IMAGE, VIDEO
    private final String content;
    private final String mediaUriString; // URI转为String存储
    private long timestamp;

    public ChatMessageRecord(boolean fromConsultant, String type, String content, Uri mediaUri) {
        this.fromConsultant = fromConsultant;
        this.type = type;
        this.content = content;
        this.mediaUriString = mediaUri != null ? mediaUri.toString() : null;
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessageRecord(boolean fromConsultant, String type, String content, Uri mediaUri, long timestamp) {
        this.fromConsultant = fromConsultant;
        this.type = type;
        this.content = content;
        this.mediaUriString = mediaUri != null ? mediaUri.toString() : null;
        this.timestamp = timestamp;
    }

    public boolean isFromConsultant() {
        return fromConsultant;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Uri getMediaUri() {
        return mediaUriString != null ? Uri.parse(mediaUriString) : null;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

