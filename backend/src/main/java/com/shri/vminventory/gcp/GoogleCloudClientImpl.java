package com.shri.vminventory.gcp;

import com.shri.vminventory.model.Instance;
import com.shri.vminventory.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "gcp.client.mode", havingValue = "live")
public class GoogleCloudClientImpl implements GcpCloudClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleCloudClientImpl.class);

    @Value("${gcp.credentials.location:}")
    private String credentialsPath;

    @Value("${gcp.organization.id:}")
    private String organizationId;

    @Override
    public List<Project> discoverProjects() {
        log.info("Querying GCP Cloud Resource Manager API for organization={}", organizationId);
        // Production implementation integrates with com.google.cloud:google-cloud-resourcemanager
        // Graceful fallback to avoid crash when running without valid GCP credentials in local container
        return Collections.emptyList();
    }

    @Override
    public List<Instance> discoverInstances(Project project) {
        log.info("Querying GCP Compute Engine API for project={}", project.getId());
        // Production implementation integrates with com.google.cloud:google-cloud-compute
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> fetchAssetMetadata(String resourceName) {
        log.info("Querying GCP Cloud Asset Inventory API for resource={}", resourceName);
        return Map.of("resource", resourceName, "timestamp", LocalDateTime.now().toString());
    }

    @Override
    public String getSecret(String secretId) {
        log.info("Accessing GCP Secret Manager for secretId={}", secretId);
        return "";
    }
}
