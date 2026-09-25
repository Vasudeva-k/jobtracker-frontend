package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.AdminDashboardResponse;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.impl.AdminDashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    @Test
    void testGetDashboardStatisticsWithData() {
        when(userRepository.count()).thenReturn(50L);
        when(jobApplicationRepository.count()).thenReturn(200L);
        when(jobApplicationRepository.countByStatusIgnoreCase("APPLIED")).thenReturn(80L);
        when(jobApplicationRepository.countByStatusIgnoreCase("INTERVIEW")).thenReturn(40L);
        when(jobApplicationRepository.countByStatusIgnoreCase("OFFERED")).thenReturn(20L);
        when(jobApplicationRepository.countByStatusIgnoreCase("REJECTED")).thenReturn(60L);

        AdminDashboardResponse response = adminDashboardService.getDashboardStatistics();

        assertNotNull(response);
        assertEquals(50L, response.getTotalUsers());
        assertEquals(200L, response.getTotalJobs());
        assertEquals(80L, response.getAppliedJobs());
        assertEquals(40L, response.getInterviewJobs());
        assertEquals(20L, response.getOfferedJobs());
        assertEquals(60L, response.getRejectedJobs());
    }

    @Test
    void testGetDashboardStatisticsEmptyDatabase() {
        when(userRepository.count()).thenReturn(0L);
        when(jobApplicationRepository.count()).thenReturn(0L);
        when(jobApplicationRepository.countByStatusIgnoreCase("APPLIED")).thenReturn(0L);
        when(jobApplicationRepository.countByStatusIgnoreCase("INTERVIEW")).thenReturn(0L);
        when(jobApplicationRepository.countByStatusIgnoreCase("OFFERED")).thenReturn(0L);
        when(jobApplicationRepository.countByStatusIgnoreCase("REJECTED")).thenReturn(0L);

        AdminDashboardResponse response = adminDashboardService.getDashboardStatistics();

        assertNotNull(response);
        assertEquals(0L, response.getTotalUsers());
        assertEquals(0L, response.getTotalJobs());
    }
}
