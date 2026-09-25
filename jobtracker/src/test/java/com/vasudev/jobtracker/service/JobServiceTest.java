package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.JobRequest;
import com.vasudev.jobtracker.dto.JobResponse;
import com.vasudev.jobtracker.entity.JobApplication;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.impl.JobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobApplicationRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobServiceImpl jobService;

    private User userA;
    private User userB;
    private JobApplication jobA;
    private JobApplication jobB;
    private JobRequest jobRequest;

    @BeforeEach
    void setUp() {
        userA = User.builder()
                .id(1L)
                .email("usera@example.com")
                .firstName("User")
                .lastName("A")
                .role("USER")
                .active(true)
                .build();

        userB = User.builder()
                .id(2L)
                .email("userb@example.com")
                .firstName("User")
                .lastName("B")
                .role("USER")
                .active(true)
                .build();

        jobA = JobApplication.builder()
                .id(100L)
                .companyName("Google")
                .jobTitle("Backend Engineer")
                .jobDescription("Java / Spring Boot role")
                .location("Bengaluru")
                .salary(new BigDecimal("2500000"))
                .jobType("Full Time")
                .status("APPLIED")
                .appliedDate(LocalDate.now())
                .user(userA)
                .build();

        jobB = JobApplication.builder()
                .id(200L)
                .companyName("Microsoft")
                .jobTitle("Cloud Engineer")
                .location("Hyderabad")
                .salary(new BigDecimal("2800000"))
                .jobType("Full Time")
                .status("INTERVIEW")
                .interviewDate(LocalDate.now())
                .interviewTime("11:00 AM")
                .appliedDate(LocalDate.now())
                .user(userB)
                .build();

        jobRequest = JobRequest.builder()
                .companyName("Google")
                .jobRole("Backend Engineer")
                .jobDescription("Java / Spring Boot role")
                .location("Bengaluru")
                .salary(new BigDecimal("2500000"))
                .jobType("Full Time")
                .status("APPLIED")
                .appliedDate(LocalDate.now())
                .notes("Referred by alum")
                .build();
    }

    @Test
    void testAddJobSuccess() {
        when(userRepository.findByEmail("usera@example.com")).thenReturn(Optional.of(userA));
        when(jobRepository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        JobResponse response = jobService.addJob(jobRequest, "usera@example.com");

        assertNotNull(response);
        assertEquals("Google", response.getCompanyName());
        assertEquals("Backend Engineer", response.getJobRole());
        assertEquals("APPLIED", response.getStatus());
        verify(jobRepository, times(1)).save(any(JobApplication.class));
    }

    @Test
    void testGetAllJobsForUser() {
        when(userRepository.findByEmail("usera@example.com")).thenReturn(Optional.of(userA));
        when(jobRepository.findByUser(userA)).thenReturn(List.of(jobA));

        List<JobResponse> jobs = jobService.getAllJobs("usera@example.com");

        assertEquals(1, jobs.size());
        assertEquals("Google", jobs.get(0).getCompanyName());
    }

    @Test
    void testGetJobByIdOwned() {
        when(userRepository.findByEmail("usera@example.com")).thenReturn(Optional.of(userA));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(jobA));

        JobResponse response = jobService.getJobById(100L, "usera@example.com");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Google", response.getCompanyName());
    }

    @Test
    void testGetJobByIdOtherUserDenied() {
        when(userRepository.findByEmail("usera@example.com")).thenReturn(Optional.of(userA));
        when(jobRepository.findById(200L)).thenReturn(Optional.of(jobB)); // owned by userB

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                jobService.getJobById(200L, "usera@example.com")
        );

        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void testUpdateJobOwnedSuccess() {
        when(userRepository.findByEmail("usera@example.com")).thenReturn(Optional.of(userA));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(jobA));
        when(jobRepository.save(any(JobApplication.class))).thenReturn(jobA);

        jobRequest.setStatus("INTERVIEW");
        jobRequest.setInterviewDate(LocalDate.now().plusDays(3));
        jobRequest.setInterviewTime("2:00 PM");

        JobResponse response = jobService.updateJob(100L, jobRequest, "usera@example.com");

        assertNotNull(response);
        assertEquals("INTERVIEW", jobA.getStatus());
        verify(jobRepository, times(1)).save(jobA);
    }

    @Test
    void testUpdateJobOtherUserDenied() {
        when(userRepository.findByEmail("usera@example.com")).thenReturn(Optional.of(userA));
        when(jobRepository.findById(200L)).thenReturn(Optional.of(jobB)); // owned by userB

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                jobService.updateJob(200L, jobRequest, "usera@example.com")
        );

        assertEquals("Access denied", ex.getMessage());
        verify(jobRepository, never()).save(any(JobApplication.class));
    }

    @Test
    void testDeleteJobOwnedSuccess() {
        when(userRepository.findByEmail("usera@example.com")).thenReturn(Optional.of(userA));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(jobA));

        jobService.deleteJob(100L, "usera@example.com");

        verify(jobRepository, times(1)).delete(jobA);
    }

    @Test
    void testDeleteJobOtherUserDenied() {
        when(userRepository.findByEmail("usera@example.com")).thenReturn(Optional.of(userA));
        when(jobRepository.findById(200L)).thenReturn(Optional.of(jobB)); // owned by userB

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                jobService.deleteJob(200L, "usera@example.com")
        );

        assertEquals("Access denied", ex.getMessage());
        verify(jobRepository, never()).delete(any());
    }

    @Test
    void testGetTodayInterviewsScopedToUser() {
        when(userRepository.findByEmail("userb@example.com")).thenReturn(Optional.of(userB));
        when(jobRepository.findTodayInterviewsByUser(userB)).thenReturn(List.of(jobB));

        List<JobResponse> interviews = jobService.getTodayInterviews("userb@example.com");

        assertEquals(1, interviews.size());
        assertEquals("Microsoft", interviews.get(0).getCompanyName());
    }

    @Test
    void testGetInterviewsByDateScopedToUser() {
        LocalDate today = LocalDate.now();
        when(userRepository.findByEmail("userb@example.com")).thenReturn(Optional.of(userB));
        when(jobRepository.findByInterviewDateAndUser(today, userB)).thenReturn(List.of(jobB));

        List<JobResponse> interviews = jobService.getInterviewsByDate(today, "userb@example.com");

        assertEquals(1, interviews.size());
        assertEquals("Microsoft", interviews.get(0).getCompanyName());
    }
}
