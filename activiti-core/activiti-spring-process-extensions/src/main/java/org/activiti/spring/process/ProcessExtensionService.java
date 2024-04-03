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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.resources.DeploymentResourceLoader;

public class ProcessExtensionService {

  private final DeploymentResourceLoader<ProcessExtensionModel> processExtensionLoader;
  private final ProcessExtensionResourceReader processExtensionReader;
  private RepositoryService repositoryService;

  private static final Extension EMPTY_EXTENSIONS = new Extension();
  private final Map<String, Map<String, Extension>> processExtensionModelDeploymentMap = new HashMap<>();

  public ProcessExtensionService(
    DeploymentResourceLoader<ProcessExtensionModel> processExtensionLoader,
    ProcessExtensionResourceReader processExtensionReader) {
    this.processExtensionLoader = processExtensionLoader;
    this.processExtensionReader = processExtensionReader;
  }

  private Map<String, Extension> getProcessExtensionsForDeploymentId(String deploymentId) {

    var processExtensionModelMap = processExtensionModelDeploymentMap.get(deploymentId);
    if (processExtensionModelMap != null) {
      return processExtensionModelMap;
    }

    var processExtensionModels = processExtensionLoader.loadResourcesForDeployment(deploymentId,
      processExtensionReader);

    processExtensionModelMap = buildProcessDefinitionAndExtensionMap(processExtensionModels);

    processExtensionModelDeploymentMap.put(deploymentId, processExtensionModelMap);

    return processExtensionModelMap;
  }

  private Map<String, Extension> buildProcessDefinitionAndExtensionMap(
    List<ProcessExtensionModel> processExtensionModels) {

    var buildProcessExtensionMap = new HashMap<String, Extension>();

    for (var processExtensionModel : processExtensionModels) {
      buildProcessExtensionMap.putAll(processExtensionModel.getAllExtensions());
    }

    return buildProcessExtensionMap;
  }

  public Extension getExtensionsFor(ProcessDefinition processDefinition) {
    var processExtensionModelMap =
      getProcessExtensionsForDeploymentId(processDefinition.getDeploymentId());

    return Optional.ofNullable(processExtensionModelMap.get(processDefinition.getKey()))
      .orElse(EMPTY_EXTENSIONS);
  }

  public boolean hasExtensionsFor(ProcessDefinition processDefinition) {
    return !EMPTY_EXTENSIONS.equals(getExtensionsFor(processDefinition));
  }

  public Extension getExtensionsForId(String processDefinitionId) {
    var processDefinition =
      repositoryService.getProcessDefinition(processDefinitionId);

    return Optional.ofNullable(getExtensionsFor(processDefinition))
      .orElse(EMPTY_EXTENSIONS);
  }

  public void setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

}
