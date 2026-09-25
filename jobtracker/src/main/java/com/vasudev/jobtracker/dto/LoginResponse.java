package com.vasudev.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Boolean active;

    public LoginResponse(String token) {
        this.token = token;
    }
}

