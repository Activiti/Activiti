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

import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.JobEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManager;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.activiti.engine.impl.util.tree.ExecutionTree;
import org.activiti.engine.impl.util.tree.ExecutionTreeBfsIterator;
import org.activiti.engine.impl.util.tree.ExecutionTreeNode;
import org.activiti.engine.impl.util.tree.ExecutionTreeUtil;

/**
 * @author Joram Barrez
 */
public class TerminateEndEventActivityBehavior extends FlowNodeActivityBehavior {
	
	protected boolean destroyProcessInstance;
	
	@Override
	public void execute(ActivityExecution execution) {
		
		CommandContext commandContext = Context.getCommandContext();
		ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
		
		// Fetch the whole execution tree 
		
		String processInstanceId = execution.getProcessInstanceId();
		ExecutionEntity processInstanceExecutionEntity = executionEntityManager.findExecutionById(processInstanceId);
		
		List<ExecutionEntity> allExecutionEntities = executionEntityManager.findChildExecutionsByProcessInstanceId(processInstanceId);
		allExecutionEntities.add(processInstanceExecutionEntity); // Collections needs to contain all for the util method to work

		ExecutionTree executionTree = ExecutionTreeUtil.buildExecutionTree(allExecutionEntities);
		
		if (destroyProcessInstance) {

			deleteExecutionEntities(commandContext, executionEntityManager, executionTree.leafsFirstIterator());
			
		} else {
			
			// Find the highest scope for the element, and schedule the destruction of the scope
			
			ExecutionTreeNode scopeTreeNode = executionTree.getTreeNode(execution.getId());
			while (!scopeTreeNode.getExecutionEntity().isScope()) {
				scopeTreeNode = scopeTreeNode.getParent();
				
				if (scopeTreeNode == null) {
					break;
				}
			}
			
			if (scopeTreeNode.getExecutionEntity().getParentId() == null) {
				deleteExecutionEntities(commandContext, executionEntityManager, executionTree.leafsFirstIterator());
			} else {
				commandContext.getAgenda().planDestroyScopeOperation(scopeTreeNode.getExecutionEntity());
				commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(scopeTreeNode.getExecutionEntity());
			}
		}
		
		
	}

	protected void deleteExecutionEntities(CommandContext commandContext, 
			ExecutionEntityManager executionEntityManager, ExecutionTreeBfsIterator treeIterator) {
		
		// Delete the execution in leafs-first order to avoid foreign key constraints firing
		
	    while (treeIterator.hasNext()) {
	    	ExecutionTreeNode treeNode = treeIterator.next();
	    	ExecutionEntity executionEntity = treeNode.getExecutionEntity();
	    	deleteDataRelatedToExecution(commandContext, executionEntity);
	    	executionEntityManager.delete(executionEntity);
	    }
    }
	
	// TODO: Copied from AbstractOperation: remove duplication!
	
	protected void deleteDataRelatedToExecution(CommandContext commandContext, ExecutionEntity executionEntity) {

		// To start, deactivate the current incoming execution
		executionEntity.setEnded(true);
		executionEntity.setActive(false);

		// Get variables related to execution and delete them
		VariableInstanceEntityManager variableInstanceEntityManager = commandContext.getVariableInstanceEntityManager();
		Collection<VariableInstanceEntity> executionVariables = variableInstanceEntityManager.findVariableInstancesByExecutionId(executionEntity.getId());
		for (VariableInstanceEntity variableInstanceEntity : executionVariables) {
			variableInstanceEntityManager.delete(variableInstanceEntity);
			if (variableInstanceEntity.getByteArrayValueId() != null) {
				commandContext.getByteArrayEntityManager().deleteByteArrayById(variableInstanceEntity.getByteArrayValueId());
			}
		}

		// Delete current user tasks
		TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
		Collection<TaskEntity> tasksForExecution = taskEntityManager.findTasksByExecutionId(executionEntity.getId());
		for (TaskEntity taskEntity : tasksForExecution) {
			taskEntityManager.delete(taskEntity);
		}

		// Delete jobs
		JobEntityManager jobEntityManager = commandContext.getJobEntityManager();
		Collection<JobEntity> jobsForExecution = jobEntityManager.findJobsByExecutionId(executionEntity.getId());
		for (JobEntity job : jobsForExecution) {
			jobEntityManager.delete(job);
		}
	}

//    public void execute(ActivityExecution execution) {
//
//        PvmActivity terminateEndEventActivity = execution.getActivity();
//        ActivityExecution scopeExecution = ScopeUtil.findScopeExecution(execution);
//
//        boolean loop = true;
//        // get top superexecution to terminate
//        while (scopeExecution.getSuperExecutionId() != null && loop) {
//            ActivityExecution superExecution = (ActivityExecution) Context.getProcessEngineConfiguration().getRuntimeService().createExecutionQuery().executionId(scopeExecution.getSuperExecutionId())
//                    .singleResult();
//            if (superExecution != null) {
//                // superExecution can be null in the case when no wait state was
//                // reached between super start event and TerminateEndEvent
//                while (superExecution.getParent() != null) {
//                    superExecution = superExecution.getParent();
//                }
//                scopeExecution = superExecution;
//            } else {
//                loop = false;
//            }
//        }
//
////        terminateExecution(execution, terminateEndEventActivity, scopeExecution);
//    }

    private void terminateExecution(ActivityExecution execution, ActivityImpl terminateEndEventActivity, ActivityExecution scopeExecution) {
        // send cancelled event
        sendCancelledEvent(execution, terminateEndEventActivity, scopeExecution);

        // destroy the scope
        scopeExecution.destroyScope("terminate end event fired");

        // set the scope execution to the terminate end event and make it end here.
        // (the history should reflect that the execution ended here and we want an 'end time' for the historic activity instance.)
        ((InterpretableExecution) scopeExecution).setActivity(terminateEndEventActivity);
        
        // end the scope execution
        scopeExecution.end();
    }

    private void sendCancelledEvent(ActivityExecution execution, ActivityImpl terminateEndEventActivity, ActivityExecution scopeExecution) {
        if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            Context.getProcessEngineConfiguration().getEventDispatcher()
                    .dispatchEvent(ActivitiEventBuilder.createCancelledEvent(execution.getId(), execution.getProcessInstanceId(), execution.getProcessDefinitionId(), terminateEndEventActivity));
        }
        dispatchExecutionCancelled(scopeExecution, terminateEndEventActivity);
    }

    private void dispatchExecutionCancelled(ActivityExecution execution, ActivityImpl causeActivity) {
        // subprocesses
        for (ActivityExecution subExecution : execution.getExecutions()) {
            dispatchExecutionCancelled(subExecution, causeActivity);
        }

        // call activities
        ExecutionEntity subProcessInstance = Context.getCommandContext().getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(execution.getId());
        if (subProcessInstance != null) {
            dispatchExecutionCancelled(subProcessInstance, causeActivity);
        }

        // activity with message/signal boundary events
        ActivityImpl activity = (ActivityImpl) execution.getActivity();
        if (activity != null && activity.getActivityBehavior() != null && activity != causeActivity) {
            dispatchActivityCancelled(execution, activity, causeActivity);
        }
    }

    private void dispatchActivityCancelled(ActivityExecution execution, ActivityImpl activity, ActivityImpl causeActivity) {
        Context.getProcessEngineConfiguration()
                .getEventDispatcher()
                .dispatchEvent(
                        ActivitiEventBuilder.createActivityCancelledEvent(activity.getId(), (String) activity.getProperties().get("name"), execution.getId(), execution.getProcessInstanceId(),
                                execution.getProcessDefinitionId(), (String) activity.getProperties().get("type"), activity.getActivityBehavior().getClass().getCanonicalName(), causeActivity));
    }

}
