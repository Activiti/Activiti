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

import java.util.List;

import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IOParameter;
import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.SubProcessActivityBehavior;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of the BPMN 2.0 call activity (limited currently to calling a subprocess and not (yet) a global task).
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class CallActivityBehavior extends AbstractBpmnActivityBehavior implements SubProcessActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected String processDefinitonKey;
  private Expression processDefinitionExpression;
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

    ProcessDefinitionEntity processDefinition = findProcessDefinition(finalProcessDefinitonKey, execution.getTenantId());

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

    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    CallActivity callActivity = (CallActivity) executionEntity.getCurrentFlowElement();

    ExecutionEntity subProcessInstance = createSubProcessInstance(processDefinition, executionEntity, initialFlowElement);

    // copy process variables
    ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
    for (IOParameter ioParameter : callActivity.getInParameters()) {
      Object value = null;
      if (StringUtils.isNotEmpty(ioParameter.getSourceExpression())) {
        Expression expression = expressionManager.createExpression(ioParameter.getSourceExpression().trim());
        value = expression.getValue(execution);

      } else {
        value = execution.getVariable(ioParameter.getSource());
      }
      subProcessInstance.setVariable(ioParameter.getTarget(), value);
    }

    // Create the first execution that will visit all the process definition elements
    ExecutionEntity subProcessInitialExecution = Context.getCommandContext().getExecutionEntityManager().createChildExecution(subProcessInstance); 
    subProcessInitialExecution.setCurrentFlowElement(initialFlowElement);

    Context.getAgenda().planContinueProcessOperation(subProcessInitialExecution);
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

  protected ExecutionEntity createSubProcessInstance(ProcessDefinitionEntity processDefinitionEntity, ExecutionEntity superExecutionEntity, FlowElement initialFlowElement) {

    ExecutionEntity subProcessInstance = Context.getCommandContext().getExecutionEntityManager().create(); 
    subProcessInstance.setProcessDefinitionId(processDefinitionEntity.getId());
    subProcessInstance.setSuperExecution(superExecutionEntity);
    subProcessInstance.setRootProcessInstanceId(superExecutionEntity.getRootProcessInstanceId());
    subProcessInstance.setScope(true); // process instance is always a scope for all child executions

    // Inherit tenant id (if any)
    if (processDefinitionEntity.getTenantId() != null) {
      subProcessInstance.setTenantId(processDefinitionEntity.getTenantId());
    }

    // Store in database
    Context.getCommandContext().getExecutionEntityManager().insert(subProcessInstance, false);

    subProcessInstance.setProcessInstanceId(subProcessInstance.getId());
    superExecutionEntity.setSubProcessInstance(subProcessInstance);

    // Fire events manage bidirectional super-subprocess relation
    Context.getCommandContext().getHistoryManager().recordSubProcessInstanceStart(superExecutionEntity, subProcessInstance, initialFlowElement);

    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, subProcessInstance));
    }

    return subProcessInstance;
  }

  public void setProcessDefinitonKey(String processDefinitonKey) {
    this.processDefinitonKey = processDefinitonKey;
  }

  public String getProcessDefinitonKey() {
    return processDefinitonKey;
  }

  // Allow subclass to determine which version of a process to start.
  protected ProcessDefinitionEntity findProcessDefinition(String processDefinitionKey, String tenantId) {
    if (tenantId == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(tenantId)) {
      return Context.getProcessEngineConfiguration().getDeploymentManager().findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
    } else {
      return Context.getProcessEngineConfiguration().getDeploymentManager().findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
    }
  }
}
