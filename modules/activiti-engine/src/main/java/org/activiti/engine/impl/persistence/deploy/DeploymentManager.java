/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.persistence.deploy;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Joram Barrez
 */
public class DeploymentManager {

  protected DeploymentCache<ProcessDefinitionCacheEntry> processDefinitionCache;
  protected DeploymentCache<Object> knowledgeBaseCache; // Needs to be object to avoid an import to Drools in this core class
  protected List<Deployer> deployers;

  public void deploy(DeploymentEntity deployment) {
    deploy(deployment, null);
  }

  public void deploy(DeploymentEntity deployment, Map<String, Object> deploymentSettings) {
    for (Deployer deployer : deployers) {
      deployer.deploy(deployment, deploymentSettings);
    }
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionById(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("Invalid process definition id : null");
    }

    // first try the cache
    ProcessDefinitionCacheEntry cacheEntry = processDefinitionCache.get(processDefinitionId);
    ProcessDefinitionEntity processDefinition = cacheEntry != null ? cacheEntry.getProcessDefinitionEntity() : null;

    if (processDefinition == null) {
      processDefinition = Context.getCommandContext().getProcessDefinitionEntityManager().findProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        throw new ActivitiObjectNotFoundException("no deployed process definition found with id '" + processDefinitionId + "'", ProcessDefinition.class);
      }
      processDefinition = resolveProcessDefinition(processDefinition).getProcessDefinitionEntity();
    }
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKey(String processDefinitionKey) {
    ProcessDefinitionEntity processDefinition = Context.getCommandContext().getProcessDefinitionEntityManager().findLatestProcessDefinitionByKey(processDefinitionKey);

    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("no processes deployed with key '" + processDefinitionKey + "'", ProcessDefinition.class);
    }
    processDefinition = resolveProcessDefinition(processDefinition).getProcessDefinitionEntity();
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKeyAndTenantId(String processDefinitionKey, String tenantId) {
    ProcessDefinitionEntity processDefinition = Context.getCommandContext().getProcessDefinitionEntityManager().findLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("no processes deployed with key '" + processDefinitionKey + "' for tenant identifier '" + tenantId + "'", ProcessDefinition.class);
    }
    processDefinition = resolveProcessDefinition(processDefinition).getProcessDefinitionEntity();
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByKeyAndVersion(String processDefinitionKey, Integer processDefinitionVersion) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) Context.getCommandContext().getProcessDefinitionEntityManager()
        .findProcessDefinitionByKeyAndVersion(processDefinitionKey, processDefinitionVersion);
    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("no processes deployed with key = '" + processDefinitionKey + "' and version = '" + processDefinitionVersion + "'", ProcessDefinition.class);
    }
    processDefinition = resolveProcessDefinition(processDefinition).getProcessDefinitionEntity();
    return processDefinition;
  }

  /**
   * Resolving the process definition will fetch the BPMN 2.0, parse it and store the {@link BpmnModel} in memory.
   */
  public ProcessDefinitionCacheEntry resolveProcessDefinition(ProcessDefinitionEntity processDefinition) {
    String processDefinitionId = processDefinition.getId();
    String deploymentId = processDefinition.getDeploymentId();

    ProcessDefinitionCacheEntry cachedProcessDefinition = processDefinitionCache.get(processDefinitionId);

    if (cachedProcessDefinition == null) {
      DeploymentEntity deployment = Context.getCommandContext().getDeploymentEntityManager().findDeploymentById(deploymentId);
      deployment.setNew(false);
      deploy(deployment, null);
      cachedProcessDefinition = processDefinitionCache.get(processDefinitionId);

      if (cachedProcessDefinition == null) {
        throw new ActivitiException("deployment '" + deploymentId + "' didn't put process definition '" + processDefinitionId + "' in the cache");
      }
    }
    return cachedProcessDefinition;
  }

  public void removeDeployment(String deploymentId, boolean cascade) {
    CommandContext commandContext = Context.getCommandContext();
    DeploymentEntityManager deploymentEntityManager = commandContext.getDeploymentEntityManager();

    DeploymentEntity deployment = deploymentEntityManager.findDeploymentById(deploymentId);
    if (deployment == null) {
      throw new ActivitiObjectNotFoundException("Could not find a deployment with id '" + deploymentId + "'.", DeploymentEntity.class);
    }
    
    if (commandContext.getProcessEngineConfiguration().isActiviti5CompatibilityEnabled() && 
        Activiti5CompatibilityHandler.ACTIVITI_5_ENGINE_TAG.equals(deployment.getEngineVersion())) {
      
      commandContext.getProcessEngineConfiguration().getActiviti5CompatibilityHandler().deleteDeployment(deploymentId, cascade);
      return;
    }

    // Remove any process definition from the cache
    List<ProcessDefinition> processDefinitions = new ProcessDefinitionQueryImpl(commandContext).deploymentId(deploymentId).list();
    ActivitiEventDispatcher eventDispatcher = Context.getProcessEngineConfiguration().getEventDispatcher();

    for (ProcessDefinition processDefinition : processDefinitions) {

      // Since all process definitions are deleted by a single query, we should dispatch the events in this loop
      if (eventDispatcher.isEnabled()) {
        eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, processDefinition));
      }
    }

    // Delete data
    deploymentEntityManager.deleteDeployment(deploymentId, cascade);

    // Since we use a delete by query, delete-events are not automatically dispatched
    if (eventDispatcher.isEnabled()) {
      eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, deployment));
    }

    for (ProcessDefinition processDefinition : processDefinitions) {
      processDefinitionCache.remove(processDefinition.getId());
    }
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public List<Deployer> getDeployers() {
    return deployers;
  }

  public void setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
  }

  public DeploymentCache<ProcessDefinitionCacheEntry> getProcessDefinitionCache() {
    return processDefinitionCache;
  }

  public void setProcessDefinitionCache(DeploymentCache<ProcessDefinitionCacheEntry> processDefinitionCache) {
    this.processDefinitionCache = processDefinitionCache;
  }

  public DeploymentCache<Object> getKnowledgeBaseCache() {
    return knowledgeBaseCache;
  }

  public void setKnowledgeBaseCache(DeploymentCache<Object> knowledgeBaseCache) {
    this.knowledgeBaseCache = knowledgeBaseCache;
  }

}
