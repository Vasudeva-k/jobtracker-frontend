package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.UserResponse;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(2L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("9876543210")
                .role("USER")
                .active(true)
                .build();
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<UserResponse> responses = adminUserService.getAllUsers();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("john.doe@example.com", responses.get(0).getEmail());
        assertEquals("John", responses.get(0).getFirstName());
        assertEquals(Boolean.TRUE, responses.get(0).getActive());
    }

    @Test
    void testSearchUsers() {
        when(userRepository.searchUsers("john")).thenReturn(List.of(sampleUser));

        List<UserResponse> responses = adminUserService.searchUsers("john");

        assertEquals(1, responses.size());
        assertEquals("john.doe@example.com", responses.get(0).getEmail());
    }

    @Test
    void testBlockUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminUserService.blockUser(2L);

        assertFalse(sampleUser.getActive());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testUnblockUser() {
        sampleUser.setActive(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminUserService.unblockUser(2L);

        assertTrue(sampleUser.getActive());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminUserService.deleteUser(2L);

        assertFalse(sampleUser.getActive());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testUserNotFoundThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminUserService.blockUser(999L));
        assertThrows(RuntimeException.class, () -> adminUserService.unblockUser(999L));
        assertThrows(RuntimeException.class, () -> adminUserService.deleteUser(999L));
    }

    @Test
    void testBlockOrDeactivateAdminThrowsException() {
        User adminUser = User.builder()
                .id(1L)
                .email("admin@jobtracker.com")
                .role("ADMIN")
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(IllegalArgumentException.class, () -> adminUserService.blockUser(1L));
        assertThrows(IllegalArgumentException.class, () -> adminUserService.deleteUser(1L));
    }
}
