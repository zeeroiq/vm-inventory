package com.shri.vminventory.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record RecordDeploymentRequest(
    @NotBlank(message = "Instance ID is required")
    String instanceId,

    @NotBlank(message = "Application Name is required")
    String applicationName,

    @NotBlank(message = "Artifact Name is required")
    String artifactName,

    @NotBlank(message = "Artifact Version is required")
    String artifactVersion,

    String buildNumber,
    String gitCommitId,
    String branch,
    LocalDateTime deploymentTimestamp,
    String deploymentSource,
    String deployedBy,
    String deploymentStatus,
    String releaseNotes
) {}
