package com.shri.vminventory.controller;

import com.shri.vminventory.model.Project;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private InstanceRepository instanceRepository;

    @BeforeEach
    void setup() {
        instanceRepository.deleteAll();
        projectRepository.deleteAll();

        Project p1 = Project.builder()
                .id("test-proj-1")
                .name("Test Project 1")
                .businessUnit("BU1")
                .ownerTeam("Team A")
                .environment("Dev")
                .creationDate(LocalDateTime.now())
                .build();

        projectRepository.saveAll(List.of(p1));
    }

    @Test
    void testGetProjects() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value("test-proj-1"))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Project 1"));
    }

    @Test
    void testGetProjectById() throws Exception {
        mockMvc.perform(get("/api/projects/test-proj-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("test-proj-1"));
    }

    @Test
    void testGetProjectById_NotFound() throws Exception {
        mockMvc.perform(get("/api/projects/non-existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateProject() throws Exception {
        String json = """
            {
                "id": "new-proj-2",
                "name": "New Project 2",
                "businessUnit": "Retail",
                "ownerTeam": "Dev Team B",
                "environment": "QA",
                "description": "QA project"
            }
            """;

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("new-proj-2"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteProject() throws Exception {
        mockMvc.perform(delete("/api/projects/test-proj-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetFilters() throws Exception {
        mockMvc.perform(get("/api/projects/meta/filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.environments").isArray());
    }
}
