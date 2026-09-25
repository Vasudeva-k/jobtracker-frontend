package com.vasudev.jobtracker.service.impl;

import com.vasudev.jobtracker.dto.CompanyAnalyticsResponse;
import com.vasudev.jobtracker.dto.DashboardAnalyticsResponse;
import com.vasudev.jobtracker.dto.DashboardResponse;
import com.vasudev.jobtracker.dto.MonthlyApplicationResponse;
import com.vasudev.jobtracker.dto.RoleAnalyticsResponse;
import com.vasudev.jobtracker.dto.StatusAnalyticsResponse;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final JobApplicationRepository jobRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(
            JobApplicationRepository jobRepository,
            UserRepository userRepository) {

        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DashboardResponse getDashboardStats(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        DashboardResponse response = new DashboardResponse();

        response.setTotalJobs(
                jobRepository.countByUser(user)
        );

        response.setApplied(
                jobRepository.countByUserAndStatus(user, "APPLIED")
        );

        response.setInterview(
                jobRepository.countByUserAndStatus(user, "INTERVIEW")
        );

        response.setOffer(
                jobRepository.countByUserAndStatus(user, "OFFERED")
        );

        response.setRejected(
                jobRepository.countByUserAndStatus(user, "REJECTED")
        );

        response.setSaved(
                jobRepository.countByUserAndStatus(user, "SAVED")
        );

        return response;
    }

    @Override
    public DashboardAnalyticsResponse getDashboardAnalytics(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // ==========================
        // Monthly Applications
        // ==========================

        List<MonthlyApplicationResponse> monthlyApplications =
                jobRepository.getMonthlyApplications(user)
                        .stream()
                        .map(row -> new MonthlyApplicationResponse(
                                Month.of(((Number) row[0]).intValue())
                                        .name(),
                                ((Number) row[1]).longValue()
                        ))
                        .collect(Collectors.toList());


        // ==========================
        // Status Analytics
        // ==========================

        List<StatusAnalyticsResponse> statusAnalytics =
                jobRepository.getStatusAnalytics(user)
                        .stream()
                        .map(row -> new StatusAnalyticsResponse(
                                (String) row[0],
                                ((Number) row[1]).longValue()
                        ))
                        .collect(Collectors.toList());


        // ==========================
        // Top Companies
        // ==========================

        List<CompanyAnalyticsResponse> topCompanies =
                jobRepository.getTopCompanies(user)
                        .stream()
                        .map(row -> new CompanyAnalyticsResponse(
                                (String) row[0],
                                ((Number) row[1]).longValue()
                        ))
                        .collect(Collectors.toList());


        // ==========================
        // Top Job Roles
        // ==========================

        List<RoleAnalyticsResponse> topJobRoles =
                jobRepository.getTopJobRoles(user)
                        .stream()
                        .map(row -> new RoleAnalyticsResponse(
                                (String) row[0],
                                ((Number) row[1]).longValue()
                        ))
                        .collect(Collectors.toList());


        return new DashboardAnalyticsResponse(
                monthlyApplications,
                statusAnalytics,
                topCompanies,
                topJobRoles
        );
    }
}