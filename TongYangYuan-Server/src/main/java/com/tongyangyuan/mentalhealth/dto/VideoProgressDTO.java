package com.tongyangyuan.mentalhealth.dto;

import lombok.Data;

@Data
public class VideoProgressDTO {
    private Long packageId;
    private Integer watchDuration;
    private Integer lastPosition;

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Integer getWatchDuration() {
        return watchDuration;
    }

    public void setWatchDuration(Integer watchDuration) {
        this.watchDuration = watchDuration;
    }

    public Integer getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(Integer lastPosition) {
        this.lastPosition = lastPosition;
    }
}
