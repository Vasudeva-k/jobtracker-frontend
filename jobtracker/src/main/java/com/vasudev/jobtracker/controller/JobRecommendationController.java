package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.JobRecommendationResponse;
import com.vasudev.jobtracker.service.JobRecommendationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-recommendations")
public class JobRecommendationController {

    private final JobRecommendationService jobRecommendationService;

    public JobRecommendationController(
            JobRecommendationService jobRecommendationService) {

        this.jobRecommendationService = jobRecommendationService;
    }

    @GetMapping
    public JobRecommendationResponse recommendJobs(
            Authentication authentication) throws Exception {

        return jobRecommendationService.recommendJobs(
                authentication.getName()
        );
    }
}
