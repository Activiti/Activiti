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
package org.activiti.spring.resources;

import static org.activiti.spring.resources.DeploymentResourceLoader.DEPLOYMENT_RESOURCES_CACHE_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.activiti.engine.RepositoryService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;

@CacheConfig(cacheNames = {DEPLOYMENT_RESOURCES_CACHE_NAME})
public class DeploymentResourceLoader<T> {

    public final static String DEPLOYMENT_RESOURCES_CACHE_NAME = "deploymentResourcesById";

    private RepositoryService repositoryService;

    @Cacheable(key = "#deploymentId")
    public List<T> loadResourcesForDeployment(String deploymentId, ResourceReader<T> resourceLoaderDescriptor) {
        List<T> resources;

        List<String> resourceNames = repositoryService.getDeploymentResourceNames(deploymentId);

        if (resourceNames != null && !resourceNames.isEmpty()) {

            List<String> selectedResources = resourceNames.stream()
                    .filter(resourceLoaderDescriptor.getResourceNameSelector())
                    .collect(Collectors.toList());

            resources = loadResources(deploymentId,
                    resourceLoaderDescriptor,
                    selectedResources);
        } else {
            resources = new ArrayList<>();
        }
        return resources;
    }

    private List<T> loadResources(String deploymentId,
                                  ResourceReader<T> resourceReader,
                                  List<String> selectedResources) {
        List<T> resources = new ArrayList<>();
        for (String name : selectedResources) {
            try (InputStream resourceAsStream = repositoryService.getResourceAsStream(deploymentId,
                    name)) {
                T resource = resourceReader.read(resourceAsStream);
                if (resource != null) {
                    resources.add(resource);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read process extension", e);
            }
        }
        return resources;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }
}
