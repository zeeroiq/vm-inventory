package com.shri.vminventory.repository;

import com.shri.vminventory.model.Instance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstanceRepository extends JpaRepository<Instance, String> {

    List<Instance> findByProjectId(String projectId);

    long countByProjectId(String projectId);

    @Query("SELECT i FROM Instance i WHERE " +
           "(:projectId IS NULL OR :projectId = '' OR i.project.id = :projectId) AND " +
           "(:environment IS NULL OR :environment = '' OR i.environment = :environment) AND " +
           "(:ownerTeam IS NULL OR :ownerTeam = '' OR i.ownerTeam = :ownerTeam) AND " +
           "(:status IS NULL OR :status = '' OR i.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(i.id) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.internalIp) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.externalIp) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Instance> findFiltered(@Param("projectId") String projectId,
                                @Param("environment") String environment,
                                @Param("ownerTeam") String ownerTeam,
                                @Param("status") String status,
                                @Param("search") String search,
                                Pageable pageable);

    @Query("SELECT i FROM Instance i WHERE " +
           "(:projectId IS NULL OR :projectId = '' OR i.project.id = :projectId) AND " +
           "(:environment IS NULL OR :environment = '' OR i.environment = :environment) AND " +
           "(:ownerTeam IS NULL OR :ownerTeam = '' OR i.ownerTeam = :ownerTeam) AND " +
           "(:status IS NULL OR :status = '' OR i.status = :status)")
    List<Instance> findFilteredList(@Param("projectId") String projectId,
                                    @Param("environment") String environment,
                                    @Param("ownerTeam") String ownerTeam,
                                    @Param("status") String status);

    long countByOwnerTeamIgnoreCaseContaining(String ownerTeamKeyword);

    long countByStatusIgnoreCase(String status);

    long countByEnvironmentIgnoreCase(String environment);

    @Query("SELECT i.project.id, COUNT(i) FROM Instance i GROUP BY i.project.id")
    List<Object[]> countInstancesGroupedByProject();

    @Query("SELECT i.environment, COUNT(i) FROM Instance i GROUP BY i.environment")
    List<Object[]> countInstancesGroupedByEnvironment();
}
