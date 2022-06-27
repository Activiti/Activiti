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


package org.activiti.spring.autodeployment;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.core.common.spring.project.ApplicationUpgradeContextService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.core.io.Resource;

/**
 * Implementation of {@link AutoDeploymentStrategy} that performs a separate deployment for each set of {@link Resource}s that share the same parent folder. The namehint is used to prefix the names of
 * deployments. If the parent folder for a {@link Resource} cannot be determined, the resource's name is used.
 *

 */
public class ResourceParentFolderAutoDeploymentStrategy extends AbstractAutoDeploymentStrategy {

  /**
   * The deployment mode this strategy handles.
   */
  public static final String DEPLOYMENT_MODE = "resource-parent-folder";

  private static final String DEPLOYMENT_NAME_PATTERN = "%s.%s";

  public ResourceParentFolderAutoDeploymentStrategy(ApplicationUpgradeContextService applicationUpgradeContextService) {
      super(applicationUpgradeContextService);
  }

  @Override
  protected String getDeploymentMode() {
    return DEPLOYMENT_MODE;
  }

  @Override
  public void deployResources(final String deploymentNameHint, final Resource[] resources, final RepositoryService repositoryService) {

    // Create a deployment for each distinct parent folder using the name
    // hint
    // as a prefix
    final Map<String, Set<Resource>> resourcesMap = createMap(resources);

    for (final Entry<String, Set<Resource>> group : resourcesMap.entrySet()) {

      final String deploymentName = determineDeploymentName(deploymentNameHint, group.getKey());

      DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().enableDuplicateFiltering().name(deploymentName);

      for (final Resource resource : group.getValue()) {
        final String resourceName = determineResourceName(resource);

        deploymentBuilder.addInputStream(resourceName,
                                         resource);
      }

      loadApplicationUpgradeContext(deploymentBuilder).deploy();
    }

  }

  private Map<String, Set<Resource>> createMap(final Resource[] resources) {
    final Map<String, Set<Resource>> resourcesMap = new HashMap<String, Set<Resource>>();

    for (final Resource resource : resources) {
      final String parentFolderName = determineGroupName(resource);
      if (resourcesMap.get(parentFolderName) == null) {
        resourcesMap.put(parentFolderName, new HashSet<Resource>());
      }
      resourcesMap.get(parentFolderName).add(resource);
    }
    return resourcesMap;
  }

  private String determineGroupName(final Resource resource) {
    String result = determineResourceName(resource);
    try {
      if (resourceParentIsDirectory(resource)) {
        result = resource.getFile().getParentFile().getName();
      }
    } catch (IOException e) {
      // no-op, fallback to resource name
    }
    return result;
  }

  private boolean resourceParentIsDirectory(final Resource resource) throws IOException {
    return resource.getFile() != null && resource.getFile().getParentFile() != null && resource.getFile().getParentFile().isDirectory();
  }

  private String determineDeploymentName(final String deploymentNameHint, final String groupName) {
    return String.format(DEPLOYMENT_NAME_PATTERN, deploymentNameHint, groupName);
  }
}
