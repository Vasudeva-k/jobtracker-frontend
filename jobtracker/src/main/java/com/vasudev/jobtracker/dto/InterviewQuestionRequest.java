package com.vasudev.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterviewQuestionRequest {

    @NotBlank(message = "Job role is required")
    private String jobRole;

    @NotBlank(message = "Experience is required")
    private String experience;

    @NotBlank(message = "Skills are required")
    private String skills;
}
