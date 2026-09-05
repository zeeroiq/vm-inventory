package com.shri.vminventory.service;

import com.shri.vminventory.dto.CreateProjectRequest;
import com.shri.vminventory.dto.ProjectDto;
import com.shri.vminventory.model.Project;
import com.shri.vminventory.repository.InstanceRepository;
import com.shri.vminventory.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final InstanceRepository instanceRepository;
    private final AuditService auditService;

    public ProjectService(ProjectRepository projectRepository,
                          InstanceRepository instanceRepository,
                          AuditService auditService) {
        this.projectRepository = projectRepository;
        this.instanceRepository = instanceRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<ProjectDto> getProjectsFiltered(String environment, String ownerTeam, String search, Pageable pageable) {
        Page<Project> page = projectRepository.findFiltered(environment, ownerTeam, search, pageable);

        // Fetch counts in one batch for performance
        List<Object[]> counts = instanceRepository.countInstancesGroupedByProject();
        Map<String, Long> countMap = new HashMap<>();
        for (Object[] row : counts) {
            String projId = (String) row[0];
            Long count = (Long) row[1];
            countMap.put(projId, count);
        }

        return page.map(p -> ProjectDto.from(p, countMap.getOrDefault(p.getId(), 0L)));
    }

    @Transactional(readOnly = true)
    public Optional<ProjectDto> getProjectById(String id) {
        return projectRepository.findById(id).map(p -> {
            long count = instanceRepository.countByProjectId(p.getId());
            return ProjectDto.from(p, count);
        });
    }

    @Transactional
    public ProjectDto createProject(CreateProjectRequest request, String username) {
        if (projectRepository.existsById(request.id())) {
            throw new IllegalArgumentException("Project already exists with ID: " + request.id());
        }

        Project project = Project.builder()
                .id(request.id())
                .name(request.name())
                .businessUnit(request.businessUnit())
                .ownerTeam(request.ownerTeam())
                .environment(request.environment() != null ? request.environment() : "Dev")
                .description(request.description())
                .creationDate(LocalDateTime.now())
                .build();

        Project saved = projectRepository.save(project);
        auditService.record(username, "CREATE_PROJECT", "PROJECT", saved.getId(),
                "Created project " + saved.getName(), null);

        return ProjectDto.from(saved, 0L);
    }

    @Transactional
    public ProjectDto updateProject(String id, CreateProjectRequest request, String username) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        existing.setName(request.name());
        existing.setBusinessUnit(request.businessUnit());
        existing.setOwnerTeam(request.ownerTeam());
        existing.setEnvironment(request.environment());
        existing.setDescription(request.description());

        Project saved = projectRepository.save(existing);
        auditService.record(username, "UPDATE_PROJECT", "PROJECT", saved.getId(),
                "Updated project metadata " + saved.getName(), null);

        long count = instanceRepository.countByProjectId(saved.getId());
        return ProjectDto.from(saved, count);
    }

    @Transactional
    public void deleteProject(String id, String username) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        projectRepository.delete(existing);
        auditService.record(username, "DELETE_PROJECT", "PROJECT", id,
                "Deleted project " + existing.getName(), null);
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctEnvironments() {
        return projectRepository.findDistinctEnvironments();
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctOwnerTeams() {
        return projectRepository.findDistinctOwnerTeams();
    }
}
