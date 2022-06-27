/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.query.Query;

/**
 * Interface containing shared methods between the {@link TaskQuery} and the {@link HistoricTaskInstanceQuery}.
 *
 */
@Internal
public interface TaskInfoQuery<T extends TaskInfoQuery<?, ?>, V extends TaskInfo> extends Query<T, V> {

  /**
   * Only select tasks with the given task id (in practice, there will be maximum one of this kind)
   */
  T taskId(String taskId);

  /** Only select tasks with the given name */
  T taskName(String name);

  /**
   * Only select tasks with a name that is in the given list
   *
   * @throws ActivitiIllegalArgumentException
   *           When passed name list is empty or <code>null</code> or contains <code>null String</code>.
   */
  T taskNameIn(List<String> nameList);

  /**
   * Only select tasks with a name that is in the given list
   *
   * This method, unlike the {@link #taskNameIn(List)} method will not take in account the upper/lower case: both the input parameters as the column value are lowercased when the query is executed.
   *
   * @throws ActivitiIllegalArgumentException
   *           When passed name list is empty or <code>null</code> or contains <code>null String</code>.
   */
  T taskNameInIgnoreCase(List<String> nameList);

  /**
   * Only select tasks with a name matching the parameter. The syntax is that of SQL: for example usage: nameLike(%activiti%)
   */
  T taskNameLike(String nameLike);

  /**
   * Only select tasks with a name matching the parameter. The syntax is that of SQL: for example usage: nameLike(%activiti%)
   *
   * This method, unlike the {@link #taskNameLike(String)} method will not take in account the upper/lower case: both the input parameter as the column value are lowercased when the query is executed.
   */
  T taskNameLikeIgnoreCase(String nameLike);

  /** Only select tasks with the given description. */
  T taskDescription(String description);

  /**
   * Only select tasks with a description matching the parameter . The syntax is that of SQL: for example usage: descriptionLike(%activiti%)
   */
  T taskDescriptionLike(String descriptionLike);

  /**
   * Only select tasks with a description matching the parameter . The syntax is that of SQL: for example usage: descriptionLike(%activiti%)
   *
   * This method, unlike the {@link #taskDescriptionLike(String)} method will not take in account the upper/lower case: both the input parameter as the column value are lowercased when the query is
   * executed.
   */
  T taskDescriptionLikeIgnoreCase(String descriptionLike);

  /** Only select tasks with the given priority. */
  T taskPriority(Integer priority);

  /** Only select tasks with the given priority or higher. */
  T taskMinPriority(Integer minPriority);

  /** Only select tasks with the given priority or lower. */
  T taskMaxPriority(Integer maxPriority);

  /** Only select tasks which are assigned to the given user. */
  T taskAssignee(String assignee);

  /**
   * Only select tasks which were last assigned to an assignee like the given value. The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  T taskAssigneeLike(String assigneeLike);

  /**
   * Only select tasks which were last assigned to an assignee like the given value. The syntax that should be used is the same as in SQL, eg. %activiti%.
   *
   * This method, unlike the {@link #taskAssigneeLike(String)} method will not take in account the upper/lower case: both the input parameter as the column value are lowercased when the query is
   * executed.
   */
  T taskAssigneeLikeIgnoreCase(String assigneeLikeIgnoreCase);

  /**
   *Only select tasks with an assignee that is in the given list
   *
   * @throws ActivitiIllegalArgumentException
   *           When passed name list is empty or <code>null</code> or contains <code>null String</code>.
   */
  T taskAssigneeIds(List<String> assigneeListIds);

  /** Only select tasks for which the given user is the owner. */
  T taskOwner(String owner);

  /**
   * Only select tasks which were last assigned to an owner like the given value. The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  T taskOwnerLike(String ownerLike);

  /**
   * Only select tasks which were last assigned to an owner like the given value. The syntax that should be used is the same as in SQL, eg. %activiti%.
   *
   * This method, unlike the {@link #taskOwnerLike(String)} method will not take in account the upper/lower case: both the input parameter as the column value are lowercased when the query is
   * executed.
   */
  T taskOwnerLikeIgnoreCase(String ownerLikeIgnoreCase);


  /** Only select tasks for which the given user is a candidate. If identity service is available then also through user's groups. */
  T taskCandidateUser(String candidateUser);

  /** Only select tasks for which the given user is a candidate. */
  T taskCandidateUser(String candidateUser, List<String> usersGroups);

  /**
   * Only select tasks for which there exist an {@link IdentityLink} with the given user, including tasks which have been assigned to the given user (assignee) or owned by the given user (owner).
   */
  T taskInvolvedUser(String involvedUser);

  /** Only select tasks for users involved in the given groups */
  T taskInvolvedGroupsIn(List<String> involvedGroups);

  /** Only select tasks for which users in the given group are candidates. */
  T taskCandidateGroup(String candidateGroup);

  /**
   * Only select tasks for which the 'candidateGroup' is one of the given groups.
   *
   * @throws ActivitiIllegalArgumentException
   *           When query is executed and {@link #taskCandidateGroup(String)} or {@link #taskCandidateUser(String)} has been executed on the query instance. When passed group list is empty or
   *           <code>null</code>.
   */
  T taskCandidateGroupIn(List<String> candidateGroups);

  /**
   * Only select tasks that have the given tenant id.
   */
  T taskTenantId(String tenantId);

  /**
   * Only select tasks with a tenant id like the given one.
   */
  T taskTenantIdLike(String tenantIdLike);

  /**
   * Only select tasks that do not have a tenant id.
   */
  T taskWithoutTenantId();

  /**
   * Only select tasks for the given process instance id.
   */
  T processInstanceId(String processInstanceId);

  /**
   * Only select tasks for the given process ids.
   */
  T processInstanceIdIn(List<String> processInstanceIds);

  /** Only select tasks foe the given business key */
  T processInstanceBusinessKey(String processInstanceBusinessKey);

  /**
   * Only select tasks with a business key like the given value The syntax is that of SQL: for example usage: processInstanceBusinessKeyLike("%activiti%").
   */
  T processInstanceBusinessKeyLike(String processInstanceBusinessKeyLike);

  /**
   * Only select tasks with a business key like the given value The syntax is that of SQL: for example usage: processInstanceBusinessKeyLike("%activiti%").
   *
   * This method, unlike the {@link #processInstanceBusinessKeyLike(String)} method will not take in account the upper/lower case: both the input parameter as the column value are lowercased when the
   * query is executed.
   */
  T processInstanceBusinessKeyLikeIgnoreCase(String processInstanceBusinessKeyLikeIgnoreCase);

  /**
   * Only select tasks for the given execution.
   */
  T executionId(String executionId);

  /**
   * Only select tasks that are created on the given date.
   */
  T taskCreatedOn(Date createTime);

  /**
   * Only select tasks that are created before the given date.
   */
  T taskCreatedBefore(Date before);

  /**
   * Only select tasks that are created after the given date.
   */
  T taskCreatedAfter(Date after);

  /**
   * Only select tasks with the given category.
   */
  T taskCategory(String category);

  /**
   * Only select tasks with the given taskDefinitionKey. The task definition key is the id of the userTask: &lt;userTask id="xxx" .../&gt;
   **/
  T taskDefinitionKey(String key);

  /**
   * Only select tasks with a taskDefinitionKey that match the given parameter. The syntax is that of SQL: for example usage: taskDefinitionKeyLike("%activiti%"). The task definition key is the id of
   * the userTask: &lt;userTask id="xxx" .../&gt;
   **/
  T taskDefinitionKeyLike(String keyLike);

  /**
   * Only select tasks with the given due date.
   */
  T taskDueDate(Date dueDate);

  /**
   * Only select tasks which have a due date before the given date.
   */
  T taskDueBefore(Date dueDate);

  /**
   * Only select tasks which have a due date after the given date.
   */
  T taskDueAfter(Date dueDate);

  /*
  * Only select subtasks of the given parent task
  */
  T taskParentTaskId(String parentTaskId);

  /**
   * Only select tasks with no due date.
   */
  T withoutTaskDueDate();

  /**
   * Only select tasks which are part of a process instance which has the given process definition key.
   */
  T processDefinitionKey(String processDefinitionKey);

  /**
   * Only select tasks which are part of a process instance which has a process definition key like the given value. The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  T processDefinitionKeyLike(String processDefinitionKeyLike);

  /**
   * Only select tasks which are part of a process instance which has a process definition key like the given value. The syntax that should be used is the same as in SQL, eg. %activiti%.
   *
   * This method, unlike the {@link #processDefinitionKeyLike(String)} method will not take in account the upper/lower case: both the input parameter as the column value are lowercased when the query
   * is executed.
   */
  T processDefinitionKeyLikeIgnoreCase(String processDefinitionKeyLikeIgnoreCase);

  /** Only select tasks that have a process definition for which the key is present in the given list **/
  T processDefinitionKeyIn(List<String> processDefinitionKeys);

  /**
   * Only select tasks which are part of a process instance which has the given process definition id.
   */
  T processDefinitionId(String processDefinitionId);

  /**
   * Only select tasks which are part of a process instance which has the given process definition name.
   */
  T processDefinitionName(String processDefinitionName);

  /**
   * Only select tasks which are part of a process instance which has a process definition name like the given value. The syntax that should be used is the same as in SQL, eg. %activiti%.
   */
  T processDefinitionNameLike(String processDefinitionNameLike);

  /**
   * Only select tasks which are part of a process instance whose definition belongs to the category which is present in the given list.
   *
   * @throws ActivitiIllegalArgumentException
   *           When passed category list is empty or <code>null</code> or contains <code>null String</code>.
   * @param processCategoryInList
   */
  T processCategoryIn(List<String> processCategoryInList);

  /**
   * Only select tasks which are part of a process instance whose definition does not belong to the category which is present in the given list.
   *
   * @throws ActivitiIllegalArgumentException
   *           When passed category list is empty or <code>null</code> or contains <code>null String</code>.
   * @param processCategoryNotInList
   */
  T processCategoryNotIn(List<String> processCategoryNotInList);

  /**
   * Only select tasks which are part of a process instance which has the given deployment id.
   */
  T deploymentId(String deploymentId);

  /**
   * Only select tasks which are part of a process instance which has the given deployment id.
   */
  T deploymentIdIn(List<String> deploymentIds);

  /**
   * Only select tasks which have a local task variable with the given name set to the given value.
   */
  T taskVariableValueEquals(String variableName, Object variableValue);

  /**
   * Only select tasks which have at least one local task variable with the given value.
   */
  T taskVariableValueEquals(Object variableValue);

  /**
   * Only select tasks which have a local string variable with the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive
   * Collations available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx" >MSDN Server Collation Reference</a>).
   * </p>
   */
  T taskVariableValueEqualsIgnoreCase(String name, String value);

  /**
   * Only select tasks which have a local task variable with the given name, but with a different value than the passed value. Byte-arrays and {@link Serializable} objects (which are not primitive
   * type wrappers) are not supported.
   */
  T taskVariableValueNotEquals(String variableName, Object variableValue);

  /**
   * Only select tasks which have a local string variable with is not the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive
   * Collations available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx" >MSDN Server Collation Reference</a>).
   * </p>
   */
  T taskVariableValueNotEqualsIgnoreCase(String name, String value);

  /**
   * Only select tasks which have a local variable value greater than the passed value when they ended. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name
   *          cannot be null.
   * @param value
   *          cannot be null.
   */
  T taskVariableValueGreaterThan(String name, Object value);

  /**
   * Only select tasks which have a local variable value greater than or equal to the passed value when they ended. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   *
   * @param name
   *          cannot be null.
   * @param value
   *          cannot be null.
   */
  T taskVariableValueGreaterThanOrEqual(String name, Object value);

  /**
   * Only select tasks which have a local variable value less than the passed value when the ended.Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   *
   * @param name
   *          cannot be null.
   * @param value
   *          cannot be null.
   */
  T taskVariableValueLessThan(String name, Object value);

  /**
   * Only select tasks which have a local variable value less than or equal to the passed value when they ended. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   *
   * @param name
   *          cannot be null.
   * @param value
   *          cannot be null.
   */
  T taskVariableValueLessThanOrEqual(String name, Object value);

  /**
   * Only select tasks which have a local variable value like the given value when they ended. This can be used on string variables only.
   *
   * @param name
   *          cannot be null.
   * @param value
   *          cannot be null. The string can include the wildcard character '%' to express like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   */
  T taskVariableValueLike(String name, String value);

  /** Only select tasks which have a local variable value like the given value (case insensitive)
   * when they ended. This can be used on string variables only.
   * @param name cannot be null.
   * @param value cannot be null. The string can include the
   *          wildcard character '%' to express like-strategy: starts with
   *          (string%), ends with (%string) or contains (%string%). */
  T taskVariableValueLikeIgnoreCase(String name, String value);

  /**
   * Only select tasks which are part of a process that has a variable with the given name set to the given value.
   */
  T processVariableValueEquals(String variableName, Object variableValue);

  /**
   * Only select tasks which are part of a process that has at least one variable with the given value.
   */
  T processVariableValueEquals(Object variableValue);

  /**
   * Only select tasks which are part of a process that has a local string variable which is not the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive
   * Collations available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx" >MSDN Server Collation Reference</a>).
   * </p>
   */
  T processVariableValueEqualsIgnoreCase(String name, String value);

  /**
   * Only select tasks which have a variable with the given name, but with a different value than the passed value. Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   */
  T processVariableValueNotEquals(String variableName, Object variableValue);

  /**
   * Only select tasks which are part of a process that has a string variable with the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive
   * Collations available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx" >MSDN Server Collation Reference</a>).
   * </p>
   */
  T processVariableValueNotEqualsIgnoreCase(String name, String value);

  /**
   * Only select tasks which have a global variable value greater than the passed value when they ended. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name
   *          cannot be null.
   * @param value
   *          cannot be null.
   */
  T processVariableValueGreaterThan(String name, Object value);

  /**
   * Only select tasks which have a global variable value greater than or equal to the passed value when they ended. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive
   * type wrappers) are not supported.
   *
   * @param name
   *          cannot be null.
   * @param value
   *          cannot be null.
   */
  T processVariableValueGreaterThanOrEqual(String name, Object value);

  /**
   * Only select tasks which have a global variable value less than the passed value when the ended.Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   *
   * @param name
   *          cannot be null.
   * @param value
   *          cannot be null.
   */
  T processVariableValueLessThan(String name, Object value);

  /**
   * Only select tasks which have a global variable value less than or equal to the passed value when they ended. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   *
   * @param name
   *          cannot be null.
   * @param value
   *          cannot be null.
   */
  T processVariableValueLessThanOrEqual(String name, Object value);

  /**
   * Only select tasks which have a global variable value like the given value when they ended. This can be used on string variables only.
   *
   * @param name
   *          cannot be null.
   * @param value
   *          cannot be null. The string can include the wildcard character '%' to express like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   */
  T processVariableValueLike(String name, String value);

  /** Only select tasks which have a global variable value like the given value (case insensitive)
   * when they ended. This can be used on string variables only.
   * @param name cannot be null.
   * @param value cannot be null. The string can include the
   *          wildcard character '%' to express like-strategy: starts with
   *          (string%), ends with (%string) or contains (%string%). */
  T processVariableValueLikeIgnoreCase(String name, String value);

  /**
   * Include local task variables in the task query result
   */
  T includeTaskLocalVariables();

  /**
   * Include global task variables in the task query result
   */
  T includeProcessVariables();

  /**
   * Limit task variables
   */
  T limitTaskVariables(Integer taskVariablesLimit);

  /**
   * Localize task name and description to specified locale.
   */
  T locale(String locale);

  /**
   * Instruct localization to fallback to more general locales including the default locale of the JVM if the specified locale is not found.
   */
  T withLocalizationFallback();

  /**
   * All query clauses called will be added to a single or-statement. This or-statement will be included with the other already existing clauses in the query, joined by an 'and'.
   *
   * Calling endOr() will add all clauses to the regular query again. Calling or() after endOr() has been called will result in an exception.
   */
  T or();

  T endOr();

  // ORDERING

  /**
   * Order by task id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByTaskId();

  /**
   * Order by task name (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByTaskName();

  /**
   * Order by description (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByTaskDescription();

  /**
   * Order by priority (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByTaskPriority();

  /**
   * Order by assignee (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByTaskAssignee();

  /**
   * Order by the time on which the tasks were created (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByTaskCreateTime();

  /**
   * Order by process instance id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByProcessInstanceId();

  /**
   * Order by execution id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByExecutionId();

  /**
   * Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByProcessDefinitionId();

  /**
   * Order by task due date (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByTaskDueDate();

  /**
   * Order by task owner (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByTaskOwner();

  /**
   * Order by task definition key (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByTaskDefinitionKey();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  T orderByTenantId();

  /**
   * Order by due date (needs to be followed by {@link #asc()} or {@link #desc()}). If any of the tasks have null for the due date, these will be first in the result.
   */
  T orderByDueDateNullsFirst();

  /**
   * Order by due date (needs to be followed by {@link #asc()} or {@link #desc()}). If any of the tasks have null for the due date, these will be last in the result.
   */
  T orderByDueDateNullsLast();

}
