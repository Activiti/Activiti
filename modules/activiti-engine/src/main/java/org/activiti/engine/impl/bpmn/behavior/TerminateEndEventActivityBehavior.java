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

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
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
  
  protected boolean destroyProcessInstance;
	
	@Override
	public void execute(ActivityExecution execution) {
		
		CommandContext commandContext = Context.getCommandContext();
		ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
		
		ExecutionTree executionTree = executionEntityManager.findExecutionTree(execution.getRootProcessInstanceId());
		
		if (destroyProcessInstance) {

		  // Not yet enabled in BPMN
		  
//			deleteExecutionEntities(commandContext, executionEntityManager, executionTree.leafsFirstIterator());
			
		} else {
			
			// Find the lowest scope for the element, and schedule the destruction of the scope
			
			ExecutionTreeNode scopeTreeNode = executionTree.getTreeNode(execution.getId());
			while (!scopeTreeNode.getExecutionEntity().isScope()) {
				scopeTreeNode = scopeTreeNode.getParent();
				
				if (scopeTreeNode == null) {
					break;
				}
			}
			
			ExecutionEntity scopeExecutionEntity = scopeTreeNode.getExecutionEntity(); 
			sendProcessInstanceCancelledEvent(scopeExecutionEntity, execution.getCurrentFlowElement());
			if (scopeExecutionEntity.getParentId() == null) {
				
				// Call activity needs special handling: the call activity is destroyed, but the main process continues
				if (scopeExecutionEntity.getSuperExecutionId() != null) {
				  
				  executionEntityManager.deleteProcessInstanceExecutionEntity(scopeExecutionEntity.getId(), execution.getCurrentFlowElement().getId(), "terminate end event");
					ExecutionEntity superExecutionEntity = executionEntityManager.findExecutionById(scopeExecutionEntity.getSuperExecutionId());
					commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(superExecutionEntity);
					
				} else {
				  
				  deleteExecutionEntities(commandContext, executionEntityManager, executionTree.leafsFirstIterator());
				}
				
			} else {
			  SubProcess subProcess = null;
			  boolean isMultiInstance = false;
			  if (scopeExecutionEntity.getCurrentFlowElement() instanceof SubProcess) {
			    subProcess = (SubProcess) scopeExecutionEntity.getCurrentFlowElement();
			    if (subProcess.getLoopCharacteristics() != null) {
			      isMultiInstance = true;
			    }
			  }
			  if (isMultiInstance) {
			    MultiInstanceActivityBehavior multiInstanceBehavior = (MultiInstanceActivityBehavior) subProcess.getBehavior();
          multiInstanceBehavior.leave(scopeExecutionEntity);
          if (subProcess.getLoopCharacteristics().isSequential() == false) {
            commandContext.getAgenda().planDestroyScopeOperation(scopeExecutionEntity);
          }
			    
			  } else {
  				commandContext.getAgenda().planDestroyScopeOperation(scopeExecutionEntity);
  				commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(scopeExecutionEntity);
			  }
			}
			
		}
	}

	protected void deleteExecutionEntities(CommandContext commandContext, 
			ExecutionEntityManager executionEntityManager, ExecutionTreeBfsIterator treeIterator) {
		
		  // Delete the execution in leafs-first order to avoid foreign key constraints firing
		
	    while (treeIterator.hasNext()) {
	    	ExecutionTreeNode treeNode = treeIterator.next();
	    	ExecutionEntity executionEntity = treeNode.getExecutionEntity();
	    	executionEntityManager.deleteExecutionAndRelatedData(executionEntity);
	    }
    }
	
    protected void sendProcessInstanceCancelledEvent(ActivityExecution execution, FlowElement terminateEndEvent) {
        if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            Context.getProcessEngineConfiguration().getEventDispatcher()
                    .dispatchEvent(ActivitiEventBuilder.createCancelledEvent(execution.getId(), execution.getProcessInstanceId(), execution.getProcessDefinitionId(), execution.getCurrentFlowElement()));
        }
        
        dispatchExecutionCancelled(execution, terminateEndEvent);
    }

    protected void dispatchExecutionCancelled(ActivityExecution execution, FlowElement terminateEndEvent) {
      
       ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();
      
        // subprocesses
        for (ActivityExecution subExecution : executionEntityManager.findChildExecutionsByParentExecutionId(execution.getId())) {
            dispatchExecutionCancelled(subExecution, terminateEndEvent);
        }

        // call activities
        ExecutionEntity subProcessInstance = Context.getCommandContext().getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(execution.getId());
        if (subProcessInstance != null) {
            dispatchExecutionCancelled(subProcessInstance,  terminateEndEvent);
        }

        // activity with message/signal boundary events
        FlowElement currentFlowElement = execution.getCurrentFlowElement();
        if (currentFlowElement != null && currentFlowElement instanceof FlowNode) {
          dispatchActivityCancelled(execution, terminateEndEvent);
        }
    }

    protected void dispatchActivityCancelled(ActivityExecution execution, FlowElement terminateEndEvent) {
        Context.getProcessEngineConfiguration()
                .getEventDispatcher()
                .dispatchEvent(
                        ActivitiEventBuilder.createActivityCancelledEvent(
                            execution.getCurrentFlowElement().getId(), 
                            execution.getCurrentFlowElement().getName(), 
                            execution.getId(), 
                            execution.getProcessInstanceId(),
                            execution.getProcessDefinitionId(), 
                            execution.getCurrentFlowElement().getClass().getName(), 
                            ((FlowNode) execution.getCurrentFlowElement()).getBehavior().getClass().getCanonicalName(), 
                            terminateEndEvent));
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
