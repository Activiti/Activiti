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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.query.Query;



/** Allows programmatic querying of {@link HistoricProcessInstance}s.
 *  
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface HistoricProcessInstanceQuery extends Query<HistoricProcessInstanceQuery, HistoricProcessInstance>{

  /** Only select historic process instances with the given process instance.
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match. */
  HistoricProcessInstanceQuery processInstanceId(String processInstanceId);
  
  /** Only select historic process instances for the given process definition */
  HistoricProcessInstanceQuery processDefinitionId(String processDefinitionId);
  
  /** Only select historic process instances for the given process definition key */
  HistoricProcessInstanceQuery processDefinitionKey(String processDefinitionKey); 
  
  /** Only select historic process instances that don't have a process-definition of which the key is present in the given list */
  HistoricProcessInstanceQuery processDefinitionKeyNotIn(List<String> processDefinitionKeys);
  
  /** Only select historic process instances with the given business key */
  HistoricProcessInstanceQuery processInstanceBusinessKey(String processInstanceBusinessKey);
  
  /** Only select historic process instances that are completely finished. */
  HistoricProcessInstanceQuery finished();
  
  /** Only select historic process instance that are not yet finished. */
  HistoricProcessInstanceQuery unfinished();
  
  /** Only select historic process instances that were started before the given date. */
  HistoricProcessInstanceQuery startedBefore(Date date);
  
  /** Only select historic process instances that were started after the given date. */
  HistoricProcessInstanceQuery startedAfter(Date date);
  
  /** Only select historic process instances that were started before the given date. */
  HistoricProcessInstanceQuery finishedBefore(Date date);
  
  /** Only select historic process instances that were started after the given date. */
  HistoricProcessInstanceQuery finishedAfter(Date date);
  
  /** Only select historic process instance that are started by the given user. */
  HistoricProcessInstanceQuery startedBy(String userId);
  
  /** 
   * Only select {@link HistoricProcessInstance}s which have a process variable with the given value. The type 
   * of variable is determined based on the value, using types configured in 
   * {@link ProcessEngineConfiguration#getVariableTypes()}. 
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   */
  HistoricProcessInstanceQuery variableValueEquals(String name, Object value);
  
  /** 
   * Only select {@link HistoricProcessInstance}s which have a process variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name name of the variable, cannot be null.
   */
  HistoricProcessInstanceQuery variableValueNotEquals(String name, Object value);
  

  /** 
   * Only select {@link HistoricProcessInstance}s which have a process variable value greater than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  HistoricProcessInstanceQuery variableValueGreaterThan(String name, Object value);
  
  /** 
   * Only select {@link HistoricProcessInstance}s which have a process variable value greater than or equal to 
   * the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which 
   * are not primitive type wrappers) are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  HistoricProcessInstanceQuery variableValueGreaterThanOrEqual(String name, Object value);
  
  /** 
   * Only select {@link HistoricProcessInstance}s which have a process variable value less than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  HistoricProcessInstanceQuery variableValueLessThan(String name, Object value);
  
  /** 
   * Only select {@link HistoricProcessInstance}s which have a process variable value less than or equal to the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   */
  HistoricProcessInstanceQuery variableValueLessThanOrEqual(String name, Object value);
  
  /** 
   * Only select {@link HistoricProcessInstance}s which have a process variable value like the given value.
   * This be used on string variables only.
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null. The string can include the
   * wildcard character '%' to express like-strategy: 
   * starts with (string%), ends with (%string) or contains (%string%).
   */
  HistoricProcessInstanceQuery variableValueLike(String name, String value);
  
  /** Order by the process instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessInstanceId();
  
  /** Order by the process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessDefinitionId();
  
  /** Order by the business key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessInstanceBusinessKey();

  /** Order by the start time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessInstanceStartTime();
  
  /** Order by the end time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessInstanceEndTime();
  
  /** Order by the duration of the process instance (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessInstanceDuration();
  
  HistoricProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId);

}
