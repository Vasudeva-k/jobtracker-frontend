package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.LoginRequest;
import com.vasudev.jobtracker.dto.LoginResponse;
import com.vasudev.jobtracker.dto.RegisterRequest;
import com.vasudev.jobtracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(userService.loginUser(request));
    }
}

