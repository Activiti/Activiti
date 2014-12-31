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
package org.activiti.engine.impl.cmd;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.DelegationState;

/**
 * @author Joram Barrez
 */
public abstract class AbstractCompleteTaskCmd extends NeedsActiveTaskCmd<Void>{

	public AbstractCompleteTaskCmd(String taskId) {
	    super(taskId);
    }
	
	protected void executeTaskComplete(TaskEntity taskEntity, Map<String, Object> variables, boolean localScope) {
		  	
		if (taskEntity.getDelegationState() != null && taskEntity.getDelegationState().equals(DelegationState.PENDING)) {
			throw new ActivitiException("A delegated task cannot be completed, but should be resolved instead.");
		}
		  	
//		    fireEvent(TaskListener.EVENTNAME_COMPLETE);
//		    if (Authentication.getAuthenticatedUserId() != null && task.getProcessInstanceId() != null) {
//		      getProcessInstance().involveUser(Authentication.getAuthenticatedUserId(), IdentityLinkType.PARTICIPANT);
//		    }
		    
		ActivitiEventDispatcher eventDispatcher = Context.getProcessEngineConfiguration().getEventDispatcher();
	    if(eventDispatcher.isEnabled()) {
	    	if (variables != null) {
	    		eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityWithVariablesEvent(ActivitiEventType.TASK_COMPLETED, this, variables, localScope));
	    	} else {
	    		eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_COMPLETED, taskEntity));
	    	}
	    }
		    
		CommandContext commandContext = Context.getCommandContext();
		commandContext.getTaskEntityManager().deleteTask(taskEntity, TaskEntity.DELETE_REASON_COMPLETED, false);
		    
		// Continue process (if not a standalone task)
		if (taskEntity.getExecutionId()!=null) {
			ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findExecutionById(taskEntity.getExecutionId());
		    commandContext.getAgenda().planTriggerExecutionOperation(executionEntity);
		}
	}

}
