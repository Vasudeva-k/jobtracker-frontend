package com.vasudev.jobtracker.dto;

import java.util.List;

public class JobSuccessPredictionRequest {

    private int atsScore;
    private int yearsOfExperience;
    private int projects;
    private int certifications;

    private List<String> skills;

    public JobSuccessPredictionRequest() {
    }

    public JobSuccessPredictionRequest(
            int atsScore,
            int yearsOfExperience,
            int projects,
            int certifications,
            List<String> skills) {

        this.atsScore = atsScore;
        this.yearsOfExperience = yearsOfExperience;
        this.projects = projects;
        this.certifications = certifications;
        this.skills = skills;
    }

    public int getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public int getProjects() {
        return projects;
    }

    public void setProjects(int projects) {
        this.projects = projects;
    }

    public int getCertifications() {
        return certifications;
    }

    public void setCertifications(int certifications) {
        this.certifications = certifications;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}