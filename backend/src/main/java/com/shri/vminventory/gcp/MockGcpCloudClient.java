package com.shri.vminventory.gcp;

import com.shri.vminventory.model.Instance;
import com.shri.vminventory.model.Project;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@ConditionalOnProperty(name = "gcp.client.mode", havingValue = "mock", matchIfMissing = true)
public class MockGcpCloudClient implements GcpCloudClient {

    @Override
    public List<Project> discoverProjects() {
        return List.of(
            Project.builder()
                .id("payments-dev")
                .name("Payments Service (Development)")
                .businessUnit("Core Banking")
                .ownerTeam("Dev Team A")
                .environment("Dev")
                .description("Microservice platform for payment gateway processing in development")
                .creationDate(LocalDateTime.now().minusMonths(6))
                .build(),
            Project.builder()
                .id("payments-prod")
                .name("Payments Service (Production)")
                .businessUnit("Core Banking")
                .ownerTeam("SRE Team")
                .environment("Production")
                .description("Production live traffic payment transactions")
                .creationDate(LocalDateTime.now().minusMonths(12))
                .build(),
            Project.builder()
                .id("checkout-qa")
                .name("Checkout Frontend & API (QA)")
                .businessUnit("E-Commerce")
                .ownerTeam("Dev Team B")
                .environment("QA")
                .description("QA verification environment for shopping cart and checkout services")
                .creationDate(LocalDateTime.now().minusMonths(4))
                .build(),
            Project.builder()
                .id("order-mgmt-uat")
                .name("Order Fulfillment (UAT)")
                .businessUnit("Supply Chain")
                .ownerTeam("Dev Team C")
                .environment("UAT")
                .description("User acceptance testing for fulfillment orchestrator")
                .creationDate(LocalDateTime.now().minusMonths(3))
                .build(),
            Project.builder()
                .id("analytics-staging")
                .name("Customer Analytics Engine (Staging)")
                .businessUnit("Data & AI")
                .ownerTeam("SRE Team")
                .environment("Staging")
                .description("Pre-production cluster for analytics and streaming data")
                .creationDate(LocalDateTime.now().minusMonths(5))
                .build(),
            Project.builder()
                .id("risk-engine-perf")
                .name("Fraud & Risk Detection (Performance)")
                .businessUnit("Risk Management")
                .ownerTeam("Dev Team A")
                .environment("Performance")
                .description("High-throughput load and stress testing cluster")
                .creationDate(LocalDateTime.now().minusMonths(2))
                .build()
        );
    }

    @Override
    public List<Instance> discoverInstances(Project project) {
        List<Instance> instances = new ArrayList<>();
        String env = project.getEnvironment();
        String team = project.getOwnerTeam();

        switch (project.getId()) {
            case "payments-dev" -> {
                instances.add(Instance.builder()
                    .id("inst-pay-dev-01")
                    .name("payment-api-vm-01")
                    .project(project)
                    .zone("us-central1-a")
                    .region("us-central1")
                    .machineType("n2-standard-4")
                    .internalIp("10.128.1.10")
                    .externalIp("34.120.45.101")
                    .status("RUNNING")
                    .labels("app=payment-api,tier=backend,env=dev")
                    .ownerTeam(team)
                    .environment(env)
                    .lastUpdateTimestamp(LocalDateTime.now().minusHours(2))
                    .cpuCores(4)
                    .memoryMb(16384L)
                    .build());
                instances.add(Instance.builder()
                    .id("inst-pay-dev-02")
                    .name("payment-worker-vm-01")
                    .project(project)
                    .zone("us-central1-b")
                    .region("us-central1")
                    .machineType("e2-medium")
                    .internalIp("10.128.1.11")
                    .externalIp("")
                    .status("RUNNING")
                    .labels("app=payment-worker,tier=worker,env=dev")
                    .ownerTeam(team)
                    .environment(env)
                    .lastUpdateTimestamp(LocalDateTime.now().minusHours(5))
                    .cpuCores(2)
                    .memoryMb(4096L)
                    .build());
            }
            case "payments-prod" -> {
                instances.add(Instance.builder()
                    .id("inst-pay-prod-01")
                    .name("payment-api-prod-01")
                    .project(project)
                    .zone("us-east1-b")
                    .region("us-east1")
                    .machineType("c2-standard-8")
                    .internalIp("10.140.2.20")
                    .externalIp("35.200.12.90")
                    .status("RUNNING")
                    .labels("app=payment-api,tier=backend,env=prod,criticality=tier-0")
                    .ownerTeam(team)
                    .environment(env)
                    .lastUpdateTimestamp(LocalDateTime.now().minusMinutes(30))
                    .cpuCores(8)
                    .memoryMb(32768L)
                    .build());
                instances.add(Instance.builder()
                    .id("inst-pay-prod-02")
                    .name("payment-api-prod-02")
                    .project(project)
                    .zone("us-east1-c")
                    .region("us-east1")
                    .machineType("c2-standard-8")
                    .internalIp("10.140.2.21")
                    .externalIp("35.200.12.91")
                    .status("RUNNING")
                    .labels("app=payment-api,tier=backend,env=prod,criticality=tier-0")
                    .ownerTeam(team)
                    .environment(env)
                    .lastUpdateTimestamp(LocalDateTime.now().minusMinutes(30))
                    .cpuCores(8)
                    .memoryMb(32768L)
                    .build());
            }
            case "checkout-qa" -> {
                instances.add(Instance.builder()
                    .id("inst-chk-qa-01")
                    .name("checkout-gateway-vm-01")
                    .project(project)
                    .zone("europe-west1-b")
                    .region("europe-west1")
                    .machineType("e2-standard-4")
                    .internalIp("10.150.3.15")
                    .externalIp("34.76.10.88")
                    .status("RUNNING")
                    .labels("app=checkout-service,tier=gateway,env=qa")
                    .ownerTeam(team)
                    .environment(env)
                    .lastUpdateTimestamp(LocalDateTime.now().minusHours(12))
                    .cpuCores(4)
                    .memoryMb(16384L)
                    .build());
            }
            case "order-mgmt-uat" -> {
                instances.add(Instance.builder()
                    .id("inst-ord-uat-01")
                    .name("order-fulfillment-vm-01")
                    .project(project)
                    .zone("us-central1-f")
                    .region("us-central1")
                    .machineType("e2-standard-2")
                    .internalIp("10.128.4.50")
                    .externalIp("")
                    .status("STOPPED")
                    .labels("app=order-fulfillment,tier=backend,env=uat")
                    .ownerTeam(team)
                    .environment(env)
                    .lastUpdateTimestamp(LocalDateTime.now().minusDays(1))
                    .cpuCores(2)
                    .memoryMb(8192L)
                    .build());
            }
            default -> {
                instances.add(Instance.builder()
                    .id("inst-" + project.getId() + "-01")
                    .name(project.getId() + "-vm-01")
                    .project(project)
                    .zone("us-central1-a")
                    .region("us-central1")
                    .machineType("e2-standard-4")
                    .internalIp("10.128.9.99")
                    .externalIp("")
                    .status("RUNNING")
                    .labels("env=" + env.toLowerCase())
                    .ownerTeam(team)
                    .environment(env)
                    .lastUpdateTimestamp(LocalDateTime.now().minusHours(1))
                    .cpuCores(4)
                    .memoryMb(16384L)
                    .build());
            }
        }
        return instances;
    }

    @Override
    public Map<String, Object> fetchAssetMetadata(String resourceName) {
        return Map.of(
            "assetType", "compute.googleapis.com/Instance",
            "name", "//compute.googleapis.com/" + resourceName,
            "version", "v1",
            "updateTime", LocalDateTime.now().toString()
        );
    }

    @Override
    public String getSecret(String secretId) {
        return "mock-secret-val-for-" + secretId;
    }
}
