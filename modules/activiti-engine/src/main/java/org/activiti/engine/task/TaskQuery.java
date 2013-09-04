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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.query.Query;

/**
 * Allows programmatic querying of {@link Task}s;
 * 
 * @author Joram Barrez
 * @author Falko Menge
 * @author Tijs Rademakers
 */
public interface TaskQuery extends Query<TaskQuery, Task>{

  /**
   * Only select tasks with the given task id (in practice, there will be
   * maximum one of this kind)
   */
  TaskQuery taskId(String taskId);

  /** Only select tasks with the given name */
  TaskQuery taskName(String name);
  
  /** Only select tasks with a name matching the parameter.
   *  The syntax is that of SQL: for example usage: nameLike(%activiti%)*/
  TaskQuery taskNameLike(String nameLike);
  
  /** Only select tasks with the given description. */
  TaskQuery taskDescription(String description);
  
  /** Only select tasks with a description matching the parameter .
   *  The syntax is that of SQL: for example usage: descriptionLike(%activiti%)*/
  TaskQuery taskDescriptionLike(String descriptionLike);
  
  /** Only select tasks with the given priority. */
  TaskQuery taskPriority(Integer priority);

  /** Only select tasks with the given priority or higher. */
  TaskQuery taskMinPriority(Integer minPriority);

  /** Only select tasks with the given priority or lower. */
  TaskQuery taskMaxPriority(Integer maxPriority);

  /** Only select tasks which are assigned to the given user. */
  TaskQuery taskAssignee(String assignee);
  
  /** 
   * Only select tasks which were last assigned to an assignee like
   * the given value.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  TaskQuery taskAssigneeLike(String assigneeLike);
  
  /** Only select tasks for which the given user is the owner. */
  TaskQuery taskOwner(String owner);
  
  /** 
   * Only select tasks which were last assigned to an owner like
   * the given value.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  TaskQuery taskOwnerLike(String ownerLike);
  
  /** Only select tasks which don't have an assignee. */
  TaskQuery taskUnassigned();
  
  /** @see {@link #taskUnassigned} */
  @Deprecated
  TaskQuery taskUnnassigned();

  /** Only select tasks with the given {@link DelegationState}. */
  TaskQuery taskDelegationState(DelegationState delegationState);

  /** Only select tasks for which the given user is a candidate. */
  TaskQuery taskCandidateUser(String candidateUser);
  
  /** Only select tasks for which there exist an {@link IdentityLink} with the given user, including tasks which have been 
   * assigned to the given user (assignee) or owned by the given user (owner). */
  TaskQuery taskInvolvedUser(String involvedUser);

  /** Only select tasks for which users in the given group are candidates. */
  TaskQuery taskCandidateGroup(String candidateGroup);
  
  /** 
   * Only select tasks for which the 'candidateGroup' is one of the given groups.
   * 
   * @throws ActivitiIllegalArgumentException 
   *   When query is executed and {@link #taskCandidateGroup(String)} or 
   *     {@link #taskCandidateUser(String)} has been executed on the query instance. 
   *   When passed group list is empty or <code>null</code>. 
   */
  TaskQuery taskCandidateGroupIn(List<String> candidateGroups);

  /** Only select tasks for the given process instance id. */
  TaskQuery processInstanceId(String processInstanceId);
  
  /** Only select tasks foe the given business key */
  TaskQuery processInstanceBusinessKey(String processInstanceBusinessKey);
  
  /** Only select tasks with a business key  like the given value
   * The syntax is that of SQL: for example usage: processInstanceBusinessKeyLike("%activiti%"). 
   */
  TaskQuery processInstanceBusinessKeyLike(String processInstanceBusinessKeyLike);

  /** Only select tasks for the given execution. */
  TaskQuery executionId(String executionId);
  
  /** Only select tasks that are created on the given date. **/
  TaskQuery taskCreatedOn(Date createTime);
  
  /** Only select tasks that are created before the given date. **/
  TaskQuery taskCreatedBefore(Date before);

  /** Only select tasks that are created after the given date. **/
  TaskQuery taskCreatedAfter(Date after);
  
  /** Only select tasks that have no parent (i.e. do not select subtasks). **/
  TaskQuery excludeSubtasks();

  /** 
   * Only select tasks with the given taskDefinitionKey.
   * The task definition key is the id of the userTask:
   * &lt;userTask id="xxx" .../&gt;
   **/
  TaskQuery taskDefinitionKey(String key);
  
  /** 
   * Only select tasks with a taskDefinitionKey that match the given parameter.
   *  The syntax is that of SQL: for example usage: taskDefinitionKeyLike("%activiti%").
   * The task definition key is the id of the userTask:
   * &lt;userTask id="xxx" .../&gt;
   **/
  TaskQuery taskDefinitionKeyLike(String keyLike);
  
  /**
   * Only select tasks which have a local task variable with the given name
   * set to the given value.
   */
  TaskQuery taskVariableValueEquals(String variableName, Object variableValue);
  
  /**
   * Only select tasks which have at least one local task variable with the given value.
   */
  TaskQuery taskVariableValueEquals(Object variableValue);
  
  /**
   * Only select tasks which have a local string variable with the given value, 
   * case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive
   * queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive Collations 
   * available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx">MSDN Server Collation Reference</a>).
   * </p>
   */
  TaskQuery taskVariableValueEqualsIgnoreCase(String name, String value);
  
  /** 
   * Only select tasks which have a local task variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   */
  TaskQuery taskVariableValueNotEquals(String variableName, Object variableValue);    
  
  /**
   * Only select tasks which have a local string variable with is not the given value, 
   * case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive
   * queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive Collations 
   * available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx">MSDN Server Collation Reference</a>).
   * </p>
   */
  TaskQuery taskVariableValueNotEqualsIgnoreCase(String name, String value);
  
  /** Only select tasks which have a local variable value greater than the
   * passed value when they ended. Booleans, Byte-arrays and
   * {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  TaskQuery taskVariableValueGreaterThan(String name, Object value);

  /** Only select tasks which have a local variable value greater than or
   * equal to the passed value when they ended. Booleans, Byte-arrays and
   * {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  TaskQuery taskVariableValueGreaterThanOrEqual(String name, Object value);

  /** Only select tasks which have a local variable value less than the
   * passed value when the ended.Booleans,
   * Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  TaskQuery taskVariableValueLessThan(String name, Object value);

  /** Only select tasks which have a local variable value less than or equal
   * to the passed value when they ended. Booleans,
   * Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  TaskQuery taskVariableValueLessThanOrEqual(String name, Object value);

  /** Only select tasks which have a local variable value like the given value
   * when they ended. This can be used on string variables only.
   * @param name cannot be null.
   * @param value cannot be null. The string can include the
   *          wildcard character '%' to express like-strategy: starts with
   *          (string%), ends with (%string) or contains (%string%). */
  TaskQuery taskVariableValueLike(String name, String value);
  
  /**
   * Only select tasks which are part of a process that has a variable
   * with the given name set to the given value.
   */
  TaskQuery processVariableValueEquals(String variableName, Object variableValue);
  
  /**
   * Only select tasks which are part of a process that has at least one variable
   * with the given value.
   */
  TaskQuery processVariableValueEquals(Object variableValue);
  
  /**
   * Only select tasks which are part of a process that has a local string variable which 
   * is not the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive
   * queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive Collations 
   * available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx">MSDN Server Collation Reference</a>).
   * </p>
   */
  TaskQuery processVariableValueEqualsIgnoreCase(String name, String value);
  
  /** 
   * Only select tasks which have a variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   */
  TaskQuery processVariableValueNotEquals(String variableName, Object variableValue); 
  
  /**
   * Only select tasks which are part of a process that has a string variable with 
   * the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive
   * queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive Collations 
   * available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx">MSDN Server Collation Reference</a>).
   * </p>
   */
  TaskQuery processVariableValueNotEqualsIgnoreCase(String name, String value);
  
  /** Only select tasks which have a global variable value greater than the
   * passed value when they ended. Booleans, Byte-arrays and
   * {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  TaskQuery processVariableValueGreaterThan(String name, Object value);

  /** Only select tasks which have a global variable value greater than or
   * equal to the passed value when they ended. Booleans, Byte-arrays and
   * {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  TaskQuery processVariableValueGreaterThanOrEqual(String name, Object value);

  /** Only select tasks which have a global variable value less than the
   * passed value when the ended.Booleans,
   * Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  TaskQuery processVariableValueLessThan(String name, Object value);

  /** Only select tasks which have a global variable value less than or equal
   * to the passed value when they ended. Booleans,
   * Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   * @param name cannot be null.
   * @param value cannot be null. */
  TaskQuery processVariableValueLessThanOrEqual(String name, Object value);

  /** Only select tasks which have a global variable value like the given value
   * when they ended. This can be used on string variables only.
   * @param name cannot be null.
   * @param value cannot be null. The string can include the
   *          wildcard character '%' to express like-strategy: starts with
   *          (string%), ends with (%string) or contains (%string%). */
  TaskQuery processVariableValueLike(String name, String value);
  
  /**
   * Only select tasks which are part of a process instance which has the given
   * process definition key.
   */
  TaskQuery processDefinitionKey(String processDefinitionKey);
  
  /**
   * Only select tasks which are part of a process instance which has a
   * process definition key like the given value.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  TaskQuery processDefinitionKeyLike(String processDefinitionKeyLike);
  
  /**
   * Only select tasks which are part of a process instance which has the given
   * process definition id.
   */
  TaskQuery processDefinitionId(String processDefinitionId);
  
  /**
   * Only select tasks which are part of a process instance which has the given
   * process definition name.
   */
  TaskQuery processDefinitionName(String processDefinitionName);
  
  /**
   * Only select tasks which are part of a process instance which has a
   * process definition name like the given value.
   * The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  TaskQuery processDefinitionNameLike(String processDefinitionNameLike);
  
  /**
   * Only select tasks with the given due date.
   */
  TaskQuery dueDate(Date dueDate);
  
  /**
   * Only select tasks which have a due date before the given date.
   */
  TaskQuery dueBefore(Date dueDate);

  /**
   * Only select tasks which have a due date after the given date.
   */
  TaskQuery dueAfter(Date dueDate);
  
  /**
   * Only select tasks with no due date.
   */
  TaskQuery withoutDueDate();
  
  /**
   * Only selects tasks which are suspended, because its process instance was suspended.
   */
  TaskQuery suspended();
  
  /**
   * Only selects tasks which are active (ie. not suspended)
   */
  TaskQuery active();
  
  /**
   * Include local task variables in the task query result
   */
  TaskQuery includeTaskLocalVariables();
  
  /**
   * Include global task variables in the task query result
   */
  TaskQuery includeProcessVariables();
  
  // ordering ////////////////////////////////////////////////////////////
  
  /** Order by task id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  TaskQuery orderByTaskId();
  
  /** Order by task name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  TaskQuery orderByTaskName();
  
  /** Order by description (needs to be followed by {@link #asc()} or {@link #desc()}). */
  TaskQuery orderByTaskDescription();
  
  /** Order by priority (needs to be followed by {@link #asc()} or {@link #desc()}). */
  TaskQuery orderByTaskPriority();
  
  /** Order by assignee (needs to be followed by {@link #asc()} or {@link #desc()}). */
  TaskQuery orderByTaskAssignee();
  
  /** Order by the time on which the tasks were created (needs to be followed by {@link #asc()} or {@link #desc()}). */
  TaskQuery orderByTaskCreateTime();
  
  /** Order by process instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  TaskQuery orderByProcessInstanceId();
  
  /** Order by execution id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  TaskQuery orderByExecutionId();
  
  /** Order by due date (needs to be followed by {@link #asc()} or {@link #desc()}). */
  TaskQuery orderByDueDate();
}
