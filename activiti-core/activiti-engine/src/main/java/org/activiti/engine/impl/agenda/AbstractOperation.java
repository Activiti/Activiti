/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.Agenda;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

/**
 * Abstract superclass for all operation interfaces (which are {@link Runnable} instances),
 * exposing some shared helper methods and member fields to subclasses.
 *
 * An operations is a {@link Runnable} instance that is put on the {@link Agenda} during
 * the execution of a {@link Command}.
 *

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
  protected FlowElement getCurrentFlowElement(final ExecutionEntity execution) {
    if (execution.getCurrentFlowElement() != null) {
      return execution.getCurrentFlowElement();
    } else if (execution.getCurrentActivityId() != null) {
      String processDefinitionId = execution.getProcessDefinitionId();
      org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
      String activityId = execution.getCurrentActivityId();
      FlowElement currentFlowElement = process.getFlowElement(activityId, true);
      return currentFlowElement;
    }
    return null;
  }

  /**
   * Executes the execution listeners defined on the given element, with the given event type.
   * Uses the {@link #execution} of this operation instance as argument for the execution listener.
   */
  protected void executeExecutionListeners(HasExecutionListeners elementWithExecutionListeners, String eventType) {
    executeExecutionListeners(elementWithExecutionListeners, execution, eventType);
  }

  /**
   * Executes the execution listeners defined on the given element, with the given event type,
   * and passing the provided execution to the {@link ExecutionListener} instances.
   */
  protected void executeExecutionListeners(HasExecutionListeners elementWithExecutionListeners,
      ExecutionEntity executionEntity, String eventType) {
    commandContext.getProcessEngineConfiguration().getListenerNotificationHelper()
      .executeExecutionListeners(elementWithExecutionListeners, executionEntity, eventType);
  }

  /**
   * Returns the first parent execution of the provided execution that is a scope.
   */
  protected ExecutionEntity findFirstParentScopeExecution(ExecutionEntity executionEntity) {
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    ExecutionEntity parentScopeExecution = null;
    ExecutionEntity currentlyExaminedExecution = executionEntityManager.findById(executionEntity.getParentId());
    while (currentlyExaminedExecution != null && parentScopeExecution == null) {
      if (currentlyExaminedExecution.isScope()) {
        parentScopeExecution = currentlyExaminedExecution;
      } else {
        currentlyExaminedExecution = executionEntityManager.findById(currentlyExaminedExecution.getParentId());
      }
    }
    return parentScopeExecution;
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

  public void setAgenda(DefaultActivitiEngineAgenda agenda) {
    this.agenda = agenda;
  }

  public ExecutionEntity getExecution() {
    return execution;
  }

  public void setExecution(ExecutionEntity execution) {
    this.execution = execution;
  }

}
