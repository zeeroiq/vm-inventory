package com.shri.vminventory.dto;

import com.shri.vminventory.model.Role;

public record AuthResponse(
    String token,
    String type,
    String username,
    String email,
    Role role,
    String department
) {
    public AuthResponse(String token, String username, String email, Role role, String department) {
        this(token, "Bearer", username, email, role, department);
    }
}
