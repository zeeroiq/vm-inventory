package com.shri.vminventory.dto;

import java.util.List;
import java.util.Map;

public record DashboardSummaryDto(
    long totalProjects,
    long totalInstances,
    long devInstances,
    long sreInstances,
    long deploymentFailures,
    long runningInstances,
    long stoppedInstances,
    List<DeploymentDto> latestDeployments,
    Map<String, Long> environmentDistribution,
    List<ChartDataPoint> projectWiseInstanceCount,
    List<ChartDataPoint> environmentBreakdown,
    List<ChartDataPoint> artifactVersionDistribution,
    List<ChartDataPoint> deploymentTrend
) {}
