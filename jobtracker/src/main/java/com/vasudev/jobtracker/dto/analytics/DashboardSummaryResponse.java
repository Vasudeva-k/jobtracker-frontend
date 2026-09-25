package com.vasudev.jobtracker.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long totalUsers;

    private long totalApplications;

    private long applied;

    private long interviews;

    private long offers;

    private long rejected;
}
