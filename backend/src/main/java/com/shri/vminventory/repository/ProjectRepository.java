package com.shri.vminventory.repository;

import com.shri.vminventory.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    @Query("SELECT p FROM Project p WHERE " +
           "(:environment IS NULL OR :environment = '' OR p.environment = :environment) AND " +
           "(:ownerTeam IS NULL OR :ownerTeam = '' OR p.ownerTeam = :ownerTeam) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(p.id) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.businessUnit) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Project> findFiltered(@Param("environment") String environment,
                               @Param("ownerTeam") String ownerTeam,
                               @Param("search") String search,
                               Pageable pageable);

    @Query("SELECT DISTINCT p.environment FROM Project p WHERE p.environment IS NOT NULL ORDER BY p.environment")
    List<String> findDistinctEnvironments();

    @Query("SELECT DISTINCT p.ownerTeam FROM Project p WHERE p.ownerTeam IS NOT NULL ORDER BY p.ownerTeam")
    List<String> findDistinctOwnerTeams();

    long countByEnvironment(String environment);
}
