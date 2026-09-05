package com.shri.vminventory.service;

import com.shri.vminventory.dto.CreateInstanceRequest;
import com.shri.vminventory.dto.InstanceDto;
import com.shri.vminventory.model.Deployment;
import com.shri.vminventory.model.Instance;
import com.shri.vminventory.model.Project;
import com.shri.vminventory.repository.DeploymentRepository;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InstanceServiceTest {

    @Mock
    private InstanceRepository instanceRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private DeploymentRepository deploymentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private InstanceService instanceService;

    private Project project;
    private Instance instance;
    private Deployment deployment;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id("proj-dev-1")
                .name("Test Project")
                .environment("Dev")
                .ownerTeam("Dev Team A")
                .build();

        instance = Instance.builder()
                .id("inst-001")
                .name("test-vm-01")
                .project(project)
                .zone("us-central1-a")
                .region("us-central1")
                .machineType("e2-standard-4")
                .internalIp("10.0.0.5")
                .externalIp("34.0.0.5")
                .status("RUNNING")
                .ownerTeam("Dev Team A")
                .environment("Dev")
                .cpuCores(4)
                .memoryMb(16384L)
                .lastUpdateTimestamp(LocalDateTime.now())
                .build();

        deployment = Deployment.builder()
                .id(1L)
                .applicationName("Test App")
                .artifactName("test-app.jar")
                .artifactVersion("1.0.0")
                .instance(instance)
                .build();
    }

    @Test
    void testGetAllInstances() {
        when(instanceRepository.findAll()).thenReturn(List.of(instance));
        List<Instance> list = instanceService.getAllInstances();
        assertEquals(1, list.size());
    }

    @Test
    void testGetInstancesByProject() {
        when(instanceRepository.findByProjectId("proj-dev-1")).thenReturn(List.of(instance));
        when(deploymentRepository.findTopByInstanceIdOrderByDeploymentTimestampDesc("inst-001"))
                .thenReturn(Optional.of(deployment));

        List<InstanceDto> list = instanceService.getInstancesByProject("proj-dev-1");
        assertEquals(1, list.size());
        assertEquals("test-app.jar", list.get(0).currentArtifact());
        assertEquals("1.0.0", list.get(0).currentVersion());
    }

    @Test
    void testGetInstancesFiltered() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(instanceRepository.findFiltered("proj-dev-1", "Dev", null, null, null, pageRequest))
                .thenReturn(new PageImpl<>(List.of(instance), pageRequest, 1));
        when(deploymentRepository.findTopByInstanceIdOrderByDeploymentTimestampDesc("inst-001"))
                .thenReturn(Optional.of(deployment));

        Page<InstanceDto> page = instanceService.getInstancesFiltered("proj-dev-1", "Dev", null, null, null, pageRequest);
        assertEquals(1, page.getTotalElements());
        assertEquals("inst-001", page.getContent().get(0).id());
    }

    @Test
    void testGetInstanceById_Success() {
        when(instanceRepository.findById("inst-001")).thenReturn(Optional.of(instance));
        when(deploymentRepository.findTopByInstanceIdOrderByDeploymentTimestampDesc("inst-001"))
                .thenReturn(Optional.of(deployment));

        Optional<InstanceDto> result = instanceService.getInstanceById("inst-001");
        assertTrue(result.isPresent());
        assertEquals("test-vm-01", result.get().name());
    }

    @Test
    void testCreateInstance_Success() {
        CreateInstanceRequest req = new CreateInstanceRequest("inst-new", "new-vm", "proj-dev-1", "us-central1-a",
                "us-central1", "e2-medium", "10.0.0.6", "", "RUNNING", "env=dev", "Dev Team A", "Dev", 2, 4096L);

        when(instanceRepository.existsById("inst-new")).thenReturn(false);
        when(projectRepository.findById("proj-dev-1")).thenReturn(Optional.of(project));
        when(instanceRepository.save(any(Instance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InstanceDto created = instanceService.createInstance(req, "admin");
        assertNotNull(created);
        assertEquals("inst-new", created.id());
        verify(auditService, times(1)).record(eq("admin"), eq("CREATE_INSTANCE"), eq("INSTANCE"), eq("inst-new"), any(), isNull());
    }

    @Test
    void testCreateInstance_ProjectNotFound() {
        CreateInstanceRequest req = new CreateInstanceRequest("inst-new", "new-vm", "unknown", "us-central1-a",
                "us-central1", "e2-medium", "10.0.0.6", "", "RUNNING", "env=dev", "Dev Team A", "Dev", 2, 4096L);

        when(instanceRepository.existsById("inst-new")).thenReturn(false);
        when(projectRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> instanceService.createInstance(req, "admin"));
    }

    @Test
    void testUpdateInstance_Success() {
        CreateInstanceRequest req = new CreateInstanceRequest("inst-001", "renamed-vm", "proj-dev-1", "us-central1-b",
                "us-central1", "n2-standard-4", "10.0.0.5", "34.0.0.5", "RUNNING", "env=dev", "Dev Team A", "Dev", 4, 16384L);

        when(instanceRepository.findById("inst-001")).thenReturn(Optional.of(instance));
        when(instanceRepository.save(any(Instance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deploymentRepository.findTopByInstanceIdOrderByDeploymentTimestampDesc("inst-001"))
                .thenReturn(Optional.of(deployment));

        InstanceDto updated = instanceService.updateInstance("inst-001", req, "sre_lead");
        assertEquals("renamed-vm", updated.name());
        verify(auditService, times(1)).record(eq("sre_lead"), eq("UPDATE_INSTANCE"), eq("INSTANCE"), eq("inst-001"), any(), isNull());
    }

    @Test
    void testDeleteInstance_Success() {
        when(instanceRepository.findById("inst-001")).thenReturn(Optional.of(instance));

        instanceService.deleteInstance("inst-001", "admin");
        verify(instanceRepository, times(1)).delete(instance);
        verify(auditService, times(1)).record(eq("admin"), eq("DELETE_INSTANCE"), eq("INSTANCE"), eq("inst-001"), any(), isNull());
    }

    @Test
    void testExportInstancesCsv() {
        when(instanceRepository.findFilteredList(null, null, null, null)).thenReturn(List.of(instance));
        when(deploymentRepository.findTopByInstanceIdOrderByDeploymentTimestampDesc("inst-001"))
                .thenReturn(Optional.of(deployment));

        byte[] csv = instanceService.exportInstancesCsv(null, null, null, null);
        assertNotNull(csv);
        String csvContent = new String(csv, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("Instance ID,Instance Name"));
        assertTrue(csvContent.contains("inst-001"));
        assertTrue(csvContent.contains("test-app.jar"));
    }
}
