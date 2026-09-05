package com.shri.vminventory.repository;

import com.shri.vminventory.model.Deployment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

    List<Deployment> findByInstanceIdOrderByDeploymentTimestampDesc(String instanceId);

    Optional<Deployment> findTopByInstanceIdOrderByDeploymentTimestampDesc(String instanceId);

    long countByDeploymentStatusIgnoreCase(String status);

    List<Deployment> findTop10ByOrderByDeploymentTimestampDesc();

    @Query("SELECT d FROM Deployment d WHERE " +
           "(:instanceId IS NULL OR :instanceId = '' OR d.instance.id = :instanceId) AND " +
           "(:status IS NULL OR :status = '' OR d.deploymentStatus = :status) AND " +
           "(:source IS NULL OR :source = '' OR d.deploymentSource = :source) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(d.applicationName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.artifactName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.artifactVersion) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.gitCommitId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Deployment> findFiltered(@Param("instanceId") String instanceId,
                                  @Param("status") String status,
                                  @Param("source") String source,
                                  @Param("search") String search,
                                  Pageable pageable);

    @Query("SELECT d.artifactVersion, COUNT(d) FROM Deployment d GROUP BY d.artifactVersion ORDER BY COUNT(d) DESC")
    List<Object[]> countByArtifactVersionGrouped();

    @Query("SELECT FUNCTION('to_char', d.deploymentTimestamp, 'YYYY-MM-DD'), COUNT(d) FROM Deployment d GROUP BY FUNCTION('to_char', d.deploymentTimestamp, 'YYYY-MM-DD') ORDER BY FUNCTION('to_char', d.deploymentTimestamp, 'YYYY-MM-DD') DESC")
    List<Object[]> countDeploymentsByDate();
}
