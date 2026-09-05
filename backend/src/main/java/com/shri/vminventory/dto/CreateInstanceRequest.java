package com.shri.vminventory.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateInstanceRequest(
    @NotBlank(message = "Instance ID is required")
    String id,

    @NotBlank(message = "Instance name is required")
    String name,

    @NotBlank(message = "Project ID is required")
    String projectId,

    String zone,
    String region,
    String machineType,
    String internalIp,
    String externalIp,
    String status,
    String labels,
    String ownerTeam,
    String environment,
    Integer cpuCores,
    Long memoryMb
) {}
