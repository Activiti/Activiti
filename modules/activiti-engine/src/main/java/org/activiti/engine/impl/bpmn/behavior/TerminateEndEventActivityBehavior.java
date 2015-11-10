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

  public TerminateEndEventActivityBehavior() {

  }

  public TerminateEndEventActivityBehavior(boolean terminateAll) {
    this.terminateAll = terminateAll;
  }

  @Override
  public void execute(DelegateExecution execution) {

    CommandContext commandContext = Context.getCommandContext();
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

    ExecutionTree executionTree = executionEntityManager.findExecutionTree(execution.getRootProcessInstanceId());

    if (terminateAll) {

      deleteExecutionEntities(commandContext, executionEntityManager, executionTree.leafsFirstIterator());

    } else {

      ExecutionTreeNode scopeTreeNode = findFirstScope(execution, executionTree);

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
  }

  protected ExecutionTreeNode findFirstScope(DelegateExecution execution, ExecutionTree executionTree) {
    ExecutionTreeNode scopeTreeNode = executionTree.getTreeNode(execution.getId());
    while (!scopeTreeNode.getExecutionEntity().isScope()) {
      scopeTreeNode = scopeTreeNode.getParent();

      if (scopeTreeNode == null) {
        break;
      }
    }
    return scopeTreeNode;
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

  protected boolean hasMultiInstanceParent(FlowNode flowNode) {
    boolean hasMultiInstanceParent = false;
    if (flowNode.getSubProcess() != null) {
      if (flowNode.getSubProcess().getLoopCharacteristics() != null) {
        hasMultiInstanceParent = true;
      } else {
        boolean hasNestedMultiInstanceParent = hasMultiInstanceParent(flowNode.getSubProcess());
        if (hasNestedMultiInstanceParent) {
          hasMultiInstanceParent = true;
        }
      }
    }

    return hasMultiInstanceParent;
  }

}
