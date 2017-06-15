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

package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the BPMN 2.0 call activity (limited currently to calling a subprocess and not (yet) a global task).
 *
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class CallActivityBehavior extends AbstractBpmnActivityBehavior implements SubProcessActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected String processDefinitonKey;
  protected Expression processDefinitionExpression;
  protected List<MapExceptionEntry> mapExceptions;

  public CallActivityBehavior(String processDefinitionKey, List<MapExceptionEntry> mapExceptions) {
    this.processDefinitonKey = processDefinitionKey;
    this.mapExceptions = mapExceptions;
  }

  public CallActivityBehavior(Expression processDefinitionExpression, List<MapExceptionEntry> mapExceptions) {
    this.processDefinitionExpression = processDefinitionExpression;
    this.mapExceptions = mapExceptions;
  }

  public void execute(DelegateExecution execution) {

    String finalProcessDefinitonKey = null;
    if (processDefinitionExpression != null) {
      finalProcessDefinitonKey = (String) processDefinitionExpression.getValue(execution);
    } else {
      finalProcessDefinitonKey = processDefinitonKey;
    }

    ProcessDefinition processDefinition = findProcessDefinition(finalProcessDefinitonKey, execution.getTenantId());

    // Get model from cache
    Process subProcess = ProcessDefinitionUtil.getProcess(processDefinition.getId());
    if (subProcess == null) {
      throw new ActivitiException("Cannot start a sub process instance. Process model " + processDefinition.getName() + " (id = " + processDefinition.getId() + ") could not be found");
    }

    FlowElement initialFlowElement = subProcess.getInitialFlowElement();
    if (initialFlowElement == null) {
      throw new ActivitiException("No start element found for process definition " + processDefinition.getId());
    }

    // Do not start a process instance if the process definition is suspended
    if (ProcessDefinitionUtil.isProcessDefinitionSuspended(processDefinition.getId())) {
      throw new ActivitiException("Cannot start process instance. Process definition " + processDefinition.getName() + " (id = " + processDefinition.getId() + ") is suspended");
    }

    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
    ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();

    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    CallActivity callActivity = (CallActivity) executionEntity.getCurrentFlowElement();

    String businessKey = null;

    if (!StringUtils.isEmpty(callActivity.getBusinessKey())) {
      Expression expression = expressionManager.createExpression(callActivity.getBusinessKey());
      businessKey = expression.getValue(execution).toString();

    } else if (callActivity.isInheritBusinessKey()) {
      ExecutionEntity processInstance = executionEntityManager.findById(execution.getProcessInstanceId());
      businessKey = processInstance.getBusinessKey();
    }

    ExecutionEntity subProcessInstance = Context.getCommandContext().getExecutionEntityManager().createSubprocessInstance(
        processDefinition,executionEntity, businessKey);
    Context.getCommandContext().getHistoryManager().recordSubProcessInstanceStart(executionEntity, subProcessInstance, initialFlowElement);

    // process template-defined data objects
    Map<String, Object> variables = processDataObjects(subProcess.getDataObjects());

    if (callActivity.isInheritVariables()) {
      Map<String, Object> executionVariables = execution.getVariables();
      for (Map.Entry<String, Object> entry : executionVariables.entrySet()) {
        variables.put(entry.getKey(), entry.getValue());
      }
    }

    // copy process variables
    for (IOParameter ioParameter : callActivity.getInParameters()) {
      Object value = null;
      if (StringUtils.isNotEmpty(ioParameter.getSourceExpression())) {
        Expression expression = expressionManager.createExpression(ioParameter.getSourceExpression().trim());
        value = expression.getValue(execution);

      } else {
        value = execution.getVariable(ioParameter.getSource());
      }
      variables.put(ioParameter.getTarget(), value);
    }

    if (!variables.isEmpty()) {
      initializeVariables(subProcessInstance, variables);
    }

    // Create the first execution that will visit all the process definition elements
    ExecutionEntity subProcessInitialExecution = executionEntityManager.createChildExecution(subProcessInstance);
    subProcessInitialExecution.setCurrentFlowElement(initialFlowElement);

    Context.getAgenda().planContinueProcessOperation(subProcessInitialExecution);

    Context.getProcessEngineConfiguration().getEventDispatcher()
      .dispatchEvent(ActivitiEventBuilder.createProcessStartedEvent(subProcessInitialExecution, variables, false));
  }

  public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {
    // only data. no control flow available on this execution.

    ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();

    // copy process variables
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    CallActivity callActivity = (CallActivity) executionEntity.getCurrentFlowElement();
    for (IOParameter ioParameter : callActivity.getOutParameters()) {
      Object value = null;
      if (StringUtils.isNotEmpty(ioParameter.getSourceExpression())) {
        Expression expression = expressionManager.createExpression(ioParameter.getSourceExpression().trim());
        value = expression.getValue(subProcessInstance);

      } else {
        value = subProcessInstance.getVariable(ioParameter.getSource());
      }
      execution.setVariable(ioParameter.getTarget(), value);
    }
  }

  public void completed(DelegateExecution execution) throws Exception {
    // only control flow. no sub process instance data available
    leave(execution);
  }

  // Allow subclass to determine which version of a process to start.
  protected ProcessDefinition findProcessDefinition(String processDefinitionKey, String tenantId) {
    if (tenantId == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(tenantId)) {
      return Context.getProcessEngineConfiguration().getDeploymentManager().findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
    } else {
      return Context.getProcessEngineConfiguration().getDeploymentManager().findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
    }
  }

  protected Map<String, Object> processDataObjects(Collection<ValuedDataObject> dataObjects) {
  	Map<String, Object> variablesMap = new HashMap<String,Object>();
  	// convert data objects to process variables
  	if (dataObjects != null) {
        variablesMap = new HashMap<String, Object>(dataObjects.size());
  	  for (ValuedDataObject dataObject : dataObjects) {
  	    variablesMap.put(dataObject.getName(), dataObject.getValue());
  	  }
  	}
  	return variablesMap;
  }

  // Allow a subclass to override how variables are initialized.
  protected void initializeVariables(ExecutionEntity subProcessInstance, Map<String,Object> variables) {
    subProcessInstance.setVariables(variables);
  }

  public void setProcessDefinitonKey(String processDefinitonKey) {
    this.processDefinitonKey = processDefinitonKey;
  }

  public String getProcessDefinitonKey() {
    return processDefinitonKey;
  }
}
