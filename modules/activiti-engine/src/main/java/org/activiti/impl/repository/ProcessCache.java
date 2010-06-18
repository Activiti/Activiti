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
package org.activiti.impl.repository;

import java.util.HashMap;
import java.util.Map;

import org.activiti.ActivitiException;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.persistence.PersistenceSession;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ProcessCache {

  DeployerManager deployerManager;
  Map<String, ProcessDefinitionImpl> processDefinitions = new HashMap<String, ProcessDefinitionImpl>();
  
  public void setDeployerManager(DeployerManager deployerManager) {
    this.deployerManager = deployerManager;
  }

  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    PersistenceSession persistenceSession = CommandContext.getCurrent().getPersistenceSession();
    if (processDefinition.isNew()) {
      ProcessDefinitionImpl latestProcessDefinition = persistenceSession.findLatestProcessDefinitionByKey(processDefinition.getKey());
      if (latestProcessDefinition!=null) {
        processDefinition.setVersion(latestProcessDefinition.getVersion()+1);
      } else {
        processDefinition.setVersion(1);
      }
      processDefinition.setId(processDefinition.getKey()+":"+processDefinition.getVersion());
      persistenceSession.insertProcessDefinition(processDefinition);

    } else {
      String deploymentId = processDefinition.getDeployment().getId();
      ProcessDefinitionImpl persistedProcessDefinition = persistenceSession.findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinition.getKey());
      processDefinition.setId(persistedProcessDefinition.getId());
      processDefinition.setVersion(persistedProcessDefinition.getVersion());
    }

    processDefinitions.put(processDefinition.getId(), processDefinition);
  }

  public synchronized ProcessDefinitionImpl findProcessDefinitionById(String processDefinitionId) {
    ProcessDefinitionImpl processDefinition = processDefinitions.get(processDefinitionId);
    if (processDefinition==null) {
      CommandContext commandContext = CommandContext.getCurrent();
      PersistenceSession persistenceSession = commandContext.getPersistenceSession();
      DeploymentImpl deployment = persistenceSession.findDeploymentByProcessDefinitionId(processDefinitionId);
      if (deployment!=null) {
        deployerManager.deploy(deployment, commandContext);
      }
    }
    
    processDefinition = processDefinitions.get(processDefinitionId);
    if (processDefinition != null) {
      return processDefinition;
    } else {
     throw new ActivitiException("Couldn't find process definiton with id " + processDefinitionId); 
    }
  }

  public ProcessDefinitionImpl findProcessDefinitionByKey(String processDefinitionKey) {
    PersistenceSession persistenceSession = CommandContext.getCurrent().getPersistenceSession();
    ProcessDefinitionImpl processDefinition = persistenceSession.findLatestProcessDefinitionByKey(processDefinitionKey);
    if (processDefinition==null) {
      return null;
    }
    return findProcessDefinitionById(processDefinition.getId());
  }
  
  public void reset() {
    processDefinitions = new HashMap<String, ProcessDefinitionImpl>();
  }
  
}
