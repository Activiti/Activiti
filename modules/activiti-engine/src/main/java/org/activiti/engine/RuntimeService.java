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

import java.util.Map;


/** provides access to {@link Deployment}s,
 * {@link ProcessDefinition}s and {@link ProcessInstance}s.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface RuntimeService {
  
  /** starts a new process instance in the latest version of the process definition with the given key */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey);

  /** starts a new process instance in the latest version of the process definition with the given key */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables);

  /** starts a new process instance in the exactly specified version of the process definition with the given id */
  ProcessInstance startProcessInstanceById(String processDefinitionId);
  
  /** starts a new process instance in the exactly specified version of the process definition with the given id */
  ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables);
  
  /** delete an existing runtime process instance */
  void deleteProcessInstance(String processInstanceId, String deleteReason);
  
  /** creates a new {@link ExecutionQuery} instance, 
   * that can be used to query the executions and process instances. */
  ExecutionQuery createExecutionQuery();

  ProcessInstanceQuery createProcessInstanceQuery();

  /** sends an external trigger to an activity instance that is waiting. */
  void signal(String activityInstanceId);
  
  /** sends an external trigger to an activity instance that is waiting. */
  void signal(String activityInstanceId, String signalName, Object signalData);
  
  /** variables for a process instance or an activity instance. */
  Map<String, Object> getVariables(String scopeInstanceId);
  
  /** retrieve a specific variable for a process instance or an activity instance */
  Object getVariable(String scopeInstanceId, String variableName);

  /** update or create a variable for a process instance or an activity instance */
  void setVariable(String scopeInstance, String variableName, Object value);

  /** update or create given variables for a process instance or an activity instance */
  void setVariables(String scopeInstance, Map<String, Object> variables);

}