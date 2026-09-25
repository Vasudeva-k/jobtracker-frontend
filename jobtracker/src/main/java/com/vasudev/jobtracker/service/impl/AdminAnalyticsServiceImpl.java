package com.vasudev.jobtracker.service.impl;

import com.vasudev.jobtracker.dto.analytics.*;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.AdminAnalyticsService;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;

    public AdminAnalyticsServiceImpl(
            JobApplicationRepository jobApplicationRepository,
            UserRepository userRepository) {

        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<MonthlyJobAnalyticsResponse> getMonthlyJobAnalytics() {

        List<Object[]> result =
                jobApplicationRepository.getMonthlyApplications();

        List<MonthlyJobAnalyticsResponse> response =
                new ArrayList<>();

        for (Object[] row : result) {

            if (row[0] != null && row[1] != null) {
                int month = ((Number) row[0]).intValue();
                long count = ((Number) row[1]).longValue();

                if (month >= 1 && month <= 12) {
                    response.add(
                            new MonthlyJobAnalyticsResponse(
                                    Month.of(month).name(),
                                    count
                            )
                    );
                }
            }
        }

        return response;
    }

    @Override
    public List<JobStatusAnalyticsResponse> getJobStatusAnalytics() {

        List<Object[]> result =
                jobApplicationRepository.getStatusAnalytics();

        List<JobStatusAnalyticsResponse> response =
                new ArrayList<>();

        for (Object[] row : result) {

            if (row[0] != null && row[1] != null) {
                response.add(
                        new JobStatusAnalyticsResponse(
                                row[0].toString(),
                                ((Number) row[1]).longValue()
                        )
                );
            }
        }

        return response;
    }

    @Override
    public List<CompanyAnalyticsResponse> getTopCompanies() {

        List<Object[]> result =
                jobApplicationRepository.getTopCompanies();

        List<CompanyAnalyticsResponse> response =
                new ArrayList<>();

        for (Object[] row : result) {

            if (row[0] != null && row[1] != null) {
                response.add(
                        new CompanyAnalyticsResponse(
                                row[0].toString(),
                                ((Number) row[1]).longValue()
                        )
                );
            }
        }

        return response;
    }

    @Override
    public List<UserAnalyticsResponse> getMonthlyUserRegistrations() {

        List<Object[]> result =
                userRepository.getMonthlyRegistrations();

        List<UserAnalyticsResponse> response =
                new ArrayList<>();

        for (Object[] row : result) {

            if (row[0] != null && row[1] != null) {
                int month = ((Number) row[0]).intValue();
                long count = ((Number) row[1]).longValue();

                if (month >= 1 && month <= 12) {
                    response.add(
                            new UserAnalyticsResponse(
                                    Month.of(month).name(),
                                    count
                            )
                    );
                }
            }
        }

        return response;
    }

    @Override
    public List<JobRoleAnalyticsResponse> getTopJobRoles() {

        List<Object[]> result =
                jobApplicationRepository.getTopJobRoles();

        List<JobRoleAnalyticsResponse> response =
                new ArrayList<>();

        for (Object[] row : result) {

            if (row[0] != null && row[1] != null) {
                response.add(
                        new JobRoleAnalyticsResponse(
                                row[0].toString(),
                                ((Number) row[1]).longValue()
                        )
                );
            }
        }

        return response;
    }

    // ==========================================
    // Admin Dashboard Summary
    // ==========================================

    @Override
    public DashboardSummaryResponse getDashboardSummary() {

        long totalUsers =
                userRepository.count();

        long totalApplications =
                jobApplicationRepository.count();

        long applied =
                jobApplicationRepository
                        .countByStatusIgnoreCase("APPLIED");

        long interviews =
                jobApplicationRepository
                        .countByStatusIgnoreCase("INTERVIEW");

        long offers =
                jobApplicationRepository
                        .countByStatusIgnoreCase("OFFERED");

        long rejected =
                jobApplicationRepository
                        .countByStatusIgnoreCase("REJECTED");

        return new DashboardSummaryResponse(
                totalUsers,
                totalApplications,
                applied,
                interviews,
                offers,
                rejected
        );
    }
}