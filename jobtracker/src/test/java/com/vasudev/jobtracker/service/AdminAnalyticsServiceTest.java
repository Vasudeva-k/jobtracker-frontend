package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.analytics.*;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.impl.AdminAnalyticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminAnalyticsServiceImpl adminAnalyticsService;

    @Test
    void testGetDashboardSummary() {
        when(userRepository.count()).thenReturn(25L);
        when(jobApplicationRepository.count()).thenReturn(100L);
        when(jobApplicationRepository.countByStatusIgnoreCase("APPLIED")).thenReturn(40L);
        when(jobApplicationRepository.countByStatusIgnoreCase("INTERVIEW")).thenReturn(25L);
        when(jobApplicationRepository.countByStatusIgnoreCase("OFFERED")).thenReturn(15L);
        when(jobApplicationRepository.countByStatusIgnoreCase("REJECTED")).thenReturn(20L);

        DashboardSummaryResponse response = adminAnalyticsService.getDashboardSummary();

        assertNotNull(response);
        assertEquals(25L, response.getTotalUsers());
        assertEquals(100L, response.getTotalApplications());
        assertEquals(40L, response.getApplied());
        assertEquals(25L, response.getInterviews());
        assertEquals(15L, response.getOffers());
        assertEquals(20L, response.getRejected());
    }

    @Test
    void testGetMonthlyJobAnalytics() {
        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[]{1, 15L});
        queryResult.add(new Object[]{2, 22L});
        when(jobApplicationRepository.getMonthlyApplications()).thenReturn(queryResult);

        List<MonthlyJobAnalyticsResponse> responses = adminAnalyticsService.getMonthlyJobAnalytics();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("JANUARY", responses.get(0).getMonth());
        assertEquals(15L, responses.get(0).getTotalApplications());
        assertEquals("FEBRUARY", responses.get(1).getMonth());
        assertEquals(22L, responses.get(1).getTotalApplications());
    }

    @Test
    void testGetJobStatusAnalytics() {
        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[]{"APPLIED", 45L});
        queryResult.add(new Object[]{"OFFERED", 10L});
        when(jobApplicationRepository.getStatusAnalytics()).thenReturn(queryResult);

        List<JobStatusAnalyticsResponse> responses = adminAnalyticsService.getJobStatusAnalytics();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("APPLIED", responses.get(0).getStatus());
        assertEquals(45L, responses.get(0).getCount());
    }

    @Test
    void testGetTopCompanies() {
        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[]{"Google", 12L});
        queryResult.add(new Object[]{"Microsoft", 9L});
        when(jobApplicationRepository.getTopCompanies()).thenReturn(queryResult);

        List<CompanyAnalyticsResponse> responses = adminAnalyticsService.getTopCompanies();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Google", responses.get(0).getCompany());
        assertEquals(12L, responses.get(0).getTotalApplications());
    }

    @Test
    void testGetMonthlyUserRegistrations() {
        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[]{3, 8L});
        when(userRepository.getMonthlyRegistrations()).thenReturn(queryResult);

        List<UserAnalyticsResponse> responses = adminAnalyticsService.getMonthlyUserRegistrations();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("MARCH", responses.get(0).getMonth());
        assertEquals(8L, responses.get(0).getUsers());
    }

    @Test
    void testGetTopJobRoles() {
        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[]{"Software Engineer", 18L});
        when(jobApplicationRepository.getTopJobRoles()).thenReturn(queryResult);

        List<JobRoleAnalyticsResponse> responses = adminAnalyticsService.getTopJobRoles();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Software Engineer", responses.get(0).getRole());
        assertEquals(18L, responses.get(0).getCount());
    }
}
