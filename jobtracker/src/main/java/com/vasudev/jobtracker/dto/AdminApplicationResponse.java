package com.vasudev.jobtracker.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminApplicationResponse {

    private Long id;

    private Long userId;
    private String userName;
    private String userEmail;

    private String companyName;
    private String jobTitle;
    private String location;
    private BigDecimal salary;
    private String jobType;
    private String status;

    private LocalDate appliedDate;
    private LocalDate interviewDate;
    private String interviewTime;

    private String jobLink;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}