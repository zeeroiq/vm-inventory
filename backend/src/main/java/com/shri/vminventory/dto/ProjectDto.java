package com.shri.vminventory.dto;

import com.shri.vminventory.model.Project;
import java.time.LocalDateTime;

public record ProjectDto(
    String id,
    String name,
    String businessUnit,
    String ownerTeam,
    String environment,
    String description,
    LocalDateTime creationDate,
    long instanceCount
) {
    public static ProjectDto from(Project p, long count) {
        return new ProjectDto(
            p.getId(),
            p.getName(),
            p.getBusinessUnit(),
            p.getOwnerTeam(),
            p.getEnvironment(),
            p.getDescription(),
            p.getCreationDate(),
            count
        );
    }

    public static ProjectDto from(Project p) {
        return from(p, 0L);
    }
}
