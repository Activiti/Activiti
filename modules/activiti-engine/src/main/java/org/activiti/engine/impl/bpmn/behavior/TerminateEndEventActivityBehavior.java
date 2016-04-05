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
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;

/**
 * @author Joram Barrez
 */
public class TerminateEndEventActivityBehavior extends FlowNodeActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected boolean terminateAll;
  protected boolean terminateMultiInstance;

  public TerminateEndEventActivityBehavior() {

  }

  @Override
  public void execute(DelegateExecution execution) {

    CommandContext commandContext = Context.getCommandContext();
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

    if (terminateAll) {
      terminateAllBehaviour(execution, commandContext, executionEntityManager);
    } else if (terminateMultiInstance) {
      terminateMultiInstanceRoot(execution, commandContext, executionEntityManager);
    } else {
      defaultTerminateEndEventBehaviour(execution, commandContext, executionEntityManager);
    }
  }

  protected void terminateAllBehaviour(DelegateExecution execution, CommandContext commandContext, ExecutionEntityManager executionEntityManager) {
    ExecutionEntity rootExecutionEntity = executionEntityManager.findByRootProcessInstanceId(execution.getRootProcessInstanceId());
    deleteExecutionEntities(executionEntityManager, rootExecutionEntity);
    commandContext.getHistoryManager().recordProcessInstanceEnd(rootExecutionEntity.getId(), "", execution.getCurrentActivityId());
  }

  protected void defaultTerminateEndEventBehaviour(DelegateExecution execution, CommandContext commandContext,
      ExecutionEntityManager executionEntityManager) {
    
    ExecutionEntity scopeExecutionEntity = executionEntityManager.findFirstScope((ExecutionEntity) execution);
    sendProcessInstanceCancelledEvent(scopeExecutionEntity, execution.getCurrentFlowElement());

    // If the scope is the process instance, we can just terminate it all
    // Special treatment is needed when the terminated activity is a subprocess (embedded/callactivity/..)
    // The subprocess is destroyed, but the execution calling it, continues further on.
    // In case of a multi-instance subprocess, only one instance is terminated, the other instances continue to exist.

    if (scopeExecutionEntity.isProcessInstanceType() && scopeExecutionEntity.getSuperExecutionId() == null) {

      deleteExecutionEntities(executionEntityManager, scopeExecutionEntity);
      commandContext.getHistoryManager().recordProcessInstanceEnd(scopeExecutionEntity.getId(), "", execution.getCurrentActivityId());

    } else if (scopeExecutionEntity.getCurrentFlowElement() != null 
        && scopeExecutionEntity.getCurrentFlowElement() instanceof SubProcess) { // SubProcess

      SubProcess subProcess = (SubProcess) scopeExecutionEntity.getCurrentFlowElement();

      if (subProcess.hasMultiInstanceLoopCharacteristics()) {
        
        commandContext.getAgenda().planDestroyScopeOperation(scopeExecutionEntity);
        MultiInstanceActivityBehavior multiInstanceBehavior = (MultiInstanceActivityBehavior) subProcess.getBehavior();
        multiInstanceBehavior.leave(scopeExecutionEntity);
        
      } else {
        commandContext.getAgenda().planDestroyScopeOperation(scopeExecutionEntity);
        ExecutionEntity outgoingFlowExecution = executionEntityManager.createChildExecution(scopeExecutionEntity.getParent());
        outgoingFlowExecution.setCurrentFlowElement(scopeExecutionEntity.getCurrentFlowElement());
        commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(outgoingFlowExecution);
      }

    } else if (scopeExecutionEntity.getParentId() == null 
        && scopeExecutionEntity.getSuperExecutionId() != null) { // CallActivity

      ExecutionEntity callActivityExecution = scopeExecutionEntity.getSuperExecution();
      CallActivity callActivity = (CallActivity) callActivityExecution.getCurrentFlowElement();

      if (callActivity.hasMultiInstanceLoopCharacteristics()) {

        MultiInstanceActivityBehavior multiInstanceBehavior = (MultiInstanceActivityBehavior) callActivity.getBehavior();
        multiInstanceBehavior.leave(callActivityExecution);
        executionEntityManager.deleteProcessInstanceExecutionEntity(scopeExecutionEntity.getId(), execution.getCurrentFlowElement().getId(), "terminate end event", false, false, true);

      } else {

        executionEntityManager.deleteProcessInstanceExecutionEntity(scopeExecutionEntity.getId(), execution.getCurrentFlowElement().getId(), "terminate end event", false, false, true);
        ExecutionEntity superExecutionEntity = executionEntityManager.findById(scopeExecutionEntity.getSuperExecutionId());
        commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(superExecutionEntity);

      }
      
    }
  }
  
  protected void terminateMultiInstanceRoot(DelegateExecution execution, CommandContext commandContext,
      ExecutionEntityManager executionEntityManager) {
    
    // When terminateMultiInstance is 'true', we look for the multi instance root and delete it from there.
    ExecutionEntity miRootExecutionEntity = executionEntityManager.findFirstMultiInstanceRoot((ExecutionEntity) execution);
    if (miRootExecutionEntity != null) {
      
      // Create sibling execution to continue process instance execution before deletion
      ExecutionEntity siblingExecution = executionEntityManager.createChildExecution(miRootExecutionEntity.getParent());
      siblingExecution.setCurrentFlowElement(miRootExecutionEntity.getCurrentFlowElement());
      
      deleteExecutionEntities(executionEntityManager, miRootExecutionEntity);
      
      commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(siblingExecution);
    } else {
      defaultTerminateEndEventBehaviour(execution, commandContext, executionEntityManager);
    }
  }

  protected void deleteExecutionEntities(ExecutionEntityManager executionEntityManager, ExecutionEntity rootExecutionEntity) {
    
    List<ExecutionEntity> childExecutions = executionEntityManager.collectChildren(rootExecutionEntity);
    for (int i=childExecutions.size()-1; i>=0; i--) {
      executionEntityManager.deleteExecutionAndRelatedData(childExecutions.get(i), null, false);
    }
    executionEntityManager.deleteExecutionAndRelatedData(rootExecutionEntity, null, false);
  }

  protected void sendProcessInstanceCancelledEvent(DelegateExecution execution, FlowElement terminateEndEvent) {
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher()
          .dispatchEvent(ActivitiEventBuilder.createCancelledEvent(execution.getId(), execution.getProcessInstanceId(), execution.getProcessDefinitionId(), execution.getCurrentFlowElement()));
    }

    dispatchExecutionCancelled(execution, terminateEndEvent);
  }

  protected void dispatchExecutionCancelled(DelegateExecution execution, FlowElement terminateEndEvent) {

    ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();

    // subprocesses
    for (DelegateExecution subExecution : executionEntityManager.findChildExecutionsByParentExecutionId(execution.getId())) {
      dispatchExecutionCancelled(subExecution, terminateEndEvent);
    }

    // call activities
    ExecutionEntity subProcessInstance = Context.getCommandContext().getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(execution.getId());
    if (subProcessInstance != null) {
      dispatchExecutionCancelled(subProcessInstance, terminateEndEvent);
    }

    // activity with message/signal boundary events
    FlowElement currentFlowElement = execution.getCurrentFlowElement();
    if (currentFlowElement != null && currentFlowElement instanceof FlowNode) {
      dispatchActivityCancelled(execution, terminateEndEvent);
    }
  }

  protected void dispatchActivityCancelled(DelegateExecution execution, FlowElement terminateEndEvent) {
    Context
        .getProcessEngineConfiguration()
        .getEventDispatcher()
        .dispatchEvent(
            ActivitiEventBuilder.createActivityCancelledEvent(execution.getCurrentFlowElement().getId(), execution.getCurrentFlowElement().getName(), execution.getId(),
                execution.getProcessInstanceId(), execution.getProcessDefinitionId(), execution.getCurrentFlowElement().getClass().getName(), ((FlowNode) execution.getCurrentFlowElement())
                    .getBehavior().getClass().getCanonicalName(), terminateEndEvent));
  }

  public boolean isTerminateAll() {
    return terminateAll;
  }

  public void setTerminateAll(boolean terminateAll) {
    this.terminateAll = terminateAll;
  }

  public boolean isTerminateMultiInstance() {
    return terminateMultiInstance;
  }

  public void setTerminateMultiInstance(boolean terminateMultiInstance) {
    this.terminateMultiInstance = terminateMultiInstance;
  }
  
}
