package com.example.tongyangyuan.dto;

public class WebRTCSignal {
    private String type; // offer, answer, ice-candidate, call, accept, reject, end
    private Long fromUserId;
    private Long toUserId;
    private Long appointmentId;
    private Object data; // Can be SDP (as a Map) or ICE candidate (as a Map)

    public WebRTCSignal(String type, Long fromUserId, Long toUserId, Long appointmentId, Object data) {
        this.type = type;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.appointmentId = appointmentId;
        this.data = data;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

