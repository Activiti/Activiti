package org.activiti.core.common.spring.project;

import java.io.FileNotFoundException;

import org.activiti.core.common.spring.project.conf.ApplicationUpgradeContextAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

@SpringBootTest(classes = ApplicationUpgradeContextAutoConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {"project.manifest.file.path=null", "application.version=0"})
public class ApplicationUpgradeContextServiceNullValuesIT {

    @Autowired
    private ApplicationUpgradeContextService applicationUpgradeContextService;

    @Test
    public void should_throwException_when_noManifestPresent() {

        assertThat(applicationUpgradeContextService.hasProjectManifest()).isFalse();
        Throwable thrown = catchThrowable(() -> applicationUpgradeContextService.loadProjectManifest());
        assertThat(thrown)
            .isInstanceOf(FileNotFoundException.class)
            .hasMessageContaining("manifest not found");
    }

    @Test
    public void should_notHaveDefaultEnforcedAppVersion() {
        assertThat(applicationUpgradeContextService.hasEnforcedAppVersion()).isFalse();
    }
}
