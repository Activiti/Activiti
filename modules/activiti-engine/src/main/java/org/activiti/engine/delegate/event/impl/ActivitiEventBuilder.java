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
package org.activiti.engine.delegate.event.impl;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiExceptionEvent;
import org.activiti.engine.delegate.event.ActivitiMessageEvent;
import org.activiti.engine.delegate.event.ActivitiSignalEvent;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.context.ExecutionContext;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.runtime.Job;

/**
 * Builder class used to create {@link ActivitiEvent} implementations.
 * 
 * @author Frederik Heremans
 */
public class ActivitiEventBuilder {

	/**
	 * @param type type of event
	 * @return an {@link ActivitiEvent} that doesn't have it's execution context-fields filled,
	 * as the event is a global event, independant of any running execution.
	 */
	public static ActivitiEvent createGlobalEvent(ActivitiEventType type) {
		ActivitiEventImpl newEvent = new ActivitiEventImpl(type);
		return newEvent;
	}
	
	/**
	 * @param type type of event
	 * @param entity the entity this event targets
	 * @return an {@link ActivitiEntityEvent}. In case an {@link ExecutionContext} is active, the execution related
	 * event fields will be populated. If not, execution details will be reteived from the {@link Object} if possible.
	 */
	public static ActivitiEntityEvent createEntityEvent(ActivitiEventType type, Object entity) {
		ActivitiEntityEventImpl newEvent = new ActivitiEntityEventImpl(entity, type);

		// In case an execution-context is active, populate the event fields related to the execution
		populateEventWithCurrentContext(newEvent);
		return newEvent;
	}
	
	/**
	 * @param type type of event
	 * @param entity the entity this event targets
	 * @return an {@link ActivitiEntityEvent}
	 */
	public static ActivitiEntityEvent createEntityEvent(ActivitiEventType type, Object entity, String executionId,
			String processInstanceId, String processDefinitionId) {
		ActivitiEntityEventImpl newEvent = new ActivitiEntityEventImpl(entity, type);

		newEvent.setExecutionId(executionId);
		newEvent.setProcessInstanceId(processInstanceId);
		newEvent.setProcessDefinitionId(processDefinitionId);
		return newEvent;
	}
	
	/**
	 * @param type type of event
	 * @param entity the entity this event targets
	 * @param cause the cause of the event
	 * @return an {@link ActivitiEntityEvent} that is also instance of {@link ActivitiExceptionEvent}. 
	 * In case an {@link ExecutionContext} is active, the execution related event fields will be populated.
	 */
	public static ActivitiEntityEvent createEntityExceptionEvent(ActivitiEventType type, Object entity, Throwable cause) {
		ActivitiEntityExceptionEventImpl newEvent = new ActivitiEntityExceptionEventImpl(entity, type, cause);

		// In case an execution-context is active, populate the event fields related to the execution
		populateEventWithCurrentContext(newEvent);
		return newEvent;
	}
	
	/**
	 * @param type type of event
	 * @param entity the entity this event targets
	 * @param cause the cause of the event
	 * @return an {@link ActivitiEntityEvent} that is also instance of {@link ActivitiExceptionEvent}. 
	 */
	public static ActivitiEntityEvent createEntityExceptionEvent(ActivitiEventType type, Object entity, Throwable cause, String executionId,
			String processInstanceId, String processDefinitionId) {
		ActivitiEntityExceptionEventImpl newEvent = new ActivitiEntityExceptionEventImpl(entity, type, cause);

		newEvent.setExecutionId(executionId);
		newEvent.setProcessInstanceId(processInstanceId);
		newEvent.setProcessDefinitionId(processDefinitionId);
		return newEvent;
	}
	
	public static ActivitiActivityEvent createActivityEvent(ActivitiEventType type, String activityId, String executionId, String processInstanceId, String processDefinitionId) {
		ActivitiActivityEventImpl newEvent = new ActivitiActivityEventImpl(type);
		newEvent.setActivityId(activityId);
		newEvent.setExecutionId(executionId);
		newEvent.setProcessDefinitionId(processDefinitionId);
		newEvent.setProcessInstanceId(processInstanceId);
		return newEvent;
	}
	
	public static ActivitiSignalEvent createSignalEvent(ActivitiEventType type, String activityId, String signalName, Object signalData, 
			String executionId, String processInstanceId, String processDefinitionId) {
		ActivitiSignalEventImpl newEvent = new ActivitiSignalEventImpl(type);
		newEvent.setActivityId(activityId);
		newEvent.setExecutionId(executionId);
		newEvent.setProcessDefinitionId(processDefinitionId);
		newEvent.setProcessInstanceId(processInstanceId);
		newEvent.setSignalName(signalName);
		newEvent.setSignalData(signalData);
		return newEvent;
	}
	
	public static ActivitiMessageEvent createMessageEvent(ActivitiEventType type, String activityId, String messageName, Object payload, 
			String executionId, String processInstanceId, String processDefinitionId) {
		ActivitiMessageEventImpl newEvent = new ActivitiMessageEventImpl(type);
		newEvent.setActivityId(activityId);
		newEvent.setExecutionId(executionId);
		newEvent.setProcessDefinitionId(processDefinitionId);
		newEvent.setProcessInstanceId(processInstanceId);
		newEvent.setMessageName(messageName);
		newEvent.setMessageData(payload);
		return newEvent;
	}
	
	protected static void populateEventWithCurrentContext(ActivitiEventImpl event) {
		boolean extractedFromContext = false;
		if(Context.isExecutionContextActive()) {
			ExecutionContext executionContext = Context.getExecutionContext();
			if(executionContext != null) {
				extractedFromContext = true;
				event.setExecutionId(executionContext.getExecution().getId());
				event.setProcessInstanceId(executionContext.getExecution().getProcessInstanceId());
				event.setProcessDefinitionId(executionContext.getExecution().getProcessDefinitionId());
			}
		} 
		
		// Fallback to fetching context from the object itself
		if(!extractedFromContext){
			if(event instanceof ActivitiEntityEvent) {
				Object persistendObject = ((ActivitiEntityEvent) event).getEntity();
				if(persistendObject instanceof Job) {
					event.setExecutionId(((Job) persistendObject).getExecutionId());
					event.setProcessInstanceId(((Job) persistendObject).getProcessInstanceId());
					event.setProcessDefinitionId(((Job) persistendObject).getProcessDefinitionId());
				} else if(persistendObject instanceof DelegateExecution) {
					event.setExecutionId(((DelegateExecution) persistendObject).getId());
					event.setProcessInstanceId(((DelegateExecution) persistendObject).getProcessInstanceId());
					event.setProcessDefinitionId(((DelegateExecution) persistendObject).getProcessDefinitionId());
				} else if(persistendObject instanceof IdentityLinkEntity) {
					IdentityLinkEntity idLink = (IdentityLinkEntity) persistendObject;
					if(idLink.getProcessDefinitionId() != null) {
						event.setProcessDefinitionId(idLink.getProcessDefId());
					} else if(idLink.getProcessInstance() != null) {
						event.setProcessDefinitionId(idLink.getProcessInstance().getProcessDefinitionId());
						event.setProcessInstanceId(idLink.getProcessInstanceId());
						event.setExecutionId(idLink.getProcessInstanceId());
					} else if(idLink.getTask() != null) {
						event.setProcessDefinitionId(idLink.getTask().getProcessDefinitionId());
						event.setProcessInstanceId(idLink.getTask().getProcessInstanceId());
						event.setExecutionId(idLink.getTask().getExecutionId());
					}
				}
			}
		}
	}
}
