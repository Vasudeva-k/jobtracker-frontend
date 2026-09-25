package com.vasudev.jobtracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_applications",
        indexes = {
                @Index(name = "idx_job_app_user_id", columnList = "user_id"),
                @Index(name = "idx_job_app_status", columnList = "status"),
                @Index(name = "idx_job_app_applied_date", columnList = "appliedDate"),
                @Index(name = "idx_job_app_interview_date", columnList = "interviewDate"),
                @Index(name = "idx_job_app_interview_time", columnList = "interviewTime")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    private String jobTitle;

    @Column(length = 5000)
    private String jobDescription;

    private String location;

    private BigDecimal salary;

    private String jobType;

    private String status;

    private LocalDate appliedDate;

    // Interview Reminder
    private LocalDate interviewDate;

    private String interviewTime;

    @Column(length = 1000)
    private String jobLink;

    @Column(length = 2000)
    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (appliedDate == null) {
            appliedDate = LocalDate.now();
        }

        if (status == null) {
            status = "APPLIED";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}