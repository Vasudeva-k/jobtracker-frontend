package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.UserResponse;
import com.vasudev.jobtracker.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // ===============================
    // Get All Users
    // ===============================
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return adminUserService.getAllUsers();
    }

    // ===============================
    // Search Users
    // ===============================
    @GetMapping("/search")
    public List<UserResponse> searchUsers(@RequestParam String keyword) {
        return adminUserService.searchUsers(keyword);
    }

    // ===============================
    // Delete User
    // ===============================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok("User deactivated successfully");
    }

    // ===============================
    // Block User
    // ===============================
    @PutMapping("/{id}/block")
    public String blockUser(@PathVariable Long id) {

        adminUserService.blockUser(id);

        return "User blocked successfully.";
    }

    // ===============================
    // Unblock User
    // ===============================
    @PutMapping("/{id}/unblock")
    public String unblockUser(@PathVariable Long id) {

        adminUserService.unblockUser(id);

        return "User unblocked successfully.";
    }
}