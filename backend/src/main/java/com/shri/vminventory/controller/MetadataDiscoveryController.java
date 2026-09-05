package com.shri.vminventory.controller;

import com.shri.vminventory.dto.AgentDiscoveryReportRequest;
import com.shri.vminventory.dto.ApiResponse;
import com.shri.vminventory.dto.DeploymentDto;
import com.shri.vminventory.service.DeploymentService;
import jakarta.validation.Valid;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/discovery")
public class MetadataDiscoveryController {

    private final DeploymentService deploymentService;
    private final Optional<BuildProperties> buildProperties;

    public MetadataDiscoveryController(DeploymentService deploymentService,
                                       Optional<BuildProperties> buildProperties) {
        this.deploymentService = deploymentService;
        this.buildProperties = buildProperties;
    }

    /**
     * Option 1: Agent-based heartbeat & installed artifact ingestion
     */
    @PostMapping("/agent-report")
    public ResponseEntity<ApiResponse<DeploymentDto>> receiveAgentReport(
            @Valid @RequestBody AgentDiscoveryReportRequest request) {
        DeploymentDto saved = deploymentService.recordAgentReport(request);
        return ResponseEntity.ok(ApiResponse.ok("Agent report recorded", saved));
    }

    /**
     * Option 3: Startup Manifest / build metadata endpoint
     */
    @GetMapping("/manifest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getManifest() {
        String version = buildProperties.map(BuildProperties::getVersion).orElse("1.0.0");
        String name = buildProperties.map(BuildProperties::getName).orElse("vm-artifact-inventory");
        String time = buildProperties.map(b -> b.getTime().toString()).orElse(LocalDateTime.now().toString());

        Map<String, Object> manifest = Map.of(
                "applicationName", name,
                "version", version,
                "buildTime", time,
                "commitId", "a1bc23d",
                "branch", "main",
                "environment", "Production",
                "javaVersion", System.getProperty("java.version")
        );

        return ResponseEntity.ok(ApiResponse.ok(manifest));
    }
}
