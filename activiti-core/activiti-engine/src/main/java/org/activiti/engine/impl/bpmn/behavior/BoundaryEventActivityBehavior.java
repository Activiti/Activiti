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

package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.CallActivity;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.CollectionUtil;

import java.util.Collection;


public class BoundaryEventActivityBehavior extends FlowNodeActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected boolean interrupting;

  public BoundaryEventActivityBehavior() {
  }

  public BoundaryEventActivityBehavior(boolean interrupting) {
    this.interrupting = interrupting;
  }

  @Override
  public void execute(DelegateExecution execution) {
    // Overridden by subclasses
  }

  @Override
  public void trigger(DelegateExecution execution, String triggerName, Object triggerData) {

    ExecutionEntity executionEntity = (ExecutionEntity) execution;

    CommandContext commandContext = Context.getCommandContext();

    if (interrupting) {
      executeInterruptingBehavior(executionEntity, commandContext);
    } else {
      executeNonInterruptingBehavior(executionEntity, commandContext);
    }
  }

  protected void executeInterruptingBehavior(ExecutionEntity executionEntity, CommandContext commandContext) {

    // The destroy scope operation will look for the parent execution and
    // destroy the whole scope, and leave the boundary event using this parent execution.
    //
    // The take outgoing seq flows operation below (the non-interrupting else clause) on the other hand uses the
    // child execution to leave, which keeps the scope alive.
    // Which is what we need here.

    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    ExecutionEntity attachedRefScopeExecution = executionEntityManager.findById(executionEntity.getParentId());

    ExecutionEntity parentScopeExecution = null;
    ExecutionEntity currentlyExaminedExecution = executionEntityManager.findById(attachedRefScopeExecution.getParentId());
    while (currentlyExaminedExecution != null && parentScopeExecution == null) {
      if (currentlyExaminedExecution.isScope()) {
        parentScopeExecution = currentlyExaminedExecution;
      } else {
        currentlyExaminedExecution = executionEntityManager.findById(currentlyExaminedExecution.getParentId());
      }
    }

    if (parentScopeExecution == null) {
      throw new ActivitiException("Programmatic error: no parent scope execution found for boundary event");
    }

    deleteChildExecutions(attachedRefScopeExecution, executionEntity, commandContext);

    // set new parent for boundary event execution
    executionEntity.setParent(parentScopeExecution);

    Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(executionEntity, true);
  }

  protected void executeNonInterruptingBehavior(ExecutionEntity executionEntity, CommandContext commandContext) {

    // Non-interrupting: the current execution is given the first parent
    // scope (which isn't its direct parent)
    //
    // Why? Because this execution does NOT have anything to do with
    // the current parent execution (the one where the boundary event is on): when it is deleted or whatever,
    // this does not impact this new execution at all, it is completely independent in that regard.

    // Note: if the parent of the parent does not exists, this becomes a concurrent execution in the process instance!

    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

    ExecutionEntity parentExecutionEntity = executionEntityManager.findById(executionEntity.getParentId());

    ExecutionEntity scopeExecution = null;
    ExecutionEntity currentlyExaminedExecution = executionEntityManager.findById(parentExecutionEntity.getParentId());
    while (currentlyExaminedExecution != null && scopeExecution == null) {
      if (currentlyExaminedExecution.isScope()) {
        scopeExecution = currentlyExaminedExecution;
      } else {
        currentlyExaminedExecution = executionEntityManager.findById(currentlyExaminedExecution.getParentId());
      }
    }

    if (scopeExecution == null) {
      throw new ActivitiException("Programmatic error: no parent scope execution found for boundary event");
    }

    ExecutionEntity nonInterruptingExecution = executionEntityManager.createChildExecution(scopeExecution);
    nonInterruptingExecution.setCurrentFlowElement(executionEntity.getCurrentFlowElement());

    Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(nonInterruptingExecution, true);
  }

  protected void deleteChildExecutions(ExecutionEntity parentExecution, ExecutionEntity notToDeleteExecution, CommandContext commandContext) {

    // TODO: would be good if this deleteChildExecutions could be removed and the one on the executionEntityManager is used
    // The problem however, is that the 'notToDeleteExecution' is passed here.
    // This could be solved by not reusing an execution, but creating a new

    // Delete all child executions
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    Collection<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(parentExecution.getId());
    if (CollectionUtil.isNotEmpty(childExecutions)) {
      for (ExecutionEntity childExecution : childExecutions) {
        if (childExecution.getId().equals(notToDeleteExecution.getId()) == false) {
          deleteChildExecutions(childExecution, notToDeleteExecution, commandContext);
        }
      }
    }

    String deleteReason = DeleteReason.BOUNDARY_EVENT_INTERRUPTING + " (" + notToDeleteExecution.getCurrentActivityId() + ")";
    if (parentExecution.getCurrentFlowElement() instanceof CallActivity) {
      ExecutionEntity subProcessExecution = executionEntityManager.findSubProcessInstanceBySuperExecutionId(parentExecution.getId());
      if (subProcessExecution != null) {
        executionEntityManager.deleteProcessInstanceExecutionEntity(subProcessExecution.getId(),
            subProcessExecution.getCurrentActivityId(), deleteReason, true, true);
      }
    }

    executionEntityManager.cancelExecutionAndRelatedData(parentExecution, deleteReason);
  }

  public boolean isInterrupting() {
    return interrupting;
  }

  public void setInterrupting(boolean interrupting) {
    this.interrupting = interrupting;
  }

}
