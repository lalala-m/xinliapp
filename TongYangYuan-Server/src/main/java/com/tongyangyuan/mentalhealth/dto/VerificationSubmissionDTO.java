package com.tongyangyuan.mentalhealth.dto;

import lombok.Data;

@Data
public class VerificationSubmissionDTO {
    private String answer;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
