package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.JobSeekerProfileResponse;
import com.vasudev.jobtracker.entity.JobSeekerProfile;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.JobSeekerProfileRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class JobSeekerProfileService {

    private final JobSeekerProfileRepository profileRepository;
    private final UserRepository userRepository;

    public JobSeekerProfileService(
            JobSeekerProfileRepository profileRepository,
            UserRepository userRepository) {

        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    public JobSeekerProfileResponse getProfileByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        JobSeekerProfile profile = profileRepository.findByUser(user)
                .orElse(JobSeekerProfile.builder()
                        .user(user)
                        .atsScore(0)
                        .yearsOfExperience(0)
                        .projects(0)
                        .certifications(0)
                        .skills("")
                        .build());

        return mapToResponse(profile, user);
    }

    public JobSeekerProfileResponse saveProfile(
            String email,
            Integer atsScore,
            Integer yearsOfExperience,
            Integer projects,
            Integer certifications,
            String skills) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        JobSeekerProfile profile =
                profileRepository.findByUser(user)
                        .orElse(
                                JobSeekerProfile.builder()
                                        .user(user)
                                        .build()
                        );

        int safeAts = atsScore != null ? Math.max(0, Math.min(100, atsScore)) : 0;
        int safeExp = yearsOfExperience != null ? Math.max(0, Math.min(50, yearsOfExperience)) : 0;
        int safeProjects = projects != null ? Math.max(0, Math.min(200, projects)) : 0;
        int safeCerts = certifications != null ? Math.max(0, Math.min(100, certifications)) : 0;

        profile.setAtsScore(safeAts);
        profile.setYearsOfExperience(safeExp);
        profile.setProjects(safeProjects);
        profile.setCertifications(safeCerts);
        profile.setSkills(skills != null ? skills.trim() : "");

        JobSeekerProfile saved = profileRepository.save(profile);
        return mapToResponse(saved, user);
    }

    private JobSeekerProfileResponse mapToResponse(JobSeekerProfile profile, User user) {
        List<String> skillList = List.of();
        if (profile.getSkills() != null && !profile.getSkills().isBlank()) {
            skillList = Arrays.stream(profile.getSkills().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        String userName = ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                (user.getLastName() != null ? user.getLastName() : "")).trim();

        return JobSeekerProfileResponse.builder()
                .id(profile.getId())
                .atsScore(profile.getAtsScore())
                .yearsOfExperience(profile.getYearsOfExperience())
                .projects(profile.getProjects())
                .certifications(profile.getCertifications())
                .skills(skillList)
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userName(userName)
                .build();
    }
}