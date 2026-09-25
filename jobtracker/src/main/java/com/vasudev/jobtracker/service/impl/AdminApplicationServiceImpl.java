package com.vasudev.jobtracker.service.impl;

import com.vasudev.jobtracker.dto.AdminApplicationResponse;
import com.vasudev.jobtracker.entity.JobApplication;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.service.AdminApplicationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminApplicationServiceImpl
        implements AdminApplicationService {

    private final JobApplicationRepository jobApplicationRepository;

    public AdminApplicationServiceImpl(
            JobApplicationRepository jobApplicationRepository) {

        this.jobApplicationRepository = jobApplicationRepository;
    }

    // ===============================
    // Get All Applications
    // ===============================
    @Override
    public List<AdminApplicationResponse> getAllApplications() {
        return jobApplicationRepository.findAllApplicationsForAdmin()
                .stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());
    }

    // ===============================
    // Search Applications
    // ===============================
    @Override
    public List<AdminApplicationResponse> searchApplications(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllApplications();
        }
        return jobApplicationRepository.searchApplicationsForAdmin(keyword.trim())
                .stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());
    }

    // ===============================
    // Filter By Status
    // ===============================
    @Override
    public List<AdminApplicationResponse> filterByStatus(String status) {
        if (status == null || status.trim().isEmpty() || status.equalsIgnoreCase("ALL")) {
            return getAllApplications();
        }
        return jobApplicationRepository.findApplicationsByStatusForAdmin(status.trim())
                .stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());
    }

    // ===============================
    // Get Application By ID
    // ===============================
    @Override
    public AdminApplicationResponse getApplicationById(Long id) {

        JobApplication jobApplication =
                jobApplicationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Application not found with id: " + id
                                ));

        return mapToAdminResponse(jobApplication);
    }

    // ===============================
    // Delete Application
    // ===============================
    @Override
    public void deleteApplication(Long id) {

        JobApplication jobApplication =
                jobApplicationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Application not found with id: " + id
                                ));

        jobApplicationRepository.delete(jobApplication);
    }

    // ===============================
    // Map Entity -> AdminApplicationResponse
    // ===============================
    private AdminApplicationResponse mapToAdminResponse(
            JobApplication job) {

        User user = job.getUser();
        Long userId = null;
        String userName = "Unknown";
        String userEmail = "Unknown";

        if (user != null) {
            userId = user.getId();
            userEmail = user.getEmail();
            userName = ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                    (user.getLastName() != null ? user.getLastName() : "")).trim();
            if (userName.isEmpty()) {
                userName = userEmail;
            }
        }

        return AdminApplicationResponse.builder()
                .id(job.getId())
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .companyName(job.getCompanyName())
                .jobTitle(job.getJobTitle())
                .location(job.getLocation())
                .salary(job.getSalary())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .appliedDate(job.getAppliedDate())
                .interviewDate(job.getInterviewDate())
                .interviewTime(job.getInterviewTime())
                .jobLink(job.getJobLink())
                .notes(job.getNotes())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}