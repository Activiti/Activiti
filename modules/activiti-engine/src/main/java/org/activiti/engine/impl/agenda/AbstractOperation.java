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
package org.activiti.engine.impl.agenda;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.HasExecutionListeners;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.BaseExecutionListener;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.ExecutionListenerCustomPropertiesResolver;
import org.activiti.engine.delegate.TransactionDependentExecutionListener;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.listener.ListenerUtil;
import org.activiti.engine.impl.bpmn.listener.TransactionDependentExecutionListeners;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

import java.util.Map;


/**
 * @author Joram Barrez
 */
public abstract class AbstractOperation implements Runnable {

  protected CommandContext commandContext;
  protected Agenda agenda;
  protected ExecutionEntity execution;

  public AbstractOperation() {

  }

  public AbstractOperation(CommandContext commandContext, ExecutionEntity execution) {
    this.commandContext = commandContext;
    this.execution = execution;
    this.agenda = commandContext.getAgenda();
  }

  /**
   * Helper method to match the activityId of an execution with a FlowElement of the process definition referenced by the execution.
   */
  protected FlowElement findCurrentFlowElement(final ExecutionEntity execution) {
    if (execution.getCurrentActivityId() != null) {
      String processDefinitionId = execution.getProcessDefinitionId();
      org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
      String activityId = execution.getCurrentActivityId();
      FlowElement currentFlowElement = process.getFlowElement(activityId, true);
      execution.setCurrentFlowElement(currentFlowElement);
      return currentFlowElement;
    }
    
    return null;
  }

  /**
   * Executes the execution listeners defined on the given element, with the given event type.
   */
  protected void executeExecutionListeners(HasExecutionListeners elementWithExecutionListeners, String eventType) {
    executeExecutionListeners(elementWithExecutionListeners, null, eventType);
  }

  protected void executeExecutionListeners(HasExecutionListeners elementWithExecutionListeners, ExecutionEntity executionToUseForListener, String eventType) {
    ExecutionEntity executionToUse = executionToUseForListener != null ? executionToUseForListener : execution;
    ListenerUtil.executeExecutionListeners(elementWithExecutionListeners, executionToUse, eventType);
  }

  protected void executeExecutionListener(ExecutionEntity executionToUse, BaseExecutionListener executionListener, String eventType) {
    executionToUse.setEventName(eventType);
    ((ExecutionListener) executionListener).notify(executionToUse);

    // TODO: is this still needed? Is this property still needed?
    executionToUse.setEventName(null);
  }

  protected void planTransactionDependentExecutionListener(BaseExecutionListener executionListener, ActivitiListener activitiListener) {
    CommandContextCloseListener executionListenerContextCloseListener = null;

    for (CommandContextCloseListener commandContextCloseListener : getCommandContext().getCloseListeners()) {
      if (commandContextCloseListener instanceof TransactionDependentExecutionListeners) {
        executionListenerContextCloseListener = commandContextCloseListener;
        break;
      }
    }

    if (executionListenerContextCloseListener == null) {
      executionListenerContextCloseListener = new TransactionDependentExecutionListeners();
      getCommandContext().addCloseListener(executionListenerContextCloseListener);
    }

    // current state of the execution variables will be stored
    Map<String, Object> executionVariablesToUse = execution.getVariables();

    // invoke custom properties resolver
    Map<String, Object> customPropertiesMapToUse = null;
    if  (activitiListener.getCustomPropertiesResolverClass() != null) {
      Object customPropertiesResolver = ClassDelegate.defaultInstantiateDelegate(activitiListener.getCustomPropertiesResolverClass(), null);
      if (customPropertiesResolver instanceof ExecutionListenerCustomPropertiesResolver == false) {
        throw new ActivitiIllegalArgumentException(customPropertiesResolver.getClass().getName() + " doesn't implement " + ExecutionListenerCustomPropertiesResolver.class.getName());
      }
      customPropertiesMapToUse = ((ExecutionListenerCustomPropertiesResolver) customPropertiesResolver).getCustomPropertiesMap();
    }

    if (TransactionDependentExecutionListener.ON_TRANSACTION_RESULT_COMMITTED.equals(activitiListener.getOnTransactionResult())) {
      ((TransactionDependentExecutionListeners) executionListenerContextCloseListener).addClosedListener((TransactionDependentExecutionListener) executionListener, getExecution().getProcessInstanceId(), getExecution().getId(),
              getExecution().getCurrentFlowElement(), executionVariablesToUse, customPropertiesMapToUse);
    } else if (TransactionDependentExecutionListener.ON_TRANSACTION_RESULT_ROLLED_BACK.equals(activitiListener.getOnTransactionResult())) {
      ((TransactionDependentExecutionListeners) executionListenerContextCloseListener).addCloseFailedListener((TransactionDependentExecutionListener) executionListener, getExecution().getProcessInstanceId(), getExecution().getId(),
              getExecution().getCurrentFlowElement(), executionVariablesToUse, customPropertiesMapToUse);
    }
  }
  
  public CommandContext getCommandContext() {
    return commandContext;
  }

  public void setCommandContext(CommandContext commandContext) {
    this.commandContext = commandContext;
  }

  public Agenda getAgenda() {
    return agenda;
  }

  public void setAgenda(Agenda agenda) {
    this.agenda = agenda;
  }

  public ExecutionEntity getExecution() {
    return execution;
  }

  public void setExecution(ExecutionEntity execution) {
    this.execution = execution;
  }

}
