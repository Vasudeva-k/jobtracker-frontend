package com.vasudev.jobtracker.repository;

import com.vasudev.jobtracker.entity.Resume;
import com.vasudev.jobtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Optional<Resume> findByUser(User user);
}