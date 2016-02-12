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
import org.activiti.engine.impl.util.tree.ExecutionTree;
import org.activiti.engine.impl.util.tree.ExecutionTreeBfsIterator;
import org.activiti.engine.impl.util.tree.ExecutionTreeNode;

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

    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

    ExecutionTree executionTree = executionEntityManager.findExecutionTree(execution.getRootProcessInstanceId());

    if (terminateAll) {
      deleteExecutionEntities(commandContext, executionEntityManager, executionTree.leafsFirstIterator());
    } else if (terminateMultiInstance) {
      terminateMultiInstanceRoot(execution, commandContext, executionEntityManager, executionTree);
    } else {
      defaultTerminateEndEventBehaviour(execution, commandContext, executionEntityManager, executionTree);
    }
  }

  protected void defaultTerminateEndEventBehaviour(DelegateExecution execution, CommandContext commandContext,
      ExecutionEntityManager executionEntityManager, ExecutionTree executionTree) {
    
    ExecutionTreeNode scopeTreeNode = executionTree.getTreeNode(execution.getId()).findFirstScope(); // There will always be one (the process instance), so no null checking needed
    ExecutionEntity scopeExecutionEntity = scopeTreeNode.getExecutionEntity();
    sendProcessInstanceCancelledEvent(scopeExecutionEntity, execution.getCurrentFlowElement());

    // If the scope is the process instance, we can just terminate it all
    // Special treatment is needed when the terminated activity is a subprocess (embedded/callactivity/..)
    // The subprocess is destroyed, but the execution calling it, continues further on.
    // In case of a multi-instance subprocess, only one instance is terminated, the other instances continue to exist.

    if (scopeExecutionEntity.isProcessInstanceType() && scopeExecutionEntity.getSuperExecutionId() == null) {

      deleteExecutionEntities(commandContext, executionEntityManager, executionTree.leafsFirstIterator());

    } else if (scopeExecutionEntity.getCurrentFlowElement() != null 
        && scopeExecutionEntity.getCurrentFlowElement() instanceof SubProcess) { // SubProcess

      SubProcess subProcess = (SubProcess) scopeExecutionEntity.getCurrentFlowElement();

      if (subProcess.hasMultiInstanceLoopCharacteristics()) {
        
        commandContext.getAgenda().planDestroyScopeOperation(scopeExecutionEntity);
        
        MultiInstanceActivityBehavior multiInstanceBehavior = (MultiInstanceActivityBehavior) subProcess.getBehavior();
        multiInstanceBehavior.leave(scopeExecutionEntity);
        
      } else {
        commandContext.getAgenda().planDestroyScopeOperation(scopeExecutionEntity);
        commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(scopeExecutionEntity);
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
      ExecutionEntityManager executionEntityManager, ExecutionTree executionTree) {
    
    // When terminateMultiInstance is 'true', we look for the multi instance root and delete it from there.
    
    ExecutionTreeNode currentTreeNode = executionTree.getTreeNode(execution.getId());
    ExecutionTreeNode multiInstanceRootTreeNode = currentTreeNode.findFirstMultiInstanceRoot();
    if (multiInstanceRootTreeNode != null) {
      
      // Create sibling execution to continue process instance execution before deletion
      ExecutionEntity multiInstanceRootExecution = multiInstanceRootTreeNode.getExecutionEntity();
      ExecutionEntity siblingExecution = executionEntityManager.createChildExecution(multiInstanceRootExecution.getParent());
      siblingExecution.setCurrentFlowElement(multiInstanceRootExecution.getCurrentFlowElement());
      
      deleteExecutionEntities(commandContext, executionEntityManager, multiInstanceRootTreeNode.leafsFirstIterator());
      
      commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(siblingExecution);
    } else {
      defaultTerminateEndEventBehaviour(execution, commandContext, executionEntityManager, executionTree);
    }
  }

  protected void deleteExecutionEntities(CommandContext commandContext, ExecutionEntityManager executionEntityManager, ExecutionTreeBfsIterator treeIterator) {

    // Delete the execution in leafs-first order to avoid foreign key
    // constraints firing

    while (treeIterator.hasNext()) {
      ExecutionTreeNode treeNode = treeIterator.next();
      ExecutionEntity executionEntity = treeNode.getExecutionEntity();
      executionEntityManager.deleteExecutionAndRelatedData(executionEntity, null, false);
    }
  }

  protected void sendProcessInstanceCancelledEvent(DelegateExecution execution, FlowElement terminateEndEvent) {
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher()
          .dispatchEvent(ActivitiEventBuilder.createCancelledEvent(execution.getId(), execution.getProcessInstanceId(), execution.getProcessDefinitionId(), execution.getCurrentFlowElement()));
    }

    dispatchExecutionCancelled(execution, terminateEndEvent);
  }

  protected void dispatchExecutionCancelled(DelegateExecution execution, FlowElement terminateEndEvent) {

    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

    // subprocesses
    for (DelegateExecution subExecution : executionEntityManager.findChildExecutionsByParentExecutionId(execution.getId())) {
      dispatchExecutionCancelled(subExecution, terminateEndEvent);
    }

    // call activities
    ExecutionEntity subProcessInstance = commandContext.getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(execution.getId());
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
