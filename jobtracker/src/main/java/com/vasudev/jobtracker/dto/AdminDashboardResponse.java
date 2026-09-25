package com.vasudev.jobtracker.dto;

public class AdminDashboardResponse {

    private long totalUsers;
    private long totalJobs;
    private long appliedJobs;
    private long interviewJobs;
    private long offeredJobs;
    private long rejectedJobs;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(long totalUsers,
                                  long totalJobs,
                                  long appliedJobs,
                                  long interviewJobs,
                                  long offeredJobs,
                                  long rejectedJobs) {
        this.totalUsers = totalUsers;
        this.totalJobs = totalJobs;
        this.appliedJobs = appliedJobs;
        this.interviewJobs = interviewJobs;
        this.offeredJobs = offeredJobs;
        this.rejectedJobs = rejectedJobs;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(long totalJobs) {
        this.totalJobs = totalJobs;
    }

    public long getAppliedJobs() {
        return appliedJobs;
    }

    public void setAppliedJobs(long appliedJobs) {
        this.appliedJobs = appliedJobs;
    }

    public long getInterviewJobs() {
        return interviewJobs;
    }

    public void setInterviewJobs(long interviewJobs) {
        this.interviewJobs = interviewJobs;
    }

    public long getOfferedJobs() {
        return offeredJobs;
    }

    public void setOfferedJobs(long offeredJobs) {
        this.offeredJobs = offeredJobs;
    }

    public long getRejectedJobs() {
        return rejectedJobs;
    }

    public void setRejectedJobs(long rejectedJobs) {
        this.rejectedJobs = rejectedJobs;
    }
}
