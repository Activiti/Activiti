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

import java.util.Date;

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
   * Only select historic task instances which are part of a (historic) process instance 
   * which has the given process definition key.
   */
  HistoricTaskInstanceQuery processDefinitionKey(String processDefinitionKey);
  
  /**
   * Only select historic task instances which are part of a (historic) process instance 
   * which has the given definition name.
   */
  HistoricTaskInstanceQuery processDefinitionName(String processDefinitionName);
  
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
   * Only select historic task instances which have the given owner.
   */
  HistoricTaskInstanceQuery taskOwner(String taskOwner);
  
  /** 
   * Only select historic task instances which have an owner like the one specified.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery taskOwnerLike(String taskOwnerLike);
  
  /** 
   * Only select historic task instances with the given priority.
   */
  HistoricTaskInstanceQuery taskPriority(Integer taskPriority);
  
  /** 
   * Only select historic task instances which are finished.
   */
  HistoricTaskInstanceQuery finished();
  
  /** 
   * Only select historic task instances which aren't finished yet.
   */
  HistoricTaskInstanceQuery unfinished();
  
  /**
   * Only select historic task instances which are part of a process
   * instance which is already finished. 
   */
  HistoricTaskInstanceQuery processFinished();
  
  /**
   * Only select historic task instances which are part of a process
   * instance which is not finished yet. 
   */
  HistoricTaskInstanceQuery processUnfinished();
  
  /**
   * Only select historic task instances which have a local task variable with the
   * given name set to the given value. The last variable value in the variable updates 
   * ({@link HistoricDetail}) will be used, so make sure history-level is configured
   * to full when this feature is used.
   */
  HistoricTaskInstanceQuery taskVariableValueEquals(String variableName, Object variableValue);
  
  /** Only select subtasks of the given parent task */
  HistoricTaskInstanceQuery taskParentTaskId(String parentTaskId);

  /**
   * Only select historic task instances which are part of a process instance which have a variable 
   * with the given name set to the given value. The last variable value in the variable updates 
   * ({@link HistoricDetail}) will be used, so make sure history-level is configured
   * to full when this feature is used.
   */
  HistoricTaskInstanceQuery processVariableValueEquals(String variableName, Object variableValue);
  
  /**
   * Only select select historic task instances with the given due date.
   */
  HistoricTaskInstanceQuery taskDueDate(Date dueDate);
  
  /**
   * Only select select historic task instances which have a due date before the given date.
   */
  HistoricTaskInstanceQuery taskDueBefore(Date dueDate);

  /**
   * Only select select historic task instances which have a due date after the given date.
   */
  HistoricTaskInstanceQuery taskDueAfter(Date dueDate);
  
  /** Order by task id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskId();
  
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
  
  /** Order by task assignee (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskAssignee();
  
  /** Order by task owner (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskOwner();
  
  /** Order by task delete reason (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByDeleteReason();

  /** Order by task definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskDefinitionKey();
  
  /** Order by task priority key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskPriority();
}
