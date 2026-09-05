package com.shri.vminventory.service;

import com.shri.vminventory.dto.AgentDiscoveryReportRequest;
import com.shri.vminventory.dto.DeploymentDto;
import com.shri.vminventory.dto.RecordDeploymentRequest;
import com.shri.vminventory.model.Deployment;
import com.shri.vminventory.model.Instance;
import com.shri.vminventory.repository.DeploymentRepository;
import com.shri.vminventory.repository.InstanceRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeploymentServiceTest {

    @Mock
    private DeploymentRepository deploymentRepository;

    @Mock
    private InstanceRepository instanceRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DeploymentService deploymentService;

    private Instance instance;
    private Deployment deployment;

    @BeforeEach
    void setUp() {
        instance = Instance.builder()
                .id("inst-001")
                .name("payment-vm")
                .build();

        deployment = Deployment.builder()
                .id(100L)
                .instance(instance)
                .applicationName("Payment Service")
                .artifactName("payment-service.jar")
                .artifactVersion("3.0.0")
                .deploymentStatus("SUCCESS")
                .deploymentTimestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetDeploymentsByInstance() {
        when(deploymentRepository.findByInstanceIdOrderByDeploymentTimestampDesc("inst-001"))
                .thenReturn(List.of(deployment));

        List<DeploymentDto> list = deploymentService.getDeploymentsByInstance("inst-001");
        assertEquals(1, list.size());
        assertEquals("payment-service.jar", list.get(0).artifactName());
    }

    @Test
    void testGetDeploymentsFiltered() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(deploymentRepository.findFiltered("inst-001", "SUCCESS", null, null, pageRequest))
                .thenReturn(new PageImpl<>(List.of(deployment), pageRequest, 1));

        Page<DeploymentDto> page = deploymentService.getDeploymentsFiltered("inst-001", "SUCCESS", null, null, pageRequest);
        assertEquals(1, page.getTotalElements());
        assertEquals(100L, page.getContent().get(0).id());
    }

    @Test
    void testRecordDeployment_Success() {
        RecordDeploymentRequest req = new RecordDeploymentRequest(
                "inst-001", "Payment Service", "payment-service.jar", "3.0.1",
                "#241", "abc1234", "main", LocalDateTime.now(),
                "Jenkins", "dev-operator", "SUCCESS", "Bugfix release"
        );

        when(instanceRepository.findById("inst-001")).thenReturn(Optional.of(instance));
        when(deploymentRepository.save(any(Deployment.class))).thenAnswer(invocation -> {
            Deployment d = invocation.getArgument(0);
            d.setId(101L);
            return d;
        });

        DeploymentDto recorded = deploymentService.recordDeployment(req, "dev-operator");
        assertNotNull(recorded);
        assertEquals("3.0.1", recorded.artifactVersion());
        verify(instanceRepository, times(1)).save(instance);
        verify(auditService, times(1)).record(eq("dev-operator"), eq("RECORD_DEPLOYMENT"), eq("DEPLOYMENT"), eq("101"), any(), isNull());
    }

    @Test
    void testRecordDeployment_InstanceNotFound() {
        RecordDeploymentRequest req = new RecordDeploymentRequest(
                "unknown", "Payment", "pay.jar", "1.0",
                null, null, null, null, null, null, null, null
        );
        when(instanceRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> deploymentService.recordDeployment(req, "dev"));
    }

    @Test
    void testRecordAgentReport() {
        AgentDiscoveryReportRequest req = new AgentDiscoveryReportRequest(
                "inst-001", "Payment Service", "payment-service.jar", "3.0.2",
                "#242", "def5678", "main", "RUNNING"
        );

        when(instanceRepository.findById("inst-001")).thenReturn(Optional.of(instance));
        when(deploymentRepository.save(any(Deployment.class))).thenAnswer(invocation -> {
            Deployment d = invocation.getArgument(0);
            d.setId(102L);
            return d;
        });

        DeploymentDto recorded = deploymentService.recordAgentReport(req);
        assertNotNull(recorded);
        assertEquals("VM Lightweight Agent", recorded.deploymentSource());
        verify(instanceRepository, times(1)).save(instance);
    }
}
