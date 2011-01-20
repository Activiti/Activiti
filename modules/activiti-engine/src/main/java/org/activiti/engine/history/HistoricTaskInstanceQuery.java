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

package org.activiti.engine.history;

import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.query.Query;
import org.activiti.engine.task.Task;


/**
 * Allows programmatic querying for {@link HistoricTaskInstance}s.
 * 
 * @author Tom Baeyens
 */
public interface HistoricTaskInstanceQuery  extends Query<HistoricTaskInstanceQuery, HistoricTaskInstance> {

  /** Only select historic task instances for the given task id. */
  HistoricTaskInstanceQuery taskId(String taskId);
  
  /** Only select historic task instances for the given process instance. */
  HistoricTaskInstanceQuery processInstanceId(String processInstanceId);
  
  /** Only select historic task instances for the given execution. */
  HistoricTaskInstanceQuery executionId(String executionId);
  
  /** Only select historic task instances for the given process definition. */
  HistoricTaskInstanceQuery processDefinitionId(String processDefinitionId);
  
  /** 
   * Only select historic task instances with the given task name.
   * This is the last name given to the task. 
   */
  HistoricTaskInstanceQuery taskName(String taskName);
  
  /** 
   * Only select historic task instances with a task name like the given value.
   * This is the last name given to the task.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery taskNameLike(String taskNameLike);
  
  /** 
   * Only select historic task instances with the given task description.
   * This is the last description given to the task.  
   */
  HistoricTaskInstanceQuery taskDescription(String taskDescription);
  
  /** 
   * Only select historic task instances with a task description like the given value.
   * This is the last description given to the task.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery taskDescriptionLike(String taskDescriptionLike);
  
  /**
   * Only select historic task instances with the given task definition key.
   * @see Task#getTaskDefinitionKey()
   * @see TaskDefinition#getKey()
   */
  HistoricTaskInstanceQuery taskDefinitionKey(String taskDefinitionKey);
  
  /** Only select historic task instances with the given task delete reason. */
  HistoricTaskInstanceQuery taskDeleteReason(String taskDeleteReason);
  
  /** 
   * Only select historic task instances with a task description like the given value.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery taskDeleteReasonLike(String taskDeleteReasonLike);
  
  /** 
   * Only select historic task instances which were last assigned to the given assignee.
   */
  HistoricTaskInstanceQuery taskAssignee(String taskAssignee);
  
  /** 
   * Only select historic task instances which were last assigned to an assignee like
   * the given value.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery taskAssigneeLike(String taskAssigneeLike);
  
  /** 
   * Only select historic task instances which are finished.
   */
  HistoricTaskInstanceQuery finished();
  
  /** 
   * Only select historic task instances which aren't finished yet.
   */
  HistoricTaskInstanceQuery unfinished();
  
  /** 
   * Order by the historic activity instance id this task was used in
   * (needs to be followed by {@link #asc()} or {@link #desc()}). 
   */
  HistoricTaskInstanceQuery orderByHistoricActivityInstanceId();
  
  /** Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByProcessDefinitionId();
  
  /** Order by process instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByProcessInstanceId();

  /** Order by execution id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByExecutionId();
  
  /** Order by duration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByHistoricTaskInstanceDuration();
  
  /** Order by end time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByHistoricTaskInstanceEndTime();
  
  /** Order by start time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByHistoricActivityInstanceStartTime();
  
  /** Order by task name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskName();
  
  /** Order by task description (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskDescription();
  
  /** Order by task delete reason (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByDeleteReason();

  /** Order by task definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskDefinitionKey();
}
