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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.runtime.ProcessInstanceBuilderImpl;
import org.activiti.engine.impl.util.cache.ProcessDefinitionCacheUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLinkType;

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

	public StartProcessInstanceCmd(String processDefinitionKey,
	        String processDefinitionId, String businessKey,
	        Map<String, Object> variables) {
		this.processDefinitionKey = processDefinitionKey;
		this.processDefinitionId = processDefinitionId;
		this.businessKey = businessKey;
		this.variables = variables;
	}

	public StartProcessInstanceCmd(String processDefinitionKey,
	        String processDefinitionId, String businessKey,
	        Map<String, Object> variables, String tenantId) {
		this(processDefinitionKey, processDefinitionId, businessKey, variables);
		this.tenantId = tenantId;
	}

	public StartProcessInstanceCmd(ProcessInstanceBuilderImpl processInstanceBuilder) {
		this(processInstanceBuilder.getProcessDefinitionKey(), 
				processInstanceBuilder.getProcessDefinitionId(),
		        processInstanceBuilder.getBusinessKey(), 
		        processInstanceBuilder.getVariables(), 
		        processInstanceBuilder.getTenantId());
		this.processInstanceName = processInstanceBuilder.getProcessInstanceName();
	}

	public ProcessInstance execute(CommandContext commandContext) {
		DeploymentManager deploymentCache = commandContext.getProcessEngineConfiguration().getDeploymentManager();
		
		//
		// TODO: Think about cache usage here. How to avoid duplication??
		// Probably best to switch to separate caches: one for entities, and one for process models.
		//

		// Find the process definition
		ProcessDefinitionEntity processDefinition = null;
		if (processDefinitionId != null) {
			
			processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
			if (processDefinition == null) {
				throw new ActivitiObjectNotFoundException("No process definition found for id = '"
				                + processDefinitionId + "'", ProcessDefinition.class);
			}
			
		} else if (processDefinitionKey != null
		        && (tenantId == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(tenantId))) {
			
			processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
			if (processDefinition == null) {
				throw new ActivitiObjectNotFoundException("No process definition found for key '"
				                + processDefinitionKey + "'", ProcessDefinition.class);
			}
			
		} else if (processDefinitionKey != null && tenantId != null
		        && !ProcessEngineConfiguration.NO_TENANT_ID.equals(tenantId)) {
			
			processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
			if (processDefinition == null) {
				throw new ActivitiObjectNotFoundException("No process definition found for key '"
				                + processDefinitionKey + "' for tenant identifier " + tenantId, ProcessDefinition.class);
			}
			
		} else {
			throw new ActivitiIllegalArgumentException(
			        "processDefinitionKey and processDefinitionId are null");
		}
		
		// Backwards compatibility
		
		if (processDefinition.getEngineVersion() != null) {
			if (Activiti5CompatibilityHandler.ACTIVITI_5_ENGINE_TAG.equals(processDefinition.getEngineVersion())) {
				Activiti5CompatibilityHandler activiti5CompatibilityHandler =
						commandContext.getProcessEngineConfiguration().getActiviti5CompatibilityHandler();
				
				if (activiti5CompatibilityHandler == null) {
					throw new ActivitiException("Found Activiti 5 process definition, but no compatibility handler on the classpath");
				}
				
				return activiti5CompatibilityHandler.startProcessInstance(processDefinitionKey, processDefinitionId, 
						variables, businessKey, tenantId, processInstanceName);
			} else {
				throw new ActivitiException("Invalid 'engine' for process definition " 
						+ processDefinition.getId() + " : " + processDefinition.getEngineVersion());
			}
		}

		// Do not start process a process instance if the process definition is
		// suspended
		if (processDefinition.isSuspended()) {
			throw new ActivitiException("Cannot start process instance. Process definition "
					+ processDefinition.getName() + " (id = " + processDefinition.getId() + ") is suspended");
		}

		// Get model from cache
		Process process = ProcessDefinitionCacheUtil.getCachedProcess(processDefinition.getId());
		if (process == null) {
		    throw new ActivitiException("Cannot start process instance. Process model "
                    + processDefinition.getName() + " (id = " + processDefinition.getId() + ") could not be found");
		}

		FlowElement initialFlowElement = process.getInitialFlowElement();
		if (initialFlowElement == null) {
			throw new ActivitiException("No start element found for process definition " + processDefinition.getId());
		}

		// Create process instance

		// //// ////// ////// //////

		// Create the process instance
		String initiatorVariableName = null;
		if (initialFlowElement instanceof StartEvent) {
		    initiatorVariableName = ((StartEvent) initialFlowElement).getInitiator();
		}
		ExecutionEntity processInstance = createProcessInstance(commandContext, processDefinition, 
		        businessKey, initiatorVariableName, initialFlowElement);
		
		processInstance.setVariables(processDataObjects(process.getDataObjects()));
		
		// Set the variables passed into the start command
		if (variables != null) {
			for (String varName : variables.keySet()) {
                processInstance.setVariable(varName, variables.get(varName));
            }
		}

		// Set processInstance name
		if (processInstanceName != null) {
			processInstance.setName(processInstanceName);
		}
		
		// Create the first execution that will visit all the process definition elements
		ExecutionEntity execution = processInstance.createExecution();
		execution.setCurrentFlowElement(initialFlowElement);
		commandContext.getAgenda().planContinueProcessOperation(execution);

		return processInstance;
	}

	protected ExecutionEntity createProcessInstance(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity, 
	        String businessKey, String initiatorVariableName, FlowElement initialFlowElement) {
		
		ExecutionEntity processInstance = new ExecutionEntity();
		processInstance.setProcessDefinitionId(processDefinitionEntity.getId());
		processInstance.setBusinessKey(businessKey);
		processInstance.setScope(true); // process instance is always a scope for all child executions
		
		// Inherit tenant id (if any)
		if (processDefinitionEntity.getTenantId() != null) {
			processInstance.setTenantId(processDefinitionEntity.getTenantId());
		}

		String authenticatedUserId = Authentication.getAuthenticatedUserId();
		if (initiatorVariableName != null) {
		  processInstance.setVariable(initiatorVariableName, authenticatedUserId);
		}
		if (authenticatedUserId != null) {
		  processInstance.addIdentityLink(authenticatedUserId, null, IdentityLinkType.STARTER);
		}
		
		// Store in database
		commandContext.getExecutionEntityManager().insert(processInstance);

		// Fire events
		commandContext.getHistoryManager().recordProcessInstanceStart(processInstance, initialFlowElement);

		if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
			Context.getProcessEngineConfiguration()
			        .getEventDispatcher()
			        .dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, processInstance));
		}
		
		return processInstance;
	}
	
	protected Map<String, Object> processDataObjects(Collection<ValuedDataObject> dataObjects) {
        Map<String, Object> variablesMap = new HashMap<String, Object>();
        // convert data objects to process variables  
        if (dataObjects != null) {
            for (ValuedDataObject dataObject : dataObjects) {
                variablesMap.put(dataObject.getName(), dataObject.getValue());
            }
        }
        return variablesMap;
    }
}
