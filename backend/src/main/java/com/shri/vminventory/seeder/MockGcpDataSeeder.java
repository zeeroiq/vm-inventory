package com.shri.vminventory.seeder;

import com.example.vminventory.model.*;
import com.shri.vminventory.model.*;
import com.shri.vminventory.repository.AuditLogRepository;
import com.shri.vminventory.repository.DeploymentRepository;
import com.shri.vminventory.repository.InstanceRepository;
import com.shri.vminventory.repository.ProjectRepository;
import com.shri.vminventory.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MockGcpDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MockGcpDataSeeder.class);

    private final ProjectRepository projectRepository;
    private final InstanceRepository instanceRepository;
    private final DeploymentRepository deploymentRepository;
    private final UserService userService;
    private final AuditLogRepository auditLogRepository;

    public MockGcpDataSeeder(ProjectRepository projectRepository,
                              InstanceRepository instanceRepository,
                              DeploymentRepository deploymentRepository,
                              UserService userService,
                              AuditLogRepository auditLogRepository) {
        this.projectRepository = projectRepository;
        this.instanceRepository = instanceRepository;
        this.deploymentRepository = deploymentRepository;
        this.userService = userService;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        if (projectRepository.count() == 0) {
            seedInventoryData();
        }
    }

    private void seedUsers() {
        userService.registerUser("admin", "Password123!", "admin@cloudenterprise.org", Role.ROLE_ADMIN, "Platform Architecture");
        userService.registerUser("sre_lead", "Password123!", "sre@cloudenterprise.org", Role.ROLE_SRE, "Site Reliability");
        userService.registerUser("dev_lead", "Password123!", "dev@cloudenterprise.org", Role.ROLE_DEVELOPER, "Core Payments");
        userService.registerUser("viewer", "Password123!", "auditor@cloudenterprise.org", Role.ROLE_VIEWER, "IT Compliance");
        log.info("Default enterprise users seeded: admin, sre_lead, dev_lead, viewer");
    }

    private void seedInventoryData() {
        log.info("Seeding GCP multi-project inventory data...");

        // Projects across all 6 environments
        Project p1 = Project.builder().id("payments-dev").name("Payment Service (Development)")
                .businessUnit("Retail Banking").ownerTeam("Dev Team A").environment("Dev")
                .description("Development environment for payment gateway").creationDate(LocalDateTime.now().minusMonths(6)).build();

        Project p2 = Project.builder().id("payments-prod").name("Payment Service (Production)")
                .businessUnit("Retail Banking").ownerTeam("SRE Team").environment("Production")
                .description("Tier-0 production payment transactions").creationDate(LocalDateTime.now().minusMonths(12)).build();

        Project p3 = Project.builder().id("checkout-qa").name("Checkout Cart (QA)")
                .businessUnit("E-Commerce").ownerTeam("Dev Team B").environment("QA")
                .description("Shopping cart test harness").creationDate(LocalDateTime.now().minusMonths(4)).build();

        Project p4 = Project.builder().id("order-mgmt-uat").name("Order Management (UAT)")
                .businessUnit("Supply Chain").ownerTeam("Dev Team C").environment("UAT")
                .description("User acceptance cluster for logistics").creationDate(LocalDateTime.now().minusMonths(3)).build();

        Project p5 = Project.builder().id("analytics-staging").name("Real-Time Analytics (Staging)")
                .businessUnit("Data & AI").ownerTeam("SRE Team").environment("Staging")
                .description("Staging cluster for streaming events").creationDate(LocalDateTime.now().minusMonths(5)).build();

        Project p6 = Project.builder().id("risk-engine-perf").name("Risk & Fraud Detection (Performance)")
                .businessUnit("Risk Management").ownerTeam("Dev Team A").environment("Performance")
                .description("Stress testing & benchmark cluster").creationDate(LocalDateTime.now().minusMonths(2)).build();

        projectRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6));

        // Instances
        Instance i1 = Instance.builder().id("inst-pay-dev-01").name("payment-api-vm-01")
                .zone("us-central1-a").region("us-central1").machineType("n2-standard-4")
                .internalIp("10.128.1.10").externalIp("34.120.45.101").status("RUNNING")
                .labels("app=payment-api,tier=backend,env=dev").ownerTeam("Dev Team A").environment("Dev")
                .lastUpdateTimestamp(LocalDateTime.now().minusHours(1)).cpuCores(4).memoryMb(16384L).project(p1).build();

        Instance i2 = Instance.builder().id("inst-pay-dev-02").name("payment-worker-vm-01")
                .zone("us-central1-b").region("us-central1").machineType("e2-medium")
                .internalIp("10.128.1.11").externalIp("").status("RUNNING")
                .labels("app=payment-worker,tier=worker,env=dev").ownerTeam("Dev Team A").environment("Dev")
                .lastUpdateTimestamp(LocalDateTime.now().minusHours(3)).cpuCores(2).memoryMb(4096L).project(p1).build();

        Instance i3 = Instance.builder().id("inst-pay-prod-01").name("payment-api-prod-01")
                .zone("us-east1-b").region("us-east1").machineType("c2-standard-8")
                .internalIp("10.140.2.20").externalIp("35.200.12.90").status("RUNNING")
                .labels("app=payment-api,tier=backend,env=prod,criticality=tier-0").ownerTeam("SRE Team").environment("Production")
                .lastUpdateTimestamp(LocalDateTime.now().minusMinutes(25)).cpuCores(8).memoryMb(32768L).project(p2).build();

        Instance i4 = Instance.builder().id("inst-pay-prod-02").name("payment-api-prod-02")
                .zone("us-east1-c").region("us-east1").machineType("c2-standard-8")
                .internalIp("10.140.2.21").externalIp("35.200.12.91").status("RUNNING")
                .labels("app=payment-api,tier=backend,env=prod,criticality=tier-0").ownerTeam("SRE Team").environment("Production")
                .lastUpdateTimestamp(LocalDateTime.now().minusMinutes(25)).cpuCores(8).memoryMb(32768L).project(p2).build();

        Instance i5 = Instance.builder().id("inst-chk-qa-01").name("checkout-gateway-vm-01")
                .zone("europe-west1-b").region("europe-west1").machineType("e2-standard-4")
                .internalIp("10.150.3.15").externalIp("34.76.10.88").status("RUNNING")
                .labels("app=checkout-service,tier=gateway,env=qa").ownerTeam("Dev Team B").environment("QA")
                .lastUpdateTimestamp(LocalDateTime.now().minusHours(12)).cpuCores(4).memoryMb(16384L).project(p3).build();

        Instance i6 = Instance.builder().id("inst-ord-uat-01").name("order-fulfillment-vm-01")
                .zone("us-central1-f").region("us-central1").machineType("e2-standard-2")
                .internalIp("10.128.4.50").externalIp("").status("STOPPED")
                .labels("app=order-fulfillment,tier=backend,env=uat").ownerTeam("Dev Team C").environment("UAT")
                .lastUpdateTimestamp(LocalDateTime.now().minusDays(1)).cpuCores(2).memoryMb(8192L).project(p4).build();

        Instance i7 = Instance.builder().id("inst-analytics-stg-01").name("analytics-worker-vm-01")
                .zone("us-central1-a").region("us-central1").machineType("n2-standard-8")
                .internalIp("10.128.6.10").externalIp("34.120.99.14").status("RUNNING")
                .labels("app=analytics-engine,tier=data,env=staging").ownerTeam("SRE Team").environment("Staging")
                .lastUpdateTimestamp(LocalDateTime.now().minusHours(2)).cpuCores(8).memoryMb(32768L).project(p5).build();

        Instance i8 = Instance.builder().id("inst-risk-perf-01").name("risk-model-vm-01")
                .zone("us-west1-a").region("us-west1").machineType("c2-standard-16")
                .internalIp("10.130.8.20").externalIp("35.203.11.45").status("RUNNING")
                .labels("app=risk-engine,tier=ml,env=perf").ownerTeam("Dev Team A").environment("Performance")
                .lastUpdateTimestamp(LocalDateTime.now().minusHours(4)).cpuCores(16).memoryMb(65536L).project(p6).build();

        instanceRepository.saveAll(List.of(i1, i2, i3, i4, i5, i6, i7, i8));

        // Deployments
        Deployment d1 = Deployment.builder().instance(i1)
                .applicationName("Payment Service").artifactName("payment-service.jar").artifactVersion("3.2.1-SNAPSHOT")
                .buildNumber("#245").gitCommitId("a1bc23d").branch("develop")
                .deploymentTimestamp(LocalDateTime.now().minusHours(2)).deploymentSource("Jenkins")
                .deployedBy("jenkins-ci").deploymentStatus("SUCCESS").releaseNotes("Added tokenized card processing").build();

        Deployment d2 = Deployment.builder().instance(i2)
                .applicationName("Payment Worker").artifactName("payment-worker.jar").artifactVersion("1.4.0")
                .buildNumber("#98").gitCommitId("c4de890").branch("feature/batch-settlement")
                .deploymentTimestamp(LocalDateTime.now().minusHours(5)).deploymentSource("Cloud Build")
                .deployedBy("dev-lead").deploymentStatus("SUCCESS").releaseNotes("Batch settlement performance fix").build();

        Deployment d3 = Deployment.builder().instance(i3)
                .applicationName("Payment Service").artifactName("payment-service.jar").artifactVersion("3.2.0")
                .buildNumber("#240").gitCommitId("9f8e7d6").branch("main")
                .deploymentTimestamp(LocalDateTime.now().minusDays(1)).deploymentSource("GitHub Actions")
                .deployedBy("sre-operator").deploymentStatus("SUCCESS").releaseNotes("Production release 3.2.0").build();

        Deployment d4 = Deployment.builder().instance(i4)
                .applicationName("Payment Service").artifactName("payment-service.jar").artifactVersion("3.2.0")
                .buildNumber("#240").gitCommitId("9f8e7d6").branch("main")
                .deploymentTimestamp(LocalDateTime.now().minusDays(1)).deploymentSource("GitHub Actions")
                .deployedBy("sre-operator").deploymentStatus("SUCCESS").releaseNotes("Production release 3.2.0 replica").build();

        Deployment d5 = Deployment.builder().instance(i5)
                .applicationName("Checkout Service").artifactName("checkout-api.jar").artifactVersion("2.1.0-RC1")
                .buildNumber("#310").gitCommitId("b2c3d4e").branch("release/2.1")
                .deploymentTimestamp(LocalDateTime.now().minusHours(14)).deploymentSource("Azure DevOps")
                .deployedBy("qa-automation").deploymentStatus("SUCCESS").releaseNotes("Candidate for sprint 14").build();

        Deployment d6 = Deployment.builder().instance(i6)
                .applicationName("Order Fulfillment").artifactName("order-service.jar").artifactVersion("1.8.5")
                .buildNumber("#112").gitCommitId("e5f6a7b").branch("hotfix/db-timeout")
                .deploymentTimestamp(LocalDateTime.now().minusDays(2)).deploymentSource("Jenkins")
                .deployedBy("dev_lead").deploymentStatus("FAILED").releaseNotes("Failed DB migration rollback triggered").build();

        Deployment d7 = Deployment.builder().instance(i7)
                .applicationName("Analytics Engine").artifactName("analytics-worker.jar").artifactVersion("4.0.0-BETA")
                .buildNumber("#502").gitCommitId("1a2b3c4").branch("feature/kafka-streaming")
                .deploymentTimestamp(LocalDateTime.now().minusHours(3)).deploymentSource("Cloud Build")
                .deployedBy("sre_lead").deploymentStatus("SUCCESS").releaseNotes("Kafka 3.7 streaming upgrade").build();

        Deployment d8 = Deployment.builder().instance(i8)
                .applicationName("Risk Detection").artifactName("risk-scorer.jar").artifactVersion("2.5.0")
                .buildNumber("#401").gitCommitId("7e8f9a0").branch("main")
                .deploymentTimestamp(LocalDateTime.now().minusHours(6)).deploymentSource("GitHub Actions")
                .deployedBy("dev_lead").deploymentStatus("SUCCESS").releaseNotes("Neural net inference update").build();

        deploymentRepository.saveAll(List.of(d1, d2, d3, d4, d5, d6, d7, d8));

        // Seed initial audit log
        auditLogRepository.save(AuditLog.builder()
                .timestamp(LocalDateTime.now().minusHours(1))
                .username("system-seeder")
                .action("SYSTEM_INITIALIZATION")
                .resourceType("SYSTEM")
                .resourceId("0")
                .details("Initialized enterprise seed database with 6 projects, 8 VMs, and RBAC users")
                .ipAddress("127.0.0.1")
                .build());

        log.info("Mock GCP Data Seeded successfully.");
    }
}
