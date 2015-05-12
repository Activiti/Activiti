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

import org.activiti.engine.query.Query;

/** 
 * Programmatic querying for {@link HistoricVariableInstance}s.
 * 
 * @author Christian Lipphardt (camunda)
 */
public interface HistoricVariableInstanceQuery extends Query<HistoricVariableInstanceQuery, HistoricVariableInstance> {

  /** Only select a historic variable with the given id. */
  HistoricVariableInstanceQuery id(String id);
  
  /** Only select historic process variables with the given process instance. */
  HistoricVariableInstanceQuery processInstanceId(String processInstanceId);
  
  /** Only select historic process variables with the given id. **/
  HistoricVariableInstanceQuery executionId(String executionId);

  /** Only select historic process variables with the given task. */
  HistoricVariableInstanceQuery taskId(String taskId);

  /** Only select historic process variables with the given variable name. */
  HistoricVariableInstanceQuery variableName(String variableName);
  
  /** Only select historic process variables where the given variable name is like. */
  HistoricVariableInstanceQuery variableNameLike(String variableNameLike);
  
  /** Only select historic process variables which were not set task-local. */
  HistoricVariableInstanceQuery excludeTaskVariables();
  
  /** Don't initialize variable values. This is foremost a way to deal with variable delete queries */
  HistoricVariableInstanceQuery excludeVariableInitialization();

  /**
   * only select historic process variables with the given name and value
   */
  HistoricVariableInstanceQuery variableValueEquals(String variableName, Object variableValue);

  HistoricVariableInstanceQuery orderByProcessInstanceId();
  
  HistoricVariableInstanceQuery orderByVariableName();

}
