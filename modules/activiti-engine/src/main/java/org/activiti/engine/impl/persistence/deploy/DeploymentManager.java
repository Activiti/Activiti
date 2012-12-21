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
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
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
      throw new ActivitiException("Invalid process definition id : null");
    }
    ProcessDefinitionEntity processDefinition = Context
      .getCommandContext()
      .getProcessDefinitionEntityManager()
      .findLatestProcessDefinitionById(processDefinitionId);
    if(processDefinition == null) {
      throw new ActivitiException("no deployed process definition found with id '" + processDefinitionId + "'");
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
      throw new ActivitiException("no processes deployed with key '"+processDefinitionKey+"'");
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
      throw new ActivitiException("no processes deployed with key = '" + processDefinitionKey + "' and version = '" + processDefinitionVersion + "'");
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
    // Remove any process definition from the cache
    List<ProcessDefinition> processDefinitions = new ProcessDefinitionQueryImpl(Context.getCommandContext())
            .deploymentId(deploymentId)
            .list();
    for (ProcessDefinition processDefinition : processDefinitions) {
      processDefinitionCache.remove(processDefinition.getId());
    }
    
    // Delete data
    Context
      .getCommandContext()
      .getDeploymentEntityManager()
      .deleteDeployment(deploymentId, cascade);
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
