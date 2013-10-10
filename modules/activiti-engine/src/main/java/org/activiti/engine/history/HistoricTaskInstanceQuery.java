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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
  
  /** Only select historic process instances with the given business key */
  HistoricTaskInstanceQuery processInstanceBusinessKey(String processInstanceBusinessKey);
  
  /** 
   * Only select historic process instances with a business key like the given value
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery processInstanceBusinessKeyLike(String processInstanceBusinessKeyLike);
  
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
   * which has a process definition key like the given value.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery processDefinitionKeyLike(String processDefinitionKeyLike);
  
  /**
   * Only select historic task instances which are part of a (historic) process instance 
   * which has the given definition name.
   */
  HistoricTaskInstanceQuery processDefinitionName(String processDefinitionName);
  
  /**
   * Only select historic task instances which are part of a (historic) process instance 
   * which has a definition name like the given value.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery processDefinitionNameLike(String processDefinitionNameLike);
  
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
  
  /** 
   * Only select historic task instances with a task definition key like the given value.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  HistoricTaskInstanceQuery taskDefinitionKeyLike(String taskDefinitionKeyLike);
  
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
  
  /** Only select historic tasks for which the given user is a candidate. */
  HistoricTaskInstanceQuery taskCandidateUser(String candidateUser);
  
  /** Only select historic task for which there exist an {@link HistoricIdentityLink} with the given user, including tasks
   *  which have been assigned to the given user (assignee) or owned by the given user (owner). */
  HistoricTaskInstanceQuery taskInvolvedUser(String involvedUser);
  
  /** Only select historic tasks for which users in the given group are candidates. */
  HistoricTaskInstanceQuery taskCandidateGroup(String candidateGroup);
  
  /**
   * Only select tasks for which the 'candidateGroup' is one of the given groups.
   * 
   * @throws ActivitiIllegalArgumentException 
   *   When query is executed and {@link #taskCandidateGroup(String)} or 
   *     {@link #taskCandidateUser(String)} has been executed on the query instance. 
   *   When passed group list is empty or <code>null</code>. 
   */
  HistoricTaskInstanceQuery taskCandidateGroupIn(List<String> candidateGroups);
  
  /** 
   * Only select historic task instances with the given priority.
   */
  HistoricTaskInstanceQuery taskPriority(Integer taskPriority);
  
  /** Only select historic tasks with the given priority or higher. */
  HistoricTaskInstanceQuery taskMinPriority(Integer minPriority);

  /** Only select historic tasks with the given priority or lower. */
  HistoricTaskInstanceQuery taskMaxPriority(Integer maxPriority);
  
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
  
  /** Only select subtasks of the given parent task */
  HistoricTaskInstanceQuery taskParentTaskId(String parentTaskId);

  /**
   * Only select select historic task instances with the given due date.
   */
  HistoricTaskInstanceQuery taskDueDate(Date dueDate);
  
  /**
   * Only select select historic task instances which have a due date before the given date.
   */
  HistoricTaskInstanceQuery taskDueBefore(Date dueDate);
  
  /**
   * Only select historic tasks instances with no due date.
   */
  HistoricTaskInstanceQuery withoutTaskDueDate();

  /**
   * Only select select historic task instances which have a due date after the given date.
   */
  HistoricTaskInstanceQuery taskDueAfter(Date dueDate);
  
  /**
   * Only select select historic task instances which are created on the given date
   */
  HistoricTaskInstanceQuery taskCreatedOn(Date startDate);
  
  /**
   * Only select select historic task instances which are created before the given date
   */
  HistoricTaskInstanceQuery taskCreatedBefore(Date startDate);
  
  /**
   * Only select select historic task instances which are created after the given date
   */
  HistoricTaskInstanceQuery taskCreatedAfter(Date startDate);
  
  /**
   * Only select select historic task instances which are completed on the given date
   */
  HistoricTaskInstanceQuery taskCompletedOn(Date endDate);
  
  /**
   * Only select select historic task instances which are completed before the given date
   */
  HistoricTaskInstanceQuery taskCompletedBefore(Date endDate);
  
  /**
   * Only select select historic task instances which are completed after the given date
   */
  HistoricTaskInstanceQuery taskCompletedAfter(Date endDate);
  
  /**
   * Only select tasks which have a local task variable with the given name
   * set to the given value.
   */
  HistoricTaskInstanceQuery taskVariableValueEquals(String variableName, Object variableValue);
  
  /**
   * Only select tasks which have at least one local task variable with the given value.
   */
  HistoricTaskInstanceQuery taskVariableValueEquals(Object variableValue);
  
  /**
   * Only select tasks which have a local string variable with the given value, 
   * case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive
   * queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive Collations 
   * available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx">MSDN Server Collation Reference</a>).
   * </p>
   */
  HistoricTaskInstanceQuery taskVariableValueEqualsIgnoreCase(String name, String value);
  
  /** 
   * Only select tasks which have a local task variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   */
  HistoricTaskInstanceQuery taskVariableValueNotEquals(String variableName, Object variableValue);    
  
  /**
   * Only select tasks which have a local string variable with is not the given value, 
   * case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive
   * queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive Collations 
   * available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx">MSDN Server Collation Reference</a>).
   * </p>
   */
  HistoricTaskInstanceQuery taskVariableValueNotEqualsIgnoreCase(String name, String value);
  
  /** Only select tasks which have a local variable value greater than the
   * passed value when they ended. Booleans, Byte-arrays and
   * {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricTaskInstanceQuery taskVariableValueGreaterThan(String name, Object value);

  /** Only select tasks which have a local variable value greater than or
   * equal to the passed value when they ended. Booleans, Byte-arrays and
   * {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricTaskInstanceQuery taskVariableValueGreaterThanOrEqual(String name, Object value);

  /** Only select tasks which have a local variable value less than the
   * passed value when the ended.Booleans,
   * Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricTaskInstanceQuery taskVariableValueLessThan(String name, Object value);

  /** Only select tasks which have a local variable value less than or equal
   * to the passed value when they ended. Booleans,
   * Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricTaskInstanceQuery taskVariableValueLessThanOrEqual(String name, Object value);

  /** Only select tasks which have a local variable value like the given value
   * when they ended. This can be used on string variables only.
   * @param name cannot be null.
   * @param value cannot be null. The string can include the
   *          wildcard character '%' to express like-strategy: starts with
   *          (string%), ends with (%string) or contains (%string%). */
  HistoricTaskInstanceQuery taskVariableValueLike(String name, String value);
  
  /**
   * Only select tasks which are part of a process that has a variable
   * with the given name set to the given value.
   */
  HistoricTaskInstanceQuery processVariableValueEquals(String variableName, Object variableValue);
  
  /**
   * Only select tasks which are part of a process that has at least one variable
   * with the given value.
   */
  HistoricTaskInstanceQuery processVariableValueEquals(Object variableValue);
  
  /**
   * Only select tasks which are part of a process that has a local string variable which 
   * is not the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive
   * queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive Collations 
   * available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx">MSDN Server Collation Reference</a>).
   * </p>
   */
  HistoricTaskInstanceQuery processVariableValueEqualsIgnoreCase(String name, String value);
  
  /** 
   * Only select tasks which have a variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   */
  HistoricTaskInstanceQuery processVariableValueNotEquals(String variableName, Object variableValue); 
  
  /**
   * Only select tasks which are part of a process that has a string variable with 
   * the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive
   * queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive Collations 
   * available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx">MSDN Server Collation Reference</a>).
   * </p>
   */
  HistoricTaskInstanceQuery processVariableValueNotEqualsIgnoreCase(String name, String value);
  
  /** Only select tasks which have a global variable value greater than the
   * passed value when they ended. Booleans, Byte-arrays and
   * {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricTaskInstanceQuery processVariableValueGreaterThan(String name, Object value);

  /** Only select tasks which have a global variable value greater than or
   * equal to the passed value when they ended. Booleans, Byte-arrays and
   * {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricTaskInstanceQuery processVariableValueGreaterThanOrEqual(String name, Object value);

  /** Only select tasks which have a global variable value less than the
   * passed value when the ended.Booleans,
   * Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricTaskInstanceQuery processVariableValueLessThan(String name, Object value);

  /** Only select tasks which have a global variable value less than or equal
   * to the passed value when they ended. Booleans,
   * Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  HistoricTaskInstanceQuery processVariableValueLessThanOrEqual(String name, Object value);

  /** Only select tasks which have a global variable value like the given value
   * when they ended. This can be used on string variables only.
   * @param name cannot be null.
   * @param value cannot be null. The string can include the
   *          wildcard character '%' to express like-strategy: starts with
   *          (string%), ends with (%string) or contains (%string%). */
  HistoricTaskInstanceQuery processVariableValueLike(String name, String value);
  
  /**
   * Include local task variables in the task query result
   */
  HistoricTaskInstanceQuery includeTaskLocalVariables();
  
  /**
   * Include global task variables in the task query result
   */
  HistoricTaskInstanceQuery includeProcessVariables();
  
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
  
  /** Order by start time (needs to be followed by {@link #asc()} or {@link #desc()}). 
   * @deprecated use {@link #orderByHistoricTaskInstanceStartTime()} */
  @Deprecated
  HistoricTaskInstanceQuery orderByHistoricActivityInstanceStartTime();
  
  /** Order by start time (needs to be followed by {@link #asc()} or {@link #desc()}).  */
  HistoricTaskInstanceQuery orderByHistoricTaskInstanceStartTime();
  
  /** Order by task name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskName();
  
  /** Order by task description (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskDescription();
  
  /** Order by task assignee (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskAssignee();
  
  /** Order by task owner (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskOwner();
  
  /** Order by task due date (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskDueDate();
  
  /** Order by task delete reason (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByDeleteReason();

  /** Order by task definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskDefinitionKey();
  
  /** Order by task priority key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricTaskInstanceQuery orderByTaskPriority();
}
