/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.resources.DeploymentResourceLoader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessExtensionsCacheManagerIT {

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
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
    void testProcessExtensionsCache() {
        var processExtensionsCache = cacheManager.getCache("processExtensionsById");
        var deploymentResourcesCache = cacheManager.getCache("deploymentResourcesById");

        assertThat(deploymentResourcesCache).isNotNull();
        assertThat(processExtensionsCache).isNotNull();

        var result = processExtensionService.getExtensionsForId("processDefinitionId");

        assertThat(result).isNotNull();
    }


    @Test
    void testProcessExtensionsWithoutCallingDBCache() {
        var processExtensionsCache = cacheManager.getCache("processExtensionsById");
        var deploymentResourcesCache = cacheManager.getCache("deploymentResourcesById");

        assertThat(deploymentResourcesCache).isNotNull();
        assertThat(processExtensionsCache).isNotNull();

        ProcessDefinition processDefinition = new ProcessDefinition() {
            @Override
            public String getId() {
                return "processDefinitionId";
            }

            @Override
            public String getCategory() {
                return "";
            }

            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getKey() {
                return "";
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public int getVersion() {
                return 0;
            }

            @Override
            public String getResourceName() {
                return "";
            }

            @Override
            public String getDeploymentId() {
                return "";
            }

            @Override
            public String getDiagramResourceName() {
                return "";
            }

            @Override
            public boolean hasStartFormKey() {
                return false;
            }

            @Override
            public boolean hasGraphicalNotation() {
                return false;
            }

            @Override
            public boolean isSuspended() {
                return false;
            }

            @Override
            public String getTenantId() {
                return "";
            }

            @Override
            public String getEngineVersion() {
                return "";
            }

            @Override
            public void setAppVersion(Integer appVersion) {

            }

            @Override
            public Integer getAppVersion() {
                return 0;
            }
        };


        var result = processExtensionService.getExtensionsForWithoutCallingDB(processDefinition);

        assertThat(result).isNotNull();
    }
}
