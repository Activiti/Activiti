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

import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;


/** provides access to {@link Deployment}s,
 * {@link ProcessDefinition}s and {@link ProcessInstance}s.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface RuntimeService {
  
  /** starts a new process instance in the latest version of the process definition with the given key.
   *  @throws ActivitiException when no process definition is deployed with the given key.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey);

  /** starts a new process instance in the latest version of the process definition with the given key 
   *  @throws ActivitiException when no process definition is deployed with the given key. 
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables);

  /** starts a new process instance in the exactly specified version of the process definition with the given id
   *  @throws ActivitiException when no process definition is deployed with the given key. 
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId);
  
  /** starts a new process instance in the exactly specified version of the process definition with the given id 
   *  @throws ActivitiException when no process definition is deployed with the given key. 
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables);
  
  /** delete an existing runtime process instance. The reason for deletion can be null.
   *  @throws ActivitiException when no process instance is found with the given id.
   */
  void deleteProcessInstance(String processInstanceId, String deleteReason);
  
  /** creates a new {@link ExecutionQuery} instance, 
   * that can be used to query the executions and process instances. */
  ExecutionQuery createExecutionQuery();
  
  /** return the execution for the given id. Returns null if no execution is found. */
  Execution findExecutionById(String executionId);
  
  /** the activity ids for all executions that are waiting in activities. 
   * This is a list because a single activity can be active multiple times.
   * @throws ActivitiException when no execution exists with the given executionId. 
   */
  List<String> findActiveActivityIds(String executionId);

  ProcessInstanceQuery createProcessInstanceQuery();

  /** sends an external trigger to an activity instance that is waiting inside the given execution. 
   *  @throws ActivitiException when no execution is found for the given executionId. 
   */
  void signal(String executionId);
  
  /** sends an external trigger to an activity instance that is waiting inside the given execution. 
   *  @throws ActivitiException when no execution is found for the given executionId.  
   */
  void signal(String executionId, String signalName, Object signalData);
  
  /** variables for an execution. 
   *  @throws ActivitiException when no execution is found for the given executionId.   
   */
  Map<String, Object> getVariables(String executionId);
  
  /** retrieve a specific variable for an execution. Returns null when the variable is set 
   *  for the execution.
   *  @throws ActivitiException when no execution is found for the given executionId.   
   */
  Object getVariable(String executionId, String variableName);

  /** update or create a variable for a process instance or an activity instance */
  void setVariable(String scopeInstance, String variableName, Object value);

  /** update or create given variables for a process instance or an activity instance */
  void setVariables(String scopeInstance, Map<String, Object> variables);

}