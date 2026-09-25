package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.AdminApplicationResponse;

import java.util.List;

public interface AdminApplicationService {

    // Get all applications with user info
    List<AdminApplicationResponse> getAllApplications();

    // Search applications across company, role, status, user email
    List<AdminApplicationResponse> searchApplications(String keyword);

    // Filter applications by status
    List<AdminApplicationResponse> filterByStatus(String status);

    // Get application by ID
    AdminApplicationResponse getApplicationById(Long id);

    // Delete application
    void deleteApplication(Long id);
}