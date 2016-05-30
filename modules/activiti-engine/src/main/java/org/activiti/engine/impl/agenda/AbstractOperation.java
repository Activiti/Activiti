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

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.HasExecutionListeners;
import org.activiti.engine.impl.bpmn.listener.ListenerUtil;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

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
