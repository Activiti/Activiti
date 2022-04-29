/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.core.common.spring.project;

import java.io.IOException;

import org.activiti.core.common.project.model.ProjectManifest;
import org.activiti.core.common.spring.project.conf.ApplicationUpgradeContextAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ApplicationUpgradeContextAutoConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:application.properties")
public class ApplicationUpgradeContextServiceIT {

    @Autowired
    private ApplicationUpgradeContextService applicationUpgradeContextService;

    @Test
    public void should_retrieveManifest() throws IOException {

        assertThat(applicationUpgradeContextService.hasProjectManifest()).isTrue();

        ProjectManifest projectModel = applicationUpgradeContextService.loadProjectManifest();

        assertThat(projectModel.getCreatedBy()).isEqualTo("superadminuser");
        assertThat(projectModel.getCreationDate()).isEqualTo("2019-08-16T15:58:46.056+0000");
        assertThat(projectModel.getDescription()).isEqualTo("");
        assertThat(projectModel.getId()).isEqualTo("c519a458-539f-4385-a937-2edfb4045eb9");
        assertThat(projectModel.getLastModifiedBy()).isEqualTo("qa-modeler-1");
        assertThat(projectModel.getLastModifiedDate()).isEqualTo("2019-08-16T16:03:41.941+0000");
        assertThat(projectModel.getName()).isEqualTo("projectA");
        assertThat(projectModel.getVersion()).isEqualTo("2");
    }

    @Test
    public void should_haveEnforcedAppVersionSet() {
        assertThat(applicationUpgradeContextService.hasEnforcedAppVersion()).isTrue();
        assertThat(applicationUpgradeContextService.getEnforcedAppVersion()).isEqualTo(1);
    }

}
