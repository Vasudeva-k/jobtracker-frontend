package com.vasudev.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Job role is required")
    private String jobRole;

    private String jobDescription;

    private String location;

    private BigDecimal salary;

    private String jobType;

    private String jobLink;

    private String status;

    private String notes;

    private LocalDate appliedDate;

    private LocalDate interviewDate;

    private String interviewTime;
}