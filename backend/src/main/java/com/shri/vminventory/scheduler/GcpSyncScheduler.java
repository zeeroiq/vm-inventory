package com.shri.vminventory.scheduler;

import com.shri.vminventory.gcp.GcpCloudClient;
import com.shri.vminventory.model.Instance;
import com.shri.vminventory.model.Project;
import com.shri.vminventory.repository.InstanceRepository;
import com.shri.vminventory.repository.ProjectRepository;
import com.shri.vminventory.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class GcpSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(GcpSyncScheduler.class);

    private final GcpCloudClient gcpCloudClient;
    private final ProjectRepository projectRepository;
    private final InstanceRepository instanceRepository;
    private final AuditService auditService;

    private final AtomicBoolean isSyncing = new AtomicBoolean(false);
    private volatile LocalDateTime lastSyncTime = LocalDateTime.now();
    private volatile String lastSyncStatus = "IDLE";
    private volatile int lastSyncedProjects = 0;
    private volatile int lastSyncedInstances = 0;

    public GcpSyncScheduler(GcpCloudClient gcpCloudClient,
                            ProjectRepository projectRepository,
                            InstanceRepository instanceRepository,
                            AuditService auditService) {
        this.gcpCloudClient = gcpCloudClient;
        this.projectRepository = projectRepository;
        this.instanceRepository = instanceRepository;
        this.auditService = auditService;
    }

    @Scheduled(fixedDelayString = "${gcp.sync.interval:300000}", initialDelay = 60000)
    public void scheduledSync() {
        triggerSync("scheduled-cron");
    }

    public synchronized SyncReport triggerSync(String triggeredBy) {
        if (!isSyncing.compareAndSet(false, true)) {
            return new SyncReport(false, "Sync is already in progress", lastSyncTime, lastSyncStatus, lastSyncedProjects, lastSyncedInstances);
        }

        log.info("Starting GCP synchronization run triggered by {}", triggeredBy);
        try {
            int projectCount = 0;
            int instanceCount = 0;

            List<Project> discoveredProjects = gcpCloudClient.discoverProjects();
            for (Project p : discoveredProjects) {
                if (!projectRepository.existsById(p.getId())) {
                    projectRepository.save(p);
                }
                projectCount++;

                List<Instance> discoveredInstances = gcpCloudClient.discoverInstances(p);
                for (Instance inst : discoveredInstances) {
                    inst.setProject(p);
                    inst.setLastUpdateTimestamp(LocalDateTime.now());
                    instanceRepository.save(inst);
                    instanceCount++;
                }
            }

            lastSyncTime = LocalDateTime.now();
            lastSyncStatus = "SUCCESS";
            lastSyncedProjects = projectCount;
            lastSyncedInstances = instanceCount;

            auditService.record(triggeredBy, "GCP_SYNC", "SYSTEM", "ALL",
                    String.format("Synchronized %d projects and %d instances from GCP", projectCount, instanceCount), null);

            log.info("Completed GCP sync: {} projects, {} instances.", projectCount, instanceCount);
            return new SyncReport(true, "Sync completed successfully", lastSyncTime, lastSyncStatus, projectCount, instanceCount);
        } catch (Exception e) {
            log.error("GCP synchronization failed: {}", e.getMessage(), e);
            lastSyncTime = LocalDateTime.now();
            lastSyncStatus = "FAILED: " + e.getMessage();
            return new SyncReport(false, "Sync failed: " + e.getMessage(), lastSyncTime, lastSyncStatus, 0, 0);
        } finally {
            isSyncing.set(false);
        }
    }

    public SyncReport getStatus() {
        return new SyncReport(
                !isSyncing.get(),
                isSyncing.get() ? "SYNC_IN_PROGRESS" : lastSyncStatus,
                lastSyncTime,
                lastSyncStatus,
                lastSyncedProjects,
                lastSyncedInstances
        );
    }

    public record SyncReport(
            boolean completed,
            String message,
            LocalDateTime lastSyncTime,
            String lastSyncStatus,
            int syncedProjects,
            int syncedInstances
    ) {}
}
