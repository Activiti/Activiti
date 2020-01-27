package org.activiti.core.common.spring.project;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.activiti.core.common.spring.project.conf.ProjectModelAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectModelAutoConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {"project.manifest.file.path=null", "application.version=0"})
public class ProjectModelServiceNoManifestIT {

    @Autowired
    private ProjectModelService projectModelService;

    @Test
    public void should_ThrowException_When_NoManifestPresent() throws IOException {

        //given
        assertThat(projectModelService.hasProjectManifest()).isFalse();

        //when
        Throwable thrown = catchThrowable(() -> projectModelService.loadProjectManifest());

        //then
        assertThat(thrown)
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("manifest not found");
    }

    @Test
    public void should_Have_DefaultEnforcedAppVersion() {
        assertThat(projectModelService.hasEnforcedAppVersion()).isFalse();
    }

}
