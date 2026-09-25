package com.vasudev.jobtracker.dto;

public class JobMarketTrendRequest {

    private String role;

    public JobMarketTrendRequest() {
    }

    public JobMarketTrendRequest(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
