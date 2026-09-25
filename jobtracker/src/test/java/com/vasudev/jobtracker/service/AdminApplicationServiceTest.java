package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.AdminApplicationResponse;
import com.vasudev.jobtracker.entity.JobApplication;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.service.impl.AdminApplicationServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @InjectMocks
    private AdminApplicationServiceImpl adminApplicationService;

    private User sampleUser;
    private JobApplication sampleApplication;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .firstName("Vasudeva")
                .lastName("K")
                .email("vasu@example.com")
                .role("USER")
                .active(true)
                .build();

        sampleApplication = JobApplication.builder()
                .id(100L)
                .companyName("Google")
                .jobTitle("Software Engineer")
                .location("Remote")
                .salary(BigDecimal.valueOf(1500000))
                .status("INTERVIEW")
                .appliedDate(LocalDate.now())
                .interviewDate(LocalDate.now().plusDays(2))
                .interviewTime("10:00 AM")
                .user(sampleUser)
                .build();
    }

    @Test
    void testGetAllApplications() {
        when(jobApplicationRepository.findAllApplicationsForAdmin()).thenReturn(List.of(sampleApplication));

        List<AdminApplicationResponse> responses = adminApplicationService.getAllApplications();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getId());
        assertEquals("Google", responses.get(0).getCompanyName());
        assertEquals("Vasudeva K", responses.get(0).getUserName());
        assertEquals("vasu@example.com", responses.get(0).getUserEmail());
        assertEquals(1L, responses.get(0).getUserId());
    }

    @Test
    void testSearchApplications() {
        when(jobApplicationRepository.searchApplicationsForAdmin("google")).thenReturn(List.of(sampleApplication));

        List<AdminApplicationResponse> responses = adminApplicationService.searchApplications("google");

        assertEquals(1, responses.size());
        assertEquals("Google", responses.get(0).getCompanyName());
    }

    @Test
    void testFilterByStatus() {
        when(jobApplicationRepository.findApplicationsByStatusForAdmin("INTERVIEW")).thenReturn(List.of(sampleApplication));

        List<AdminApplicationResponse> responses = adminApplicationService.filterByStatus("INTERVIEW");

        assertEquals(1, responses.size());
        assertEquals("INTERVIEW", responses.get(0).getStatus());
    }

    @Test
    void testGetApplicationById() {
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(sampleApplication));

        AdminApplicationResponse response = adminApplicationService.getApplicationById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Software Engineer", response.getJobTitle());
    }

    @Test
    void testDeleteApplication() {
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(sampleApplication));
        doNothing().when(jobApplicationRepository).delete(sampleApplication);

        assertDoesNotThrow(() -> adminApplicationService.deleteApplication(100L));
        verify(jobApplicationRepository, times(1)).delete(sampleApplication);
    }
}
