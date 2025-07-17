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
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.cache.SpringProcessDefinitionCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.activiti.process-definition-cache-name=processDefinitions",
    "activiti.spring.cache-manager.caches.processDefinitions.caffeine.spec=maximumSize=100, expireAfterAccess=10m, recordStats"
})
public class SpringProcessDefinitionCacheConfigurationTest {

    @Autowired
    private SpringProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    private ActivitiProperties activitiProperties;

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void shouldConfigureProcessDefinitionCacheSpec() {
        assertThat(activitiProperties.getProcessDefinitionCacheName()).isEqualTo("processDefinitions");
    }

    @Test
    public void shouldDeployAllProcessDefinitions() {
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isGreaterThan(100);
    }

    @Test
    public void shouldApplyProcessDefinitionCacheSpec() {
        var springProcessDefinitionCache = (SpringProcessDefinitionCache) processEngineConfiguration.getProcessDefinitionCache();

        assertThat(((CaffeineCache) springProcessDefinitionCache.getDelegate()).getNativeCache().estimatedSize()).isEqualTo(100);
    }

}
