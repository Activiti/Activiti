package org.activiti.spring.boot;

import org.activiti.core.common.project.model.ProjectManifest;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ApplicationUpgradeIT {

    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void should_UpdateDeploymentVersion_When_ManifestIsPresent() {

        ProjectManifest projectManifest = new ProjectManifest();
        projectManifest.setVersion("7");


        Deployment deployment1 = repositoryService.createDeployment()
                .setProjectManifest(projectManifest)
                .enableDuplicateFiltering()
                .name("deploymentName")
                .deploy();

        assertThat(deployment1.getVersion()).isEqualTo(1);
        assertThat(deployment1.getProjectReleaseVersion()).isEqualTo("7");

        projectManifest.setVersion("17");

        Deployment deployment2 = repositoryService.createDeployment()
                .setProjectManifest(projectManifest)
                .enableDuplicateFiltering()
                .name("deploymentName")
                .deploy();

        assertThat(deployment2.getProjectReleaseVersion()).isEqualTo("17");
        assertThat(deployment2.getVersion()).isEqualTo(2);

    }

}
