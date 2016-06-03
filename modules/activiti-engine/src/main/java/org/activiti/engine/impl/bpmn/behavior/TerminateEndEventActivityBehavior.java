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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.TerminateEventDefinition;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
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
    	ActivityExecution processInstanceExecution = findRootProcessInstanceExecution((ExecutionEntity) execution);
    	terminateProcessInstanceExecution(execution, terminateEndEventActivity, processInstanceExecution);
    } else {
    	ActivityExecution scopeExecution = ScopeUtil.findScopeExecution(execution);
    	if (scopeExecution != null) {
    		terminateExecution(execution, terminateEndEventActivity, scopeExecution);
    	} 
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
  
  
  protected ActivityExecution findRootProcessInstanceExecution(ExecutionEntity execution) {
    ExecutionEntity currentExecution = execution;
    while (currentExecution.getParentId() != null || currentExecution.getSuperExecutionId() != null) {
      ExecutionEntity parentExecution = currentExecution.getParent();
      if (parentExecution != null) {
        currentExecution = parentExecution;
      } else if (currentExecution.getSuperExecutionId() != null) {
        currentExecution = currentExecution.getSuperExecution();
      }
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
    
    // Scope execution can already have been ended (for example when multiple seq flow arrive in the same terminate end event)
    // in that case, we need to make sure the activity instance is ended
    if (scopeExecution.isEnded()) {
      Context.getCommandContext().getHistoryManager().recordActivityEnd((ExecutionEntity) execution);
    } 
    
  }
  
  protected void terminateProcessInstanceExecution(ActivityExecution execution, ActivityImpl terminateEndEventActivity, ActivityExecution processInstanceExecution) {
    sendCancelledEvent( execution, terminateEndEventActivity, processInstanceExecution);
    deleteProcessInstance((ExecutionEntity) processInstanceExecution, execution, "terminate end event (" + terminateEndEventActivity.getId() + ")");
  }
  
  protected void deleteProcessInstance(ExecutionEntity processInstanceExecution, ActivityExecution execution, String deleteReason) {
  	
    List<ExecutionEntity> orderedExecutions = orderExecutionsRootToLeaf(processInstanceExecution);
    Collections.reverse(orderedExecutions);
    
    endAllHistoricActivities(processInstanceExecution.getId());
    
    for (ExecutionEntity executionToDelete : orderedExecutions) {
      
    	executionToDelete.setDeleteReason(deleteReason);
    	executionToDelete.setEnded(true);
    	executionToDelete.setActive(false);
    	executionToDelete.setDeleteRoot(true);
      
    	executionToDelete.remove();
    }
    
    Context.getCommandContext().getHistoryManager().recordProcessInstanceEnd(processInstanceExecution.getId(), deleteReason, execution.getActivity().getId());
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
  
  protected void endAllHistoricActivities(String processInstanceId) {
    
    if (!Context.getProcessEngineConfiguration().getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      return;
    }
    
    Map<String, HistoricActivityInstanceEntity> historicActivityInstancMap = new HashMap<String, HistoricActivityInstanceEntity>();
    
    List<HistoricActivityInstance> historicActivityInstances = new HistoricActivityInstanceQueryImpl(Context.getCommandContext())
      .processInstanceId(processInstanceId)
      .unfinished()
      .list();
    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
      historicActivityInstancMap.put(historicActivityInstance.getId(), (HistoricActivityInstanceEntity) historicActivityInstance);
    }
    
    // Cached version overwites entity
    List<HistoricActivityInstanceEntity> cachedHistoricActivityInstances = Context.getCommandContext().getDbSqlSession()
        .findInCache(HistoricActivityInstanceEntity.class);
    for (HistoricActivityInstanceEntity cachedHistoricActivityInstance : cachedHistoricActivityInstances) {
      if (processInstanceId.equals(cachedHistoricActivityInstance.getProcessInstanceId())
          && (cachedHistoricActivityInstance.getEndTime() == null)) {
        historicActivityInstancMap.put(cachedHistoricActivityInstance.getId(), cachedHistoricActivityInstance);
      }
    }

    for (HistoricActivityInstanceEntity historicActivityInstance : historicActivityInstancMap.values()) {
      historicActivityInstance.markEnded(null);
      
      // Fire event
      ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
      if (config != null && config.getEventDispatcher().isEnabled()) {
        config.getEventDispatcher().dispatchEvent(
            ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED, historicActivityInstance));
      }
    }
    
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