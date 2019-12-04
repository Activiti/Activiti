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
package org.activiti.engine.impl.util;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * A utility class that hides the complexity of {@link ProcessDefinitionEntity} and {@link Process} lookup. Use this class rather than accessing the process definition cache or
 * {@link DeploymentManager} directly.
 * 

 */
public class ProcessDefinitionUtil {

  public static ProcessDefinition getProcessDefinition(String processDefinitionId) {
    return getProcessDefinition(processDefinitionId, false);
  }

  public static ProcessDefinition getProcessDefinition(String processDefinitionId, boolean checkCacheOnly) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if (checkCacheOnly) {
      ProcessDefinitionCacheEntry cacheEntry = processEngineConfiguration.getProcessDefinitionCache().get(processDefinitionId);
      if (cacheEntry != null) {
        return cacheEntry.getProcessDefinition();
      }
      return null;
      
    } else {
      // This will check the cache in the findDeployedProcessDefinitionById method
      return processEngineConfiguration.getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId);
    }
  }

  public static Process getProcess(String processDefinitionId) {
    DeploymentManager deploymentManager = Context.getProcessEngineConfiguration().getDeploymentManager();

    // This will check the cache in the findDeployedProcessDefinitionById and resolveProcessDefinition method
    ProcessDefinition processDefinitionEntity = deploymentManager.findDeployedProcessDefinitionById(processDefinitionId);
    return deploymentManager.resolveProcessDefinition(processDefinitionEntity).getProcess();
  }

  public static BpmnModel getBpmnModel(String processDefinitionId) {
    DeploymentManager deploymentManager = Context.getProcessEngineConfiguration().getDeploymentManager();

    // This will check the cache in the findDeployedProcessDefinitionById and resolveProcessDefinition method
    ProcessDefinition processDefinitionEntity = deploymentManager.findDeployedProcessDefinitionById(processDefinitionId);
    return deploymentManager.resolveProcessDefinition(processDefinitionEntity).getBpmnModel();
  }
  
  public static BpmnModel getBpmnModelFromCache(String processDefinitionId) {
    ProcessDefinitionCacheEntry cacheEntry = Context.getProcessEngineConfiguration().getProcessDefinitionCache().get(processDefinitionId);
    if (cacheEntry != null) {
      return cacheEntry.getBpmnModel();
    }
    return null;
  }
  
  public static boolean isProcessDefinitionSuspended(String processDefinitionId) {
    ProcessDefinitionEntity processDefinition = getProcessDefinitionFromDatabase(processDefinitionId);
    return processDefinition.isSuspended();
  }
  
  public static ProcessDefinitionEntity getProcessDefinitionFromDatabase(String processDefinitionId) {
    ProcessDefinitionEntityManager processDefinitionEntityManager = Context.getProcessEngineConfiguration().getProcessDefinitionEntityManager();
    ProcessDefinitionEntity processDefinition = processDefinitionEntityManager.findById(processDefinitionId);
    if (processDefinition == null) {
      throw new ActivitiException("No process definition found with id " + processDefinitionId);
    }
    
    return processDefinition;
  }
}
