package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.ai.ResumeJobMatcher;
import com.vasudev.jobtracker.dto.JobMatchRequest;
import com.vasudev.jobtracker.dto.JobMatchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class ResumeJobMatcherController {

    private final ResumeJobMatcher resumeJobMatcher;

    public ResumeJobMatcherController(ResumeJobMatcher resumeJobMatcher) {
        this.resumeJobMatcher = resumeJobMatcher;
    }

    @PostMapping("/job-match")
    public ResponseEntity<JobMatchResponse> matchJob(
            @RequestBody JobMatchRequest request) {

        if (request == null
                || request.getResumeText() == null
                || request.getResumeText().isBlank()
                || request.getJobDescription() == null
                || request.getJobDescription().isBlank()) {

            return ResponseEntity.badRequest().build();
        }

        JobMatchResponse response =
                resumeJobMatcher.match(request);

        return ResponseEntity.ok(response);
    }
}