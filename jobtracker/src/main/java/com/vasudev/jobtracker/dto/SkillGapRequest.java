package com.vasudev.jobtracker.dto;

public class SkillGapRequest {

    private String currentSkills;
    private String targetRole;

    public SkillGapRequest() {
    }

    public SkillGapRequest(String currentSkills, String targetRole) {
        this.currentSkills = currentSkills;
        this.targetRole = targetRole;
    }

    public String getCurrentSkills() {
        return currentSkills;
    }

    public void setCurrentSkills(String currentSkills) {
        this.currentSkills = currentSkills;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }
}
