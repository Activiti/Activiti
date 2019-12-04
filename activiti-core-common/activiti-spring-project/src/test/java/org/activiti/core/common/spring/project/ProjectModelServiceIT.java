package org.activiti.core.common.spring.project;

import java.io.IOException;

import org.activiti.core.common.project.model.ProjectManifest;
import org.activiti.core.common.spring.project.conf.ProjectModelAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectModelAutoConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:application.properties")
public class ProjectModelServiceIT {

    @Autowired
    private ProjectModelService projectModelService;

    @Test
    public void should_RetrieveManifest_When_Called() throws IOException {

        assertThat(projectModelService.hasProjectManifest()).isTrue();

        ProjectManifest projectModel = projectModelService.loadProjectManifest();

        assertThat(projectModel.getCreatedBy()).isEqualTo("superadminuser");
        assertThat(projectModel.getCreationDate()).isEqualTo("2019-08-16T15:58:46.056+0000");
        assertThat(projectModel.getDescription()).isEqualTo("");
        assertThat(projectModel.getId()).isEqualTo("c519a458-539f-4385-a937-2edfb4045eb9");
        assertThat(projectModel.getLastModifiedBy()).isEqualTo("qa-modeler-1");
        assertThat(projectModel.getLastModifiedDate()).isEqualTo("2019-08-16T16:03:41.941+0000");
        assertThat(projectModel.getName()).isEqualTo("projectA");
        assertThat(projectModel.getVersion()).isEqualTo("2");
    }
}