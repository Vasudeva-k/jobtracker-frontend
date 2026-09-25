package com.vasudev.jobtracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class JobResponse {

    private Long id;

    private String companyName;

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}