package com.vasudev.jobtracker.dto;

public class ATSScoreRequest {

    private String resumeText;

    public ATSScoreRequest() {
    }

    public ATSScoreRequest(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }
}
