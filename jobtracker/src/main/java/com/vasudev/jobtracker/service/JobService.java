package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.JobRequest;
import com.vasudev.jobtracker.dto.JobResponse;

import java.time.LocalDate;
import java.util.List;

public interface JobService {

    JobResponse addJob(JobRequest request, String email);

    List<JobResponse> getAllJobs(String email);

    JobResponse getJobById(Long id, String email);

    JobResponse updateJob(Long id, JobRequest request, String email);

    void deleteJob(Long id, String email);

    List<JobResponse> getTodayInterviews(String email);

    List<JobResponse> getInterviewsByDate(LocalDate date, String email);
}