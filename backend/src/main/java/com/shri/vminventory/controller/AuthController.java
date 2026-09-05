package com.shri.vminventory.controller;

import com.shri.vminventory.dto.ApiResponse;
import com.shri.vminventory.dto.AuthRequest;
import com.shri.vminventory.dto.AuthResponse;
import com.shri.vminventory.dto.UserDto;
import com.shri.vminventory.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = userService.authenticate(request);
            return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("Invalid username or password", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(ApiResponse.failure("Unauthenticated", "No active session"));
        }
        return userService.getUserByUsername(authentication.getName())
                .map(u -> ResponseEntity.ok(ApiResponse.ok(u)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.failure("User profile not found")));
    }
}
