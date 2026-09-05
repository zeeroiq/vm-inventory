package com.shri.vminventory.dto;

import com.shri.vminventory.model.Deployment;
import java.time.LocalDateTime;

public record DeploymentDto(
    Long id,
    String applicationName,
    String artifactName,
    String artifactVersion,
    String buildNumber,
    String gitCommitId,
    String branch,
    LocalDateTime deploymentTimestamp,
    String deploymentSource,
    String deployedBy,
    String deploymentStatus,
    String releaseNotes,
    String instanceId,
    String instanceName,
    String projectId
) {
    public static DeploymentDto from(Deployment d) {
        String instId = d.getInstance() != null ? d.getInstance().getId() : null;
        String instName = d.getInstance() != null ? d.getInstance().getName() : null;
        String projId = (d.getInstance() != null && d.getInstance().getProject() != null)
            ? d.getInstance().getProject().getId()
            : null;
        return new DeploymentDto(
            d.getId(),
            d.getApplicationName(),
            d.getArtifactName(),
            d.getArtifactVersion(),
            d.getBuildNumber(),
            d.getGitCommitId(),
            d.getBranch(),
            d.getDeploymentTimestamp(),
            d.getDeploymentSource(),
            d.getDeployedBy(),
            d.getDeploymentStatus(),
            d.getReleaseNotes(),
            instId,
            instName,
            projId
        );
    }
}
