package com.shri.vminventory.controller;

import com.example.vminventory.dto.*;
import com.shri.vminventory.dto.ApiResponse;
import com.shri.vminventory.dto.DeploymentDto;
import com.shri.vminventory.dto.PageResponse;
import com.shri.vminventory.dto.RecordDeploymentRequest;
import com.shri.vminventory.service.DeploymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deployments")
public class DeploymentController {

    private final DeploymentService deploymentService;

    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeploymentDto>>> getDeployments(
            @RequestParam(required = false) String instanceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deploymentTimestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<DeploymentDto> result = PageResponse.from(
                deploymentService.getDeploymentsFiltered(instanceId, status, source, search, pageable)
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeploymentDto>> getDeployment(@PathVariable Long id) {
        return deploymentService.getDeploymentById(id)
                .map(d -> ResponseEntity.ok(ApiResponse.ok(d)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Deployment not found with ID: " + id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeploymentDto>> recordDeployment(
            @Valid @RequestBody RecordDeploymentRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "ci-bot";
        DeploymentDto created = deploymentService.recordDeployment(request, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Deployment recorded successfully", created));
    }
}
