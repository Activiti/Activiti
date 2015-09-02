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
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * A utility class that hides the complexity of {@link ProcessDefinitionEntity} and {@link Process} lookup. Use this class rather than accessing the process definition cache or
 * {@link DeploymentManager} directly.
 * 
 * @author Joram Barrez
 */
public class ProcessDefinitionUtil {

  public static ProcessDefinitionEntity getProcessDefinitionEntity(String processDefinitionId) {
    return getProcessDefinitionEntity(processDefinitionId, false);
  }

  public static ProcessDefinitionEntity getProcessDefinitionEntity(String processDefinitionId, boolean checkCacheOnly) {
    if (checkCacheOnly) {
      ProcessDefinitionCacheEntry cacheEntry = Context.getProcessEngineConfiguration().getProcessDefinitionCache().get(processDefinitionId);
      if (cacheEntry != null) {
        return cacheEntry.getProcessDefinitionEntity();
      }
      return null;
    } else {
      // This will check the cache in the findDeployedProcessDefinitionById method
      return Context.getProcessEngineConfiguration().getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId);
    }
  }

  public static Process getProcess(String processDefinitionId) {
    if (Context.getProcessEngineConfiguration() == null) {
      return Activiti5Util.getActiviti5CompatibilityHandler().getProcessDefinitionProcessObject(processDefinitionId);
      
    } else {
      DeploymentManager deploymentManager = Context.getProcessEngineConfiguration().getDeploymentManager();
      
      // This will check the cache in the findDeployedProcessDefinitionById and resolveProcessDefinition method
      ProcessDefinitionEntity processDefinitionEntity = deploymentManager.findDeployedProcessDefinitionById(processDefinitionId);
      return deploymentManager.resolveProcessDefinition(processDefinitionEntity).getProcess();
    }
  }

  public static BpmnModel getBpmnModel(String processDefinitionId) {
    if (Context.getProcessEngineConfiguration() == null) {
      return Activiti5Util.getActiviti5CompatibilityHandler().getProcessDefinitionBpmnModel(processDefinitionId);
      
    } else {
      DeploymentManager deploymentManager = Context.getProcessEngineConfiguration().getDeploymentManager();
      
      // This will check the cache in the findDeployedProcessDefinitionById and resolveProcessDefinition method
      ProcessDefinitionEntity processDefinitionEntity = deploymentManager.findDeployedProcessDefinitionById(processDefinitionId);
      return deploymentManager.resolveProcessDefinition(processDefinitionEntity).getBpmnModel();
    }
  }
}
