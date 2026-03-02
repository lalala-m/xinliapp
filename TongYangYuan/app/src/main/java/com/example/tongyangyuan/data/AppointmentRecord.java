package com.example.tongyangyuan.data;

import com.example.tongyangyuan.consult.Consultant;

import java.io.Serializable;

public class AppointmentRecord implements Serializable {
    private final String id;
    private final Consultant consultant;
    private final String date;
    private final String timeSlot;
    private final String description;
    private final long createTime;
    private boolean hasChatted;
    private boolean pinned;
    private final String childId;
    private final String childName;
    private long serverId;
    private String status = "PENDING";
    private String domain; // 新增领域字段

    public AppointmentRecord(String id, Consultant consultant, String date,
                             String timeSlot, String description, long createTime,
                             String childId, String childName) {
        this.id = id;
        this.consultant = consultant;
        this.date = date;
        this.timeSlot = timeSlot;
        this.description = description;
        this.createTime = createTime;
        this.hasChatted = false;
        this.pinned = false;
        this.childId = childId;
        this.childName = childName;
        this.serverId = -1;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public String getId() {
        return id;
    }

    public Consultant getConsultant() {
        return consultant;
    }

    public String getDate() {
        return date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getDescription() {
        return description;
    }

    public long getCreateTime() {
        return createTime;
    }

    public boolean hasChatted() {
        return hasChatted;
    }

    public void setHasChatted(boolean hasChatted) {
        this.hasChatted = hasChatted;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public String getChildId() {
        return childId;
    }

    public String getChildName() {
        return childName;
    }
}

