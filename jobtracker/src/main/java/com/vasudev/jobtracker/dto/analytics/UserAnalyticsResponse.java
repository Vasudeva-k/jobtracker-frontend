package com.vasudev.jobtracker.dto.analytics;

public class UserAnalyticsResponse {

    private String month;
    private long users;

    public UserAnalyticsResponse() {
    }

    public UserAnalyticsResponse(String month, long users) {
        this.month = month;
        this.users = users;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getUsers() {
        return users;
    }

    public void setUsers(long users) {
        this.users = users;
    }
}
