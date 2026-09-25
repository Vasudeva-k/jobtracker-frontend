package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.JobRequest;
import com.vasudev.jobtracker.dto.JobResponse;
import com.vasudev.jobtracker.service.JobService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public JobResponse addJob(
            @Valid @RequestBody JobRequest request,
            Authentication authentication) {

        return jobService.addJob(
                request,
                authentication.getName()
        );
    }

    @GetMapping
    public List<JobResponse> getAllJobs(
            Authentication authentication) {

        return jobService.getAllJobs(
                authentication.getName()
        );
    }

    @GetMapping("/{id}")
    public JobResponse getJob(
            @PathVariable Long id,
            Authentication authentication) {

        return jobService.getJobById(
                id,
                authentication.getName()
        );
    }

    @PutMapping("/{id}")
    public JobResponse updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request,
            Authentication authentication) {

        return jobService.updateJob(
                id,
                request,
                authentication.getName()
        );
    }

    @DeleteMapping("/{id}")
    public String deleteJob(
            @PathVariable Long id,
            Authentication authentication) {

        jobService.deleteJob(
                id,
                authentication.getName()
        );

        return "Job deleted successfully";
    }

    // ===============================
    // Today's Interviews
    // ===============================
    @GetMapping("/interviews/today")
    public List<JobResponse> getTodayInterviews(
            Authentication authentication) {

        return jobService.getTodayInterviews(
                authentication.getName()
        );
    }

    // ===============================
    // Interviews by Date
    // ===============================
    @GetMapping("/interviews/date/{date}")
    public List<JobResponse> getInterviewsByDate(
            @PathVariable LocalDate date,
            Authentication authentication) {

        return jobService.getInterviewsByDate(
                date,
                authentication.getName()
        );
    }
}