package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.DashboardResponse;
import com.vasudev.jobtracker.dto.DashboardAnalyticsResponse;

public interface DashboardService {

    DashboardResponse getDashboardStats(String email);

    DashboardAnalyticsResponse getDashboardAnalytics(String email);
}