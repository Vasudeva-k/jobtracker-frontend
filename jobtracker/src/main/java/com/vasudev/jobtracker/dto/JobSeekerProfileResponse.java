package com.vasudev.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerProfileResponse {

    private Long id;
    private Integer atsScore;
    private Integer yearsOfExperience;
    private Integer projects;
    private Integer certifications;
    private List<String> skills;
    private Long userId;
    private String userEmail;
    private String userName;
}
