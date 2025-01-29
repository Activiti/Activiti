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
package org.activiti.spring.process;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.RepositoryService;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.resources.DeploymentResourceLoader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessExtensionsCacheManagerIT {

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private RepositoryService repositoryService;

    @Autowired
    DeploymentResourceLoader<ProcessExtensionModel> processExtensionLoader;

    @Autowired
    private ProcessExtensionService processExtensionService;

    @Test
    void testCacheManager() {
        assertThat(cacheManager.getCacheNames()).containsExactly("processExtensionsById", "deploymentResourcesById");
    }

    @Test
    @Disabled
    void testProcessExtensionsCache(){
        var processExtensionsCache = cacheManager.getCache("processExtensionsById");
        var deploymentResourcesCache = cacheManager.getCache("deploymentResourcesById");

        assertThat(deploymentResourcesCache).isNotNull();
        assertThat(processExtensionsCache).isNotNull();

        var result = processExtensionService.getExtensionsForId("processDefinitionId");

        assertThat(result).isNotNull();
    }

}
