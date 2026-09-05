package com.shri.vminventory.service;

import com.shri.vminventory.dto.CreateInstanceRequest;
import com.shri.vminventory.dto.InstanceDto;
import com.shri.vminventory.model.Deployment;
import com.shri.vminventory.model.Instance;
import com.shri.vminventory.model.Project;
import com.shri.vminventory.repository.DeploymentRepository;
import com.shri.vminventory.repository.InstanceRepository;
import com.shri.vminventory.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InstanceService {

    private final InstanceRepository instanceRepository;
    private final ProjectRepository projectRepository;
    private final DeploymentRepository deploymentRepository;
    private final AuditService auditService;

    public InstanceService(InstanceRepository instanceRepository,
                           ProjectRepository projectRepository,
                           DeploymentRepository deploymentRepository,
                           AuditService auditService) {
        this.instanceRepository = instanceRepository;
        this.projectRepository = projectRepository;
        this.deploymentRepository = deploymentRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<Instance> getAllInstances() {
        return instanceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<InstanceDto> getInstancesByProject(String projectId) {
        return instanceRepository.findByProjectId(projectId).stream()
                .map(this::mapToDtoWithLatestDeployment)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<InstanceDto> getInstancesFiltered(String projectId,
                                                 String environment,
                                                 String ownerTeam,
                                                 String status,
                                                 String search,
                                                 Pageable pageable) {
        Page<Instance> page = instanceRepository.findFiltered(projectId, environment, ownerTeam, status, search, pageable);
        return page.map(this::mapToDtoWithLatestDeployment);
    }

    @Transactional(readOnly = true)
    public Optional<InstanceDto> getInstanceById(String id) {
        return instanceRepository.findById(id).map(this::mapToDtoWithLatestDeployment);
    }

    @Transactional
    public InstanceDto createInstance(CreateInstanceRequest request, String username) {
        if (instanceRepository.existsById(request.id())) {
            throw new IllegalArgumentException("Instance already exists with ID: " + request.id());
        }

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + request.projectId()));

        Instance instance = Instance.builder()
                .id(request.id())
                .name(request.name())
                .project(project)
                .zone(request.zone() != null ? request.zone() : "us-central1-a")
                .region(request.region() != null ? request.region() : "us-central1")
                .machineType(request.machineType() != null ? request.machineType() : "e2-standard-4")
                .internalIp(request.internalIp() != null ? request.internalIp() : "10.128.0.2")
                .externalIp(request.externalIp() != null ? request.externalIp() : "")
                .status(request.status() != null ? request.status() : "RUNNING")
                .labels(request.labels() != null ? request.labels() : "env=" + project.getEnvironment().toLowerCase())
                .ownerTeam(request.ownerTeam() != null ? request.ownerTeam() : project.getOwnerTeam())
                .environment(request.environment() != null ? request.environment() : project.getEnvironment())
                .lastUpdateTimestamp(LocalDateTime.now())
                .cpuCores(request.cpuCores() != null ? request.cpuCores() : 4)
                .memoryMb(request.memoryMb() != null ? request.memoryMb() : 16384L)
                .build();

        Instance saved = instanceRepository.save(instance);
        auditService.record(username, "CREATE_INSTANCE", "INSTANCE", saved.getId(),
                "Created VM instance " + saved.getName() + " in project " + project.getId(), null);

        return InstanceDto.from(saved);
    }

    @Transactional
    public InstanceDto updateInstance(String id, CreateInstanceRequest request, String username) {
        Instance existing = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instance not found with ID: " + id));

        existing.setName(request.name());
        existing.setZone(request.zone());
        existing.setRegion(request.region());
        existing.setMachineType(request.machineType());
        existing.setInternalIp(request.internalIp());
        existing.setExternalIp(request.externalIp());
        existing.setStatus(request.status());
        existing.setLabels(request.labels());
        existing.setOwnerTeam(request.ownerTeam());
        existing.setEnvironment(request.environment());
        existing.setLastUpdateTimestamp(LocalDateTime.now());
        if (request.cpuCores() != null) existing.setCpuCores(request.cpuCores());
        if (request.memoryMb() != null) existing.setMemoryMb(request.memoryMb());

        Instance saved = instanceRepository.save(existing);
        auditService.record(username, "UPDATE_INSTANCE", "INSTANCE", saved.getId(),
                "Updated instance " + saved.getName(), null);

        return mapToDtoWithLatestDeployment(saved);
    }

    @Transactional
    public void deleteInstance(String id, String username) {
        Instance existing = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instance not found with ID: " + id));

        instanceRepository.delete(existing);
        auditService.record(username, "DELETE_INSTANCE", "INSTANCE", id,
                "Deleted VM instance " + existing.getName(), null);
    }

    @Transactional(readOnly = true)
    public byte[] exportInstancesCsv(String projectId, String environment, String ownerTeam, String status) {
        List<Instance> instances = instanceRepository.findFilteredList(projectId, environment, ownerTeam, status);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            writer.println("Instance ID,Instance Name,Project ID,Environment,Owner Team,Status,Zone,Region,Machine Type,Internal IP,External IP,Deployed Artifact,Deployed Version,Last Update");
            for (Instance i : instances) {
                Optional<Deployment> dep = deploymentRepository.findTopByInstanceIdOrderByDeploymentTimestampDesc(i.getId());
                String artifact = dep.map(Deployment::getArtifactName).orElse("N/A");
                String version = dep.map(Deployment::getArtifactVersion).orElse("N/A");
                String projId = i.getProject() != null ? i.getProject().getId() : "N/A";

                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        escapeCsv(i.getId()),
                        escapeCsv(i.getName()),
                        escapeCsv(projId),
                        escapeCsv(i.getEnvironment()),
                        escapeCsv(i.getOwnerTeam()),
                        escapeCsv(i.getStatus()),
                        escapeCsv(i.getZone()),
                        escapeCsv(i.getRegion()),
                        escapeCsv(i.getMachineType()),
                        escapeCsv(i.getInternalIp()),
                        escapeCsv(i.getExternalIp()),
                        escapeCsv(artifact),
                        escapeCsv(version),
                        i.getLastUpdateTimestamp() != null ? i.getLastUpdateTimestamp().toString() : ""
                );
            }
        }
        return out.toByteArray();
    }

    private InstanceDto mapToDtoWithLatestDeployment(Instance i) {
        Optional<Deployment> dep = deploymentRepository.findTopByInstanceIdOrderByDeploymentTimestampDesc(i.getId());
        String art = dep.map(Deployment::getArtifactName).orElse(null);
        String ver = dep.map(Deployment::getArtifactVersion).orElse(null);
        return InstanceDto.from(i, art, ver);
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
