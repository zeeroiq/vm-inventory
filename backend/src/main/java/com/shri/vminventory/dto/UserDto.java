package com.shri.vminventory.dto;

import com.shri.vminventory.model.Role;
import com.shri.vminventory.model.User;
import java.time.LocalDateTime;

public record UserDto(
    Long id,
    String username,
    String email,
    Role role,
    String department,
    boolean enabled,
    LocalDateTime createdAt
) {
    public static UserDto from(User u) {
        return new UserDto(
            u.getId(),
            u.getUsername(),
            u.getEmail(),
            u.getRole(),
            u.getDepartment(),
            u.isEnabled(),
            u.getCreatedAt()
        );
    }
}
