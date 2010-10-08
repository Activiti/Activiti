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

import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.query.Query;



/** Allows programatic querying of {@link Execution}s.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public interface ExecutionQuery extends Query<ExecutionQuery, Execution>{
  
  /** Only select executions which have the given process definition key. **/
  ExecutionQuery processDefinitionKey(String processDefinitionKey);
  
  /** Only select executions which have the given process definition id. **/
  ExecutionQuery processDefinitionId(String processDefinitionId);
  
  /** Only select executions which have the given process instance id. **/
  ExecutionQuery processInstanceId(String processInstanceId);
  
  /** Only select executions with the given id. **/
  ExecutionQuery executionId(String executionId);
  
  /** Only select executions which contain an activity with the given id. **/
  ExecutionQuery activityId(String activityId);
  
  /** 
   * Only select executions which have a variable with the given value. The type 
   * of variable is determined based on the value, using types configured in 
   * {@link ProcessEngineConfiguration#getVariableTypes()}. 
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   */
  ExecutionQuery variableValue(String name, Object value);
  
  /** 
   * Only select executions which have a variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   */
  ExecutionQuery variableValueNot(String name, Object value);
  

  /** 
   * Only select executions which have a variable value greater than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ExecutionQuery variableValueGreaterThan(String name, Object value);
  
  /** 
   * Only select executions which have a variable value greater than or equal to 
   * the passed value. Byte-arrays and {@link Serializable} objects (which 
   * are not primitive type wrappers) are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ExecutionQuery variableValueGreaterThanOrEqual(String name, Object value);
  
  /** 
   * Only select executions which have a variable value less than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ExecutionQuery variableValueLessThan(String name, Object value);
  
  /** 
   * Only select executions which have a variable value less than or equal to the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  ExecutionQuery variableValueLessThanOrEqual(String name, Object value);
  
  /** 
   * Only select executions which have a variable value like the given value.
   * This be used on string variables only.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null. The string can include the
   * wildcard character '%' to express like-strategy: 
   * starts with (string%), ends with (%string) or contains (%string%).
   */
  ExecutionQuery variableValueLike(String name, String value);

  //ordering //////////////////////////////////////////////////////////////
  
  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ExecutionQuery orderByProcessInstanceId();
  
  /** Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ExecutionQuery orderByProcessDefinitionKey();
  
  /** Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ExecutionQuery orderByProcessDefinitionId();
  
}
