package com.vasudev.jobtracker.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = "YzNkZjY3OWJmYzMwNTNkNjQ3YjY5MzEzMjNmNjQ5NjJjYmM5MjM0NjViY2Q3N2QxYzQ0YzQ4N2MwNmQ0M2M1MQ==";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
    }

    @Test
    void testGenerateAndExtractUsername() {
        String email = "testuser@example.com";
        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertFalse(token.isBlank());

        String extracted = jwtService.extractUsername(token);
        assertEquals(email, extracted);
    }

    @Test
    void testIsTokenValid() {
        String email = "testuser@example.com";
        String token = jwtService.generateToken(email);

        UserDetails userDetails = new User(email, "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testIsTokenInvalidForDifferentUser() {
        String token = jwtService.generateToken("user1@example.com");

        UserDetails userDetails = new User("user2@example.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }
}
