package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.JobSeekerProfileRequest;
import com.vasudev.jobtracker.dto.JobSeekerProfileResponse;
import com.vasudev.jobtracker.service.JobSeekerProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class JobSeekerProfileController {

    private final JobSeekerProfileService profileService;

    public JobSeekerProfileController(
            JobSeekerProfileService profileService) {

        this.profileService = profileService;
    }

    @PostMapping
    public JobSeekerProfileResponse saveProfile(
            @RequestBody JobSeekerProfileRequest request,
            Authentication authentication) {

        return profileService.saveProfile(
                authentication.getName(),
                request.getAtsScore(),
                request.getYearsOfExperience(),
                request.getProjects(),
                request.getCertifications(),
                request.getSkills() == null
                        ? ""
                        : String.join(", ", request.getSkills())
        );
    }

    @GetMapping
    public JobSeekerProfileResponse getProfile(
            Authentication authentication) {

        return profileService.getProfileByEmail(
                authentication.getName()
        );
    }
}
