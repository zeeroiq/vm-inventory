package com.shri.vminventory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    private String id; // Project ID from GCP (e.g., payments-dev)

    @Column(nullable = false)
    private String name;

    private String businessUnit;
    private String ownerTeam;
    private String environment;
    private String description;
    private LocalDateTime creationDate;

    public Project() {
    }

    public Project(String id, String name, String businessUnit, String ownerTeam, String environment, String description, LocalDateTime creationDate) {
        this.id = id;
        this.name = name;
        this.businessUnit = businessUnit;
        this.ownerTeam = ownerTeam;
        this.environment = environment;
        this.description = description;
        this.creationDate = creationDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getOwnerTeam() {
        return ownerTeam;
    }

    public void setOwnerTeam(String ownerTeam) {
        this.ownerTeam = ownerTeam;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public static class Builder {
        private String id;
        private String name;
        private String businessUnit;
        private String ownerTeam;
        private String environment;
        private String description;
        private LocalDateTime creationDate = LocalDateTime.now();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder businessUnit(String businessUnit) {
            this.businessUnit = businessUnit;
            return this;
        }

        public Builder ownerTeam(String ownerTeam) {
            this.ownerTeam = ownerTeam;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder creationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Project build() {
            return new Project(id, name, businessUnit, ownerTeam, environment, description, creationDate);
        }
    }
}
