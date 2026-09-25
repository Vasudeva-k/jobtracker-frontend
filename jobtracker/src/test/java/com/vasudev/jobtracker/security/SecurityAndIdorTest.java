package com.vasudev.jobtracker.security;

import com.vasudev.jobtracker.entity.JobApplication;
import com.vasudev.jobtracker.entity.JobSeekerProfile;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.repository.JobSeekerProfileRepository;
import com.vasudev.jobtracker.repository.ResumeRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.JobSeekerProfileService;
import com.vasudev.jobtracker.service.impl.AdminUserServiceImpl;
import com.vasudev.jobtracker.service.impl.JobServiceImpl;
import com.vasudev.jobtracker.service.impl.ResumeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityAndIdorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobApplicationRepository jobRepository;

    @Mock
    private JobSeekerProfileRepository profileRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private JobServiceImpl jobService;

    @InjectMocks
    private JobSeekerProfileService profileService;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User userAlice;
    private User userBob;
    private JobApplication aliceJob;
    private JobApplication bobJob;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(resumeService, "uploadDir", "uploads/resumes");

        userAlice = User.builder()
                .id(1L)
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Smith")
                .role("USER")
                .active(true)
                .build();

        userBob = User.builder()
                .id(2L)
                .email("bob@example.com")
                .firstName("Bob")
                .lastName("Jones")
                .role("USER")
                .active(true)
                .build();

        aliceJob = JobApplication.builder()
                .id(101L)
                .companyName("Alice Corp")
                .jobTitle("Backend Engineer")
                .status("APPLIED")
                .user(userAlice)
                .build();

        bobJob = JobApplication.builder()
                .id(102L)
                .companyName("Bob Inc")
                .jobTitle("Frontend Engineer")
                .status("INTERVIEW")
                .user(userBob)
                .build();
    }

    // ==========================================
    // 1. IDOR Prevention in Job Application Service
    // ==========================================

    @Test
    void testAliceCanAccessHerOwnJob() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(userAlice));
        when(jobRepository.findById(101L)).thenReturn(Optional.of(aliceJob));

        var response = jobService.getJobById(101L, "alice@example.com");
        assertNotNull(response);
        assertEquals("Alice Corp", response.getCompanyName());
    }

    @Test
    void testAliceCannotAccessBobsJob_ThrowsAccessDenied() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(userAlice));
        when(jobRepository.findById(102L)).thenReturn(Optional.of(bobJob));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                jobService.getJobById(102L, "alice@example.com")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("access denied"));
    }

    @Test
    void testAliceCannotDeleteBobsJob_ThrowsAccessDenied() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(userAlice));
        when(jobRepository.findById(102L)).thenReturn(Optional.of(bobJob));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                jobService.deleteJob(102L, "alice@example.com")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("access denied"));
    }

    // ==========================================
    // 2. IDOR Prevention in User Profile & Resume
    // ==========================================

    @Test
    void testUserProfileIsolation() {
        JobSeekerProfile aliceProfile = JobSeekerProfile.builder()
                .id(1L)
                .atsScore(90)
                .yearsOfExperience(3)
                .projects(4)
                .certifications(2)
                .skills("Java, Spring Boot")
                .user(userAlice)
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(userAlice));
        when(profileRepository.findByUser(userAlice)).thenReturn(Optional.of(aliceProfile));

        var response = profileService.getProfileByEmail("alice@example.com");
        assertNotNull(response);
        assertEquals(90, response.getAtsScore());
        assertEquals("alice@example.com", response.getUserEmail());
    }

    @Test
    void testResumeIsolation_NotFoundForAnotherUser() {
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(userBob));
        when(resumeRepository.findByUser(userBob)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                resumeService.getMyResume("bob@example.com")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    // ==========================================
    // 3. User State & Account Activation
    // ==========================================

    @Test
    void testDeactivatedUserOperations() {
        User activeUser = User.builder()
                .id(3L)
                .email("test@example.com")
                .role("USER")
                .active(true)
                .build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(activeUser));

        adminUserService.blockUser(3L);
        assertFalse(activeUser.getActive());

        adminUserService.unblockUser(3L);
        assertTrue(activeUser.getActive());
    }
}
