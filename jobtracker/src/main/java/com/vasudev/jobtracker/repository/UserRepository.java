package com.vasudev.jobtracker.repository;

import com.vasudev.jobtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ===============================
    // Monthly User Registrations
    // ===============================
    @Query("""
        SELECT MONTH(u.createdAt), COUNT(u)
        FROM User u
        GROUP BY MONTH(u.createdAt)
        ORDER BY MONTH(u.createdAt)
    """)
    List<Object[]> getMonthlyRegistrations();

    // ===============================
    // Search Users
    // ===============================
    List<User> findByFirstNameContainingIgnoreCase(String firstName);

    List<User> findByLastNameContainingIgnoreCase(String lastName);

    List<User> findByEmailContainingIgnoreCase(String email);

    @Query("""
        SELECT u
        FROM User u
        WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<User> searchUsers(String keyword);
}