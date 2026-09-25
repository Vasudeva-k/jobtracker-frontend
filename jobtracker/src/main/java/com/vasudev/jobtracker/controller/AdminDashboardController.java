package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.AdminDashboardResponse;
import com.vasudev.jobtracker.service.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboardStatistics() {
        return adminDashboardService.getDashboardStatistics();
    }
}
