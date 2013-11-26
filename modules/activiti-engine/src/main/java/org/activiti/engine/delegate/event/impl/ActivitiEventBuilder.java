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

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiExceptionEvent;
import org.activiti.engine.delegate.event.ActivityEntityEvent;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.context.ExecutionContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
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
	 * @return an {@link ActivityEntityEvent}. In case an {@link ExecutionContext} is active, the execution related
	 * event fields will be populated. If not, execution details will be reteived from the {@link Object} if possible.
	 */
	public static ActivityEntityEvent createEntityEvent(ActivitiEventType type, Object entity) {
		ActivitiEntityEventImpl newEvent = new ActivitiEntityEventImpl(entity, type);

		// In case an execution-context is active, populate the event fields related to the execution
		populateEventWithCurrentContext(newEvent);
		return newEvent;
	}
	
	/**
	 * @param type type of event
	 * @param entity the entity this event targets
	 * @return an {@link ActivityEntityEvent}
	 */
	public static ActivityEntityEvent createEntityEvent(ActivitiEventType type, Object entity, String executionId,
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
	 * @return an {@link ActivityEntityEvent} that is also instance of {@link ActivitiExceptionEvent}. 
	 * In case an {@link ExecutionContext} is active, the execution related event fields will be populated.
	 */
	public static ActivityEntityEvent createEntityExceptionEvent(ActivitiEventType type, Object entity, Throwable cause) {
		ActivitiEntityExceptionEventImpl newEvent = new ActivitiEntityExceptionEventImpl(entity, type, cause);

		// In case an execution-context is active, populate the event fields related to the execution
		populateEventWithCurrentContext(newEvent);
		return newEvent;
	}
	
	/**
	 * @param type type of event
	 * @param entity the entity this event targets
	 * @param cause the cause of the event
	 * @return an {@link ActivityEntityEvent} that is also instance of {@link ActivitiExceptionEvent}. 
	 */
	public static ActivityEntityEvent createEntityExceptionEvent(ActivitiEventType type, Object entity, Throwable cause, String executionId,
			String processInstanceId, String processDefinitionId) {
		ActivitiEntityExceptionEventImpl newEvent = new ActivitiEntityExceptionEventImpl(entity, type, cause);

		newEvent.setExecutionId(executionId);
		newEvent.setProcessInstanceId(processInstanceId);
		newEvent.setProcessDefinitionId(processDefinitionId);
		return newEvent;
	}
	
	protected static void populateEventWithCurrentContext(ActivitiEventImpl event) {
		if(Context.isExecutionContextActive()) {
			ExecutionContext executionContext = Context.getExecutionContext();
			if(executionContext != null) {
				event.setExecutionId(executionContext.getExecution().getId());
				event.setProcessInstanceId(executionContext.getExecution().getProcessInstanceId());
				event.setProcessDefinitionId(executionContext.getExecution().getProcessDefinitionId());
			}
		} else {
			// Fallback to fetching context from the object itself
			if(event instanceof ActivityEntityEvent) {
				Object persistendObject = ((ActivityEntityEvent) event).getEntity();
				if(persistendObject instanceof Job) {
					event.setExecutionId(((Job) persistendObject).getExecutionId());
					event.setProcessInstanceId(((Job) persistendObject).getProcessInstanceId());
					event.setProcessDefinitionId(((Job) persistendObject).getProcessDefinitionId());
				} else if(persistendObject instanceof ExecutionEntity) {
					event.setExecutionId(((ExecutionEntity) persistendObject).getId());
					event.setProcessInstanceId(((ExecutionEntity) persistendObject).getProcessInstanceId());
					event.setProcessDefinitionId(((ExecutionEntity) persistendObject).getProcessDefinitionId());
				}
			}
		}
	}
}
