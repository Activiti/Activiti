package org.activiti.core.common.spring.project;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.activiti.core.common.spring.project.conf.ApplicationContextAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationContextAutoConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = "project.manifest.file.path=null")
public class ApplicationContextServiceNoManifestIT {

    @Autowired
    private ApplicationContextService applicationContextService;

    @Test
    public void should_ThrowException_When_NoManifestPresent() throws IOException {

        //given
        assertThat(applicationContextService.hasProjectManifest()).isFalse();

        //when
        Throwable thrown = catchThrowable(() -> applicationContextService.loadProjectManifest());

        //then
        assertThat(thrown)
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("manifest not found");
    }
}
