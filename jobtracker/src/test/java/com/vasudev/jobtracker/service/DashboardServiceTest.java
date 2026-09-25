package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.DashboardAnalyticsResponse;
import com.vasudev.jobtracker.dto.DashboardResponse;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private JobApplicationRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .role("USER")
                .active(true)
                .build();
    }

    @Test
    void testGetDashboardStatsSuccess() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jobRepository.countByUser(user)).thenReturn(10L);
        when(jobRepository.countByUserAndStatus(user, "APPLIED")).thenReturn(4L);
        when(jobRepository.countByUserAndStatus(user, "INTERVIEW")).thenReturn(3L);
        when(jobRepository.countByUserAndStatus(user, "OFFERED")).thenReturn(1L);
        when(jobRepository.countByUserAndStatus(user, "REJECTED")).thenReturn(1L);
        when(jobRepository.countByUserAndStatus(user, "SAVED")).thenReturn(1L);

        DashboardResponse stats = dashboardService.getDashboardStats("user@example.com");

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalJobs());
        assertEquals(4L, stats.getApplied());
        assertEquals(3L, stats.getInterview());
        assertEquals(1L, stats.getOffer());
        assertEquals(1L, stats.getRejected());
        assertEquals(1L, stats.getSaved());

        verify(jobRepository, times(1)).countByUser(user);
        verify(jobRepository, times(1)).countByUserAndStatus(user, "APPLIED");
    }

    @Test
    void testGetDashboardStatsUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                dashboardService.getDashboardStats("unknown@example.com")
        );
    }

    @Test
    void testGetDashboardAnalyticsSuccess() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        List<Object[]> monthlyData = new ArrayList<>();
        monthlyData.add(new Object[]{1, 5L});
        monthlyData.add(new Object[]{2, 8L});

        List<Object[]> statusData = new ArrayList<>();
        statusData.add(new Object[]{"APPLIED", 5L});
        statusData.add(new Object[]{"INTERVIEW", 3L});

        List<Object[]> companyData = new ArrayList<>();
        companyData.add(new Object[]{"Google", 4L});
        companyData.add(new Object[]{"Amazon", 3L});

        List<Object[]> roleData = new ArrayList<>();
        roleData.add(new Object[]{"Software Engineer", 6L});

        when(jobRepository.getMonthlyApplications(user)).thenReturn(monthlyData);
        when(jobRepository.getStatusAnalytics(user)).thenReturn(statusData);
        when(jobRepository.getTopCompanies(user)).thenReturn(companyData);
        when(jobRepository.getTopJobRoles(user)).thenReturn(roleData);

        DashboardAnalyticsResponse analytics = dashboardService.getDashboardAnalytics("user@example.com");

        assertNotNull(analytics);
        assertEquals(2, analytics.getMonthlyApplications().size());
        assertEquals("JANUARY", analytics.getMonthlyApplications().get(0).getMonth());
        assertEquals(5L, analytics.getMonthlyApplications().get(0).getApplications());

        assertEquals(2, analytics.getStatusAnalytics().size());
        assertEquals("APPLIED", analytics.getStatusAnalytics().get(0).getStatus());
        assertEquals(5L, analytics.getStatusAnalytics().get(0).getCount());

        assertEquals(2, analytics.getTopCompanies().size());
        assertEquals("Google", analytics.getTopCompanies().get(0).getCompany());
        assertEquals(4L, analytics.getTopCompanies().get(0).getApplications());

        assertEquals(1, analytics.getTopJobRoles().size());
        assertEquals("Software Engineer", analytics.getTopJobRoles().get(0).getRole());
        assertEquals(6L, analytics.getTopJobRoles().get(0).getCount());
    }

    @Test
    void testGetDashboardAnalyticsEmptyData() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jobRepository.getMonthlyApplications(user)).thenReturn(Collections.emptyList());
        when(jobRepository.getStatusAnalytics(user)).thenReturn(Collections.emptyList());
        when(jobRepository.getTopCompanies(user)).thenReturn(Collections.emptyList());
        when(jobRepository.getTopJobRoles(user)).thenReturn(Collections.emptyList());

        DashboardAnalyticsResponse analytics = dashboardService.getDashboardAnalytics("user@example.com");

        assertNotNull(analytics);
        assertTrue(analytics.getMonthlyApplications().isEmpty());
        assertTrue(analytics.getStatusAnalytics().isEmpty());
        assertTrue(analytics.getTopCompanies().isEmpty());
        assertTrue(analytics.getTopJobRoles().isEmpty());
    }
}
