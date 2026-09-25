package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.DashboardAnalyticsResponse;
import com.vasudev.jobtracker.dto.DashboardResponse;
import com.vasudev.jobtracker.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboardStats(
            Authentication authentication) {

        return dashboardService.getDashboardStats(
                authentication.getName()
        );
    }

    @GetMapping("/analytics")
    public DashboardAnalyticsResponse getDashboardAnalytics(
            Authentication authentication) {

        return dashboardService.getDashboardAnalytics(
                authentication.getName()
        );
    }
}