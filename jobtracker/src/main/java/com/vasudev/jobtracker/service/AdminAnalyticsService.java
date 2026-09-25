package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.analytics.CompanyAnalyticsResponse;
import com.vasudev.jobtracker.dto.analytics.DashboardSummaryResponse;
import com.vasudev.jobtracker.dto.analytics.JobRoleAnalyticsResponse;
import com.vasudev.jobtracker.dto.analytics.JobStatusAnalyticsResponse;
import com.vasudev.jobtracker.dto.analytics.MonthlyJobAnalyticsResponse;
import com.vasudev.jobtracker.dto.analytics.UserAnalyticsResponse;

import java.util.List;

public interface AdminAnalyticsService {

    List<MonthlyJobAnalyticsResponse> getMonthlyJobAnalytics();

    List<JobStatusAnalyticsResponse> getJobStatusAnalytics();

    List<CompanyAnalyticsResponse> getTopCompanies();

    List<UserAnalyticsResponse> getMonthlyUserRegistrations();

    List<JobRoleAnalyticsResponse> getTopJobRoles();

    // Admin Dashboard Summary
    DashboardSummaryResponse getDashboardSummary();
}