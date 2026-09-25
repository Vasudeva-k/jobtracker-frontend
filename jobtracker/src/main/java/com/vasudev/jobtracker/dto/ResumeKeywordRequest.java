package com.vasudev.jobtracker.dto;

public class ResumeKeywordRequest {

    private String resumeText;
    private String targetRole;

    public ResumeKeywordRequest() {
    }

    public ResumeKeywordRequest(String resumeText, String targetRole) {
        this.resumeText = resumeText;
        this.targetRole = targetRole;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }
}
