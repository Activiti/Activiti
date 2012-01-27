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
package org.activiti.engine;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;


/** Service which provides access to {@link Deployment}s,
 * {@link ProcessDefinition}s and {@link ProcessInstance}s.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface RuntimeService {
  
  /** 
   * Starts a new process instance in the latest version of the process definition with the given key.
   * @param processDefinitionKey key of process definition, cannot be null.
   * @throws ActivitiException when no process definition is deployed with the given key.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey);
  
  /**
   * Starts a new process instance in the latest version of the process
   * definition with the given key.
   * 
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   * 
   * Note that a business key MUST be unique for the given process definition.
   * Process instance from different process definition are allowed to have the
   * same business key.
   * 
   * The combination of processdefinitionKey-businessKey must be unique.
   * 
   * @param processDefinitionKey
   *          key of process definition, cannot be null.
   * @param businessKey
   *          a key that uniquely identifies the process instance in the context
   *          or the given process definition.
   * @throws ActivitiException
   *           when no process definition is deployed with the given key.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey);
  
  /** Starts a new process instance in the latest version of the process definition with the given key 
   * @param processDefinitionKey key of process definition, cannot be null.
   * @param variables the variables to pass, can be null.
   * @throws ActivitiException when no process definition is deployed with the given key. 
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables);
  
  /** 
   * Starts a new process instance in the latest version of the process definition with the given key.
   * 
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   * 
   * Note that a business key MUST be unique for the given process definition.
   * Process instance from different process definition are allowed to have the
   * same business key.
   * 
   * The combination of processdefinitionKey-businessKey must be unique.
   * @param processDefinitionKey key of process definition, cannot be null.
   * @param variables the variables to pass, can be null.
   * @param businessKey a key that uniquely identifies the process instance in the context or the
   *                    given process definition.
   * @throws ActivitiException when no process definition is deployed with the given key.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables);

  /** Starts a new process instance in the exactly specified version of the process definition with the given id.
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @throws ActivitiException when no process definition is deployed with the given key. 
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId);
  
  /** 
   * Starts a new process instance in the exactly specified version of the process definition with the given id.
   * 
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   * 
   * Note that a business key MUST be unique for the given process definition.
   * Process instance from different process definition are allowed to have the
   * same business key.
   * 
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @param businessKey a key that uniquely identifies the process instance in the context or the
   *                    given process definition.
   * @throws ActivitiException when no process definition is deployed with the given key. 
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey);
  
  /** Starts a new process instance in the exactly specified version of the process definition with the given id.
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @param variables variables to be passed, can be null
   * @throws ActivitiException when no process definition is deployed with the given key. 
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables);
  
  /** 
   * Starts a new process instance in the exactly specified version of the process definition with the given id.
   * 
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   * 
   * Note that a business key MUST be unique for the given process definition.
   * Process instance from different process definition are allowed to have the
   * same business key.
   * 
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @param variables variables to be passed, can be null
   * @throws ActivitiException when no process definition is deployed with the given key. 
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables);

  /** Delete an existing runtime process instance.
   * @param processInstanceId id of process instance to delete, cannot be null.
   * @param deleteReason reason for deleting, can be null.
   * @throws ActivitiException when no process instance is found with the given id.
   */
  void deleteProcessInstance(String processInstanceId, String deleteReason);
    
  /** Finds the activity ids for all executions that are waiting in activities. 
   * This is a list because a single activity can be active multiple times.
   * @param executionId id of the execution, cannot be null.
   * @throws ActivitiException when no execution exists with the given executionId. 
   */
  List<String> getActiveActivityIds(String executionId);

  /** Sends an external trigger to an activity instance that is waiting inside the given execution.
   * @param executionId id of execution to signal, cannot be null.
   * @throws ActivitiException when no execution is found for the given executionId. 
   */
  void signal(String executionId);
  
  /** All variables visible from the given execution scope (including parent scopes).
   * @param executionId id of execution, cannot be null.
   * @return the variables or an empty map if no such variables are found.
   * @throws ActivitiException when no execution is found for the given executionId. */
  Map<String, Object> getVariables(String executionId);
  
  /** All variable values that are defined in the execution scope, without taking outer scopes into account.
   * If you have many task local variables and you only need a few, consider using {@link #getVariablesLocal(String, Collection)} 
   * for better performance.
   * @param executionId id of execution, cannot be null.
   * @return the variables or an empty map if no such variables are found.
   * @throws ActivitiException when no execution is found for the given executionId. */
   Map<String, Object> getVariablesLocal(String executionId);

   /** The variable values for all given variableNames, takes all variables into account which are visible from the given execution scope (including parent scopes). 
   * @param executionId id of execution, cannot be null.
   * @param variableNames the collection of variable names that should be retrieved.
   * @return the variables or an empty map if no such variables are found.
   * @throws ActivitiException when no execution is found for the given executionId. */
   Map<String, Object> getVariables(String executionId, Collection<String> variableNames);

   /** The variable values for the given variableNames only taking the given execution scope into account, not looking in outer scopes. 
   * @param executionId id of execution, cannot be null.
   * @param variableNames the collection of variable names that should be retrieved.
   * @return the variables or an empty map if no such variables are found.
   * @throws ActivitiException when no execution is found for the given executionId.  */
   Map<String, Object> getVariablesLocal(String executionId, Collection<String> variableNames);

  /** The variable value.  Searching for the variable is done in all scopes that are visible to the given execution (including parent scopes).
   * Returns null when no variable value is found with the given name or when the value is set to null.
   * @param executionId id of execution, cannot be null.
   * @param variableName name of variable, cannot be null.
   * @return the variable value or null if the variable is undefined or the value of the variable is null.
   * @throws ActivitiException when no execution is found for the given executionId. */
  Object getVariable(String executionId, String variableName);

  /** The variable value for an execution. Returns the value when the variable is set 
   * for the execution (and not searching parent scopes). Returns null when no variable value is found with the given name or when the value is set to null.  */
  Object getVariableLocal(String executionId, String variableName);

  /** Update or create a variable for an execution.  If the variable is not already existing somewhere in the execution hierarchy,
   * it will be created in the process instance (which is the root execution). 
   * @param executionId id of execution to set variable in, cannot be null.
   * @param variableName name of variable to set, cannot be null.
   * @param value value to set. When null is passed, the variable is not removed,
   * only it's value will be set to null.
   * @throws ActivitiException when no execution is found for the given executionId. 
   */
  void setVariable(String executionId, String variableName, Object value);

  /** Update or create a variable for an execution (not considering parent scopes). 
   * If the variable is not already existing, it will be created in the given execution.
   * @param executionId id of execution to set variable in, cannot be null.
   * @param variableName name of variable to set, cannot be null.
   * @param value value to set. When null is passed, the variable is not removed,
   * only it's value will be set to null.
   * @throws ActivitiException when no execution is found for the given executionId.  */
  void setVariableLocal(String executionId, String variableName, Object value);

  /** Update or create given variables for an execution (including parent scopes). If the variables are not already existing, they will be created in the process instance 
   * (which is the root execution).
   * @param executionId id of the execution, cannot be null.
   * @param variables map containing name (key) and value of variables, can be null.
   * @throws ActivitiException when no execution is found for the given executionId.  */
  void setVariables(String executionId, Map<String, ? extends Object> variables);
  
  /** Update or create given variables for an execution (not considering parent scopes). If the variables are not already existing, it will be created in the given execution.
   * @param executionId id of the execution, cannot be null.
   * @param variables map containing name (key) and value of variables, can be null.
   * @throws ActivitiException when no execution is found for the given executionId. */
  void setVariablesLocal(String executionId, Map<String, ? extends Object> variables);


  
  
  
  /** Creates a new {@link ExecutionQuery} instance, 
   * that can be used to query the executions and process instances. */
  ExecutionQuery createExecutionQuery();
  
  /**
   * Creates a new {@link ProcessInstanceQuery} instance, that can be used
   * to query process instances.
   */
  ProcessInstanceQuery createProcessInstanceQuery();
  
  /**
   * Suspends the process instance with the given id. 
   * 
   * If a process instance is in state suspended, activiti will not 
   * execute jobs (timers, messages) associated with this instance.
   * 
   * If you have a process instance hierarchy, suspending
   * one process instance form the hierarchy will not suspend other 
   * process instances form that hierarchy.
   * 
   *  @throws ActivitiException if no such processInstance can be found or if the process instance is already in state suspended.
   */
  void suspendProcessInstanceById(String processInstanceId);
  
  /**
   * Activates the process instance with the given id. 
   * 
   * If you have a process instance hierarchy, suspending
   * one process instance form the hierarchy will not suspend other 
   * process instances form that hierarchy.
   * 
   * @throws ActivitiException if no such processInstance can be found or if the process instance is already in state active.
   */
  void activateProcessInstanceById(String processInstanceId);
  
}