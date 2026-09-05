package com.shri.vminventory.controller;

import com.shri.vminventory.dto.ApiResponse;
import com.shri.vminventory.dto.AuditLogDto;
import com.shri.vminventory.dto.PageResponse;
import com.shri.vminventory.dto.UserDto;
import com.shri.vminventory.model.Role;
import com.shri.vminventory.scheduler.GcpSyncScheduler;
import com.shri.vminventory.service.AuditService;
import com.shri.vminventory.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;
    private final GcpSyncScheduler gcpSyncScheduler;

    public AdminController(UserService userService,
                           AuditService auditService,
                           GcpSyncScheduler gcpSyncScheduler) {
        this.userService = userService;
        this.auditService = auditService;
        this.gcpSyncScheduler = gcpSyncScheduler;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers()));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserDto>> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String roleStr = body.get("role");
        Role newRole = Role.valueOf(roleStr);
        String adminUser = authentication != null ? authentication.getName() : "admin";
        UserDto updated = userService.updateUserRole(id, newRole, adminUser);
        return ResponseEntity.ok(ApiResponse.ok("Role updated successfully", updated));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogDto>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<AuditLogDto> logs = PageResponse.from(
                auditService.getLogs(pageable).map(AuditLogDto::from)
        );
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    @PostMapping("/sync/trigger")
    public ResponseEntity<ApiResponse<GcpSyncScheduler.SyncReport>> triggerSync(Authentication authentication) {
        String triggeredBy = authentication != null ? authentication.getName() : "admin";
        GcpSyncScheduler.SyncReport report = gcpSyncScheduler.triggerSync(triggeredBy);
        return ResponseEntity.ok(ApiResponse.ok("Sync initiated", report));
    }

    @GetMapping("/sync/status")
    public ResponseEntity<ApiResponse<GcpSyncScheduler.SyncReport>> getSyncStatus() {
        return ResponseEntity.ok(ApiResponse.ok(gcpSyncScheduler.getStatus()));
    }
}
