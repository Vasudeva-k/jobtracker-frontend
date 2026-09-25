package com.vasudev.jobtracker.dto;

public class CompanyInterviewRequest {

    private String company;
    private String role;

    public CompanyInterviewRequest() {
    }

    public CompanyInterviewRequest(String company, String role) {
        this.company = company;
        this.role = role;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}