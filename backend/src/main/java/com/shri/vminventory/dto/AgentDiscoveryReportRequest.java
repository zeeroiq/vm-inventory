package com.shri.vminventory.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentDiscoveryReportRequest(
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
    String status
) {}
