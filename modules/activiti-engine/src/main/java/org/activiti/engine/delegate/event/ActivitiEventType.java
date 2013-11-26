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
	ENGINE_CLOSED;
	
	
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
