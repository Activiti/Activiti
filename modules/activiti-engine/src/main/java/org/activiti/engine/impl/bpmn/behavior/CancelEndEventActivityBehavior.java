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

import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author Tijs Rademakers
 */
public class CancelEndEventActivityBehavior extends FlowNodeActivityBehavior {
  
  private static final long serialVersionUID = 1L;

  @Override
  public void execute(ActivityExecution execution) {

    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    CommandContext commandContext = Context.getCommandContext();
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    
    // find cancel boundary event:
    ExecutionEntity parentScopeExecution = null;
    ExecutionEntity currentlyExaminedExecution = executionEntityManager.findExecutionById(executionEntity.getParentId());
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
        currentlyExaminedExecution = executionEntityManager.findExecutionById(currentlyExaminedExecution.getParentId());
      }
    }
    
    if (parentScopeExecution == null) {
      throw new ActivitiException("No sub process execution found for cancel end event " + executionEntity.getCurrentActivityId());
    }
    
    SubProcess subProcess = (SubProcess) parentScopeExecution.getCurrentFlowElement();
    BoundaryEvent cancelBoundaryEvent = null;
    if (CollectionUtils.isNotEmpty(subProcess.getBoundaryEvents())) {
      for (BoundaryEvent boundaryEvent : subProcess.getBoundaryEvents()) {
        if (CollectionUtils.isNotEmpty(boundaryEvent.getEventDefinitions()) && 
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
    currentlyExaminedExecution = executionEntityManager.findExecutionById(parentScopeExecution.getParentId());
    while (currentlyExaminedExecution != null && newParentScopeExecution == null) {
      if (currentlyExaminedExecution.isScope()) {
        newParentScopeExecution = currentlyExaminedExecution;
      } else {
        currentlyExaminedExecution = executionEntityManager.findExecutionById(currentlyExaminedExecution.getParentId());
      }
    }

    if (newParentScopeExecution == null) {
      throw new ActivitiException("Programmatic error: no parent scope execution found for boundary event " + cancelBoundaryEvent.getId());
    }
    
    ScopeUtil.createCopyOfSubProcessExecutionForCompensation(parentScopeExecution, newParentScopeExecution);
    
    if (subProcess.getLoopCharacteristics() != null) {
      List<ExecutionEntity> multiInstanceExecutions = parentScopeExecution.getExecutions();
      for (ExecutionEntity multiInstanceExecution : multiInstanceExecutions) {
        if (multiInstanceExecution.getId().equals(parentScopeExecution.getId()) == false) {
          ScopeUtil.createCopyOfSubProcessExecutionForCompensation(multiInstanceExecution, newParentScopeExecution);
          
          // end all executions in the scope of the transaction
          deleteChildExecutions(multiInstanceExecution, executionEntity, commandContext);
          
        }
      }
    }
    
    // set new parent for boundary event execution
    executionEntity.setParent(newParentScopeExecution);
    executionEntity.setCurrentFlowElement(cancelBoundaryEvent);
    
    // end all executions in the scope of the transaction
    deleteChildExecutions(parentScopeExecution, executionEntity, commandContext);

    commandContext.getAgenda().planTriggerExecutionOperation(executionEntity);
  }
  
  protected void deleteChildExecutions(ExecutionEntity parentExecution, ExecutionEntity notToDeleteExecution, CommandContext commandContext) {
    // Delete all child executions
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    Collection<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(parentExecution.getId());
    if (CollectionUtils.isNotEmpty(childExecutions)) {
      for (ExecutionEntity childExecution : childExecutions) {
        if (childExecution.getId().equals(notToDeleteExecution.getId()) == false) {
          deleteChildExecutions(childExecution, notToDeleteExecution, commandContext);
        }
      }
    }

    executionEntityManager.deleteDataRelatedToExecution(parentExecution);
    executionEntityManager.delete(parentExecution);
  }

}
