package com.shri.vminventory.scheduler;

import com.shri.vminventory.gcp.GcpCloudClient;
import com.shri.vminventory.model.Instance;
import com.shri.vminventory.model.Project;
import com.shri.vminventory.repository.InstanceRepository;
import com.shri.vminventory.repository.ProjectRepository;
import com.shri.vminventory.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GcpSyncSchedulerTest {

    @Mock
    private GcpCloudClient gcpCloudClient;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private InstanceRepository instanceRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private GcpSyncScheduler gcpSyncScheduler;

    private Project project;
    private Instance instance;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id("proj-gcp-sync")
                .name("GCP Project")
                .creationDate(LocalDateTime.now())
                .build();

        instance = Instance.builder()
                .id("inst-gcp-sync")
                .name("gcp-instance")
                .build();
    }

    @Test
    void testTriggerSync_Success() {
        when(gcpCloudClient.discoverProjects()).thenReturn(List.of(project));
        when(projectRepository.existsById("proj-gcp-sync")).thenReturn(false);
        when(gcpCloudClient.discoverInstances(project)).thenReturn(List.of(instance));

        GcpSyncScheduler.SyncReport report = gcpSyncScheduler.triggerSync("admin");
        assertNotNull(report);
        assertTrue(report.completed());
        assertEquals(1, report.syncedProjects());
        assertEquals(1, report.syncedInstances());
        assertEquals("SUCCESS", report.lastSyncStatus());

        verify(projectRepository, times(1)).save(project);
        verify(instanceRepository, times(1)).save(instance);
        verify(auditService, times(1)).record(eq("admin"), eq("GCP_SYNC"), eq("SYSTEM"), eq("ALL"), any(), isNull());
    }

    @Test
    void testGetStatus() {
        GcpSyncScheduler.SyncReport status = gcpSyncScheduler.getStatus();
        assertNotNull(status);
    }
}
