package com.vasudev.jobtracker.dto;

import java.util.List;

public class ResumePdfRequest {

    private String fullName;
    private String email;
    private String phone;
    private String summary;

    private List<String> skills;

    private String education;
    private String experience;
    private String projects;

    public ResumePdfRequest() {
    }

    public ResumePdfRequest(String fullName,
                            String email,
                            String phone,
                            String summary,
                            List<String> skills,
                            String education,
                            String experience,
                            String projects) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.summary = summary;
        this.skills = skills;
        this.education = education;
        this.experience = experience;
        this.projects = projects;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    @com.fasterxml.jackson.annotation.JsonSetter("skills")
    public void setSkillsFlexible(com.fasterxml.jackson.databind.JsonNode node) {
        if (node == null || node.isNull()) {
            this.skills = java.util.Collections.emptyList();
        } else if (node.isArray()) {
            java.util.List<String> list = new java.util.ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode elem : node) {
                list.add(elem.asText());
            }
            this.skills = list;
        } else if (node.isTextual()) {
            this.skills = java.util.Arrays.stream(node.asText().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }
}
