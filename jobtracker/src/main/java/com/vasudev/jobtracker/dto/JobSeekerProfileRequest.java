package com.vasudev.jobtracker.dto;

import java.util.List;

public class JobSeekerProfileRequest {

    private Integer atsScore;
    private Integer yearsOfExperience;
    private Integer projects;
    private Integer certifications;
    private List<String> skills;

    public JobSeekerProfileRequest() {
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public Integer getProjects() {
        return projects;
    }

    public void setProjects(Integer projects) {
        this.projects = projects;
    }

    public Integer getCertifications() {
        return certifications;
    }

    public void setCertifications(Integer certifications) {
        this.certifications = certifications;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}
