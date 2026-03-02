package com.tongyangyuan.mentalhealth.dto;

import lombok.Data;

@Data
public class TestSubmissionDTO {
    private String answers;
    private Integer timeSpent;

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }
}
