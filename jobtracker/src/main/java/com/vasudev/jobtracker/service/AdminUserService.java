package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.UserResponse;

import java.util.List;

public interface AdminUserService {

    // Get all users
    List<UserResponse> getAllUsers();

    // Search users
    List<UserResponse> searchUsers(String keyword);

    // Delete user
    void deleteUser(Long userId);

    // Block user
    void blockUser(Long userId);

    // Unblock user
    void unblockUser(Long userId);
}
