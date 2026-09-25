package com.vasudev.jobtracker.dto;

import java.util.List;

public class SkillGapResponse {

    private String targetRole;
    private List<String> existingSkills;
    private List<String> missingSkills;
    private List<String> learningPlan;
    private String estimatedDuration;
    private String source;

    public SkillGapResponse() {
    }

    public SkillGapResponse(
            String targetRole,
            List<String> existingSkills,
            List<String> missingSkills,
            List<String> learningPlan,
            String estimatedDuration) {

        this.targetRole = targetRole;
        this.existingSkills = existingSkills;
        this.missingSkills = missingSkills;
        this.learningPlan = learningPlan;
        this.estimatedDuration = estimatedDuration;
        this.source = "FALLBACK";
    }

    public SkillGapResponse(
            String targetRole,
            List<String> existingSkills,
            List<String> missingSkills,
            List<String> learningPlan,
            String estimatedDuration,
            String source) {

        this.targetRole = targetRole;
        this.existingSkills = existingSkills;
        this.missingSkills = missingSkills;
        this.learningPlan = learningPlan;
        this.estimatedDuration = estimatedDuration;
        this.source = source;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public List<String> getExistingSkills() {
        return existingSkills;
    }

    public void setExistingSkills(List<String> existingSkills) {
        this.existingSkills = existingSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getLearningPlan() {
        return learningPlan;
    }

    public void setLearningPlan(List<String> learningPlan) {
        this.learningPlan = learningPlan;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(String estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
