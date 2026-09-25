package com.vasudev.jobtracker.dto.analytics;

public class MonthlyJobAnalyticsResponse {

    private String month;
    private long totalApplications;

    public MonthlyJobAnalyticsResponse() {
    }

    public MonthlyJobAnalyticsResponse(String month, long totalApplications) {
        this.month = month;
        this.totalApplications = totalApplications;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }
}
