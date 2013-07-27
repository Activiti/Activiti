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
package org.activiti.engine.task;

import java.util.Date;
import java.util.Map;



/** Represents one task for a human user.
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public interface Task {

  /**
   * Default value used for priority when a new {@link Task} is created.
   */
  int DEFAULT_PRIORITY = 50;
  
  /** DB id of the task. */
	String getId();
	
  /** Name or title of the task. */
	String getName();

  /** Name or title of the task. */
	void setName(String name);
	
  /** Free text description of the task. */
	String getDescription();
	
  /** Change the description of the task */
	void setDescription(String description);
	
	/** Indication of how important/urgent this task is */
	int getPriority();
	
	/** Sets the indication of how important/urgent this task is */
	void setPriority(int priority);
	
  /** The {@link User.getId() userId} of the person that is responsible for this task. */
  String getOwner();
  
  /** The {@link User.getId() userId} of the person that is responsible for this task. */
  void setOwner(String owner);
  
  /** The {@link User.getId() userId} of the person to which this task is delegated. */
	String getAssignee();
	
	/** The {@link User.getId() userId} of the person to which this task is delegated. */
	void setAssignee(String assignee);
	
	/** The current {@link DelegationState} for this task. */ 
  DelegationState getDelegationState();
  
  /** The current {@link DelegationState} for this task. */ 
  void setDelegationState(DelegationState delegationState);
	
  /** Reference to the process instance or null if it is not related to a process instance. */
	String getProcessInstanceId();
	
  /** Reference to the path of execution or null if it is not related to a process instance. */
	String getExecutionId();
	
  /** Reference to the process definition or null if it is not related to a process. */
	String getProcessDefinitionId();

	/** The date/time when this task was created */
	Date getCreateTime();
	
	/** The id of the activity in the process defining this task or null if this is not related to a process */
	String getTaskDefinitionKey();
	
	/** Due date of the task. */
	Date getDueDate();
	
	/** Change due date of the task. */
	void setDueDate(Date dueDate);

	/** delegates this task to the given user and sets the {@link #getDelegationState() delegationState} to {@link DelegationState#PENDING}.
	 * If no owner is set on the task, the owner is set to the current assignee of the task. */
  void delegate(String userId);
  
  /** the parent task for which this task is a subtask */
  void setParentTaskId(String parentTaskId);

  /** the parent task for which this task is a subtask */
  String getParentTaskId();
  
  /** Indicated whether this task is suspended or not. */
  boolean isSuspended();
  
  /** Returns the local task variables if requested in the task query */
  Map<String, Object> getTaskLocalVariables();
  
  /** Returns the process variables if requested in the task query */
  Map<String, Object> getProcessVariables();
}
