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

package org.activiti.engine.impl.bpmn.deployer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionInfoCacheObject;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntityManager;

/**
 * Updates caches and artifacts for a deployment, its process definitions,
 * and its process definition infos.
 */
public class CachingAndArtifactsManager {

  /**
   * Ensures that the process definition is cached in the appropriate places, including the
   * deployment's collection of deployed artifacts and the deployment manager's cache, as well
   * as caching any ProcessDefinitionInfos.
   */
  public void updateCachingAndArtifacts(ParsedDeployment parsedDeployment) {
    CommandContext commandContext = Context.getCommandContext();
    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    DeploymentCache<ProcessDefinitionCacheEntry> processDefinitionCache
      = processEngineConfiguration.getDeploymentManager().getProcessDefinitionCache();
    DeploymentEntity deployment = parsedDeployment.getDeployment();

    for (ProcessDefinitionEntity processDefinition : parsedDeployment.getAllProcessDefinitions()) {
      BpmnModel bpmnModel = parsedDeployment.getBpmnModelForProcessDefinition(processDefinition);
      Process process = parsedDeployment.getProcessModelForProcessDefinition(processDefinition);
      ProcessDefinitionCacheEntry cacheEntry = new ProcessDefinitionCacheEntry(processDefinition, bpmnModel, process);
      processDefinitionCache.add(processDefinition.getId(), cacheEntry);
      addDefinitionInfoToCache(processDefinition, processEngineConfiguration, commandContext);

      // Add to deployment for further usage
      deployment.addDeployedArtifact(processDefinition);
    }
  }

  protected void addDefinitionInfoToCache(ProcessDefinitionEntity processDefinition,
      ProcessEngineConfigurationImpl processEngineConfiguration, CommandContext commandContext) {

    if (!processEngineConfiguration.isEnableProcessDefinitionInfoCache()) {
      return;
    }

    DeploymentManager deploymentManager = processEngineConfiguration.getDeploymentManager();
    ProcessDefinitionInfoEntityManager definitionInfoEntityManager = commandContext.getProcessDefinitionInfoEntityManager();
    ObjectMapper objectMapper = commandContext.getProcessEngineConfiguration().getObjectMapper();
    ProcessDefinitionInfoEntity definitionInfoEntity = definitionInfoEntityManager.findProcessDefinitionInfoByProcessDefinitionId(processDefinition.getId());

    ObjectNode infoNode = null;
    if (definitionInfoEntity != null && definitionInfoEntity.getInfoJsonId() != null) {
      byte[] infoBytes = definitionInfoEntityManager.findInfoJsonById(definitionInfoEntity.getInfoJsonId());
      if (infoBytes != null) {
        try {
          infoNode = (ObjectNode) objectMapper.readTree(infoBytes);
        } catch (Exception e) {
          throw new ActivitiException("Error deserializing json info for process definition " + processDefinition.getId());
        }
      }
    }

    ProcessDefinitionInfoCacheObject definitionCacheObject = new ProcessDefinitionInfoCacheObject();
    if (definitionInfoEntity == null) {
      definitionCacheObject.setRevision(0);
    } else {
      definitionCacheObject.setId(definitionInfoEntity.getId());
      definitionCacheObject.setRevision(definitionInfoEntity.getRevision());
    }

    if (infoNode == null) {
      infoNode = objectMapper.createObjectNode();
    }
    definitionCacheObject.setInfoNode(infoNode);

    deploymentManager.getProcessDefinitionInfoCache().add(processDefinition.getId(), definitionCacheObject);
  }
}
