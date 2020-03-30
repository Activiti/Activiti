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

import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.api.internal.Internal;

/**
 * Helper for starting new ProcessInstance.
 *
 * An instance can be obtained through {@link org.activiti.engine.RuntimeService#createProcessInstanceBuilder()}.
 *
 * processDefinitionId or processDefinitionKey should be set before calling {@link #start()} to start a process instance.
 *
 *
 */
@Internal
public interface ProcessInstanceBuilder {

  /**
   * Set the id of the process definition
   **/
  ProcessInstanceBuilder processDefinitionId(String processDefinitionId);

  /**
   * Set the key of the process definition, latest version of the process definition with the given key.
   * If processDefinitionId was set this will be ignored
   **/
  ProcessInstanceBuilder processDefinitionKey(String processDefinitionKey);

  /**
   * Set the message name that needs to be used to look up the process definition that needs to be used to start the process instance.
   */
  ProcessInstanceBuilder messageName(String messageName);

  /**
   * Set the name of process instance
   **/
  ProcessInstanceBuilder name(String processInstanceName);

  /**
   * Set the businessKey of process instance
   **/
  ProcessInstanceBuilder businessKey(String businessKey);

  /**
   * Set the tenantId of process instance
   **/
  ProcessInstanceBuilder tenantId(String tenantId);

  /**
   * Sets the process variables
   */
  ProcessInstanceBuilder variables(Map<String, Object> variables);

  /**
   * Adds a variable to the process instance
   **/
  ProcessInstanceBuilder variable(String variableName, Object value);

  /**
   * Sets the transient variables
   */
  ProcessInstanceBuilder transientVariables(Map<String, Object> transientVariables);

  /**
   * Adds a transient variable to the process instance
   */
  ProcessInstanceBuilder transientVariable(String variableName, Object value);

  /**
   * Start the process instance
   *
   * @throws ActivitiIllegalArgumentException
   *           if processDefinitionKey and processDefinitionId are null
   * @throws ActivitiObjectNotFoundException
   *           when no process definition is deployed with the given processDefinitionKey or processDefinitionId
   * **/
  ProcessInstance start();

  /**
   * Create the process instance
   *
   * @throws ActivitiIllegalArgumentException
   *           if processDefinitionKey and processDefinitionId are null
   * @throws ActivitiObjectNotFoundException
   *           when no process definition is deployed with the given processDefinitionKey or processDefinitionId
   * **/
  ProcessInstance create();

}
