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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.context.Context;
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

  protected DeploymentCache<ProcessDefinitionEntity> processDefinitionCache;
  protected DeploymentCache<Object> knowledgeBaseCache; // Needs to be object to avoid an import to Drools in this core class
  protected List<Deployer> deployers;
  
  public void deploy(DeploymentEntity deployment) {
    for (Deployer deployer: deployers) {
      deployer.deploy(deployment);
    }
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionById(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("Invalid process definition id : null");
    }
    ProcessDefinitionEntity processDefinition = Context
      .getCommandContext()
      .getProcessDefinitionEntityManager()
      .findProcessDefinitionById(processDefinitionId);
    if(processDefinition == null) {
      throw new ActivitiObjectNotFoundException("no deployed process definition found with id '" + processDefinitionId + "'", ProcessDefinition.class);
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKey(String processDefinitionKey) {
    ProcessDefinitionEntity processDefinition = Context
      .getCommandContext()
      .getProcessDefinitionEntityManager()
      .findLatestProcessDefinitionByKey(processDefinitionKey);
    if (processDefinition==null) {
      throw new ActivitiObjectNotFoundException("no processes deployed with key '"+processDefinitionKey+"'", ProcessDefinition.class);
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionByKeyAndVersion(String processDefinitionKey, Integer processDefinitionVersion) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) Context
      .getCommandContext()
      .getProcessDefinitionEntityManager()
      .findProcessDefinitionByKeyAndVersion(processDefinitionKey, processDefinitionVersion);
    if (processDefinition==null) {
      throw new ActivitiObjectNotFoundException("no processes deployed with key = '" + processDefinitionKey + "' and version = '" + processDefinitionVersion + "'", ProcessDefinition.class);
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  public ProcessDefinitionEntity resolveProcessDefinition(ProcessDefinitionEntity processDefinition) {
    String processDefinitionId = processDefinition.getId();
    String deploymentId = processDefinition.getDeploymentId();
    processDefinition = processDefinitionCache.get(processDefinitionId);
    if (processDefinition==null) {
      DeploymentEntity deployment = Context
        .getCommandContext()
        .getDeploymentEntityManager()
        .findDeploymentById(deploymentId);
      deployment.setNew(false);
      deploy(deployment);
      processDefinition = processDefinitionCache.get(processDefinitionId);
      
      if (processDefinition==null) {
        throw new ActivitiException("deployment '"+deploymentId+"' didn't put process definition '"+processDefinitionId+"' in the cache");
      }
    }
    return processDefinition;
  }
  
  public void removeDeployment(String deploymentId, boolean cascade) {
	  DeploymentEntityManager deploymentEntityManager = Context
			  .getCommandContext()
			  .getDeploymentEntityManager();
	  
	  DeploymentEntity deployment = deploymentEntityManager.findDeploymentById(deploymentId); 
	  if(deployment == null)
		  throw new ActivitiObjectNotFoundException("Could not find a deployment with id '" + deploymentId + "'.", DeploymentEntity.class);

    // Remove any process definition from the cache
    List<ProcessDefinition> processDefinitions = new ProcessDefinitionQueryImpl(Context.getCommandContext())
            .deploymentId(deploymentId)
            .list();
    ActivitiEventDispatcher eventDispatcher = Context.getProcessEngineConfiguration().getEventDispatcher();
    
    for (ProcessDefinition processDefinition : processDefinitions) {
      processDefinitionCache.remove(processDefinition.getId());
      
      // Since all process definitions are deleted by a single query, we should dispatch the
      // events in this loop
      if(eventDispatcher.isEnabled()) {
      	eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(
      			ActivitiEventType.ENTITY_DELETED, processDefinition));
      }
    }
    
    // Delete data
    deploymentEntityManager.deleteDeployment(deploymentId, cascade);
    
    // Since we use a delete by query, delete-events are not automatically dispatched
    if(eventDispatcher.isEnabled()) {
    	eventDispatcher.dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, deployment));
    }
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public List<Deployer> getDeployers() {
    return deployers;
  }
  
  public void setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
  }

  public DeploymentCache<ProcessDefinitionEntity> getProcessDefinitionCache() {
    return processDefinitionCache;
  }
  
  public void setProcessDefinitionCache(DeploymentCache<ProcessDefinitionEntity> processDefinitionCache) {
    this.processDefinitionCache = processDefinitionCache;
  }
  
  public DeploymentCache<Object> getKnowledgeBaseCache() {
    return knowledgeBaseCache;
  }

  public void setKnowledgeBaseCache(DeploymentCache<Object> knowledgeBaseCache) {
    this.knowledgeBaseCache = knowledgeBaseCache;
  }
  
}
