package com.vasudev.jobtracker.config;

import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void testSkipsAdminCreationWhenEmailOrPasswordMissing() {
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", "");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "");

        dataInitializer.run();

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testSkipsAdminCreationWhenPasswordIsNull() {
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", "admin@domain.com");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", null);

        dataInitializer.run();

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreatesAdminWhenCredentialsConfiguredAndUserDoesNotExist() {
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", "admin@domain.com");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "SecureP@ss123!");

        when(userRepository.existsByEmail("admin@domain.com")).thenReturn(false);
        when(passwordEncoder.encode("SecureP@ss123!")).thenReturn("encoded_hash");

        dataInitializer.run();

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testDoesNotDuplicateAdminWhenAlreadyExists() {
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", "admin@domain.com");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "SecureP@ss123!");

        when(userRepository.existsByEmail("admin@domain.com")).thenReturn(true);

        dataInitializer.run();

        verify(userRepository, never()).save(any(User.class));
    }
}
