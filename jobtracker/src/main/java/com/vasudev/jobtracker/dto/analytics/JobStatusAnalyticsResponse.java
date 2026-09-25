package com.vasudev.jobtracker.dto.analytics;

public class JobStatusAnalyticsResponse {

    private String status;
    private long count;

    public JobStatusAnalyticsResponse() {
    }

    public JobStatusAnalyticsResponse(String status, long count) {
        this.status = status;
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
