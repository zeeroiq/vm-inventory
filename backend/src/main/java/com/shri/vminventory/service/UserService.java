package com.shri.vminventory.service;

import com.shri.vminventory.dto.AuthRequest;
import com.shri.vminventory.dto.AuthResponse;
import com.shri.vminventory.dto.UserDto;
import com.shri.vminventory.model.Role;
import com.shri.vminventory.model.User;
import com.shri.vminventory.repository.UserRepository;
import com.shri.vminventory.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (!user.isEnabled()) {
            throw new IllegalStateException("User account is disabled");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole(), user.getEmail());
        auditService.record(user.getUsername(), "LOGIN", "USER", String.valueOf(user.getId()),
                "User logged in successfully", null);

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole(), user.getDepartment());
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(UserDto::from);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    @Transactional
    public UserDto updateUserRole(Long userId, Role newRole, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Role oldRole = user.getRole();
        user.setRole(newRole);
        User saved = userRepository.save(user);

        auditService.record(adminUsername, "CHANGE_ROLE", "USER", String.valueOf(userId),
                "Changed role for " + user.getUsername() + " from " + oldRole + " to " + newRole, null);

        return UserDto.from(saved);
    }

    @Transactional
    public User registerUser(String username, String rawPassword, String email, Role role, String department) {
        if (userRepository.existsByUsername(username)) {
            return userRepository.findByUsername(username).orElseThrow();
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .email(email)
                .role(role)
                .department(department)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }
}
