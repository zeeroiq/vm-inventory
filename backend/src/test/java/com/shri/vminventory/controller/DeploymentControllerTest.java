package com.shri.vminventory.controller;

import com.shri.vminventory.model.Deployment;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DeploymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;

    private Instance instance;

    @BeforeEach
    void setup() {
        deploymentRepository.deleteAll();
        instanceRepository.deleteAll();
        projectRepository.deleteAll();

        Project p = Project.builder().id("proj-dep").name("Project").build();
        projectRepository.save(p);

        instance = Instance.builder().id("inst-dep-1").name("dep-vm").project(p).build();
        instanceRepository.save(instance);

        Deployment d = Deployment.builder()
                .instance(instance)
                .applicationName("Payment App")
                .artifactName("payment.jar")
                .artifactVersion("2.0.0")
                .deploymentTimestamp(LocalDateTime.now())
                .deploymentStatus("SUCCESS")
                .build();
        deploymentRepository.save(d);
    }

    @Test
    void testGetDeployments() throws Exception {
        mockMvc.perform(get("/api/deployments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].artifactName").value("payment.jar"));
    }

    @Test
    void testRecordDeployment_ViaPost() throws Exception {
        String json = """
            {
                "instanceId": "inst-dep-1",
                "applicationName": "Payment API",
                "artifactName": "payment-api.jar",
                "artifactVersion": "2.1.0",
                "buildNumber": "#500",
                "gitCommitId": "789def0",
                "branch": "main",
                "deploymentSource": "Jenkins",
                "deploymentStatus": "SUCCESS"
            }
            """;

        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.artifactVersion").value("2.1.0"));
    }
}
