package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.analytics.CompanyAnalyticsResponse;
import com.vasudev.jobtracker.dto.analytics.DashboardSummaryResponse;
import com.vasudev.jobtracker.dto.analytics.JobRoleAnalyticsResponse;
import com.vasudev.jobtracker.dto.analytics.JobStatusAnalyticsResponse;
import com.vasudev.jobtracker.dto.analytics.MonthlyJobAnalyticsResponse;
import com.vasudev.jobtracker.dto.analytics.UserAnalyticsResponse;
import com.vasudev.jobtracker.service.AdminAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    public AdminAnalyticsController(
            AdminAnalyticsService adminAnalyticsService) {

        this.adminAnalyticsService = adminAnalyticsService;
    }

    // ===============================
    // Dashboard Summary
    // ===============================
    @GetMapping("/summary")
    public DashboardSummaryResponse getDashboardSummary() {
        return adminAnalyticsService.getDashboardSummary();
    }

    // ===============================
    // Monthly Job Applications
    // ===============================
    @GetMapping("/monthly-jobs")
    public List<MonthlyJobAnalyticsResponse> getMonthlyJobs() {
        return adminAnalyticsService.getMonthlyJobAnalytics();
    }

    // ===============================
    // Job Status Analytics
    // ===============================
    @GetMapping("/job-status")
    public List<JobStatusAnalyticsResponse> getJobStatus() {
        return adminAnalyticsService.getJobStatusAnalytics();
    }

    // ===============================
    // Top Companies
    // ===============================
    @GetMapping("/top-companies")
    public List<CompanyAnalyticsResponse> getTopCompanies() {
        return adminAnalyticsService.getTopCompanies();
    }

    // ===============================
    // Monthly User Registrations
    // ===============================
    @GetMapping("/monthly-users")
    public List<UserAnalyticsResponse> getMonthlyUsers() {
        return adminAnalyticsService.getMonthlyUserRegistrations();
    }

    // ===============================
    // Top Job Roles
    // ===============================
    @GetMapping("/top-job-roles")
    public List<JobRoleAnalyticsResponse> getTopJobRoles() {
        return adminAnalyticsService.getTopJobRoles();
    }
}