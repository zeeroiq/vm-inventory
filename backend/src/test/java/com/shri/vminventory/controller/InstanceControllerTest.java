package com.shri.vminventory.controller;

import com.shri.vminventory.model.Instance;
import com.shri.vminventory.model.Project;
import com.shri.vminventory.repository.DeploymentRepository;
import com.shri.vminventory.repository.InstanceRepository;
import com.shri.vminventory.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class InstanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;

    private Project project;
    private Instance instance;

    @BeforeEach
    void setup() {
        deploymentRepository.deleteAll();
        instanceRepository.deleteAll();
        projectRepository.deleteAll();

        project = Project.builder()
                .id("proj-test")
                .name("Test Project")
                .environment("Dev")
                .ownerTeam("Dev Team A")
                .build();
        projectRepository.save(project);

        instance = Instance.builder()
                .id("inst-001")
                .name("test-vm-01")
                .project(project)
                .zone("us-central1-a")
                .region("us-central1")
                .machineType("e2-standard-4")
                .internalIp("10.0.0.5")
                .externalIp("34.0.0.5")
                .status("RUNNING")
                .ownerTeam("Dev Team A")
                .environment("Dev")
                .lastUpdateTimestamp(LocalDateTime.now())
                .build();
        instanceRepository.save(instance);
    }

    @Test
    void testGetInstances() throws Exception {
        mockMvc.perform(get("/api/instances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value("inst-001"));
    }

    @Test
    void testGetInstanceById() throws Exception {
        mockMvc.perform(get("/api/instances/inst-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("test-vm-01"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateInstance() throws Exception {
        String json = """
            {
                "id": "inst-new-002",
                "name": "new-vm-02",
                "projectId": "proj-test",
                "zone": "us-central1-b",
                "region": "us-central1",
                "machineType": "e2-medium",
                "status": "RUNNING"
            }
            """;

        mockMvc.perform(post("/api/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("inst-new-002"));
    }

    @Test
    void testExportCsv() throws Exception {
        mockMvc.perform(get("/api/instances/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().contentType("text/csv"));
    }
}
