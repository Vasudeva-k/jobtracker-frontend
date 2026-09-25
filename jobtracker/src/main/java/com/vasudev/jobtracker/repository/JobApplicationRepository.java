package com.vasudev.jobtracker.repository;

import com.vasudev.jobtracker.entity.JobApplication;
import com.vasudev.jobtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JobApplicationRepository
        extends JpaRepository<JobApplication, Long> {

    // ==========================
    // User Jobs
    // ==========================

    List<JobApplication> findByUser(User user);


    // ==========================
    // Dashboard - User Specific
    // ==========================

    long countByUser(User user);

    long countByUserAndStatus(User user, String status);


    // ==========================
    // Global Counts - Admin
    // ==========================

    long countByStatus(String status);

    long countByStatusIgnoreCase(String status);


    // ==========================
    // Monthly Applications
    // ==========================

    // User-specific
    @Query("""
        SELECT MONTH(j.appliedDate), COUNT(j)
        FROM JobApplication j
        WHERE j.user = :user
        GROUP BY MONTH(j.appliedDate)
        ORDER BY MONTH(j.appliedDate)
    """)
    List<Object[]> getMonthlyApplications(
            @Param("user") User user
    );


    // Global - Admin
    @Query("""
        SELECT MONTH(j.appliedDate), COUNT(j)
        FROM JobApplication j
        GROUP BY MONTH(j.appliedDate)
        ORDER BY MONTH(j.appliedDate)
    """)
    List<Object[]> getMonthlyApplications();


    // ==========================
    // Status Analytics
    // ==========================

    // User-specific
    @Query("""
        SELECT j.status, COUNT(j)
        FROM JobApplication j
        WHERE j.user = :user
        GROUP BY j.status
    """)
    List<Object[]> getStatusAnalytics(
            @Param("user") User user
    );


    // Global - Admin
    @Query("""
        SELECT j.status, COUNT(j)
        FROM JobApplication j
        GROUP BY j.status
    """)
    List<Object[]> getStatusAnalytics();


    // ==========================
    // Top Companies
    // ==========================

    // User-specific
    @Query("""
        SELECT j.companyName, COUNT(j)
        FROM JobApplication j
        WHERE j.user = :user
        GROUP BY j.companyName
        ORDER BY COUNT(j) DESC
    """)
    List<Object[]> getTopCompanies(
            @Param("user") User user
    );


    // Global - Admin
    @Query("""
        SELECT j.companyName, COUNT(j)
        FROM JobApplication j
        GROUP BY j.companyName
        ORDER BY COUNT(j) DESC
    """)
    List<Object[]> getTopCompanies();


    // ==========================
    // Top Job Roles
    // ==========================

    // User-specific
    @Query("""
        SELECT j.jobTitle, COUNT(j)
        FROM JobApplication j
        WHERE j.user = :user
        GROUP BY j.jobTitle
        ORDER BY COUNT(j) DESC
    """)
    List<Object[]> getTopJobRoles(
            @Param("user") User user
    );


    // Global - Admin
    @Query("""
        SELECT j.jobTitle, COUNT(j)
        FROM JobApplication j
        GROUP BY j.jobTitle
        ORDER BY COUNT(j) DESC
    """)
    List<Object[]> getTopJobRoles();


    // ==========================
    // Today's Interviews
    // ==========================

    @Query("""
        SELECT j
        FROM JobApplication j
        JOIN FETCH j.user
        WHERE j.interviewDate = CURRENT_DATE
        ORDER BY j.interviewTime ASC, j.id ASC
    """)
    List<JobApplication> findTodayInterviews();

    @Query("""
        SELECT j
        FROM JobApplication j
        JOIN FETCH j.user
        WHERE j.user = :user AND j.interviewDate = CURRENT_DATE
        ORDER BY j.interviewTime ASC, j.id ASC
    """)
    List<JobApplication> findTodayInterviewsByUser(
            @Param("user") User user
    );


    // ==========================
    // Find Interviews by Date
    // ==========================

    @Query("""
        SELECT j
        FROM JobApplication j
        JOIN FETCH j.user
        WHERE j.interviewDate = :date
        ORDER BY j.interviewTime ASC, j.id ASC
    """)
    List<JobApplication> findByInterviewDate(
            @Param("date") LocalDate date
    );

    @Query("""
        SELECT j
        FROM JobApplication j
        JOIN FETCH j.user
        WHERE j.user = :user AND j.interviewDate = :date
        ORDER BY j.interviewTime ASC, j.id ASC
    """)
    List<JobApplication> findByInterviewDateAndUser(
            @Param("date") LocalDate date,
            @Param("user") User user
    );


    // ==========================
    // Admin - All Applications
    // ==========================

    @Query("""
        SELECT j
        FROM JobApplication j
        JOIN FETCH j.user
        ORDER BY j.createdAt DESC
    """)
    List<JobApplication> findAllApplicationsForAdmin();


    // ==========================
    // Admin - Search Applications
    // ==========================

    @Query("""
        SELECT j
        FROM JobApplication j
        JOIN FETCH j.user
        WHERE
            LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(j.status) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(j.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY j.createdAt DESC
    """)
    List<JobApplication> searchApplicationsForAdmin(
            @Param("keyword") String keyword
    );


    // ==========================
    // Admin - Filter by Status
    // ==========================

    @Query("""
        SELECT j
        FROM JobApplication j
        JOIN FETCH j.user
        WHERE LOWER(j.status) = LOWER(:status)
        ORDER BY j.createdAt DESC
    """)
    List<JobApplication> findApplicationsByStatusForAdmin(
            @Param("status") String status
    );
}