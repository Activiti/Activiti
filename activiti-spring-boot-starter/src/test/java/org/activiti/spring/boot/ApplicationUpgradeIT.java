package org.activiti.spring.boot;

import org.activiti.core.common.project.model.ProjectManifest;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.*;

public class ApplicationUpgradeIT {

    private static ConfigurableApplicationContext rbCtx1;

    @BeforeClass
    public static void setUp() {
        rbCtx1 = new SpringApplicationBuilder(Application.class).properties("server.port=8081")
                .run();
    }

    @AfterClass
    public static void tearDown() {
        rbCtx1.close();
    }

    @Test
    public void contextLoads() {
        assertThat(rbCtx1).isNotNull();
    }

    @Test
    public void should_UpdateDeploymentVersion_When_ManifestIsPresent() {

        RepositoryService repositoryService = rbCtx1.getBean(RepositoryService.class);

        ProjectManifest projectManifest = new ProjectManifest();
        projectManifest.setVersion("7");


        Deployment deployment1 = repositoryService.createDeployment()
                .setProjectManifest(projectManifest)
                .name("deploymentName")
                .deploy();

        assertThat(deployment1.getVersion()).isEqualTo(1);
        assertThat(deployment1.getProjectReleaseVersion()).isEqualTo("7");

        projectManifest.setVersion("17");

        Deployment deployment2 = repositoryService.createDeployment()
                .setProjectManifest(projectManifest)
                .name("deploymentName")
                .deploy();

        assertThat(deployment2.getProjectReleaseVersion()).isEqualTo("17");
        assertThat(deployment2.getVersion()).isEqualTo(2);

    }

}
