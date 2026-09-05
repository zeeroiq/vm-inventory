package com.shri.vminventory.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deployments")
public class Deployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String applicationName;

    @Column(nullable = false)
    private String artifactName;

    @Column(nullable = false)
    private String artifactVersion;

    private String buildNumber;
    private String gitCommitId;
    private String branch;
    private LocalDateTime deploymentTimestamp;
    private String deploymentSource; // Jenkins, GitHub Actions, Azure DevOps, Cloud Build
    private String deployedBy;
    private String deploymentStatus; // SUCCESS, FAILED, IN_PROGRESS, ROLLED_BACK

    @Column(length = 2000)
    private String releaseNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id")
    @JsonIgnore
    private Instance instance;

    public Deployment() {
    }

    public Deployment(Long id, String applicationName, String artifactName, String artifactVersion, String buildNumber, String gitCommitId, String branch, LocalDateTime deploymentTimestamp, String deploymentSource, String deployedBy, String deploymentStatus, String releaseNotes, Instance instance) {
        this.id = id;
        this.applicationName = applicationName;
        this.artifactName = artifactName;
        this.artifactVersion = artifactVersion;
        this.buildNumber = buildNumber;
        this.gitCommitId = gitCommitId;
        this.branch = branch;
        this.deploymentTimestamp = deploymentTimestamp;
        this.deploymentSource = deploymentSource;
        this.deployedBy = deployedBy;
        this.deploymentStatus = deploymentStatus;
        this.releaseNotes = releaseNotes;
        this.instance = instance;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getArtifactVersion() {
        return artifactVersion;
    }

    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getGitCommitId() {
        return gitCommitId;
    }

    public void setGitCommitId(String gitCommitId) {
        this.gitCommitId = gitCommitId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public LocalDateTime getDeploymentTimestamp() {
        return deploymentTimestamp;
    }

    public void setDeploymentTimestamp(LocalDateTime deploymentTimestamp) {
        this.deploymentTimestamp = deploymentTimestamp;
    }

    public String getDeploymentSource() {
        return deploymentSource;
    }

    public void setDeploymentSource(String deploymentSource) {
        this.deploymentSource = deploymentSource;
    }

    public String getDeployedBy() {
        return deployedBy;
    }

    public void setDeployedBy(String deployedBy) {
        this.deployedBy = deployedBy;
    }

    public String getDeploymentStatus() {
        return deploymentStatus;
    }

    public void setDeploymentStatus(String deploymentStatus) {
        this.deploymentStatus = deploymentStatus;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public static class Builder {
        private Long id;
        private String applicationName;
        private String artifactName;
        private String artifactVersion;
        private String buildNumber;
        private String gitCommitId;
        private String branch;
        private LocalDateTime deploymentTimestamp = LocalDateTime.now();
        private String deploymentSource;
        private String deployedBy;
        private String deploymentStatus = "SUCCESS";
        private String releaseNotes;
        private Instance instance;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder artifactName(String artifactName) {
            this.artifactName = artifactName;
            return this;
        }

        public Builder artifactVersion(String artifactVersion) {
            this.artifactVersion = artifactVersion;
            return this;
        }

        public Builder buildNumber(String buildNumber) {
            this.buildNumber = buildNumber;
            return this;
        }

        public Builder gitCommitId(String gitCommitId) {
            this.gitCommitId = gitCommitId;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder deploymentTimestamp(LocalDateTime deploymentTimestamp) {
            this.deploymentTimestamp = deploymentTimestamp;
            return this;
        }

        public Builder deploymentSource(String deploymentSource) {
            this.deploymentSource = deploymentSource;
            return this;
        }

        public Builder deployedBy(String deployedBy) {
            this.deployedBy = deployedBy;
            return this;
        }

        public Builder deploymentStatus(String deploymentStatus) {
            this.deploymentStatus = deploymentStatus;
            return this;
        }

        public Builder releaseNotes(String releaseNotes) {
            this.releaseNotes = releaseNotes;
            return this;
        }

        public Builder instance(Instance instance) {
            this.instance = instance;
            return this;
        }

        public Deployment build() {
            return new Deployment(id, applicationName, artifactName, artifactVersion, buildNumber, gitCommitId, branch, deploymentTimestamp, deploymentSource, deployedBy, deploymentStatus, releaseNotes, instance);
        }
    }
}
