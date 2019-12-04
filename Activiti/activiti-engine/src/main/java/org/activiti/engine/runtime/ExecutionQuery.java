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
package org.activiti.engine.runtime;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.query.Query;

/**
 * Allows programmatic querying of {@link Execution}s.
 *
 */
@Internal
public interface ExecutionQuery extends Query<ExecutionQuery, Execution> {

  /** Only select executions which have the given process definition key. **/
  ExecutionQuery processDefinitionKey(String processDefinitionKey);
  
  /** Only select executions which have process definitions with the given keys. **/
  ExecutionQuery processDefinitionKeys(Set<String> processDefinitionKeys);
  
  /** Only select executions which have the given process definition id. **/
  ExecutionQuery processDefinitionId(String processDefinitionId);
  
  /** Only select executions which have the given process definition category. */
  ExecutionQuery processDefinitionCategory(String processDefinitionCategory);

  /** Only select executions which have the given process definition name. */
  ExecutionQuery processDefinitionName(String processDefinitionName);
  
  /**
  * Only select executions which have the given process definition version.
  * Particulary useful when used in combination with {@link #processDefinitionKey(String)}
  */
  ExecutionQuery processDefinitionVersion(Integer processDefinitionVersion);

  /** Only select executions which have the given process instance id. **/
  ExecutionQuery processInstanceId(String processInstanceId);

  /** Only select executions which have the given root process instance id. **/
  ExecutionQuery rootProcessInstanceId(String rootProcessInstanceId);

  /**
   * Only executions with the given business key.
   * 
   * Note that only process instances have a business key and as such, child executions will NOT be returned. If you want to return child executions of the process instance with the given business key
   * too, use the {@link #processInstanceBusinessKey(String, boolean)} method with a boolean value of <i>true</i> instead.
   */
  ExecutionQuery processInstanceBusinessKey(String processInstanceBusinessKey);

  /**
   * Only executions with the given business key. Similar to {@link #processInstanceBusinessKey(String)}, but allows to choose whether child executions are returned or not.
   */
  ExecutionQuery processInstanceBusinessKey(String processInstanceBusinessKey, boolean includeChildExecutions);

  /** Only select executions with the given id. **/
  ExecutionQuery executionId(String executionId);

  /** Only select executions which contain an activity with the given id. **/
  ExecutionQuery activityId(String activityId);

  /**
   * Only select executions which are a direct child-execution of the execution with the given id.
   **/
  ExecutionQuery parentId(String parentId);

  /**
   * Only selects executions that have a parent id set, ie non-processinstance executions.
   */
  ExecutionQuery onlyChildExecutions();

  /**
   * Only selects executions that are a subprocess.
   */
  ExecutionQuery onlySubProcessExecutions();

  /**
   * Only selects executions that have no parent id set, ie process instance executions
   */
  ExecutionQuery onlyProcessInstanceExecutions();

  /**
   * Only select process instances that have the given tenant id.
   */
  ExecutionQuery executionTenantId(String tenantId);

  /**
   * Only select process instances with a tenant id like the given one.
   */
  ExecutionQuery executionTenantIdLike(String tenantIdLike);

  /**
   * Only select process instances that do not have a tenant id.
   */
  ExecutionQuery executionWithoutTenantId();

  /**
   * Only select executions which have a local variable with the given value. The type of variable is determined based on the value, using types configured in
   * {@link ProcessEngineConfiguration#getVariableTypes()}. Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are not supported.
   * 
   * @param name
   *          name of the variable, cannot be null.
   */
  ExecutionQuery variableValueEquals(String name, Object value);

  /**
   * Only select executions which have a local string variable with the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive
   * Collations available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx" >MSDN Server Collation Reference</a>).
   * </p>
   * 
   * @param name
   *          name of the variable, cannot be null.
   * @param value
   *          value of the variable, cannot be null.
   */
  ExecutionQuery variableValueEqualsIgnoreCase(String name, String value);

  /**
   * Only select executions which have at least one local variable with the given value. The type of variable is determined based on the value, using types configured in
   * {@link ProcessEngineConfiguration#getVariableTypes()} . Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are not supported.
   */
  ExecutionQuery variableValueEquals(Object value);

  /**
   * Only select executions which have a local variable with the given name, but with a different value than the passed value. Byte-arrays and {@link Serializable} objects (which are not primitive
   * type wrappers) are not supported.
   * 
   * @param name
   *          name of the variable, cannot be null.
   */
  ExecutionQuery variableValueNotEquals(String name, Object value);

  /**
   * Only select executions which have a local string variable which is not the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive
   * Collations available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx" >MSDN Server Collation Reference</a>).
   * </p>
   * 
   * @param name
   *          name of the variable, cannot be null.
   * @param value
   *          value of the variable, cannot be null.
   */
  ExecutionQuery variableValueNotEqualsIgnoreCase(String name, String value);

  /**
   * Only select executions which have a local variable value greater than the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are not
   * supported.
   * 
   * @param name
   *          variable name, cannot be null.
   * @param value
   *          variable value, cannot be null.
   */
  ExecutionQuery variableValueGreaterThan(String name, Object value);

  /**
   * Only select executions which have a local variable value greater than or equal to the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * 
   * @param name
   *          variable name, cannot be null.
   * @param value
   *          variable value, cannot be null.
   */
  ExecutionQuery variableValueGreaterThanOrEqual(String name, Object value);

  /**
   * Only select executions which have a local variable value less than the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are not
   * supported.
   * 
   * @param name
   *          variable name, cannot be null.
   * @param value
   *          variable value, cannot be null.
   */
  ExecutionQuery variableValueLessThan(String name, Object value);

  /**
   * Only select executions which have a local variable value less than or equal to the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   * 
   * @param name
   *          variable name, cannot be null.
   * @param value
   *          variable value, cannot be null.
   */
  ExecutionQuery variableValueLessThanOrEqual(String name, Object value);

  /**
   * Only select executions which have a local variable value like the given value. This be used on string variables only.
   * 
   * @param name
   *          variable name, cannot be null.
   * @param value
   *          variable value, cannot be null. The string can include the wildcard character '%' to express like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   */
  ExecutionQuery variableValueLike(String name, String value);
  
  /** 
   * Only select executions which have a local variable value like the given value (case insensitive).
   * This be used on string variables only.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null. The string can include the
   * wildcard character '%' to express like-strategy: 
   * starts with (string%), ends with (%string) or contains (%string%).
   */
  ExecutionQuery variableValueLikeIgnoreCase(String name, String value);
  
  /**
   * Only select executions which are part of a process that have a variable with the given name set to the given value. Byte-arrays and {@link Serializable} objects (which are not primitive type
   * wrappers) are not supported.
   */
  ExecutionQuery processVariableValueEquals(String variableName, Object variableValue);

  /**
   * Only select executions which are part of a process that have at least one variable with the given value. Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are
   * not supported.
   */
  ExecutionQuery processVariableValueEquals(Object variableValue);

  /**
   * Only select executions which are part of a process that have a variable with the given name, but with a different value than the passed value. Byte-arrays and {@link Serializable} objects (which
   * are not primitive type wrappers) are not supported.
   */
  ExecutionQuery processVariableValueNotEquals(String variableName, Object variableValue);

  /**
   * Only select executions which are part of a process that have a local string variable with the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive
   * Collations available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx" >MSDN Server Collation Reference</a>).
   * </p>
   * 
   * @param name
   *          name of the variable, cannot be null.
   * @param value
   *          value of the variable, cannot be null.
   */
  ExecutionQuery processVariableValueEqualsIgnoreCase(String name, String value);

  /**
   * Only select executions which are part of a process that have a local string variable which is not the given value, case insensitive.
   * <p>
   * This method only works if your database has encoding/collation that supports case-sensitive queries. For example, use "collate UTF-8" on MySQL and for MSSQL, select one of the case-sensitive
   * Collations available (<a href="http://msdn.microsoft.com/en-us/library/ms144250(v=sql.105).aspx" >MSDN Server Collation Reference</a>).
   * </p>
   * 
   * @param name
   *          name of the variable, cannot be null.
   * @param value
   *          value of the variable, cannot be null.
   */
  ExecutionQuery processVariableValueNotEqualsIgnoreCase(String name, String value);
  
  /**
   * Only select executions which are part of a process that have at least one variable like the given value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are not supported.
   */
  ExecutionQuery processVariableValueLike(String name, String value);
  
  /**
   * Only select executions which are part of a process that have at least one variable like the given value (case insensitive).
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers) are not supported.
   */
  ExecutionQuery processVariableValueLikeIgnoreCase(String name, String value);
  
  // event subscriptions //////////////////////////////////////////////////

  /**
   * Only select executions which have a signal event subscription for the given signal name.
   * 
   * (The signalName is specified using the 'name' attribute of the signal element in the BPMN 2.0 XML.)
   * 
   * @param signalName
   *          the name of the signal the execution has subscribed to
   */
  ExecutionQuery signalEventSubscriptionName(String signalName);

  /**
   * Only select executions which have a message event subscription for the given messageName.
   * 
   * (The messageName is specified using the 'name' attribute of the message element in the BPMN 2.0 XML.)
   * 
   * @param messageName
   *          the name of the message the execution has subscribed to
   */
  ExecutionQuery messageEventSubscriptionName(String messageName);

  /**
   * Localize execution name and description to specified locale.
   */
  ExecutionQuery locale(String locale);
  
  /**
   * Instruct localization to fallback to more general locales including the default locale of the JVM if the specified locale is not found. 
   */
  ExecutionQuery withLocalizationFallback();


  /**
   * Only select executions that were started before the given start time.
   *
   * @param beforeTime
   *          executions started before this time will be returned (cannot be null)
   */
  ExecutionQuery startedBefore(Date beforeTime);

  /**
   * Only select executions that were started after the given start time.
   *
   * @param afterTime
   *          executions started after this time will be returned (cannot be null)
   */
  ExecutionQuery startedAfter(Date afterTime);

  /**
   * Only select executions that were started after by the given user id.
   *
   * @param userId
   *          the user id of the authenticated user that started the execution (cannot be null)
   */
  ExecutionQuery startedBy(String userId);

  // ordering //////////////////////////////////////////////////////////////

  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ExecutionQuery orderByProcessInstanceId();

  /**
   * Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  ExecutionQuery orderByProcessDefinitionKey();

  /**
   * Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  ExecutionQuery orderByProcessDefinitionId();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  ExecutionQuery orderByTenantId();
}
