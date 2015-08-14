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

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.HasExecutionListeners;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.parser.factory.ListenerFactory;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

/**
 * @author Joram Barrez
 */
public abstract class AbstractOperation implements Runnable {

  protected CommandContext commandContext;
  protected Agenda agenda;
  protected ActivityExecution execution;

  public AbstractOperation() {

  }

  public AbstractOperation(CommandContext commandContext, ActivityExecution execution) {
    this.commandContext = commandContext;
    this.execution = execution;
    this.agenda = commandContext.getAgenda();
  }

  /**
   * Helper method to match the activityId of an execution with a FlowElement of the process definition referenced by the execution.
   */
  protected FlowElement findCurrentFlowElement(final ActivityExecution execution) {
    String processDefinitionId = execution.getProcessDefinitionId();
    org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
    String activityId = execution.getCurrentActivityId();
    FlowElement currentFlowElement = process.getFlowElement(activityId, true);
    execution.setCurrentFlowElement(currentFlowElement);
    return currentFlowElement;
  }

  /**
   * Executes the execution listeners defined on the given element, with the given event type.
   */
  protected void executeExecutionListeners(HasExecutionListeners elementWithExecutionListeners, String eventType) {
    executeExecutionListeners(elementWithExecutionListeners, null, eventType, false);
  }

  protected void executeExecutionListeners(HasExecutionListeners elementWithExecutionListeners, ActivityExecution executionToUseForListener, String eventType, boolean ignoreType) {
    List<ActivitiListener> listeners = elementWithExecutionListeners.getExecutionListeners();
    ListenerFactory listenerFactory = Context.getProcessEngineConfiguration().getListenerFactory();
    if (listeners != null) {
      for (ActivitiListener activitiListener : listeners) {

        if (ignoreType || eventType.equals(activitiListener.getEvent())) {

          ExecutionListener executionListener = null;

          if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createClassDelegateExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createExpressionExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = listenerFactory.createDelegateExpressionExecutionListener(activitiListener);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_INSTANCE.equalsIgnoreCase(activitiListener.getImplementationType())) {
            executionListener = (ExecutionListener) activitiListener.getInstance();
          }
          
          ActivityExecution executionToUse = executionToUseForListener != null ? executionToUseForListener : execution;

          if (executionListener != null) {
            ((ExecutionEntity) executionToUse).setEventName(eventType);
            executionListener.notify(executionToUse);
            
            // TODO: is this still needed? Is this property still needed?
            ((ExecutionEntity) executionToUse).setEventName(null);
          }

        }
      }
    }
  }
  
  /* TODO: Should following methods be moved to the entityManager */

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

  public ActivityExecution getExecution() {
    return execution;
  }

  public void setExecution(ActivityExecution execution) {
    this.execution = execution;
  }

}
