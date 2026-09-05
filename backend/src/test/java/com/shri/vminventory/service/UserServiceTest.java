package com.shri.vminventory.service;

import com.shri.vminventory.dto.AuthRequest;
import com.shri.vminventory.dto.AuthResponse;
import com.shri.vminventory.dto.UserDto;
import com.shri.vminventory.model.Role;
import com.shri.vminventory.model.User;
import com.shri.vminventory.repository.UserRepository;
import com.shri.vminventory.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("admin")
                .password("encoded-pass")
                .email("admin@test.com")
                .role(Role.ROLE_ADMIN)
                .department("SecOps")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testAuthenticate_Success() {
        AuthRequest req = new AuthRequest("admin", "password123");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-pass")).thenReturn(true);
        when(jwtTokenProvider.generateToken("admin", Role.ROLE_ADMIN, "admin@test.com")).thenReturn("mock-jwt-token");

        AuthResponse resp = userService.authenticate(req);
        assertNotNull(resp);
        assertEquals("mock-jwt-token", resp.token());
        assertEquals("admin", resp.username());
        assertEquals(Role.ROLE_ADMIN, resp.role());
    }

    @Test
    void testAuthenticate_InvalidPassword() {
        AuthRequest req = new AuthRequest("admin", "wrong-pass");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-pass", "encoded-pass")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.authenticate(req));
    }

    @Test
    void testAuthenticate_DisabledUser() {
        user.setEnabled(false);
        AuthRequest req = new AuthRequest("admin", "password123");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-pass")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> userService.authenticate(req));
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<UserDto> users = userService.getAllUsers();
        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).username());
    }

    @Test
    void testUpdateUserRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updated = userService.updateUserRole(1L, Role.ROLE_SRE, "super-admin");
        assertEquals(Role.ROLE_SRE, updated.role());
        verify(auditService, times(1)).record(eq("super-admin"), eq("CHANGE_ROLE"), eq("USER"), eq("1"), any(), isNull());
    }

    @Test
    void testRegisterUser() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("rawpass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerUser("newuser", "rawpass", "nu@test.com", Role.ROLE_DEVELOPER, "Dev");
        assertNotNull(registered);
        assertEquals("newuser", registered.getUsername());
    }
}
