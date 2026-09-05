package com.shri.vminventory.service;

import com.shri.vminventory.dto.AgentDiscoveryReportRequest;
import com.shri.vminventory.dto.DeploymentDto;
import com.shri.vminventory.dto.RecordDeploymentRequest;
import com.shri.vminventory.model.Deployment;
import com.shri.vminventory.model.Instance;
import com.shri.vminventory.repository.DeploymentRepository;
import com.shri.vminventory.repository.InstanceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeploymentService {

    private final DeploymentRepository deploymentRepository;
    private final InstanceRepository instanceRepository;
    private final AuditService auditService;

    public DeploymentService(DeploymentRepository deploymentRepository,
                             InstanceRepository instanceRepository,
                             AuditService auditService) {
        this.deploymentRepository = deploymentRepository;
        this.instanceRepository = instanceRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<Deployment> getAllDeployments() {
        return deploymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<DeploymentDto> getDeploymentsFiltered(String instanceId,
                                                      String status,
                                                      String source,
                                                      String search,
                                                      Pageable pageable) {
        Page<Deployment> page = deploymentRepository.findFiltered(instanceId, status, source, search, pageable);
        return page.map(DeploymentDto::from);
    }

    @Transactional(readOnly = true)
    public List<DeploymentDto> getDeploymentsByInstance(String instanceId) {
        return deploymentRepository.findByInstanceIdOrderByDeploymentTimestampDesc(instanceId).stream()
                .map(DeploymentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DeploymentDto> getDeploymentById(Long id) {
        return deploymentRepository.findById(id).map(DeploymentDto::from);
    }

    @Transactional
    public DeploymentDto recordDeployment(RecordDeploymentRequest request, String username) {
        Instance instance = instanceRepository.findById(request.instanceId())
                .orElseThrow(() -> new IllegalArgumentException("Instance not found with ID: " + request.instanceId()));

        Deployment deployment = Deployment.builder()
                .instance(instance)
                .applicationName(request.applicationName())
                .artifactName(request.artifactName())
                .artifactVersion(request.artifactVersion())
                .buildNumber(request.buildNumber())
                .gitCommitId(request.gitCommitId())
                .branch(request.branch())
                .deploymentTimestamp(request.deploymentTimestamp() != null ? request.deploymentTimestamp() : LocalDateTime.now())
                .deploymentSource(request.deploymentSource() != null ? request.deploymentSource() : "CI/CD Pipeline")
                .deployedBy(request.deployedBy() != null ? request.deployedBy() : (username != null ? username : "ci-bot"))
                .deploymentStatus(request.deploymentStatus() != null ? request.deploymentStatus() : "SUCCESS")
                .releaseNotes(request.releaseNotes())
                .build();

        Deployment saved = deploymentRepository.save(deployment);

        // Update instance last updated timestamp
        instance.setLastUpdateTimestamp(LocalDateTime.now());
        instanceRepository.save(instance);

        auditService.record(username, "RECORD_DEPLOYMENT", "DEPLOYMENT", String.valueOf(saved.getId()),
                "Recorded deployment of " + saved.getArtifactName() + ":" + saved.getArtifactVersion() +
                        " on instance " + instance.getName(), null);

        return DeploymentDto.from(saved);
    }

    @Transactional
    public DeploymentDto recordAgentReport(AgentDiscoveryReportRequest report) {
        Instance instance = instanceRepository.findById(report.instanceId())
                .orElseThrow(() -> new IllegalArgumentException("Instance not found: " + report.instanceId()));

        Deployment deployment = Deployment.builder()
                .instance(instance)
                .applicationName(report.applicationName())
                .artifactName(report.artifactName())
                .artifactVersion(report.artifactVersion())
                .buildNumber(report.buildNumber())
                .gitCommitId(report.gitCommitId())
                .branch(report.branch())
                .deploymentTimestamp(LocalDateTime.now())
                .deploymentSource("VM Lightweight Agent")
                .deployedBy("vm-agent-daemon")
                .deploymentStatus(report.status() != null ? report.status() : "SUCCESS")
                .releaseNotes("Discovered via VM host agent heartbeat")
                .build();

        Deployment saved = deploymentRepository.save(deployment);
        instance.setLastUpdateTimestamp(LocalDateTime.now());
        instanceRepository.save(instance);

        return DeploymentDto.from(saved);
    }
}
