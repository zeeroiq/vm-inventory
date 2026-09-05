package com.shri.vminventory.service;

import com.shri.vminventory.dto.DashboardSummaryDto;
import com.shri.vminventory.model.Deployment;
import com.shri.vminventory.model.Project;
import com.shri.vminventory.repository.DeploymentRepository;
import com.shri.vminventory.repository.InstanceRepository;
import com.shri.vminventory.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private InstanceRepository instanceRepository;

    @Mock
    private DeploymentRepository deploymentRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void testGetDashboardSummary() {
        when(projectRepository.count()).thenReturn(10L);
        when(instanceRepository.count()).thenReturn(25L);
        when(instanceRepository.countByOwnerTeamIgnoreCaseContaining("dev")).thenReturn(15L);
        when(instanceRepository.countByOwnerTeamIgnoreCaseContaining("sre")).thenReturn(10L);
        when(deploymentRepository.countByDeploymentStatusIgnoreCase("FAILED")).thenReturn(2L);
        when(instanceRepository.countByStatusIgnoreCase("RUNNING")).thenReturn(20L);
        when(instanceRepository.countByStatusIgnoreCase("STOPPED")).thenReturn(5L);

        Deployment d = Deployment.builder().id(1L).applicationName("App").artifactName("art.jar").artifactVersion("1.0").deploymentTimestamp(LocalDateTime.now()).build();
        when(deploymentRepository.findTop10ByOrderByDeploymentTimestampDesc()).thenReturn(List.of(d));

        List<Object[]> envCounts = new ArrayList<>();
        envCounts.add(new Object[]{"Dev", 10L});
        envCounts.add(new Object[]{"Production", 15L});
        when(instanceRepository.countInstancesGroupedByEnvironment()).thenReturn(envCounts);

        List<Object[]> projCounts = new ArrayList<>();
        projCounts.add(new Object[]{"proj-1", 5L});
        when(instanceRepository.countInstancesGroupedByProject()).thenReturn(projCounts);

        Project p = Project.builder().id("proj-1").name("Project One").build();
        when(projectRepository.findAll()).thenReturn(List.of(p));

        List<Object[]> versionCounts = new ArrayList<>();
        versionCounts.add(new Object[]{"1.0.0", 8L});
        when(deploymentRepository.countByArtifactVersionGrouped()).thenReturn(versionCounts);

        DashboardSummaryDto summary = dashboardService.getDashboardSummary();
        assertNotNull(summary);
        assertEquals(10L, summary.totalProjects());
        assertEquals(25L, summary.totalInstances());
        assertEquals(15L, summary.devInstances());
        assertEquals(10L, summary.sreInstances());
        assertEquals(2L, summary.deploymentFailures());
        assertEquals(1, summary.latestDeployments().size());
        assertEquals(2, summary.environmentBreakdown().size());
        assertEquals(1, summary.projectWiseInstanceCount().size());
        assertEquals("Project One", summary.projectWiseInstanceCount().get(0).label());
    }
}
