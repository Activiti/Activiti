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


/** provides access to {@link Deployment}s,
 * {@link ProcessDefinition}s and {@link ProcessInstance}s.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface ProcessService {
  
  /** starts a new process instance in the latest version of the process definition with the given key */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey);

  /** starts a new process instance in the latest version of the process definition with the given key */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables);

  /** starts a new process instance in the exactly specified version of the process definition with the given id */
  ProcessInstance startProcessInstanceById(String processDefinitionId);
  
  /** starts a new process instance in the exactly specified version of the process definition with the given id */
  ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables);
  
  /** delete an existing runtime process instance */
  void deleteProcessInstance(String processInstanceId);
  
  /** gets the details of a process instance 
   * @return the process instance or null if no process instance could be found with the given id. */
  ProcessInstance findProcessInstanceById(String processInstanceId);

  /** creates a new {@link ProcessInstanceQuery} instance, 
   * that can be used to dynamically query the process instances. */
  ProcessInstanceQuery createProcessInstanceQuery();

  /** gets the details of an execution
   * @return the execution or null if no execution could be found with the given id. */
  Execution findExecutionById(String executionId);
  
  /** returns the execution that currently is waiting at the given activityId,
   * or null if none exists. */
  Execution findExecutionInActivity(String processInstanceId, String activityId);

  /** sends an external trigger to an execution that is waiting. */
  void sendEvent(String executionId);
  
  /** sends an external trigger to an execution that is waiting. */
  void sendEvent(String executionId, Object eventData);
  
  /** variables for the given execution. */
  Map<String, Object> getVariables(String executionId);
  
  /** retrieve a specific variable from an execution */
  Object getVariable(String executionId, String variableName);

  /** update or create a variable */
  void setVariable(String executionId, String variableName, Object value);

  /** update or create given variables */
  void setVariables(String executionId, Map<String, Object> variables);
}