package com.shri.vminventory.dto;

import com.shri.vminventory.model.Instance;
import java.time.LocalDateTime;

public record InstanceDto(
    String id,
    String name,
    String zone,
    String region,
    String machineType,
    String internalIp,
    String externalIp,
    String status,
    String labels,
    String ownerTeam,
    String environment,
    LocalDateTime lastUpdateTimestamp,
    Integer cpuCores,
    Long memoryMb,
    String projectId,
    String projectName,
    String currentArtifact,
    String currentVersion
) {
    public static InstanceDto from(Instance i) {
        String pId = i.getProject() != null ? i.getProject().getId() : null;
        String pName = i.getProject() != null ? i.getProject().getName() : null;
        return new InstanceDto(
            i.getId(),
            i.getName(),
            i.getZone(),
            i.getRegion(),
            i.getMachineType(),
            i.getInternalIp(),
            i.getExternalIp(),
            i.getStatus(),
            i.getLabels(),
            i.getOwnerTeam(),
            i.getEnvironment(),
            i.getLastUpdateTimestamp(),
            i.getCpuCores(),
            i.getMemoryMb(),
            pId,
            pName,
            null,
            null
        );
    }

    public static InstanceDto from(Instance i, String artifact, String version) {
        String pId = i.getProject() != null ? i.getProject().getId() : null;
        String pName = i.getProject() != null ? i.getProject().getName() : null;
        return new InstanceDto(
            i.getId(),
            i.getName(),
            i.getZone(),
            i.getRegion(),
            i.getMachineType(),
            i.getInternalIp(),
            i.getExternalIp(),
            i.getStatus(),
            i.getLabels(),
            i.getOwnerTeam(),
            i.getEnvironment(),
            i.getLastUpdateTimestamp(),
            i.getCpuCores(),
            i.getMemoryMb(),
            pId,
            pName,
            artifact,
            version
        );
    }
}
