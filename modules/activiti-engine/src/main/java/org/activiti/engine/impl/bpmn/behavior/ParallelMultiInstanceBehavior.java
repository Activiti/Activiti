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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Transaction;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.tree.ExecutionTree;
import org.activiti.engine.impl.util.tree.ExecutionTreeNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class ParallelMultiInstanceBehavior extends MultiInstanceActivityBehavior {

  private static final long serialVersionUID = 1L;

  public ParallelMultiInstanceBehavior(Activity activity, AbstractBpmnActivityBehavior originalActivityBehavior) {
    super(activity, originalActivityBehavior);
  }

  /**
   * Handles the parallel case of spawning the instances. Will create child executions accordingly for every instance needed.
   */
  protected void createInstances(ActivityExecution execution) {
    int nrOfInstances = resolveNrOfInstances(execution);
    if (nrOfInstances < 0) {
      throw new ActivitiIllegalArgumentException("Invalid number of instances: must be non-negative integer value" + ", but was " + nrOfInstances);
    }

    setLoopVariable(execution, NUMBER_OF_INSTANCES, nrOfInstances);
    setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, 0);
    setLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES, nrOfInstances);

    List<ActivityExecution> concurrentExecutions = new ArrayList<ActivityExecution>();
    for (int loopCounter = 0; loopCounter < nrOfInstances; loopCounter++) {
      ActivityExecution concurrentExecution = execution.createExecution();
      concurrentExecution.setCurrentFlowElement(activity);
      concurrentExecution.setActive(true);
      concurrentExecution.setConcurrent(true);
      concurrentExecution.setScope(false);

      concurrentExecutions.add(concurrentExecution);
      logLoopDetails(concurrentExecution, "initialized", loopCounter, 0, nrOfInstances, nrOfInstances);
    }

    // Before the activities are executed, all executions MUST be created up front
    // Do not try to merge this loop with the previous one, as it will lead
    // to bugs, due to possible child execution pruning.
    for (int loopCounter = 0; loopCounter < nrOfInstances; loopCounter++) {
      ActivityExecution concurrentExecution = concurrentExecutions.get(loopCounter);
      // executions can be inactive, if instances are all automatics
      // (no-waitstate) and completionCondition has been met in the meantime
      if (concurrentExecution.isActive() && !concurrentExecution.isEnded() && concurrentExecution.getParent().isActive() && !concurrentExecution.getParent().isEnded()) {
        setLoopVariable(concurrentExecution, getCollectionElementIndexVariable(), loopCounter);
        executeOriginalBehavior(concurrentExecution, loopCounter);
      }
    }

    // See ACT-1586: ExecutionQuery returns wrong results when using multi
    // instance on a receive task The parent execution must be set to false, so it wouldn't show up in
    // the execution query when using .activityId(something). Do not we cannot nullify the
    // activityId (that would have been a better solution), as it would break boundary event behavior.
    if (!concurrentExecutions.isEmpty()) {
      ExecutionEntity executionEntity = (ExecutionEntity) execution;
      executionEntity.setActive(false);
    }
  }

  /**
   * Called when the wrapped {@link ActivityBehavior} calls the {@link AbstractBpmnActivityBehavior#leave(ActivityExecution)} method. Handles the completion of one of the parallel instances
   */
  public void leave(ActivityExecution execution) {

    if (resolveNrOfInstances(execution) == 0) {
      // Empty collection, just leave.
      removeLocalLoopVariable(execution, getCollectionElementIndexVariable());
      super.leave(execution);
    }

    int loopCounter = getLoopVariable(execution, getCollectionElementIndexVariable());
    int nrOfInstances = getLoopVariable(execution, NUMBER_OF_INSTANCES);
    int nrOfCompletedInstances = getLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES) + 1;
    int nrOfActiveInstances = getLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES) - 1;
    
    callActivityEndListeners(execution);

    if (execution.getParent() != null) { // will be null in case of empty collection
      setLoopVariable(execution.getParent(), NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances);
      setLoopVariable(execution.getParent(), NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances);
    }
    
    //executeCompensationBoundaryEvents(execution.getCurrentFlowElement(), execution);
    
    logLoopDetails(execution, "instance completed", loopCounter, nrOfCompletedInstances, nrOfActiveInstances, nrOfInstances);

    ExecutionEntity executionEntity = (ExecutionEntity) execution;

    if (executionEntity.getParent() != null) {

      executionEntity.inactivate();
      lockFirstParentScope(executionEntity);

      if (nrOfCompletedInstances >= nrOfInstances || completionConditionSatisfied(execution.getParent())) {

        ExecutionEntity workWithExecution = null;
        if (nrOfInstances > 0) {
          workWithExecution = executionEntity.getParent();
        } else {
          workWithExecution = executionEntity;
        }
        
        boolean hasCompensation = false;
        Activity activity = (Activity) execution.getCurrentFlowElement(); 
        if (activity instanceof Transaction) {
          hasCompensation = true;
        } else if (activity instanceof SubProcess) {
          SubProcess subProcess = (SubProcess) activity;
          for (FlowElement subElement : subProcess.getFlowElements()) {
            if (subElement instanceof Activity) {
              Activity subActivity = (Activity) subElement;
              if (CollectionUtils.isNotEmpty(subActivity.getBoundaryEvents())) {
                for (BoundaryEvent boundaryEvent : subActivity.getBoundaryEvents()) {
                  if (CollectionUtils.isNotEmpty(boundaryEvent.getEventDefinitions()) && 
                      boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
                    
                    hasCompensation = true;
                    break;
                  }
                }
              }
            }
          }
        }
        
        if (hasCompensation) {
          ScopeUtil.createCopyOfSubProcessExecutionForCompensation(workWithExecution, workWithExecution.getParent());
        }
        
        if (activity instanceof CallActivity) {
          ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
          ExecutionTree executionTree = executionEntityManager.findExecutionTree(executionEntity.getRootProcessInstanceId());
          ExecutionTreeNode executionTreeNode = executionTree.getTreeNode(workWithExecution.getId());
          
          if (executionTreeNode != null) {
            List<String> callActivityExecutionIds = new ArrayList<String>();
            List<ExecutionTreeNode> callActivityChildren = executionTreeNode.getChildren();
            for (ExecutionTreeNode callActivityChild : callActivityChildren) {
              if (activity.getId().equals(callActivityChild.getExecutionEntity().getCurrentActivityId())) {
                callActivityExecutionIds.add(callActivityChild.getExecutionEntity().getId());
              }
            }
            
            Iterator<ExecutionTreeNode> iterator = executionTreeNode.leafsFirstIterator();
            while (iterator.hasNext()) {
              ExecutionEntity childExecutionEntity = iterator.next().getExecutionEntity();
              if (StringUtils.isNotEmpty(childExecutionEntity.getSuperExecutionId()) && callActivityExecutionIds.contains(childExecutionEntity.getSuperExecutionId())) {
                executionEntityManager.deleteProcessInstanceExecutionEntity(childExecutionEntity, activity.getId(), "call activity completion condition met", true, false);
              }
            }
          }
        }
        
        deleteChildExecutions(workWithExecution, false, Context.getCommandContext());
        Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(workWithExecution, true);
      }

    } else {
      removeLocalLoopVariable(execution, getCollectionElementIndexVariable());
      super.leave(executionEntity);
    }
  }
  
  protected void lockFirstParentScope(ActivityExecution execution) {
    
    ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
    
    boolean found = false;
    ExecutionEntity parentScopeExecution = null;
    ExecutionEntity currentExecution = (ExecutionEntity) execution;
    while (!found && currentExecution != null && currentExecution.getParentId() != null) {
      parentScopeExecution = executionEntityManager.findExecutionById(currentExecution.getParentId());
      if (parentScopeExecution != null && parentScopeExecution.isScope()) {
        found = true;
      }
      currentExecution = parentScopeExecution;
    }
    
    parentScopeExecution.forceUpdate();
  }

  // TODO: can the ExecutionManager.deleteChildExecution not be used?
  protected void deleteChildExecutions(ExecutionEntity parentExecution, boolean deleteExecution, CommandContext commandContext) {
    // Delete all child executions
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    Collection<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(parentExecution.getId());
    if (CollectionUtils.isNotEmpty(childExecutions)) {
      for (ExecutionEntity childExecution : childExecutions) {
        deleteChildExecutions(childExecution, true, commandContext);
      }
    }

    if (deleteExecution) {
      executionEntityManager.deleteDataRelatedToExecution(parentExecution);
      commandContext.getExecutionEntityManager().delete(parentExecution);
    }
  }

}
