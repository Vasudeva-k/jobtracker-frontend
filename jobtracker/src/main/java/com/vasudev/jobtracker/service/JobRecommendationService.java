package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.JobRecommendationResponse;

public interface JobRecommendationService {

    JobRecommendationResponse recommendJobs(
            String email
    ) throws Exception;
}
