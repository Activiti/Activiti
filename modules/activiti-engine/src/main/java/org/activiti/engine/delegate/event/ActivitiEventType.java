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
package org.activiti.engine.delegate.event;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration containing all possible types of {@link ActivitiEvent}s.
 * 
 * @author Frederik Heremans
 *
 */
public enum ActivitiEventType {

	/**
	 * New entity is created.
	 */
	ENTITY_CREATED,
	
	/**
	 * New entity has been created and all child-entities that are created as a result of the creation of this
	 * particular entity are also created and initialized.
	 */
	ENTITY_INITIALIZED,
	
	/**
	 * Existing entity us updated.
	 */
	ENTITY_UPDATED,
	
	/**
	 * Existing entity is deleted.
	 */
	ENTITY_DELETED,
	
	/**
	 * Existing entity has been suspended.							
	 */
	ENTITY_SUSPENDED,
	
	/**
	 * Existing entity has been activated.							
	 */
	ENTITY_ACTIVATED,
	
	/**
	 * Timer has been fired successfully.
	 */
	TIMER_FIRED,
	
	/**
	 * A job has been successfully executed.
	 */
	JOB_EXECUTION_SUCCESS,
	
	/**
	 * A job has been executed, but failed. Event should be an instance of a {@link ActivitiExceptionEvent}.
	 */
	JOB_EXECUTION_FAILURE,
	
	/**
	 * The retry-count on a job has been decremented.
	 */
	JOB_RETRIES_DECREMENTED,
	
	/**
	 * An event type to be used by custom events. These types of events are never thrown by the engine itself,
	 * only be an external API call to dispatch an event.
	 */
	CUSTOM,
	
	/**
	 * The process-engine that dispatched this event has been created and is ready for use.
	 */
	ENGINE_CREATED,
	
	/**
	 * The process-engine that dispatched this event has been closed and cannot be used anymore.
	 */
	ENGINE_CLOSED,
	
	/**
	 * An activity is starting to execute. This event is dispatch right before an activity is executed.
	 */
	ACTIVITY_STARTED,
	
	/**
	 * An activity has been completed successfully.
	 */
	ACTIVITY_COMPLETED,
	
	/**
	 * An activity has received a signal. Dispatched after the activity has responded to the signal.
	 */
	ACTIVITY_SIGNALED,
	
	/**
	 * An activity is about to be executed as a compensation for another activity. The event targets the
	 * activity that is about to be executed for compensation.
	 */
	ACTIVITY_COMPENSATE,
	
	/**
	 * An activity has received a message event. Dispatched before the actual message has been received by
	 * the activity. This event will be either followed by a {@link #ACTIVITY_SIGNALLED} event or {@link #ACTIVITY_COMPLETE}
	 * for the involved activity, if the message was delivered successfully.
	 */
	ACTIVITY_MESSAGE_RECEIVED,
	
	/**
	 * An activity has received an error event. Dispatched before the actual error has been received by
	 * the activity. This event will be either followed by a {@link #ACTIVITY_SIGNALLED} event or {@link #ACTIVITY_COMPLETE}
	 * for the involved activity, if the error was delivered successfully.
	 */
	ACTIVITY_ERROR_RECEIVED,
	
	/**
	 * When a BPMN Error was thrown, but was not caught within in the process.
	 */
	UNCAUGHT_BPMN_ERROR,
	
	/**
	 * A new variable has been created.
	 */
	VARIABLE_CREATED,
	
	/**
	 * An existing variable has been updated.
	 */
	VARIABLE_UPDATED,
	
	/**
	 * An existing variable has been deleted.
	 */
	VARIABLE_DELETED,

  /**
   * A task has been created. This is thrown when task is fully initialized (before TaskListener.EVENTNAME_CREATE).
   */
  TASK_CREATED,

  /**
	 * A task as been assigned. This is thrown alongside with an {@link #ENTITY_UPDATED} event.
	 */
	TASK_ASSIGNED,
	
	/**
	 * A task has been completed. Dispatched before the task entity is deleted ({@link #ENTITY_DELETED}).
	 * If the task is part of a process, this event is dispatched before the process moves on, as a result of
	 * the task completion. In that case, a {@link #ACTIVITY_COMPLETED} will be dispatched after an event of this type
	 * for the activity corresponding to the task. 
	 */
	TASK_COMPLETED,
	
	/**
	 * A new membership has been created.
	 */
	MEMBERSHIP_CREATED,
	
	/**
	 * A single membership has been deleted.
	 */
	MEMBERSHIP_DELETED,
	
	/**
	 * All memberships in the related group have been deleted. No individual {@link #MEMBERSHIP_DELETED} events will
	 * be dispatched due to possible performance reasons. The event is dispatched before the memberships are deleted,
	 * so they can still be accessed in the dispatch method of the listener.
	 */
	MEMBERSHIPS_DELETED;
	
	public static final ActivitiEventType[] EMPTY_ARRAY =  new ActivitiEventType[] {};
	
	/**
	 * @param string the string containing a comma-separated list of event-type names
	 * @return a list of {@link ActivitiEventType} based on the given list.
	 * @throws ActivitiIllegalArgumentException when one of the given string is not a valid type name
	 */
	public static ActivitiEventType[] getTypesFromString(String string) {
		List<ActivitiEventType> result = new ArrayList<ActivitiEventType>();
		if(string != null && !string.isEmpty()) {
			String[] split = StringUtils.split(string, ",");
			for(String typeName : split) {
				boolean found = false;
				for(ActivitiEventType type : values()) {
					if(typeName.equals(type.name())) {
						result.add(type);
						found = true;
						break;
					}
				}
				if(!found) {
					throw new ActivitiIllegalArgumentException("Invalid event-type: " + typeName);
				}
			}
		}
		
		return result.toArray(EMPTY_ARRAY);
	}
}
