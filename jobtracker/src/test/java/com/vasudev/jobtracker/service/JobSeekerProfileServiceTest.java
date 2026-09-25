package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.JobSeekerProfileResponse;
import com.vasudev.jobtracker.entity.JobSeekerProfile;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.JobSeekerProfileRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobSeekerProfileServiceTest {

    @Mock
    private JobSeekerProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobSeekerProfileService profileService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("Test")
                .lastName("Candidate")
                .role("USER")
                .active(true)
                .build();
    }

    @Test
    void testGetProfileExisting() {
        JobSeekerProfile profile = JobSeekerProfile.builder()
                .id(10L)
                .atsScore(88)
                .yearsOfExperience(3)
                .projects(5)
                .certifications(2)
                .skills("Java, Spring Boot, React")
                .user(user)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));

        JobSeekerProfileResponse res = profileService.getProfileByEmail("user@example.com");

        assertNotNull(res);
        assertEquals(88, res.getAtsScore());
        assertEquals(3, res.getYearsOfExperience());
        assertEquals(5, res.getProjects());
        assertEquals(2, res.getCertifications());
        assertEquals(3, res.getSkills().size());
        assertTrue(res.getSkills().contains("Java"));
        assertEquals("Test Candidate", res.getUserName());
    }

    @Test
    void testGetProfileDefaultsWhenNoneExists() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        JobSeekerProfileResponse res = profileService.getProfileByEmail("user@example.com");

        assertNotNull(res);
        assertEquals(0, res.getAtsScore());
        assertEquals(0, res.getYearsOfExperience());
        assertTrue(res.getSkills().isEmpty());
    }

    @Test
    void testSaveProfileSuccess() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        JobSeekerProfile savedProfile = JobSeekerProfile.builder()
                .id(11L)
                .atsScore(90)
                .yearsOfExperience(4)
                .projects(6)
                .certifications(3)
                .skills("Java, Docker, AWS")
                .user(user)
                .build();

        when(profileRepository.save(any(JobSeekerProfile.class))).thenReturn(savedProfile);

        JobSeekerProfileResponse res = profileService.saveProfile(
                "user@example.com",
                90,
                4,
                6,
                3,
                "Java, Docker, AWS"
        );

        assertNotNull(res);
        assertEquals(90, res.getAtsScore());
        assertEquals(4, res.getYearsOfExperience());
        assertEquals(3, res.getSkills().size());
        verify(profileRepository, times(1)).save(any(JobSeekerProfile.class));
    }

    @Test
    void testSaveProfileClampsOutOfBoundsValues() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        when(profileRepository.save(any(JobSeekerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobSeekerProfileResponse res = profileService.saveProfile(
                "user@example.com",
                500, // ATS > 100
                100, // Exp > 50
                500, // Projects > 200
                200, // Certs > 100
                "Java, Spring"
        );

        assertNotNull(res);
        assertEquals(100, res.getAtsScore());
        assertEquals(50, res.getYearsOfExperience());
        assertEquals(200, res.getProjects());
        assertEquals(100, res.getCertifications());
    }
}
