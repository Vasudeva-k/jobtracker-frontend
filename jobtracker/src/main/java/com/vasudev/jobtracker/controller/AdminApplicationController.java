package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.AdminApplicationResponse;
import com.vasudev.jobtracker.service.AdminApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/applications")
public class AdminApplicationController {

    private final AdminApplicationService adminApplicationService;

    public AdminApplicationController(
            AdminApplicationService adminApplicationService) {

        this.adminApplicationService = adminApplicationService;
    }

    // ===============================
    // Get All Applications (supports optional status query)
    // ===============================
    @GetMapping
    public List<AdminApplicationResponse> getAllApplications(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return adminApplicationService.filterByStatus(status);
        }
        return adminApplicationService.getAllApplications();
    }

    // ===============================
    // Search Applications
    // ===============================
    @GetMapping("/search")
    public List<AdminApplicationResponse> searchApplications(
            @RequestParam String keyword) {
        return adminApplicationService.searchApplications(keyword);
    }

    // ===============================
    // Filter By Status (query param or path variable)
    // ===============================
    @GetMapping("/status")
    public List<AdminApplicationResponse> filterByStatusQuery(
            @RequestParam String status) {
        return adminApplicationService.filterByStatus(status);
    }

    @GetMapping("/status/{status}")
    public List<AdminApplicationResponse> filterByStatus(
            @PathVariable String status) {
        return adminApplicationService.filterByStatus(status);
    }

    // ===============================
    // Get Application By ID
    // ===============================
    @GetMapping("/{id}")
    public AdminApplicationResponse getApplicationById(
            @PathVariable Long id) {

        return adminApplicationService.getApplicationById(id);
    }

    // ===============================
    // Delete Application
    // ===============================
    @DeleteMapping("/{id}")
    public String deleteApplication(
            @PathVariable Long id) {

        adminApplicationService.deleteApplication(id);

        return "Application deleted successfully";
    }
}