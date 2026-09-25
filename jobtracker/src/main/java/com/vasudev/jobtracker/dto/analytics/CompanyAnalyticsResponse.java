package com.vasudev.jobtracker.dto.analytics;

public class CompanyAnalyticsResponse {

    private String company;
    private long totalApplications;

    public CompanyAnalyticsResponse() {
    }

    public CompanyAnalyticsResponse(String company, long totalApplications) {
        this.company = company;
        this.totalApplications = totalApplications;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }
}
