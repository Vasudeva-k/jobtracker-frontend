package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.LoginRequest;
import com.vasudev.jobtracker.dto.LoginResponse;
import com.vasudev.jobtracker.dto.RegisterRequest;

public interface UserService {

    String registerUser(RegisterRequest request);

    LoginResponse loginUser(LoginRequest request);
}
