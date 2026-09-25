package com.vasudev.jobtracker.repository;

import com.vasudev.jobtracker.entity.JobSeekerProfile;
import com.vasudev.jobtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobSeekerProfileRepository
        extends JpaRepository<JobSeekerProfile, Long> {

    Optional<JobSeekerProfile> findByUser(User user);
}
