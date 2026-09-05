package com.shri.vminventory.dto;

import com.shri.vminventory.model.AuditLog;
import java.time.LocalDateTime;

public record AuditLogDto(
    Long id,
    LocalDateTime timestamp,
    String username,
    String action,
    String resourceType,
    String resourceId,
    String details,
    String ipAddress
) {
    public static AuditLogDto from(AuditLog log) {
        return new AuditLogDto(
            log.getId(),
            log.getTimestamp(),
            log.getUsername(),
            log.getAction(),
            log.getResourceType(),
            log.getResourceId(),
            log.getDetails(),
            log.getIpAddress()
        );
    }
}
