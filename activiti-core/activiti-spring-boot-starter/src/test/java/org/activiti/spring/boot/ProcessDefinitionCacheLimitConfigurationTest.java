/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.deploy.DefaultDeploymentCache;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {"spring.activiti.process-definition-cache-limit=100"})
public class ProcessDefinitionCacheLimitConfigurationTest {

    @Autowired
    private SpringProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    private ActivitiProperties activitiProperties;

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void shouldConfigureProcessDefinitionCacheLimit() {
        assertThat(activitiProperties.getProcessDefinitionCacheLimit()).isEqualTo(100);

        assertThat(processEngineConfiguration.getProcessDefinitionCacheLimit()).isEqualTo(activitiProperties.getProcessDefinitionCacheLimit());
    }

    @Test
    public void shouldDeployAllProcessDefinitions() {
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isGreaterThan(100);
    }

    @Test
    public void shouldApplyProcessDefinitionCacheLimit() {
        var processDefinitionCache = (DefaultDeploymentCache<ProcessDefinitionCacheEntry>) processEngineConfiguration.getProcessDefinitionCache();

        assertThat(processDefinitionCache.size()).isEqualTo(100);
    }

}
