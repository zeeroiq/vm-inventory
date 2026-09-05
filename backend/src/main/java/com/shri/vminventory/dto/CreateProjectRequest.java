package com.shri.vminventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
    @NotBlank(message = "Project ID is required")
    @Size(min = 3, max = 50, message = "Project ID must be between 3 and 50 characters")
    String id,

    @NotBlank(message = "Project Name is required")
    @Size(max = 100, message = "Project Name must not exceed 100 characters")
    String name,

    String businessUnit,
    String ownerTeam,
    String environment,
    String description
) {}
