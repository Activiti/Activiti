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



/** Represents one task for a human user.
 * 
 * @author Joram Barrez
 */
public interface Task {
  
  int PRIORITY_NORMAL = 50; 
	
  /** DB id of the task. */
	String getId();
	
  /** Name or title of the task. */
	String getName();

  /** Name or title of the task. */
	void setName(String name);
	
  /** Free text description of the task. */
	String getDescription();
	
  /** Refers to a {@link User.getId() user} which is the owner or person responsible for completing this task. */
	void setDescription(String description);
	
	/** indication of how important/urgent this task is with a number between 
	 * 0 and 100 where higher values mean a higher priority and lower values mean 
	 * lower priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high 
	 * [80..100] highest */
	int getPriority();
	
  /** indication of how important/urgent this task is with a number between 
   * 0 and 100 where higher values mean a higher priority and lower values mean 
   * lower priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high 
   * [80..100] highest */
	void setPriority(int priority);
	
  /** Refers to a {@link User.getId() user} which is the owner or person responsible for completing this task. */
	String getAssignee();
	
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
}
