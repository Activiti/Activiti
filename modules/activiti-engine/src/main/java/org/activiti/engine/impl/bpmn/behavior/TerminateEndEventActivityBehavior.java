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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.TerminateEventDefinition;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;

/**
 * @author Martin Grofcik
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class TerminateEndEventActivityBehavior extends FlowNodeActivityBehavior {
  
  private static final long serialVersionUID = 1L;
  
  protected EndEvent endEvent;
  protected boolean terminateAll; 
  
  public TerminateEndEventActivityBehavior(EndEvent endEvent) {
    this.endEvent = endEvent.clone();
    
    // Terminate all attribute
    if (endEvent.getEventDefinitions() != null) {
    	for (EventDefinition eventDefinition : endEvent.getEventDefinitions()) {
    		if (eventDefinition instanceof TerminateEventDefinition) {
    			TerminateEventDefinition terminateEventDefinition = (TerminateEventDefinition) eventDefinition;
    			if (terminateEventDefinition.isTerminateAll()) {
    				this.terminateAll = true;
    				break;
    			}
    		}
    	}
    }
    
  }

  public void execute(ActivityExecution execution) throws Exception {
    ActivityImpl terminateEndEventActivity = (ActivityImpl) execution.getActivity();
    
    if (terminateAll) {
    	ActivityExecution processInstanceExecution = findProcessInstanceExecution(execution);
    	terminateProcessInstanceExecution(execution, terminateEndEventActivity, processInstanceExecution);
    } else {
    	ActivityExecution scopeExecution = ScopeUtil.findScopeExecution(execution);
    	terminateExecution(execution, terminateEndEventActivity, scopeExecution);
    }
    
  }
  
  /**
   * Finds the parent execution that is a process instance.
   * For a callactivity, this will be the process instance representing the called process instance
   * and NOT the root process instance! 
   */
  protected ActivityExecution findProcessInstanceExecution(ActivityExecution execution) {
  	ActivityExecution currentExecution = execution;
  	while (currentExecution.getParent() != null) {
  		currentExecution = currentExecution.getParent();
  	}
  	return currentExecution;
  }

  protected void terminateExecution(ActivityExecution execution, ActivityImpl terminateEndEventActivity, ActivityExecution scopeExecution) {
    // send cancelled event
    sendCancelledEvent( execution, terminateEndEventActivity, scopeExecution);

    // destroy the scope
    scopeExecution.destroyScope("terminate end event fired");

    // set the scope execution to the terminate end event and make it end here.
    // (the history should reflect that the execution ended here and we want an 'end time' for the
    // historic activity instance.)
    ((InterpretableExecution)scopeExecution).setActivity(terminateEndEventActivity);
    // end the scope execution
    scopeExecution.end();
  }
  
  protected void terminateProcessInstanceExecution(ActivityExecution execution, ActivityImpl terminateEndEventActivity, ActivityExecution processInstanceExecution) {
    sendCancelledEvent( execution, terminateEndEventActivity, processInstanceExecution);
    deleteProcessInstance((ExecutionEntity) processInstanceExecution, "terminate end event (" + terminateEndEventActivity.getId() + ")");
  }
  
  protected void deleteProcessInstance(ExecutionEntity processInstanceExecution, String deleteReason) {
  	
    List<ExecutionEntity> orderedExecutions = orderExecutionsRootToLeaf(processInstanceExecution);
    Collections.reverse(orderedExecutions);
    
    for (ExecutionEntity executionToDelete : orderedExecutions) {
    	executionToDelete.setDeleteReason(deleteReason);
    	executionToDelete.setEnded(true);
    	executionToDelete.setActive(false);
    	executionToDelete.setDeleteRoot(true);
      
    	executionToDelete.remove();
    }
    
  }
  
  protected List<ExecutionEntity> orderExecutionsRootToLeaf(ExecutionEntity execution) {
  	
  	// Find root process instance
  	ExecutionEntity rootExecution = execution;
  	while (rootExecution.getParent() != null || rootExecution.getSuperExecution() != null) {
  		rootExecution = rootExecution.getParent() != null ? rootExecution.getParent() : rootExecution.getSuperExecution();
  	}
  	
  	return orderExecutionsRootToLeaf(rootExecution, new ArrayList<ExecutionEntity>());
  }
  
  protected List<ExecutionEntity> orderExecutionsRootToLeaf(ExecutionEntity rootExecution, List<ExecutionEntity> orderedExecutions) {
    orderedExecutions.add(rootExecution);
    
    
    // Children
    if (rootExecution.getExecutions() != null && rootExecution.getExecutions().size() > 0) {
    	for (ExecutionEntity childExecution : rootExecution.getExecutions()) {
    		orderExecutionsRootToLeaf(childExecution, orderedExecutions);
    	}
    }
    
    // Called process instances (subprocess)
    if (rootExecution.getSubProcessInstance() != null) {
    	orderExecutionsRootToLeaf(rootExecution.getSubProcessInstance(), orderedExecutions);
    }
    
    return orderedExecutions;
  }
  
  protected void sendCancelledEvent(ActivityExecution execution, ActivityImpl terminateEndEventActivity, ActivityExecution scopeExecution) {
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
              ActivitiEventBuilder.createCancelledEvent(execution.getId(), execution.getProcessInstanceId(),
                      execution.getProcessDefinitionId(), terminateEndEventActivity));
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

  protected void dispatchActivityCancelled(ActivityExecution execution, ActivityImpl activity, ActivityImpl causeActivity) {
    Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
            ActivitiEventBuilder.createActivityCancelledEvent(activity.getId(),
                    (String) activity.getProperties().get("name"),
                    execution.getId(),
                    execution.getProcessInstanceId(), execution.getProcessDefinitionId(),
                    (String) activity.getProperties().get("type"),
                    activity.getActivityBehavior().getClass().getCanonicalName(),
                    causeActivity)
    );
  }
  
  public EndEvent getEndEvent() {
    return this.endEvent;
  }

}