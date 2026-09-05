package com.shri.vminventory.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "instances")
public class Instance {

    @Id
    private String id; // Instance ID / resource identifier

    @Column(nullable = false)
    private String name;

    private String zone;
    private String region;
    private String machineType;
    private String internalIp;
    private String externalIp;
    private String status; // RUNNING, STOPPED, TERMINATED, PROVISIONING

    @Column(length = 1000)
    private String labels; // JSON or key=val,key2=val2

    private String ownerTeam;
    private String environment;
    private LocalDateTime lastUpdateTimestamp;

    private Integer cpuCores;
    private Long memoryMb;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;

    public Instance() {
    }

    public Instance(String id, String name, String zone, String region, String machineType, String internalIp, String externalIp, String status, String labels, String ownerTeam, String environment, LocalDateTime lastUpdateTimestamp, Integer cpuCores, Long memoryMb, Project project) {
        this.id = id;
        this.name = name;
        this.zone = zone;
        this.region = region;
        this.machineType = machineType;
        this.internalIp = internalIp;
        this.externalIp = externalIp;
        this.status = status;
        this.labels = labels;
        this.ownerTeam = ownerTeam;
        this.environment = environment;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.cpuCores = cpuCores;
        this.memoryMb = memoryMb;
        this.project = project;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public String getInternalIp() {
        return internalIp;
    }

    public void setInternalIp(String internalIp) {
        this.internalIp = internalIp;
    }

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getOwnerTeam() {
        return ownerTeam;
    }

    public void setOwnerTeam(String ownerTeam) {
        this.ownerTeam = ownerTeam;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public LocalDateTime getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(LocalDateTime lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public Long getMemoryMb() {
        return memoryMb;
    }

    public void setMemoryMb(Long memoryMb) {
        this.memoryMb = memoryMb;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public static class Builder {
        private String id;
        private String name;
        private String zone;
        private String region;
        private String machineType;
        private String internalIp;
        private String externalIp;
        private String status = "RUNNING";
        private String labels;
        private String ownerTeam;
        private String environment;
        private LocalDateTime lastUpdateTimestamp = LocalDateTime.now();
        private Integer cpuCores = 2;
        private Long memoryMb = 4096L;
        private Project project;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder zone(String zone) {
            this.zone = zone;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder machineType(String machineType) {
            this.machineType = machineType;
            return this;
        }

        public Builder internalIp(String internalIp) {
            this.internalIp = internalIp;
            return this;
        }

        public Builder externalIp(String externalIp) {
            this.externalIp = externalIp;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder labels(String labels) {
            this.labels = labels;
            return this;
        }

        public Builder ownerTeam(String ownerTeam) {
            this.ownerTeam = ownerTeam;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder lastUpdateTimestamp(LocalDateTime lastUpdateTimestamp) {
            this.lastUpdateTimestamp = lastUpdateTimestamp;
            return this;
        }

        public Builder cpuCores(Integer cpuCores) {
            this.cpuCores = cpuCores;
            return this;
        }

        public Builder memoryMb(Long memoryMb) {
            this.memoryMb = memoryMb;
            return this;
        }

        public Builder project(Project project) {
            this.project = project;
            return this;
        }

        public Instance build() {
            return new Instance(id, name, zone, region, machineType, internalIp, externalIp, status, labels, ownerTeam, environment, lastUpdateTimestamp, cpuCores, memoryMb, project);
        }
    }
}
