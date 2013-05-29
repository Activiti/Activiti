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

import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.NativeExecutionQuery;
import org.activiti.engine.runtime.NativeProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;


/** 
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 */
public interface RuntimeService {
  
  /** 
   * Starts a new process instance in the latest version of the process definition with the given key.
   * @param processDefinitionKey key of process definition, cannot be null.
   * @throws ActivitiObjectNotFoundException when no process definition is deployed with the given key.
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
   * @throws ActivitiObjectNotFoundException
   *           when no process definition is deployed with the given key.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey);
  
  /** Starts a new process instance in the latest version of the process definition with the given key 
   * @param processDefinitionKey key of process definition, cannot be null.
   * @param variables the variables to pass, can be null.
   * @throws ActivitiObjectNotFoundException when no process definition is deployed with the given key. 
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
   * @throws ActivitiObjectNotFoundException when no process definition is deployed with the given key.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables);

  /** Starts a new process instance in the exactly specified version of the process definition with the given id.
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @throws ActivitiObjectNotFoundException when no process definition is deployed with the given key. 
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
   * @throws ActivitiObjectNotFoundException when no process definition is deployed with the given key. 
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey);
  
  /** Starts a new process instance in the exactly specified version of the process definition with the given id.
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @param variables variables to be passed, can be null
   * @throws ActivitiObjectNotFoundException when no process definition is deployed with the given key. 
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
   * @throws ActivitiObjectNotFoundException when no process definition is deployed with the given key. 
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables);
  
  /**
   * <p>Signals the process engine that a message is received and starts a new 
   * {@link ProcessInstance}.</p>
   * 
   * <p>Calling this method can have two different outcomes:
   * <ul>
   * <li>If the message name is associated with a message start event, a new
   * process instance is started.</li>
   * <li>If no subscription to a message with the given name exists, {@link ActivitiException}
   * is thrown</li>
   * </ul>
   * </p>
   *  
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element.

   * @return the {@link ProcessInstance} object representing the started process instance
   * 
   * @throws ActivitiExeception if no subscription to a message with the given name exists
   * 
   * @since 5.9
   */
  ProcessInstance startProcessInstanceByMessage(String messageName);
  
  /**
   * <p>Signals the process engine that a message is received and starts a new 
   * {@link ProcessInstance}.</p>
   * 
   * See {@link #startProcessInstanceByMessage(String, Map)}. This method allows 
   * specifying a business key.
   *  
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element.
   * @param businessKey
   *          the business key which is added to the started process instance 
   * 
   * @throws ActivitiExeception if no subscription to a message with the given name exists
   * 
   * @since 5.10
   */
  ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey);
  
  /**
   * <p>Signals the process engine that a message is received and starts a new 
   * {@link ProcessInstance}.</p>
   * 
   * See {@link #startProcessInstanceByMessage(String)}. In addition, this method allows 
   * specifying a the payload of the message as a map of process variables.
   *  
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element.
   * @param processVariables
   *          the 'payload' of the message. The variables are added as processes
   *          variables to the started process instance.
   * @return the {@link ProcessInstance} object representing the started process instance
   * 
   * @throws ActivitiExeception if no subscription to a message with the given name exists
   * 
   * @since 5.9
   */
  ProcessInstance startProcessInstanceByMessage(String messageName, Map<String, Object> processVariables);
  
  /**
   * <p>Signals the process engine that a message is received and starts a new 
   * {@link ProcessInstance}.</p>
   * 
   * See {@link #startProcessInstanceByMessage(String, Map)}. In addition, this method allows 
   * specifying a business key.
   *  
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element.
   * @param businessKey
   *          the business key which is added to the started process instance 
   * @param processVariables
   *          the 'payload' of the message. The variables are added as processes
   *          variables to the started process instance.
   * @return the {@link ProcessInstance} object representing the started process instance
   * 
   * @throws ActivitiExeception if no subscription to a message with the given name exists
   * 
   * @since 5.9
   */
  ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey, Map<String, Object> processVariables);

  /** Delete an existing runtime process instance.
   * @param processInstanceId id of process instance to delete, cannot be null.
   * @param deleteReason reason for deleting, can be null.
   * @throws ActivitiObjectNotFoundException when no process instance is found with the given id.
   */
  void deleteProcessInstance(String processInstanceId, String deleteReason);
    
  /** Finds the activity ids for all executions that are waiting in activities. 
   * This is a list because a single activity can be active multiple times.
   * @param executionId id of the execution, cannot be null.
   * @throws ActivitiObjectNotFoundException when no execution exists with the given executionId. 
   */
  List<String> getActiveActivityIds(String executionId);

  /** Sends an external trigger to an activity instance that is waiting inside the given execution.
   * @param executionId id of execution to signal, cannot be null.
   * @throws ActivitiObjectNotFoundException when no execution is found for the given executionId. 
   */
  void signal(String executionId);
  
  /** Sends an external trigger to an activity instance that is waiting inside the given execution.
   * @param executionId id of execution to signal, cannot be null.
   * @param processVariables a map of process variables
   * @throws ActivitiObjectNotFoundException when no execution is found for the given executionId. 
   */
  void signal(String executionId, Map<String, Object> processVariables);
  
  // Identity Links ///////////////////////////////////////////////////////////////
  
  /**
   * Involves a user with a process instance. The type of identity link is defined by the
   * given identityLinkType.
   * @param processInstanceId id of the process instance, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiObjectNotFoundException when the process instance doesn't exist.
   */
  void addUserIdentityLink(String processInstanceId, String userId, String identityLinkType);
  
  /**
   * Retrieves the {@link IdentityLink}s associated with the given process instance.
   * Such an {@link IdentityLink} informs how a certain user is involved with a process instance.
   */
  List<IdentityLink> getIdentityLinksForProcessInstance(String instanceId);
  
  // Variables ////////////////////////////////////////////////////////////////////
  
  /** All variables visible from the given execution scope (including parent scopes).
   * @param executionId id of execution, cannot be null.
   * @return the variables or an empty map if no such variables are found.
   * @throws ActivitiObjectNotFoundException when no execution is found for the given executionId. */
  Map<String, Object> getVariables(String executionId);
  
  /** All variable values that are defined in the execution scope, without taking outer scopes into account.
   * If you have many task local variables and you only need a few, consider using {@link #getVariablesLocal(String, Collection)} 
   * for better performance.
   * @param executionId id of execution, cannot be null.
   * @return the variables or an empty map if no such variables are found.
   * @throws ActivitiObjectNotFoundException when no execution is found for the given executionId. */
   Map<String, Object> getVariablesLocal(String executionId);

   /** The variable values for all given variableNames, takes all variables into account which are visible from the given execution scope (including parent scopes). 
   * @param executionId id of execution, cannot be null.
   * @param variableNames the collection of variable names that should be retrieved.
   * @return the variables or an empty map if no such variables are found.
   * @throws ActivitiObjectNotFoundException when no execution is found for the given executionId. */
   Map<String, Object> getVariables(String executionId, Collection<String> variableNames);

   /** The variable values for the given variableNames only taking the given execution scope into account, not looking in outer scopes. 
   * @param executionId id of execution, cannot be null.
   * @param variableNames the collection of variable names that should be retrieved.
   * @return the variables or an empty map if no such variables are found.
   * @throws ActivitiObjectNotFoundException when no execution is found for the given executionId.  */
   Map<String, Object> getVariablesLocal(String executionId, Collection<String> variableNames);

  /** The variable value.  Searching for the variable is done in all scopes that are visible to the given execution (including parent scopes).
   * Returns null when no variable value is found with the given name or when the value is set to null.
   * @param executionId id of execution, cannot be null.
   * @param variableName name of variable, cannot be null.
   * @return the variable value or null if the variable is undefined or the value of the variable is null.
   * @throws ActivitiObjectNotFoundException when no execution is found for the given executionId. */
  Object getVariable(String executionId, String variableName);
  
  /** Check whether or not this execution has variable set with the given name, Searching for the variable is done in all scopes that are visible to the given execution (including parent scopes). */
  boolean hasVariable(String executionId, String variableName);

  /** The variable value for an execution. Returns the value when the variable is set 
   * for the execution (and not searching parent scopes). Returns null when no variable value is found with the given name or when the value is set to null.  */
  Object getVariableLocal(String executionId, String variableName);
  
  /** Check whether or not this execution has a local variable set with the given name. */
  boolean hasVariableLocal(String executionId, String variableName);

  /** Update or create a variable for an execution.  If the variable is not already existing somewhere in the execution hierarchy,
   * it will be created in the process instance (which is the root execution). 
   * @param executionId id of execution to set variable in, cannot be null.
   * @param variableName name of variable to set, cannot be null.
   * @param value value to set. When null is passed, the variable is not removed,
   * only it's value will be set to null.
   * @throws ActivitiObjectNotFoundException when no execution is found for the given executionId. 
   */
  void setVariable(String executionId, String variableName, Object value);

  /** Update or create a variable for an execution (not considering parent scopes). 
   * If the variable is not already existing, it will be created in the given execution.
   * @param executionId id of execution to set variable in, cannot be null.
   * @param variableName name of variable to set, cannot be null.
   * @param value value to set. When null is passed, the variable is not removed,
   * only it's value will be set to null.
   * @throws ActivitiObjectNotFoundException when no execution is found for the given executionId.  */
  void setVariableLocal(String executionId, String variableName, Object value);

  /** Update or create given variables for an execution (including parent scopes). If the variables are not already existing, they will be created in the process instance 
   * (which is the root execution).
   * @param executionId id of the execution, cannot be null.
   * @param variables map containing name (key) and value of variables, can be null.
   * @throws ActivitiObjectNotFoundException when no execution is found for the given executionId.  */
  void setVariables(String executionId, Map<String, ? extends Object> variables);
  
  /** Update or create given variables for an execution (not considering parent scopes). If the variables are not already existing, it will be created in the given execution.
   * @param executionId id of the execution, cannot be null.
   * @param variables map containing name (key) and value of variables, can be null.
   * @throws ActivitiObjectNotFoundException when no execution is found for the given executionId. */
  void setVariablesLocal(String executionId, Map<String, ? extends Object> variables);
  
  /**
   * Removes a variable for an execution.
   * @param executionId id of execution to remove variable in.
   * @param variableName name of variable to remove.
   */
  void removeVariable(String executionId, String variableName);
  
  /**
   * Removes a variable for an execution (not considering parent scopes).
   * @param executionId id of execution to remove variable in.
   * @param variableName name of variable to remove.
   */
  void removeVariableLocal(String executionId, String variableName);
  
  /**
   * Removes variables for an execution.
   * @param executionId id of execution to remove variable in.
   * @param variableNames collection containing name of variables to remove.
   */
  void removeVariables(String executionId, Collection<String> variableNames);

  /**
   * Remove variables for an execution (not considering parent scopes).
   * @param executionId id of execution to remove variable in.
   * @param variableNames collection containing name of variables to remove.
   */
  void removeVariablesLocal(String executionId, Collection<String> variableNames);
  
  // Queries ////////////////////////////////////////////////////////
  
  /** Creates a new {@link ExecutionQuery} instance, 
   * that can be used to query the executions and process instances. */
  ExecutionQuery createExecutionQuery();
  
  /**
   * creates a new {@link NativeExecutionQuery} to query {@link Execution}s
   * by SQL directly
   */
  NativeExecutionQuery createNativeExecutionQuery();
  
  /**
   * Creates a new {@link ProcessInstanceQuery} instance, that can be used
   * to query process instances.
   */
  ProcessInstanceQuery createProcessInstanceQuery();

  /**
   * creates a new {@link NativeProcessInstanceQuery} to query {@link ProcessInstance}s 
   * by SQL directly
   */
  NativeProcessInstanceQuery createNativeProcessInstanceQuery();
  
  // Process instance state //////////////////////////////////////////
    
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
   *  @throws ActivitiObjectNotFoundException if no such processInstance can be found.
   *  @throws ActivitiException the process instance is already in state suspended.
   */
  void suspendProcessInstanceById(String processInstanceId);
  
  /**
   * Activates the process instance with the given id. 
   * 
   * If you have a process instance hierarchy, suspending
   * one process instance form the hierarchy will not suspend other 
   * process instances form that hierarchy.
   * 
   * @throws ActivitiObjectNotFoundException if no such processInstance can be found. 
   * @throws ActivitiException if the process instance is already in state active.
   */
  void activateProcessInstanceById(String processInstanceId);
  
  // Events ////////////////////////////////////////////////////////////////////////
  
  /**
   * Notifies the process engine that a signal event of name 'signalName' has
   * been received. This method delivers the signal to all executions waiting on
   * the signal.<p/> 
   * 
   * <strong>NOTE:</strong> The waiting executions are notified synchronously. 
   * 
   * @param signalName
   *          the name of the signal event
   */
  void signalEventReceived(String signalName);
  
  /**
   * Notifies the process engine that a signal event of name 'signalName' has
   * been received. This method delivers the signal to all executions waiting on
   * the signal.<p/>
   * 
   * <strong>NOTE:</strong> The waiting executions are notified synchronously.
   * 
   * @param signalName
   *          the name of the signal event
   * @param processVariables
   *          a map of variables added to the execution(s)
   */
  void signalEventReceived(String signalName, Map<String, Object> processVariables);
  
  /**
   * Notifies the process engine that a signal event of name 'signalName' has
   * been received. This method delivers the signal to a single execution, being the 
   * execution referenced by 'executionId'. 
   * The waiting execution is notified synchronously.
   * 
   * @param signalName
   *          the name of the signal event
   * @param executionId
   *          the id of the execution to deliver the signal to
   * @throws ActivitiObjectNotFoundException if no such execution exists.
   * @throws ActivitiException if the execution has not subscribed to the signal.
   */
  void signalEventReceived(String signalName, String executionId);  
  
  /**
   * Notifies the process engine that a signal event of name 'signalName' has
   * been received. This method delivers the signal to a single execution, being the 
   * execution referenced by 'executionId'. 
   * The waiting execution is notified synchronously.
   * 
   * @param signalName
   *          the name of the signal event
   * @param executionId
   *          the id of the execution to deliver the signal to
   * @param processVariables
   *          a map of variables added to the execution(s)
   * @throws ActivitiObjectNotFoundException if no such execution exists.
   * @throws ActivitiException if the execution has not subscribed to the signal
   */
  void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables);

  /**
   * Notifies the process engine that a message event with name 'messageName' has
   * been received and has been correlated to an execution with id 'executionId'. 
   * 
   * The waiting execution is notified synchronously.
   * 
   * @param messageName
   *          the name of the message event
   * @param executionId
   *          the id of the execution to deliver the message to
   * @throws ActivitiObjectNotFoundException if no such execution exists.
   * @throws ActivitiException if the execution has not subscribed to the signal
   */
  void messageEventReceived(String messageName, String executionId);
  
  /**
   * Notifies the process engine that a message event with the name 'messageName' has
   * been received and has been correlated to an execution with id 'executionId'. 
   * 
   * The waiting execution is notified synchronously.
   * 
   * @param messageName
   *          the name of the message event
   * @param executionId
   *          the id of the execution to deliver the message to
   * @param processVariables
   *          a map of variables added to the execution
   * @throws ActivitiObjectNotFoundException if no such execution exists.
   * @throws ActivitiException if the execution has not subscribed to the signal
   */
  void messageEventReceived(String messageName, String executionId, Map<String, Object> processVariables);

}