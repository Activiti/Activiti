/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.runtime.ProcessInstanceBuilderImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class StartProcessInstanceCmd<T> implements Command<ProcessInstance>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected Map<String, Object> variables;
  protected String businessKey;
  protected String tenantId;
  protected String processInstanceName;
  
  public StartProcessInstanceCmd(String processDefinitionKey, String processDefinitionId, String businessKey, Map<String, Object> variables) {
    this.processDefinitionKey = processDefinitionKey;
    this.processDefinitionId = processDefinitionId;
    this.businessKey = businessKey;
    this.variables = variables;
  }
  
  public StartProcessInstanceCmd(String processDefinitionKey, String processDefinitionId, 
  		String businessKey, Map<String, Object> variables, String tenantId) {
  	this(processDefinitionKey, processDefinitionId, businessKey, variables);
  	this.tenantId = tenantId;
  }
  
  public StartProcessInstanceCmd(ProcessInstanceBuilderImpl processInstanceBuilder) {
    this(processInstanceBuilder.getProcessDefinitionKey(), processInstanceBuilder.getProcessDefinitionId(),
        processInstanceBuilder.getBusinessKey(), processInstanceBuilder.getVariables(), processInstanceBuilder.getTenantId());
    this.processInstanceName = processInstanceBuilder.getProcessInstanceName();
  }
  
  public ProcessInstance execute(CommandContext commandContext) {
    DeploymentManager deploymentManager = commandContext
      .getProcessEngineConfiguration()
      .getDeploymentManager();
    
    // Find the process definition
    ProcessDefinitionEntity processDefinition = null;
    if (processDefinitionId != null) {
      processDefinition = deploymentManager.findDeployedProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        throw new ActivitiObjectNotFoundException("No process definition found for id = '" + processDefinitionId + "'", ProcessDefinition.class);
      }
    } else if (processDefinitionKey != null && (tenantId == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(tenantId))){
      processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
      if (processDefinition == null) {
        throw new ActivitiObjectNotFoundException("No process definition found for key '" + processDefinitionKey +"'", ProcessDefinition.class);
      }
    } else if (processDefinitionKey != null && tenantId != null && !ProcessEngineConfiguration.NO_TENANT_ID.equals(tenantId)) {
    	 processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
       if (processDefinition == null) {
         throw new ActivitiObjectNotFoundException("No process definition found for key '" + processDefinitionKey +"' for tenant identifier " + tenantId, ProcessDefinition.class);
       }
    } else {
      throw new ActivitiIllegalArgumentException("processDefinitionKey and processDefinitionId are null");
    }
    
    // Do not start process a process instance if the process definition is suspended
    if (deploymentManager.isProcessDefinitionSuspended(processDefinition.getId())) {
      throw new ActivitiException("Cannot start process instance. Process definition " 
              + processDefinition.getName() + " (id = " + processDefinition.getId() + ") is suspended");
    }

    // Start the process instance
    ExecutionEntity processInstance = processDefinition.createProcessInstance(businessKey);

    // now set the variables passed into the start command
    initializeVariables(processInstance);

    // now set processInstance name
    if (processInstanceName != null) {
      processInstance.setName(processInstanceName);
      commandContext.getHistoryManager().recordProcessInstanceNameChange(processInstance.getId(), processInstanceName);
    }
    
    processInstance.start();
    
    return processInstance;
  }

  protected void initializeVariables(ExecutionEntity processInstance) {
    if (variables != null) {
      processInstance.setVariables(variables);
    }
  }
}
