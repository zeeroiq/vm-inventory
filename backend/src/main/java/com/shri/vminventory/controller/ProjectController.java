package com.shri.vminventory.controller;

import com.example.vminventory.dto.*;
import com.shri.vminventory.dto.*;
import com.shri.vminventory.service.InstanceService;
import com.shri.vminventory.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final InstanceService instanceService;

    public ProjectController(ProjectService projectService, InstanceService instanceService) {
        this.projectService = projectService;
        this.instanceService = instanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectDto>>> getProjects(
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String ownerTeam,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProjectDto> result = PageResponse.from(projectService.getProjectsFiltered(environment, ownerTeam, search, pageable));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getAllProjects() {
        List<ProjectDto> list = projectService.getAllProjects().stream()
                .map(ProjectDto::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> getProject(@PathVariable String id) {
        return projectService.getProjectById(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(p)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Project not found with ID: " + id)));
    }

    @GetMapping("/{id}/instances")
    public ResponseEntity<ApiResponse<List<InstanceDto>>> getProjectInstances(@PathVariable String id) {
        List<InstanceDto> instances = instanceService.getInstancesByProject(id);
        return ResponseEntity.ok(ApiResponse.ok(instances));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        ProjectDto created = projectService.createProject(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Project created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
            @PathVariable String id,
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        ProjectDto updated = projectService.updateProject(id, request, username);
        return ResponseEntity.ok(ApiResponse.ok("Project updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable String id,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "system";
        projectService.deleteProject(id, username);
        return ResponseEntity.ok(ApiResponse.ok("Project deleted successfully", null));
    }

    @GetMapping("/meta/filters")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getFilters() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "environments", projectService.getDistinctEnvironments(),
                "ownerTeams", projectService.getDistinctOwnerTeams()
        )));
    }
}
