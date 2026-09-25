package com.vasudev.jobtracker.service.impl;

import com.vasudev.jobtracker.dto.JobRequest;
import com.vasudev.jobtracker.dto.JobResponse;
import com.vasudev.jobtracker.entity.JobApplication;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.JobService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    private final JobApplicationRepository jobRepository;
    private final UserRepository userRepository;

    public JobServiceImpl(
            JobApplicationRepository jobRepository,
            UserRepository userRepository) {

        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    // ==============================
    // Add Job Application
    // ==============================

    @Override
    public JobResponse addJob(JobRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        JobApplication job = new JobApplication();

        job.setCompanyName(request.getCompanyName());
        job.setJobTitle(request.getJobRole());
        job.setJobDescription(request.getJobDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setJobType(request.getJobType());
        job.setJobLink(request.getJobLink());

        job.setStatus(
                request.getStatus() == null ||
                        request.getStatus().isBlank()
                        ? "APPLIED"
                        : request.getStatus()
        );

        if (request.getAppliedDate() != null) {
            job.setAppliedDate(request.getAppliedDate());
        }

        job.setNotes(request.getNotes());

        job.setInterviewDate(request.getInterviewDate());
        job.setInterviewTime(request.getInterviewTime());

        job.setUser(user);

        JobApplication savedJob =
                jobRepository.save(job);

        return mapToResponse(savedJob);
    }


    // ==============================
    // Get All User Jobs
    // ==============================

    @Override
    public List<JobResponse> getAllJobs(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return jobRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // ==============================
    // Get Job By ID
    // ==============================

    @Override
    public JobResponse getJobById(
            Long id,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        JobApplication job =
                jobRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Job not found"));

        if (!job.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(job);
    }


    // ==============================
    // Update Job Application
    // ==============================

    @Override
    public JobResponse updateJob(
            Long id,
            JobRequest request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        JobApplication job =
                jobRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Job not found"));

        if (!job.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        job.setCompanyName(request.getCompanyName());
        job.setJobTitle(request.getJobRole());
        job.setJobDescription(request.getJobDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setJobType(request.getJobType());
        job.setJobLink(request.getJobLink());

        if (request.getStatus() != null &&
                !request.getStatus().isBlank()) {

            job.setStatus(request.getStatus());
        }

        if (request.getAppliedDate() != null) {
            job.setAppliedDate(request.getAppliedDate());
        }

        job.setNotes(request.getNotes());

        job.setInterviewDate(request.getInterviewDate());
        job.setInterviewTime(request.getInterviewTime());

        JobApplication updatedJob =
                jobRepository.save(job);

        return mapToResponse(updatedJob);
    }


    // ==============================
    // Delete Job Application
    // ==============================

    @Override
    public void deleteJob(
            Long id,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        JobApplication job =
                jobRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Job not found"));

        if (!job.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        jobRepository.delete(job);
    }


    // ==============================
    // Today's Interviews
    // ==============================

    @Override
    public List<JobResponse> getTodayInterviews(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return jobRepository.findTodayInterviewsByUser(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // ==============================
    // Interviews By Date
    // ==============================

    @Override
    public List<JobResponse> getInterviewsByDate(
            LocalDate date,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return jobRepository.findByInterviewDateAndUser(date, user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // ==============================
    // Entity → Response Mapping
    // ==============================

    private JobResponse mapToResponse(
            JobApplication job) {

        JobResponse response =
                new JobResponse();

        response.setId(job.getId());

        response.setCompanyName(
                job.getCompanyName());

        response.setJobRole(
                job.getJobTitle());

        response.setJobDescription(
                job.getJobDescription());

        response.setLocation(
                job.getLocation());

        response.setSalary(
                job.getSalary());

        response.setJobType(
                job.getJobType());

        response.setJobLink(
                job.getJobLink());

        response.setStatus(
                job.getStatus());

        response.setNotes(
                job.getNotes());

        response.setAppliedDate(
                job.getAppliedDate());

        response.setInterviewDate(
                job.getInterviewDate());

        response.setInterviewTime(
                job.getInterviewTime());

        response.setCreatedAt(
                job.getCreatedAt());

        response.setUpdatedAt(
                job.getUpdatedAt());

        return response;
    }
}