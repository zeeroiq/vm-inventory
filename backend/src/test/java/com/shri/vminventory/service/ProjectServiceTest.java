package com.shri.vminventory.service;

import com.shri.vminventory.dto.CreateProjectRequest;
import com.shri.vminventory.dto.ProjectDto;
import com.shri.vminventory.model.Project;
import com.shri.vminventory.repository.InstanceRepository;
import com.shri.vminventory.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private InstanceRepository instanceRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id("proj-payments")
                .name("Payments Service")
                .businessUnit("Banking")
                .ownerTeam("Dev Team A")
                .environment("Dev")
                .description("Payment service platform")
                .creationDate(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetAllProjects() {
        when(projectRepository.findAll()).thenReturn(List.of(testProject));

        List<Project> result = projectService.getAllProjects();
        assertEquals(1, result.size());
        assertEquals("proj-payments", result.get(0).getId());
    }

    @Test
    void testGetProjectById_Success() {
        when(projectRepository.findById("proj-payments")).thenReturn(Optional.of(testProject));
        when(instanceRepository.countByProjectId("proj-payments")).thenReturn(5L);

        Optional<ProjectDto> result = projectService.getProjectById("proj-payments");
        assertTrue(result.isPresent());
        assertEquals("proj-payments", result.get().id());
        assertEquals(5L, result.get().instanceCount());
    }

    @Test
    void testGetProjectById_NotFound() {
        when(projectRepository.findById("unknown")).thenReturn(Optional.empty());

        Optional<ProjectDto> result = projectService.getProjectById("unknown");
        assertFalse(result.isPresent());
    }

    @Test
    void testGetProjectsFiltered() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(projectRepository.findFiltered("Dev", "Dev Team A", null, pageRequest))
                .thenReturn(new PageImpl<>(List.of(testProject), pageRequest, 1));

        List<Object[]> counts = new ArrayList<>();
        counts.add(new Object[]{"proj-payments", 3L});
        when(instanceRepository.countInstancesGroupedByProject()).thenReturn(counts);

        Page<ProjectDto> result = projectService.getProjectsFiltered("Dev", "Dev Team A", null, pageRequest);
        assertEquals(1, result.getTotalElements());
        assertEquals(3L, result.getContent().get(0).instanceCount());
    }

    @Test
    void testCreateProject_Success() {
        CreateProjectRequest req = new CreateProjectRequest("proj-new", "New Project", "Retail", "Team X", "QA", "Desc");
        when(projectRepository.existsById("proj-new")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectDto result = projectService.createProject(req, "admin");
        assertNotNull(result);
        assertEquals("proj-new", result.id());
        verify(auditService, times(1)).record(eq("admin"), eq("CREATE_PROJECT"), eq("PROJECT"), eq("proj-new"), any(), isNull());
    }

    @Test
    void testCreateProject_AlreadyExists() {
        CreateProjectRequest req = new CreateProjectRequest("proj-payments", "Duplicate", "Retail", "Team", "Dev", "Desc");
        when(projectRepository.existsById("proj-payments")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(req, "admin"));
    }

    @Test
    void testUpdateProject_Success() {
        CreateProjectRequest req = new CreateProjectRequest("proj-payments", "Updated Name", "Updated BU", "Updated Team", "Staging", "New Desc");
        when(projectRepository.findById("proj-payments")).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(instanceRepository.countByProjectId("proj-payments")).thenReturn(2L);

        ProjectDto result = projectService.updateProject("proj-payments", req, "admin");
        assertEquals("Updated Name", result.name());
        assertEquals("Staging", result.environment());
        verify(auditService, times(1)).record(eq("admin"), eq("UPDATE_PROJECT"), eq("PROJECT"), eq("proj-payments"), any(), isNull());
    }

    @Test
    void testDeleteProject_Success() {
        when(projectRepository.findById("proj-payments")).thenReturn(Optional.of(testProject));

        projectService.deleteProject("proj-payments", "admin");
        verify(projectRepository, times(1)).delete(testProject);
        verify(auditService, times(1)).record(eq("admin"), eq("DELETE_PROJECT"), eq("PROJECT"), eq("proj-payments"), any(), isNull());
    }

    @Test
    void testGetDistinctFilters() {
        when(projectRepository.findDistinctEnvironments()).thenReturn(List.of("Dev", "Production"));
        when(projectRepository.findDistinctOwnerTeams()).thenReturn(List.of("Dev Team A", "SRE Team"));

        List<String> envs = projectService.getDistinctEnvironments();
        List<String> teams = projectService.getDistinctOwnerTeams();

        assertEquals(2, envs.size());
        assertEquals(2, teams.size());
    }
}
