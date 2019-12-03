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
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration containing all possible types of {@link ActivitiEvent}s.
 * 

 * 
 */
public enum ActivitiEventType {

  /**
   * New entity is created.
   */
  ENTITY_CREATED,

  /**
   * New entity has been created and all child-entities that are created as a result of the creation of this particular entity are also created and initialized.
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
   * A Timer has been scheduled.
   */
  TIMER_SCHEDULED,

  /**
   * Timer has been fired successfully.
   */
  TIMER_FIRED,

  /**
   * Timer has been cancelled (e.g. user task on which it was bounded has been completed earlier than expected)
   */
  JOB_CANCELED,

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
   * An event type to be used by custom events. These types of events are never thrown by the engine itself, only be an external API call to dispatch an event.
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
   * An activity has been cancelled because of boundary event.
   */
  ACTIVITY_CANCELLED,

  /**
   * An activity has received a signal. Dispatched after the activity has responded to the signal.
   */
  ACTIVITY_SIGNALED,

  /**
   * An activity is about to be executed as a compensation for another activity. The event targets the activity that is about to be executed for compensation.
   */
  ACTIVITY_COMPENSATE,
  /**
   * A message has been sent via message intermediate throw or message end event
   */
  ACTIVITY_MESSAGE_SENT,
  /**
   * A boundary, intermediate, or subprocess start message catching event has started and it is waiting for message.
   */
  ACTIVITY_MESSAGE_WAITING,

  /**
   * An activity has received a message event. Dispatched before the actual message has been received by the activity. This event will be either followed by a {@link #ACTIVITY_SIGNALLED} event or
   * {@link #ACTIVITY_COMPLETE} for the involved activity, if the message was delivered successfully.
   */
  ACTIVITY_MESSAGE_RECEIVED,

  /**
   * An activity has received an error event. Dispatched before the actual error has been received by the activity. This event will be either followed by a {@link #ACTIVITY_SIGNALLED} event or
   * {@link #ACTIVITY_COMPLETE} for the involved activity, if the error was delivered successfully.
   */
  ACTIVITY_ERROR_RECEIVED,
  
  /**
   * A event dispatched when a {@link HistoricActivityInstance} is created. 
   * This is a specialized version of the {@link ActivitiEventType#ENTITY_CREATED} and {@link ActivitiEventType#ENTITY_INITIALIZED} event,
   * with the same use case as the {@link ActivitiEventType#ACTIVITY_STARTED}, but containing
   * slightly different data.
   * 
   * Note this will be an {@link ActivitiEntityEvent}, where the entity is the {@link HistoricActivityInstance}.
   *  
   * Note that history (minimum level ACTIVITY) must be enabled to receive this event.  
   */
  HISTORIC_ACTIVITY_INSTANCE_CREATED,
  
  /**
   * A event dispatched when a {@link HistoricActivityInstance} is marked as ended. 
   * his is a specialized version of the {@link ActivitiEventType#ENTITY_UPDATED} event,
   * with the same use case as the {@link ActivitiEventType#ACTIVITY_COMPLETED}, but containing
   * slightly different data (e.g. the end time, the duration, etc.). 
   *  
   * Note that history (minimum level ACTIVITY) must be enabled to receive this event.  
   */
  HISTORIC_ACTIVITY_INSTANCE_ENDED,

  /**
   * Indicates the engine has taken (ie. followed) a sequenceflow from a source activity to a target activity.
   */
  SEQUENCEFLOW_TAKEN,

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
   * A task has been completed. Dispatched before the task entity is deleted ( {@link #ENTITY_DELETED}). If the task is part of a process, this event is dispatched before the process moves on, as a
   * result of the task completion. In that case, a {@link #ACTIVITY_COMPLETED} will be dispatched after an event of this type for the activity corresponding to the task.
   */
  TASK_COMPLETED,

    /**
     * A process instance has been started. Dispatched when starting a process instance previously created. The event
     * PROCESS_STARTED is dispatched after the associated event ENTITY_INITIALIZED.
     */
  PROCESS_STARTED,

  /**
   * A process has been completed. Dispatched after the last activity is ACTIVITY_COMPLETED. Process is completed when it reaches state in which process instance does not have any transition to take.
   */
  PROCESS_COMPLETED,
  
  /**
   * A process has been completed with an error end event.
   */
  PROCESS_COMPLETED_WITH_ERROR_END_EVENT,

  /**
   * A process has been cancelled. Dispatched when process instance is deleted by
   * 
   * @see org.activiti.engine.impl.RuntimeServiceImpl#deleteProcessInstance(java.lang.String, java.lang.String), before DB delete.
   */
  PROCESS_CANCELLED,
  
  /**
   * A event dispatched when a {@link HistoricProcessInstance} is created. 
   * This is a specialized version of the {@link ActivitiEventType#ENTITY_CREATED} and {@link ActivitiEventType#ENTITY_INITIALIZED} event,
   * with the same use case as the {@link ActivitiEventType#PROCESS_STARTED}, but containing
   * slightly different data (e.g. the start time, the start user id, etc.). 
   * 
   * Note this will be an {@link ActivitiEntityEvent}, where the entity is the {@link HistoricProcessInstance}.
   *  
   * Note that history (minimum level ACTIVITY) must be enabled to receive this event.  
   */
  HISTORIC_PROCESS_INSTANCE_CREATED,
  
  /**
   * A event dispatched when a {@link HistoricProcessInstance} is marked as ended. 
   * his is a specialized version of the {@link ActivitiEventType#ENTITY_UPDATED} event,
   * with the same use case as the {@link ActivitiEventType#PROCESS_COMPLETED}, but containing
   * slightly different data (e.g. the end time, the duration, etc.). 
   *  
   * Note that history (minimum level ACTIVITY) must be enabled to receive this event.  
   */
  HISTORIC_PROCESS_INSTANCE_ENDED,

  /**
   * A new membership has been created.
   */
  MEMBERSHIP_CREATED,

  /**
   * A single membership has been deleted.
   */
  MEMBERSHIP_DELETED,

  /**
   * All memberships in the related group have been deleted. No individual {@link #MEMBERSHIP_DELETED} events will be dispatched due to possible performance reasons. The event is dispatched before the
   * memberships are deleted, so they can still be accessed in the dispatch method of the listener.
   */
  MEMBERSHIPS_DELETED;

  public static final ActivitiEventType[] EMPTY_ARRAY = new ActivitiEventType[] {};

  /**
   * @param string
   *          the string containing a comma-separated list of event-type names
   * @return a list of {@link ActivitiEventType} based on the given list.
   * @throws ActivitiIllegalArgumentException
   *           when one of the given string is not a valid type name
   */
  public static ActivitiEventType[] getTypesFromString(String string) {
    List<ActivitiEventType> result = new ArrayList<ActivitiEventType>();
    if (string != null && !string.isEmpty()) {
      String[] split = StringUtils.split(string, ",");
      for (String typeName : split) {
        boolean found = false;
        for (ActivitiEventType type : values()) {
          if (typeName.equals(type.name())) {
            result.add(type);
            found = true;
            break;
          }
        }
        if (!found) {
          throw new ActivitiIllegalArgumentException("Invalid event-type: " + typeName);
        }
      }
    }

    return result.toArray(EMPTY_ARRAY);
  }
}
