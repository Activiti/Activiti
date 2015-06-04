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

package org.activiti.compatibility;

import java.util.HashMap;
import java.util.Map;

import org.activiti.compatibility.wrapper.Activiti5DeploymentWrapper;
import org.activiti.compatibility.wrapper.Activiti5ProcessInstanceWrapper;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti5.engine.ProcessEngine;
import org.activiti5.engine.repository.DeploymentBuilder;

/**
 * @author Joram Barrez
 */
public class DefaultActiviti5CompatibilityHandler implements Activiti5CompatibilityHandler {

  protected ProcessEngine processEngine;

  @Override
  public ProcessInstance startProcessInstance(String processDefinitionKey, String processDefinitionId, 
      Map<String, Object> variables, String businessKey, String tenantId, String processInstanceName) {
    
    org.activiti5.engine.runtime.ProcessInstance activiti5ProcessInstance 
        = getProcessEngine().getRuntimeService().startProcessInstanceByKey(processDefinitionKey);
    return new Activiti5ProcessInstanceWrapper(activiti5ProcessInstance);
    
  }
  
  @Override
  public Deployment deploy(DeploymentBuilderImpl activiti6DeploymentBuilder) {
    DeploymentBuilder deploymentBuilder = getProcessEngine().getRepositoryService().createDeployment();
    
    // Copy settings 
    
    deploymentBuilder.name(activiti6DeploymentBuilder.getDeployment().getName());
    deploymentBuilder.category(activiti6DeploymentBuilder.getDeployment().getCategory());
    deploymentBuilder.tenantId(activiti6DeploymentBuilder.getDeployment().getTenantId());
    
    // Copy flags 
    
    if (!activiti6DeploymentBuilder.isBpmn20XsdValidationEnabled()) {
      deploymentBuilder.disableSchemaValidation();
    }
    
    if (!activiti6DeploymentBuilder.isProcessValidationEnabled()) {
      deploymentBuilder.disableBpmnValidation();
    }
    
    if (activiti6DeploymentBuilder.isDuplicateFilterEnabled()) {
      deploymentBuilder.enableDuplicateFiltering();
    }
    
    if (activiti6DeploymentBuilder.getProcessDefinitionsActivationDate() != null) {
      deploymentBuilder.activateProcessDefinitionsOn(activiti6DeploymentBuilder.getProcessDefinitionsActivationDate());
    }

    // Copy resources
    DeploymentEntity activiti6DeploymentEntity = activiti6DeploymentBuilder.getDeployment();
    Map<String, org.activiti5.engine.impl.persistence.entity.ResourceEntity> activiti5Resources = new HashMap<String, org.activiti5.engine.impl.persistence.entity.ResourceEntity>();
    for (String resourceKey : activiti6DeploymentEntity.getResources().keySet()) {
      ResourceEntity activiti6ResourceEntity = activiti6DeploymentEntity.getResources().get(resourceKey);
      
      org.activiti5.engine.impl.persistence.entity.ResourceEntity activiti5ResourceEntity = new org.activiti5.engine.impl.persistence.entity.ResourceEntity();
      activiti5ResourceEntity.setName(activiti6ResourceEntity.getName());
      activiti5ResourceEntity.setBytes(activiti6ResourceEntity.getBytes());
      activiti5Resources.put(resourceKey, activiti5ResourceEntity);
    }

    org.activiti5.engine.impl.persistence.entity.DeploymentEntity activiti5DeploymentEntity 
      = ((org.activiti5.engine.impl.repository.DeploymentBuilderImpl) deploymentBuilder).getDeployment();
    activiti5DeploymentEntity.setResources(activiti5Resources);
    
    
    return new Activiti5DeploymentWrapper(deploymentBuilder.deploy());
  }
  
  protected ProcessEngine getProcessEngine() {
    if (processEngine == null) {
      synchronized (this) {
        if (processEngine == null) {
          processEngine = ProcessEngineFactory.buildProcessEngine(Context.getProcessEngineConfiguration());
        }
      }
    }
    return processEngine;
  }

}
