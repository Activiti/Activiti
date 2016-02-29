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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
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
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.util.io.BytesStreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Joram Barrez
 */
public class DeploymentManager {

  protected DeploymentCache<ProcessDefinitionEntity> processDefinitionCache;
  protected DeploymentCache<BpmnModel> bpmnModelCache;
  protected ProcessDefinitionInfoCache processDefinitionInfoCache;
  protected DeploymentCache<Object> knowledgeBaseCache; // Needs to be object to avoid an import to Drools in this core class
  protected List<Deployer> deployers;
  
  public void deploy(DeploymentEntity deployment) {
    deploy(deployment, null);
  }
  
  public void deploy(DeploymentEntity deployment, Map<String, Object> deploymentSettings) {
    for (Deployer deployer: deployers) {
      deployer.deploy(deployment, deploymentSettings);
    }
  }

  public ProcessDefinitionEntity findDeployedProcessDefinitionById(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("Invalid process definition id : null");
    }
    
    // first try the cache
    ProcessDefinitionEntity processDefinition = processDefinitionCache.get(processDefinitionId);
    
    if (processDefinition == null) {
      processDefinition = Context.getCommandContext()
        .getProcessDefinitionEntityManager()
        .findProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        throw new ActivitiObjectNotFoundException("no deployed process definition found with id '" + processDefinitionId + "'", ProcessDefinition.class);
      }
      processDefinition = resolveProcessDefinition(processDefinition);
    }
    return processDefinition;
  }
  
  public ProcessDefinitionEntity findProcessDefinitionByIdFromDatabase(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("Invalid process definition id : null");
    }
    
    ProcessDefinitionEntity processDefinition = Context.getCommandContext()
        .getProcessDefinitionEntityManager()
        .findProcessDefinitionById(processDefinitionId);
    
    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("no deployed process definition found with id '" + processDefinitionId + "'", ProcessDefinition.class);
    }
    
    return processDefinition;
  }
  
  public boolean isProcessDefinitionSuspended(String processDefinitionId) {
    return findProcessDefinitionByIdFromDatabase(processDefinitionId).isSuspended();
  }
  
  public BpmnModel getBpmnModelById(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("Invalid process definition id : null");
    }
    
    // first try the cache
    BpmnModel bpmnModel = bpmnModelCache.get(processDefinitionId);
    
    if (bpmnModel == null) {
      ProcessDefinitionEntity processDefinition = findDeployedProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        throw new ActivitiObjectNotFoundException("no deployed process definition found with id '" + processDefinitionId + "'", ProcessDefinition.class);
      }
      
      // Fetch the resource
      String resourceName = processDefinition.getResourceName();
      ResourceEntity resource = Context.getCommandContext().getResourceEntityManager()
              .findResourceByDeploymentIdAndResourceName(processDefinition.getDeploymentId(), resourceName);
      if (resource == null) {
        if (Context.getCommandContext().getDeploymentEntityManager().findDeploymentById(processDefinition.getDeploymentId()) == null) {
          throw new ActivitiObjectNotFoundException("deployment for process definition does not exist: " 
              + processDefinition.getDeploymentId(), Deployment.class);
        } else {
          throw new ActivitiObjectNotFoundException("no resource found with name '" + resourceName 
                  + "' in deployment '" + processDefinition.getDeploymentId() + "'", InputStream.class);
        }
      }
      
      // Convert the bpmn 2.0 xml to a bpmn model
      BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
      bpmnModel = bpmnXMLConverter.convertToBpmnModel(new BytesStreamSource(resource.getBytes()), false, false);
      bpmnModelCache.add(processDefinition.getId(), bpmnModel);
    }
    return bpmnModel;
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

  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKeyAndTenantId(String processDefinitionKey, String tenantId) {
    ProcessDefinitionEntity processDefinition = Context
      .getCommandContext()
      .getProcessDefinitionEntityManager()
      .findLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
    if (processDefinition==null) {
      throw new ActivitiObjectNotFoundException("no processes deployed with key '"+processDefinitionKey+"' for tenant identifier '" + tenantId + "'", ProcessDefinition.class);
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
      deploy(deployment, null);
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
      
      // Since all process definitions are deleted by a single query, we should dispatch the events in this loop
      if (eventDispatcher.isEnabled()) {
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
    
    for (ProcessDefinition processDefinition : processDefinitions) {
      processDefinitionCache.remove(processDefinition.getId());
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
  
  public DeploymentCache<BpmnModel> getBpmnModelCache() {
    return bpmnModelCache;
  }

  public void setBpmnModelCache(DeploymentCache<BpmnModel> bpmnModelCache) {
    this.bpmnModelCache = bpmnModelCache;
  }

  public ProcessDefinitionInfoCache getProcessDefinitionInfoCache() {
    return processDefinitionInfoCache;
  }

  public void setProcessDefinitionInfoCache(ProcessDefinitionInfoCache processDefinitionInfoCache) {
    this.processDefinitionInfoCache = processDefinitionInfoCache;
  }

  public DeploymentCache<Object> getKnowledgeBaseCache() {
    return knowledgeBaseCache;
  }

  public void setKnowledgeBaseCache(DeploymentCache<Object> knowledgeBaseCache) {
    this.knowledgeBaseCache = knowledgeBaseCache;
  }
  
}
