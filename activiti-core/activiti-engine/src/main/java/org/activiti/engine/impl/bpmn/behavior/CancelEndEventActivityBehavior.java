/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CancelEndEventActivityBehavior extends FlowNodeActivityBehavior {

  private static final long serialVersionUID = 1L;

  @Override
  public void execute(DelegateExecution execution) {

    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    CommandContext commandContext = Context.getCommandContext();
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

    // find cancel boundary event:
    ExecutionEntity parentScopeExecution = null;
    ExecutionEntity currentlyExaminedExecution = executionEntityManager.findById(executionEntity.getParentId());
    while (currentlyExaminedExecution != null && parentScopeExecution == null) {
      if (currentlyExaminedExecution.getCurrentFlowElement() instanceof SubProcess) {
        parentScopeExecution = currentlyExaminedExecution;
        SubProcess subProcess = (SubProcess) currentlyExaminedExecution.getCurrentFlowElement();
        if (subProcess.getLoopCharacteristics() != null) {
          ExecutionEntity miExecution = parentScopeExecution.getParent();
          FlowElement miElement = miExecution.getCurrentFlowElement();
          if (miElement != null && miElement.getId().equals(subProcess.getId())) {
            parentScopeExecution = miExecution;
          }
        }

      } else {
        currentlyExaminedExecution = executionEntityManager.findById(currentlyExaminedExecution.getParentId());
      }
    }

    if (parentScopeExecution == null) {
      throw new ActivitiException("No sub process execution found for cancel end event " + executionEntity.getCurrentActivityId());
    }

    SubProcess subProcess = (SubProcess) parentScopeExecution.getCurrentFlowElement();
    BoundaryEvent cancelBoundaryEvent = null;
    if (CollectionUtil.isNotEmpty(subProcess.getBoundaryEvents())) {
      for (BoundaryEvent boundaryEvent : subProcess.getBoundaryEvents()) {
        if (CollectionUtil.isNotEmpty(boundaryEvent.getEventDefinitions()) &&
            boundaryEvent.getEventDefinitions().get(0) instanceof CancelEventDefinition) {

          cancelBoundaryEvent = boundaryEvent;
          break;
        }
      }
    }

    if (cancelBoundaryEvent == null) {
      throw new ActivitiException("Could not find cancel boundary event for cancel end event " + executionEntity.getCurrentActivityId());
    }

    ExecutionEntity newParentScopeExecution = null;
    currentlyExaminedExecution = executionEntityManager.findById(parentScopeExecution.getParentId());
    while (currentlyExaminedExecution != null && newParentScopeExecution == null) {
      if (currentlyExaminedExecution.isScope()) {
        newParentScopeExecution = currentlyExaminedExecution;
      } else {
        currentlyExaminedExecution = executionEntityManager.findById(currentlyExaminedExecution.getParentId());
      }
    }

    if (newParentScopeExecution == null) {
      throw new ActivitiException("Programmatic error: no parent scope execution found for boundary event " + cancelBoundaryEvent.getId());
    }

    ScopeUtil.createCopyOfSubProcessExecutionForCompensation(parentScopeExecution);

    if (subProcess.getLoopCharacteristics() != null) {
      List<? extends ExecutionEntity> multiInstanceExecutions = parentScopeExecution.getExecutions();
      List<ExecutionEntity> executionsToDelete = new ArrayList<ExecutionEntity>();
      for (ExecutionEntity multiInstanceExecution : multiInstanceExecutions) {
        if (!multiInstanceExecution.getId().equals(parentScopeExecution.getId())) {
          ScopeUtil.createCopyOfSubProcessExecutionForCompensation(multiInstanceExecution);

          // end all executions in the scope of the transaction
          executionsToDelete.add(multiInstanceExecution);
          deleteChildExecutions(multiInstanceExecution, executionEntity, commandContext, DeleteReason.TRANSACTION_CANCELED);

        }
      }

      for (ExecutionEntity executionEntityToDelete : executionsToDelete) {
        deleteChildExecutions(executionEntityToDelete, executionEntity, commandContext, DeleteReason.TRANSACTION_CANCELED);
      }
    }

    // The current activity is finished (and will not be ended in the deleteChildExecutions)
    commandContext.getHistoryManager().recordActivityEnd(executionEntity, null);

    // set new parent for boundary event execution
    executionEntity.setParent(newParentScopeExecution);
    executionEntity.setCurrentFlowElement(cancelBoundaryEvent);

    // end all executions in the scope of the transaction
    deleteChildExecutions(parentScopeExecution, executionEntity, commandContext, DeleteReason.TRANSACTION_CANCELED);
    commandContext.getHistoryManager().recordActivityEnd(parentScopeExecution, DeleteReason.TRANSACTION_CANCELED);

    Context.getAgenda().planTriggerExecutionOperation(executionEntity);
  }

  protected void deleteChildExecutions(ExecutionEntity parentExecution, ExecutionEntity notToDeleteExecution,
      CommandContext commandContext, String deleteReason) {
    // Delete all child executions
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    Collection<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(parentExecution.getId());
    if (CollectionUtil.isNotEmpty(childExecutions)) {
      for (ExecutionEntity childExecution : childExecutions) {
        if (!(childExecution.getId().equals(notToDeleteExecution.getId()))) {
          deleteChildExecutions(childExecution, notToDeleteExecution, commandContext, deleteReason);
        }
      }
    }

    executionEntityManager.deleteExecutionAndRelatedData(parentExecution, deleteReason);
  }

}
