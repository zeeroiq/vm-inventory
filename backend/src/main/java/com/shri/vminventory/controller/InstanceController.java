package com.shri.vminventory.controller;

import com.example.vminventory.dto.*;
import com.shri.vminventory.dto.*;
import com.shri.vminventory.service.DeploymentService;
import com.shri.vminventory.service.InstanceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instances")
public class InstanceController {

    private final InstanceService instanceService;
    private final DeploymentService deploymentService;

    public InstanceController(InstanceService instanceService, DeploymentService deploymentService) {
        this.instanceService = instanceService;
        this.deploymentService = deploymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InstanceDto>>> getInstances(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String ownerTeam,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<InstanceDto> result = PageResponse.from(
                instanceService.getInstancesFiltered(projectId, environment, ownerTeam, status, search, pageable)
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InstanceDto>> getInstance(@PathVariable String id) {
        return instanceService.getInstanceById(id)
                .map(i -> ResponseEntity.ok(ApiResponse.ok(i)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Instance not found with ID: " + id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    public ResponseEntity<ApiResponse<InstanceDto>> createInstance(
            @Valid @RequestBody CreateInstanceRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        InstanceDto created = instanceService.createInstance(request, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Instance created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    public ResponseEntity<ApiResponse<InstanceDto>> updateInstance(
            @PathVariable String id,
            @Valid @RequestBody CreateInstanceRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        InstanceDto updated = instanceService.updateInstance(id, request, username);
        return ResponseEntity.ok(ApiResponse.ok("Instance updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInstance(
            @PathVariable String id,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        instanceService.deleteInstance(id, username);
        return ResponseEntity.ok(ApiResponse.ok("Instance deleted successfully", null));
    }

    @GetMapping("/{id}/deployments")
    public ResponseEntity<ApiResponse<List<DeploymentDto>>> getInstanceDeployments(@PathVariable String id) {
        List<DeploymentDto> list = deploymentService.getDeploymentsByInstance(id);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String ownerTeam,
            @RequestParam(required = false) String status) {

        byte[] csvData = instanceService.exportInstancesCsv(projectId, environment, ownerTeam, status);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"vm_inventory_" + System.currentTimeMillis() + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
