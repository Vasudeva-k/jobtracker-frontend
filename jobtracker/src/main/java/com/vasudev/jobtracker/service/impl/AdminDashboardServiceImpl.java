package com.vasudev.jobtracker.service.impl;

import com.vasudev.jobtracker.dto.AdminDashboardResponse;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.AdminDashboardService;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public AdminDashboardServiceImpl(
            UserRepository userRepository,
            JobApplicationRepository jobApplicationRepository) {

        this.userRepository = userRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @Override
    public AdminDashboardResponse getDashboardStatistics() {

        long totalUsers = userRepository.count();
        long totalJobs = jobApplicationRepository.count();

        long appliedJobs = jobApplicationRepository.countByStatusIgnoreCase("APPLIED");
        long interviewJobs = jobApplicationRepository.countByStatusIgnoreCase("INTERVIEW");
        long offeredJobs = jobApplicationRepository.countByStatusIgnoreCase("OFFERED");
        long rejectedJobs = jobApplicationRepository.countByStatusIgnoreCase("REJECTED");

        return new AdminDashboardResponse(
                totalUsers,
                totalJobs,
                appliedJobs,
                interviewJobs,
                offeredJobs,
                rejectedJobs
        );
    }
}