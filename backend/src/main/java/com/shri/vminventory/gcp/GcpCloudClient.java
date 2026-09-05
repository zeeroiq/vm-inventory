package com.shri.vminventory.gcp;

import com.shri.vminventory.model.Instance;
import com.shri.vminventory.model.Project;

import java.util.List;
import java.util.Map;

public interface GcpCloudClient {
    /**
     * Discovers projects via GCP Resource Manager API.
     */
    List<Project> discoverProjects();

    /**
     * Discovers VM instances in a project via GCP Compute Engine API.
     */
    List<Instance> discoverInstances(Project project);

    /**
     * Fetches metadata for an asset via GCP Cloud Asset Inventory API.
     */
    Map<String, Object> fetchAssetMetadata(String resourceName);

    /**
     * Fetches a secret via GCP Secret Manager.
     */
    String getSecret(String secretId);
}
